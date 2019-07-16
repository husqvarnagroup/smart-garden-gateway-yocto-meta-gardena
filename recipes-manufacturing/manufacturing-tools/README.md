# EOL Test

The EOL test service is meant to run at ELRAD as the last step of
manufacturing, after the casing has been assembled. It is triggered by
the DHCP search domain being 'manufacturing.husqvarnagroup.net', but
only after the FCT has been finalized (so that it does not happen
during the initial bootstrapping).

The reason that the EOL test startup is split into two systemd units
is that the EOL test will wait for the system to boot completely (this
is required as checking that the system booted correctly is part of
the EOL test) and therefore other services can not wait on this unit.

The eoltest-check service is of type oneshot (considered as started
once the main process exits), so that other services can wait for it,
whereas the eoltest service itself is of type simple (considered as
started immediately after the main process has been forked off), so
that it can wait for systemd to finish booting without blocking the
boot process itself.

The EOL test must be idempotent, i.e. ELRAD wants be able to put a
gateway through the EOL test multiple times and still get a positive
result (if all tests are successful).
