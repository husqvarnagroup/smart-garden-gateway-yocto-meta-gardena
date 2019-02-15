# Purge an Overlayfs Upperdir

This tool removes files in an upperdir from an Overlay Filesystem while keeping some files. The files to keep can be specified by multiple config files. It is intended for a cleanup whenever a lowerdir has been updated to keep a minimal set of files. The overlayfs does not have to be mounted, only the lowerdir and the upperdir must be available.

See [Overlayfs specification](https://www.kernel.org/doc/Documentation/filesystems/overlayfs.txt).

## Overlayfs Assumptions

* The "redirect_dir" feature is not enabled.
* The metadata only copy up feature is not enabled.
* There are no special attributes like the immutable flag.

## Files to Keep

This tool looks in the following config files for patterns:

* "/etc/sysupgrade.conf"
* Any file directly under "/lib/upgrade/keep.d/"

Each config file contains one glob pattern per line for each file to keep. Each line usually starts with a '/'. Lines starting with zero or more whitespace characters followed by a '#' as well as lines only containing zero or more whitespace characters are ignored.

See [glob::Pattern](https://docs.rs/glob/0.2/glob/struct.Pattern.html) documentation for a list of supported glob patterns.

## Files in the Upperdir

* Every character device with device number 0/0 (whiteout) is removed, even if it matches a glob pattern.
* Every other non-directory file in the uppderdir matching a glob pattern is kept and its attributes and extended attributes are unchanged. Even if the lowerdir contains a directory at that path.
* Every directory matching a glob pattern is kept and its extended attribute "trusted.overlay.opaque" is removed. Any other attributes and extended attributes are unchanged. Even if the lowerdir contains a non-direcory file at that path.
* For every non-matching directory which cannot be purged because it contains matching components:
  * If the lowerdir contains a directory at the same path all attributes and extended attributes from the directory in the lowerdir are copied to the directory in the upperdir and the extended attribute "trusted.overlay.opaque" is removed.
  * If the lowerdir does not contain a directory at the same path only the extended attribute "trusted.overlay.opaque" is removed. Any other attributes and extended attributes are unchanged.
* Everything else is removed.
