#!/usr/bin/env bash

set -euo pipefail

rm -rf ./target || true

lein javac

./test.sh --skip-compile 2>&1          | sed -u -e "s/.*/test.sh          &/"
./test_replay.sh --skip-compile 2>&1   | sed -u -e "s/.*/test_replay.sh   &/"
./test_truncate.sh --skip-compile 2>&1 | sed -u -e "s/.*/test_truncate.sh &/"