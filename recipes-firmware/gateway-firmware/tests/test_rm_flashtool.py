#!/usr/bin/env python3
"""Testing rm-flashtool with mock commands.

The script (SUT) is run as separate process so that the implementation
can be replaced and the tests can be rerun.

The script to test needs to be set as environment variable: RM_FLASHTOOL.

Run the tests like:
RM_FLASHTOOL=files/rm-flashtool.py tests/test_rm_flashtool.py
"""

import os
import shutil
import subprocess
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
FAKE_COMMAND = os.path.join(HERE, "fake_command.sh")
COMMANDS = ["fw_printenv", "ps", "systemctl", "reset-rm", "openocd"]
SGTIN = "a" * 24
RMADDR = "8c:05:51:00:06:e2"
OPENOCD_CALL = (
    "openocd -f board/gardena_radio.cfg -c "
    "program /usr/share/gardena/firmware/current/gateway.bin verify exit 0x0"
)
FLASH_CALLS = ["reset-rm ", OPENOCD_CALL, "reset-rm "]


class RmFlashtoolTestCase(unittest.TestCase):
    def setUp(self):
        self.script = os.environ["RM_FLASHTOOL"]
        self.fake_dir = tempfile.mkdtemp()
        self.addCleanup(shutil.rmtree, self.fake_dir)
        for command in COMMANDS:
            os.symlink(FAKE_COMMAND, os.path.join(self.fake_dir, command))
        self.set_env(sgtin=SGTIN, rmaddr=RMADDR)

    def write(self, filename, content):
        with open(os.path.join(self.fake_dir, filename), "w") as f:
            f.write(content)

    def set_env(self, **variables):
        self.write(
            "fw_printenv.stdout",
            "".join("{}={}\n".format(*kv) for kv in variables.items()),
        )

    def set_output(self, name, stdout="", stderr=""):
        self.write(name + ".stdout", stdout)
        self.write(name + ".stderr", stderr)

    def set_exit(self, name, code, arg1=None):
        self.write(name + (f".{arg1}" if arg1 else "") + ".exit", str(code))

    def set_pppd_running(self):
        self.set_output("ps", "  42 root  pppd call gprs\n")

    def run_tool(self):
        env = dict(
            os.environ,
            PATH=self.fake_dir + ":" + os.environ["PATH"],
            FAKE_DIR=self.fake_dir,
        )
        return subprocess.run(
            [self.script], env=env, check=False, capture_output=True, text=True
        )

    def calls(self):
        try:
            with open(os.path.join(self.fake_dir, "calls.log")) as f:
                return f.read().splitlines()
        except FileNotFoundError:
            return []

    def assert_no_flash(self):
        self.assertNotIn("reset-rm ", self.calls())
        self.assertNotIn(OPENOCD_CALL, self.calls())


class TestFlash(RmFlashtoolTestCase):
    def test_flashes_in_order_without_ppp(self):
        result = self.run_tool()
        self.assertEqual(result.returncode, 0)
        self.assertEqual(
            [c for c in self.calls() if not c.startswith("fw_printenv")],
            ["ps "] + FLASH_CALLS,
        )

    def test_stops_and_restarts_ppp_when_pppd_runs(self):
        self.set_pppd_running()
        result = self.run_tool()
        self.assertEqual(result.returncode, 0)
        flash = [
            c for c in self.calls() if not c.startswith("fw_printenv") and c != "ps "
        ]
        self.assertEqual(
            flash, ["systemctl stop ppp"] + FLASH_CALLS + ["systemctl start ppp"]
        )

    def test_no_systemctl_without_pppd(self):
        self.run_tool()
        self.assertFalse([c for c in self.calls() if c.startswith("systemctl")])

    def test_ppp_stop_failure_aborts_before_flashing(self):
        self.set_pppd_running()
        self.set_exit("systemctl", 1, "stop")
        result = self.run_tool()
        self.assertNotEqual(result.returncode, 0)
        self.assert_no_flash()

    def test_openocd_failure_restarts_ppp_and_prints_output(self):
        self.set_pppd_running()
        self.set_output("openocd", stdout="out text\n", stderr="err text\n")
        self.set_exit("openocd", 1)
        result = self.run_tool()
        self.assertEqual(result.returncode, 1)
        self.assertIn("out text", result.stdout)
        self.assertIn("err text", result.stdout)

    def test_openocd_failure_without_ppp_does_not_touch_systemctl(self):
        self.set_exit("openocd", 1)
        result = self.run_tool()
        self.assertEqual(result.returncode, 1)
        self.assertFalse([c for c in self.calls() if c.startswith("systemctl")])
        self.assertEqual(self.calls().count("reset-rm "), 1)

    def test_reset_rm_failure_is_fatal(self):
        self.set_exit("reset-rm", 1)
        self.assertNotEqual(self.run_tool().returncode, 0)


class TestEnvChecks(RmFlashtoolTestCase):
    def assert_rejected(self, message):
        result = self.run_tool()
        self.assertEqual(result.returncode, 1)
        self.assertIn(message, result.stdout)
        self.assert_no_flash()

    def test_sgtin_too_short(self):
        self.set_env(sgtin="a" * 23, rmaddr=RMADDR)
        self.assert_rejected("invalid SGTIN")

    def test_sgtin_too_long(self):
        self.set_env(sgtin="a" * 25, rmaddr=RMADDR)
        self.assert_rejected("invalid SGTIN")

    def test_sgtin_missing(self):
        self.set_env(rmaddr=RMADDR)
        self.assert_rejected("invalid SGTIN")

    def test_rmaddr_too_short(self):
        self.set_env(sgtin=SGTIN, rmaddr="8c:05:51:00:06:e")
        self.assert_rejected("invalid rmaddr")

    def test_rmaddr_too_long(self):
        self.set_env(sgtin=SGTIN, rmaddr="8c:05:51:00:06:e2:f")
        self.assert_rejected("invalid rmaddr")

    def test_rmaddr_without_colons_is_valid(self):
        self.set_env(sgtin=SGTIN, rmaddr="8c05510006e2")
        self.assertEqual(self.run_tool().returncode, 0)

    def test_rmaddr_missing_prints_notice_and_flashes(self):
        self.set_env(sgtin=SGTIN)
        result = self.run_tool()
        self.assertEqual(result.returncode, 0)
        self.assertIn(
            "No rmaddr found, using static value used in all MP devices", result.stdout
        )
        self.assertIn(OPENOCD_CALL, self.calls())

    def test_fw_printenv_failure(self):
        self.set_exit("fw_printenv", 1)
        self.assertNotEqual(self.run_tool().returncode, 0)
        self.assert_no_flash()


if __name__ == "__main__":
    unittest.main()
