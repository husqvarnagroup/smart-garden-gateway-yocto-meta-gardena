#!/bin/sh

set -eu

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

for x in whiteout_dir whiteout_dir_keep; do
    mkdir -p lowerdir/$x
done

# files

for x in file_overlayed_keep file_overlayed_remove; do
    touch lowerdir/$x upperdir/$x
done

for x in file_new_keep file_new_remove dir_overlayed/file_keep dir_new/file_keep dir_opaque/file_keep; do
    touch upperdir/$x
done

for x in whiteout_file whiteout_file_keep; do
    touch lowerdir/$x
done

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

for x in whiteout_dir whiteout_dir_keep whiteout_file whiteout_file_keep; do
    mknod upperdir/$x c 0 0
done

# opaque dirs

xattr -w trusted.overlay.opaque y upperdir/dir_opaque_keep
xattr -w trusted.overlay.opaque y upperdir/dir_opaque

# permissions and ownership

chmod a-rwx,+s upperdir/dir_overlayed
chmod g+rwx,+t upperdir/dir_new
chmod o+rwx,+t lowerdir/dir_overlayed
chown 100:101 upperdir/dir_overlayed
chown 200:201 upperdir/dir_new
chown 300:301 lowerdir/dir_overlayed

# extended attributes

xattr -w user.test hello upperdir/dir_overlayed
xattr -w user.test hello upperdir/dir_new
xattr -w user.cat meow lowerdir/dir_overlayed
xattr -w user.asdf 1234 upperdir/dir_overlayed
xattr -w user.asdf 5678 lowerdir/dir_overlayed
