mod lib;

use lib::run;
use std::path::Path;

fn main() {
    run(
        Path::new("/etc/sysupgrade.conf"),
        Path::new("/lib/upgrade/keep.d"),
        Path::new("/media/rfs/ro"),
        Path::new("/media/rfs/rw/upperdir"),
    )
}
