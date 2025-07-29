#!/usr/bin/env bash

set -euo pipefail

rm -rf ./target || true

lein javac

rm -f ./sout.log || true

echo "Begin Java process ..."
java -cp $(pwd)/target/classes com.github.ivarref.capturesoutserr.TestConsumePrintStream
echo "Begin Java process ... Done"

file1="./sout.log"
file2="./expected_stdout.log"

if cmp -s "$file1" "$file2"; then
    printf 'The file "%s" is the same as "%s"\n' "$file1" "$file2"
    echo "Test OK"
    exit 0
else
    printf 'The file "%s" is different from "%s"\n' "$file1" "$file2"
    echo "Test FAIL"
    exit 1
fi
