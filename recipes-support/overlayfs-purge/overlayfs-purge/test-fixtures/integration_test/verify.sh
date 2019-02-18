#!/bin/sh

set -eux

cd tmp/upperdir

# directories

for x in dir_overlayed_keep dir_overlayed dir_opaque_keep dir_opaque dir_new_keep dir_new; do
    test -d $x
done

for x in dir_overlayed_remove dir_new_remove; do
    ! test -e $x
done

# files

for x in file_overlayed_keep file_new_keep dir_overlayed/file_keep dir_new/file_keep dir_opaque/file_keep; do
    test -f $x
done

for x in file_overlayed_remove file_new_remove; do
    ! test -e $x
done

# symlinks

for x in symlink_dir_overlayed_keep symlink_dir_new_keep symlink_file_overlayed_keep symlink_file_new_keep; do
    test -L $x
done

for x in symlink_dir_overlayed_remove symlink_dir_new_remove symlink_file_overlayed_remove symlink_file_new_remove; do
    ! test -e $x
done

# whiteouts

for x in whiteout_dir whiteout_dir_keep whiteout_file whiteout_file_keep; do
    ! test -e $x
done

# opaque dirs

for x in dir_opaque_keep dir_opaque; do
    test "$(xattr -p trusted.overlay.opaque $x 2>/dev/null || true)" != "y"
done

# TODO permissions

# TODO time stamps

# TODO extended attributes
