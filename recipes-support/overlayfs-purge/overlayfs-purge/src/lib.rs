#[macro_use]
extern crate failure_derive;

extern crate failure;
extern crate globset;
extern crate nix;
extern crate xattr;

use globset::{Glob, GlobSet, GlobSetBuilder};
use nix::sys::stat::{fchmodat, FchmodatFlags, Mode};
use nix::unistd::{chown, Gid, Uid};
use std::collections::HashSet;
use std::fs;
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::os::unix::fs::{FileTypeExt, MetadataExt};
use std::path::Path;

#[derive(Debug, Fail)]
pub enum Error {
    #[fail(display = "IO Error: {}", _0)]
    Io(::std::io::Error),
    #[fail(display = "UNIX Error: {}", _0)]
    Unix(nix::Error),
    #[fail(display = "Error: {}", _0)]
    Other(String),
}

impl From<::std::io::Error> for Error {
    fn from(error: ::std::io::Error) -> Self {
        Error::Io(error)
    }
}

impl From<nix::Error> for Error {
    fn from(error: nix::Error) -> Self {
        Error::Unix(error)
    }
}

type Result<T> = ::std::result::Result<T, Error>;

pub fn run(keep_file: &Path, keep_dir: &Path, lower_dir: &Path, upper_dir: &Path) {
    let mut patterns = load_keep_patterns(keep_file, keep_dir).expect("error loading config files");
    let mut builder = GlobSetBuilder::new();
    patterns.drain(..).for_each(|p| {
        builder.add(Glob::new(&p).expect("config parse error"));
    });
    let glob_patterns = builder.build().expect("config parse error");
    purge_upper_dir(lower_dir, upper_dir, &glob_patterns, upper_dir).expect("error while purging");
}

fn load_keep_patterns(config_file: &Path, keep_dir: &Path) -> Result<Vec<String>> {
    let mut patterns: Vec<_> = read_keep_file(config_file)?.collect();
    for file in keep_dir.read_dir()? {
        for pattern in read_keep_file(&file?.path())? {
            patterns.push(pattern);
        }
    }
    Ok(patterns)
}

fn read_keep_file(path: &Path) -> Result<impl Iterator<Item = String>> {
    let file = File::open(path)?;
    Ok(BufReader::new(file).lines().filter_map(|x| {
        if x.is_err() {
            // the line probably contains invalid utf-8
            return None;
        }
        let line = x.unwrap();
        {
            let t = line.trim(); // TODO: use trim_start() instead of trim() here in newer rust
            if t.is_empty() || t.starts_with('#') {
                return None;
            }
        }
        Some(line)
    }))
}

fn purge_upper_dir(lower_dir: &Path, upper_dir: &Path, keep: &GlobSet, dir: &Path) -> Result<bool> {
    let mut remove_dir = true;
    for entry in dir.read_dir()? {
        match handle_entry(lower_dir, upper_dir, keep, entry) {
            Ok(true) => (),
            Ok(false) => remove_dir = false,
            Err(e) => {
                println!("ERROR: {:?}", e);
                remove_dir = false;
            }
        }
    }
    Ok(remove_dir)
}

fn handle_entry(
    lower_dir: &Path,
    upper_dir: &Path,
    keep: &GlobSet,
    entry: ::std::result::Result<::std::fs::DirEntry, ::std::io::Error>,
) -> Result<bool> {
    let entry = entry?;
    let path = entry.path();
    let meta = entry.metadata()?;
    let filetype = meta.file_type();
    let stripped_path = path
        .strip_prefix(upper_dir)
        .map_err(|_| Error::Other("Error dissecting path".to_string()))?;

    if filetype.is_char_device() && meta.rdev() == 0 {
        println!("removing whiteout: {:?}", stripped_path);
        fs::remove_file(&path)?;
        return Ok(true);
    }

    if keep.is_match(Path::new("/").join(&stripped_path)) {
        println!("keeping explicitly: {:?}", stripped_path);
        if filetype.is_dir() {
            remove_opaque_flag(&path, &stripped_path)?;
        }
        return Ok(false);
    }

    if filetype.is_dir() {
        if purge_upper_dir(lower_dir, upper_dir, keep, &path)? {
            println!("removing directory: {:?}", stripped_path);
            fs::remove_dir(&path)?;
        } else {
            println!("keeping implicitly: {:?}", stripped_path);
            copy_metadata(lower_dir, upper_dir, &stripped_path)?;
            remove_opaque_flag(&path, &stripped_path)?;
            return Ok(false);
        }
    } else {
        println!("removing file: {:?}", stripped_path);
        fs::remove_file(&path)?;
    }

    Ok(true)
}

fn copy_metadata(lower_dir: &Path, upper_dir: &Path, stripped_path: &Path) -> Result<()> {
    let lower_path = lower_dir.join(stripped_path);
    let upper_path = upper_dir.join(stripped_path);

    let lower_meta = match lower_path.metadata() {
        Ok(m) => m,
        Err(_) => return Ok(()), // lower file not found, nothing to copy
    };
    let upper_meta = upper_path.metadata()?;

    if upper_meta.mode() != lower_meta.mode() {
        println!(
            "updating mode {} -> {}: {:?}",
            0o7777 & upper_meta.mode(),
            0o7777 & lower_meta.mode(),
            stripped_path
        );
        fchmodat(
            None,
            &upper_path,
            Mode::from_bits_truncate(lower_meta.mode()),
            FchmodatFlags::FollowSymlink,
        )?;
    }
    if upper_meta.uid() != lower_meta.uid() || upper_meta.gid() != lower_meta.gid() {
        println!(
            "updating uid:gid {}:{} -> {}:{}: {:?}",
            upper_meta.uid(),
            upper_meta.gid(),
            lower_meta.uid(),
            lower_meta.gid(),
            stripped_path
        );
        chown(
            &upper_path,
            Some(Uid::from_raw(lower_meta.uid())),
            Some(Gid::from_raw(lower_meta.gid())),
        )?;
    }

    let lower_xattrs: HashSet<_> = xattr::list(&lower_path)?.collect();
    let upper_xattrs: HashSet<_> = xattr::list(&upper_path)?.collect();
    for x in upper_xattrs.difference(&lower_xattrs) {
        println!("removing xattr {:?}: {:?}", x, stripped_path);
        xattr::remove(&upper_path, x)?;
    }
    for x in lower_xattrs.difference(&upper_xattrs) {
        println!("adding xattr {:?}: {:?}", x, stripped_path);
        xattr::set(
            &upper_path,
            x,
            xattr::get(&lower_path, x)?
                .ok_or_else(|| Error::Other("xattr vanished while reading".to_string()))?
                .as_slice(),
        )?;
    }
    for x in lower_xattrs.intersection(&upper_xattrs) {
        let lower_xattr = xattr::get(&lower_path, x)?
            .ok_or_else(|| Error::Other("xattr vanished while reading".to_string()))?;
        let upper_xattr = xattr::get(&upper_path, x)?
            .ok_or_else(|| Error::Other("xattr vanished while reading".to_string()))?;
        if lower_xattr != upper_xattr {
            println!("updating xattr {:?}: {:?}", x, stripped_path);
            xattr::set(&upper_path, x, lower_xattr.as_slice())?;
        }
    }

    Ok(())
}

fn remove_opaque_flag(path: &Path, stripped_path: &Path) -> Result<()> {
    let opaque_name = "trusted.overlay.opaque";
    match xattr::get(path, opaque_name)? {
        Some(_) => {
            println!("making transparent: {:?}", stripped_path);
            xattr::remove(path, opaque_name).map_err(|e| e.into())
        }
        None => Ok(()),
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::collections::HashSet;
    use std::iter::FromIterator;

    #[test]
    fn test_load_keep_patterns() {
        let mut patterns = load_keep_patterns(
            Path::new("test-fixtures/load_keep_patterns/sysupgrade.conf"),
            Path::new("test-fixtures/load_keep_patterns/keep.d"),
        )
        .unwrap();
        let patterns: HashSet<_> = HashSet::from_iter(patterns.drain(..));
        let expected_patterns = vec![
            "/etc/hostname",
            "/foo",
            "/foobar",
            "/bar",
            "/blubber/blubb-*.txt",
        ];
        let expected_patterns = HashSet::from_iter(expected_patterns.iter().map(|s| s.to_string()));
        assert_eq!(expected_patterns, patterns);
    }
}
