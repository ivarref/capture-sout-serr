#!/usr/bin/env bash

set -euo pipefail

./test.sh 2>&1          | sed -u -e "s/.*/test.sh          &/"
./test_replay.sh 2>&1   | sed -u -e "s/.*/test_replay.sh   &/"
./test_truncate.sh 2>&1 | sed -u -e "s/.*/test_truncate.sh &/"