# Purging an Overlayfs Upperdir

This tool removes files in an upperdir from an Overlay Filesystem while keeping some files. The files to keep can be specified by multiple config files. It is intended for a cleanup whenever a lowerdir has been updated to keep a minimal set of files. The overlayfs does not have to be mounted, only the lowerdir and the upperdir must be available.

See [Overlayfs specification].

## Assumptions

* The "redirect_dir" feature in Overlayfs is not enabled.
* The metadata only copy up feature in Overlayfs is not enabled.
* File attributes, like the immutable flag, are not used, see [chattr(1)].
* ACLs are not used, see [setfacl(1)].
* SELinux is not used.
* Timestamps in file metadata of directories are not important.
* The contents of the lowerdir and upperdir directories are not changed by any other process while this tool is running.

[Overlayfs specification]: https://www.kernel.org/doc/Documentation/filesystems/overlayfs.txt
[chattr(1)]: https://manpages.debian.org/stretch/e2fsprogs/chattr.1.en.html
[setfacl(1)]: https://manpages.debian.org/stretch/acl/setfacl.1.en.html

## Keeping Files

This tool looks in the following config files for patterns:

* "/etc/sysupgrade.conf"
* Any file directly under "/lib/upgrade/keep.d"

Each config file contains one glob pattern per line for each file to keep. Each line usually starts with a '/'. Lines starting with zero or more whitespace characters followed by a '#' as well as lines only containing zero or more whitespace characters are ignored.

See the [glob::Pattern documentation] for a list of supported glob patterns.

Please note: Listed directories must *not* end with a trailing slash!

[glob::Pattern documentation]: https://docs.rs/glob/0.2/glob/struct.Pattern.html

## Files in the Upperdir

* Every character device with device number 0/0 (whiteout) is removed, even if it matches a glob pattern.
* Every other regular file, special file or directory in the uppderdir matching a glob pattern is kept and its attributes and extended attributes are unchanged (see exception below).
* For every non-matching directory which cannot be purged because it contains matching components if the lowerdir contains a directory at the same path all attributes (user, group and access permissions, but not times) and extended attributes from the directory in the lowerdir are copied to the directory in the upperdir.
* Everything else is removed.
* For every regular file or directory in the upperdir all extended attributes with names starting with "trusted.overlay." are removed.
