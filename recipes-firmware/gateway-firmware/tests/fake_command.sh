#!/bin/sh
# shellcheck shell=dash
# Generic fake for external commands. It is symlinked under each command name.
# FAKE_DIR holds calls.log and the per-command <name>.stdout, <name>.stderr and
# <name>.exit files. <name>.<arg1>.exit takes precedence over <name>.exit.
name=$(basename "$0")
echo "$name $*" >> "$FAKE_DIR/calls.log"
[ -f "$FAKE_DIR/$name.stdout" ] && cat "$FAKE_DIR/$name.stdout"
[ -f "$FAKE_DIR/$name.stderr" ] && cat "$FAKE_DIR/$name.stderr" >&2
for exit_file in "$FAKE_DIR/$name.${1:-}.exit" "$FAKE_DIR/$name.exit"; do
    if [ -f "$exit_file" ]; then
        exit "$(cat "$exit_file")"
    fi
done
exit 0
