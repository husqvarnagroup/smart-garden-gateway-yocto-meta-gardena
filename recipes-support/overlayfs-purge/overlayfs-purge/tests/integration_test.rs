extern crate nix;
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

fn setup_fakeroot() {
    if nix::unistd::getuid().as_raw() != 0 {
        println!("Running test with fakeroot.");
        let args: Vec<_> = ::std::env::args().collect();
        let mut command_builder = std::process::Command::new("fakeroot");
        let status = command_builder.args(args).status().unwrap();
        ::std::process::exit(status.code().unwrap());
    }
}

fn setup_test_data() {
    let status = std::process::Command::new("sh")
        .arg("test-fixtures/integration_test/setup.sh")
        .status()
        .expect("Failed to setup test.");
    assert!(status.success());
}

fn verify_test_data() {
    let status = std::process::Command::new("sh")
        .arg("test-fixtures/integration_test/verify.sh")
        .status()
        .expect("Failed to verify test.");
    assert!(status.success());
}

#[test]
fn integration_test() {
    setup_fakeroot();
    setup_test_data();
    run_purger();
    verify_test_data();
}
