extern crate libc;
extern crate overlayfs_purge;
extern crate xattr;

use overlayfs_purge::run;
use std::path::Path;

fn run_purger() {
    run(
        Path::new("test-fixtures/integration_test/sysupgrade.conf"),
        Path::new("test-fixtures/integration_test/keep.d"),
        Path::new("tmp/lowerdir"),
        Path::new("tmp/upperdir"),
    )
}

fn setup() {
    if unsafe { libc::getuid() } != 0 {
        println!("Running test with fakeroot.");
        let args: Vec<_> = ::std::env::args().collect();
        let mut command_builder = std::process::Command::new("fakeroot");
        let status = command_builder.args(args).status().unwrap();
        ::std::process::exit(status.code().unwrap());
    }

    let status = std::process::Command::new("sh")
        .arg("test-fixtures/integration_test/setup.sh")
        .status()
        .expect("Failed to setup test.");
    assert!(status.success());
}

fn verify() {
    let dir = Path::new("tmp/upperdir");
}

#[test]
fn integration_test() {
    setup();
    run_purger();
    verify();
}
