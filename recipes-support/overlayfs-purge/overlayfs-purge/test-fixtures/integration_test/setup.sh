#!/bin/sh

set -e

rm -rf tmp
mkdir -p tmp
cd tmp

# directories

for x in dir_overlayed_keep dir_overlayed_remove dir_overlayed dir_opaque_keep dir_opaque; do
    mkdir -p lowerdir/$x upperdir/$x
done

for x in dir_new_keep dir_new_remove dir_new; do
    mkdir -p upperdir/$x
done

mkdir -p lowerdir/whiteout_dir

# files

for x in file_overlayed_keep file_overlayed_remove; do
    touch lowerdir/$x upperdir/$x
done

for x in file_new_keep file_new_remove dir_overlayed/file_keep dir_new/file_keep dir_opaque/file_keep; do
    touch upperdir/$x
done

touch lowerdir/whiteout_file

# symlinks

for x in symlink_dir_overlayed_keep symlink_dir_overlayed_remove; do
    ln -s dir_overlayed_keep lowerdir/$x
    ln -s dir_overlayed_keep upperdir/$x
done

for x in symlink_dir_new_keep symlink_dir_new_remove; do
    ln -s dir_overlayed_keep upperdir/$x
done

for x in symlink_file_overlayed_keep symlink_file_overlayed_remove; do
    ln -s file_overlayed_keep lowerdir/$x
    ln -s file_overlayed_keep upperdir/$x
done

for x in symlink_file_new_keep symlink_file_new_remove; do
    ln -s file_overlayed_keep upperdir/$x
done

# whiteouts

mknod upperdir/whiteout_dir c 0 0
mknod upperdir/whiteout_file c 0 0

# opaque dirs

xattr -w trusted.overlay.opaque y upperdir/dir_opaque_keep
xattr -w trusted.overlay.opaque y upperdir/dir_opaque

## permissions

chmod a-rwx upperdir/dir_overlayed
chmod a+rwx upperdir/dir_new

# extended attributes

xattr -w user.test hello upperdir/dir_overlayed
xattr -w user.test hello upperdir/dir_new
xattr -w user.cat meow lowerdir/dir_overlayed
