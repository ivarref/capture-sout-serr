#!/usr/bin/env bash

set -euo pipefail

rm -rf ./target || true

lein javac

file1="./sout_replay.log"
file2="./expected_stdout.log"
rm -f "$file1" || true

java -cp $(pwd)/target/classes com.github.ivarref.capturesoutserr.ReplayConsumePrintStreamTest

if cmp -s "$file1" "$file2"; then
  printf 'The file "%s" is the same as "%s"\n' "$file1" "$file2"
  echo -e "\e[32mTest OK\e[0m"
  exit 0
else
  printf 'The file "%s" is different from "%s"\n' "$file1" "$file2"
  echo -e "\e[31mTest FAIL\e[0m"
  exit 1
fi
