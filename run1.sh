#!/usr/bin/env bash

set -euo pipefail

if [[ "$#" -eq 1 && "$1" == "--watch" ]]; then
  git ls-files | entr -ccr bash -c 'exec "./nrepl-consumer/run.sh"'
elif [[ "$#" -eq 1 && "$1" == "-w" ]]; then
  git ls-files | entr -ccr bash -c 'exec "./nrepl-consumer/run.sh"'
else
  exec "./nrepl-consumer/run.sh"
fi