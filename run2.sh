#!/usr/bin/env bash

set -euo pipefail

if [[ "$#" -eq 1 && "$1" == "--watch" ]]; then
  git ls-files | entr -ccr bash -c 'exec "./nrepl-auto-consumer/run2.sh"'
elif [[ "$#" -eq 1 && "$1" == "-w" ]]; then
  git ls-files | entr -ccr bash -c 'exec "./nrepl-auto-consumer/run2.sh"'
else
  exec "./nrepl-auto-consumer/run2.sh"
fi