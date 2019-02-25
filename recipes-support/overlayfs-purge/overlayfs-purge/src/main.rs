extern crate overlayfs_purge;

use overlayfs_purge::run;
use std::path::Path;

fn main() {
    let args: Vec<_> = ::std::env::args().collect();
    if args[1..] != ["-f".to_string()] {
        println!("Aborting. Run with `-f` if you know what you are doing.");
        ::std::process::exit(1);
    }

    run(
        Path::new("/etc/sysupgrade.conf"),
        Path::new("/lib/upgrade/keep.d"),
        Path::new("/media/rfs/ro"),
        Path::new("/media/rfs/rw/upperdir"),
    )
}
