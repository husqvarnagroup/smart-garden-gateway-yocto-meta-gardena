# Gateway Manufacturing Scripts

## Dependencies

- python3
- i2c-tools (to verify connection to MFi chip)

## Available Scripts

- `ipr-tool.py` – script for initial IPR setup of gateway; also takes
  care of setting up X.509 certificate and secure token
- `homekit-tool.py` – script to set up HomeKit tokens during manufacturing
- `selftest.py` — gateway self test, to be run on the gateway itself
  during manufacturing
- `fct-tool.py` — functional circuit test helper script, to be run on
  the gateway itself during manufacturing
- `eoltest.py` – EOL test script that runs as last step of
  manufacturing
- `errorhandler.py` – generic error handler that collects data about
  manufacturing problems

## Code Style Guidelines

- please use `pylint` to check all Python code
- please use `shellcheck` to check all shell scripts
