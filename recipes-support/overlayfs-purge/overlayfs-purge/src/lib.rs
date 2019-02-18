extern crate globset;
extern crate xattr;

use self::globset::{Glob, GlobSet, GlobSetBuilder};
use std::fs;
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::os::unix::fs::{FileTypeExt, MetadataExt};
use std::path::Path;

pub fn run(keep_file: &Path, keep_dir: &Path, lowerdir: &Path, upperdir: &Path) {
    let mut patterns = load_keep_patterns(keep_file, keep_dir).expect("error loading config files");
    let mut builder = GlobSetBuilder::new();
    patterns.drain(..).for_each(|p| {
        builder.add(Glob::new(&p).expect("config parse error"));
    });
    let glob_patterns = builder.build().expect("config parse error");
    purge_upperdir(lowerdir, upperdir, upperdir, &glob_patterns).expect("error while purging");
}

fn load_keep_patterns(config_file: &Path, keep_dir: &Path) -> ::std::io::Result<Vec<String>> {
    let mut patterns: Vec<_> = read_keep_file(config_file)?.collect();
    for file in keep_dir.read_dir()? {
        for pattern in read_keep_file(&file?.path())? {
            patterns.push(pattern);
        }
    }
    Ok(patterns)
}

fn read_keep_file(path: &Path) -> ::std::io::Result<impl Iterator<Item = String>> {
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

fn purge_upperdir(
    lowerdir: &Path,
    upperdir: &Path,
    dir: &Path,
    keep: &GlobSet,
) -> ::std::io::Result<bool> {
    let mut remove_dir = true;
    for entry in dir.read_dir()? {
        let entry = entry?;
        let path = entry.path();
        let meta = entry.metadata()?;
        let filetype = meta.file_type();
        let stripped_path = match path.strip_prefix(upperdir) {
            Ok(sp) => Path::new("/").join(sp),
            Err(_) => continue,
        };

        if filetype.is_char_device() && meta.rdev() == 0 {
            println!("removing whiteout: {:?}", stripped_path);
            fs::remove_file(path)?;
            continue;
        }

        if keep.is_match(&stripped_path) {
            println!("keeping explicitly: {:?}", stripped_path);
            remove_dir = false;
            if filetype.is_dir() {
                remove_opaque_flag(&path, &stripped_path)?;
            }
            continue;
        }

        if filetype.is_dir() {
            if purge_upperdir(lowerdir, upperdir, &path, keep)? {
                println!("removing directory: {:?}", stripped_path);
                fs::remove_dir(path)?;
            } else {
                println!("keeping implicitly: {:?}", stripped_path);
                remove_dir = false;
                copy_all_metadata(lowerdir, upperdir, &path)?;
                remove_opaque_flag(&path, &stripped_path)?;
            }
        } else {
            println!("removing file: {:?}", stripped_path);
            fs::remove_file(path)?;
        }
    }
    Ok(remove_dir)
}

fn copy_all_metadata(lowerdir: &Path, upperdir: &Path, path: &Path) -> ::std::io::Result<()> {
    // TODO
    Ok(())
}

fn remove_opaque_flag(path: &Path, stripped_path: &Path) -> ::std::io::Result<()> {
    let opaque_name = "trusted.overlay.opaque";
    match xattr::get(path, opaque_name)? {
        Some(_) => {
            println!("making transparent: {:?}", stripped_path);
            xattr::remove(path, opaque_name)
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
