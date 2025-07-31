#!/usr/bin/env bash

set -euo pipefail

ATTEMPTS="1"
MAX_ATTEMPTS="60"

echo "Waiting for nREPL server to open port 7888 ..."

while [ ! "$ATTEMPTS" -eq "$MAX_ATTEMPTS" ]
do
  RES="$(nc -z 127.0.0.1 7888 >/dev/null 2>&1; echo $?)"
  if [[ "$RES" == "0" ]]; then
    break;
  else
    echo "Waiting ... Exit code for netcat was ${RES}."
    sleep 1
    ((ATTEMPTS++))
  fi
done

if [[ "$ATTEMPTS" == "$MAX_ATTEMPTS" ]]; then
  echo "nREPL server not available"
  exit 1
else
  :
fi