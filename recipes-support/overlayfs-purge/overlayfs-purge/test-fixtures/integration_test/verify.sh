#!/bin/sh
# shellcheck shell=dash

set -eux

cd tmp/upperdir

# directories

for x in dir_overlayed_keep dir_overlayed dir_opaque_keep dir_opaque dir_new_keep dir_new; do
    test -d $x
done

for x in dir_overlayed_remove dir_new_remove; do
    test -e $x && exit 1
done

# files

for x in file_overlayed_keep file_new_keep dir_overlayed/file_keep dir_new/file_keep dir_opaque/file_keep; do
    test -f $x
done

for x in file_overlayed_remove file_new_remove; do
    test -e $x && exit 1
done

# symlinks

for x in symlink_dir_overlayed_keep symlink_dir_new_keep symlink_file_overlayed_keep symlink_file_new_keep; do
    test -L $x
done

for x in symlink_dir_overlayed_remove symlink_dir_new_remove symlink_file_overlayed_remove symlink_file_new_remove; do
    test -e $x && exit 1
done

# whiteouts

for x in whiteout_dir whiteout_dir_keep whiteout_file whiteout_file_keep; do
    test -e $x && exit 1
done

# overlayfs attributes

for x in dir_opaque_keep dir_opaque; do
    test "$(xattr -p trusted.overlay.opaque $x 2>/dev/null || true)" != "y"
done

for x in dir_overlayed_keep dir_new_keep file_overlayed_keep file_new_keep; do
    test "$(xattr -p trusted.overlay.fubar $x 2>/dev/null || true)" = ""
done

# permissions and ownership

test "$(stat -c "%a %u %g" dir_overlayed)" = "1757 300 301"
test "$(stat -c "%a %u %g" dir_new)" = "1775 200 201"

# extended attributes

test "$(xattr -p user.test dir_overlayed 2>/dev/null || true)" = ""
test "$(xattr -p user.cat dir_overlayed 2>/dev/null)" = "meow"
test "$(xattr -p user.test dir_new 2>/dev/null)" = "hello"
test "$(xattr -p user.asdf dir_overlayed 2>/dev/null)" = "5678"
