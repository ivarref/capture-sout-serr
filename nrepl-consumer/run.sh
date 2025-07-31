#!/usr/bin/env bash

set -euo pipefail

echo "clojure -X:run-server" > ./.max_prefix.txt

EXITCOLOR='31'
SELF_NAME="$(basename "$0")"

trap "trap - SIGTERM && { printf \"\e[0;\${EXITCOLOR}mExiting\e[0m\n\" | ./prefix.py \"\$SELF_NAME\"; } && kill -- -$$" SIGHUP SIGINT SIGTERM EXIT

# https://stackoverflow.com/questions/59895/how-do-i-get-the-directory-where-a-bash-script-is-located-from-within-the-script
SOURCE=${BASH_SOURCE[0]}
while [ -L "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )
  SOURCE=$(readlink "$SOURCE")
  [[ $SOURCE != /* ]] && SOURCE=$DIR/$SOURCE # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )

cd "$DIR"

if [[ "$#" -eq 1 && "$1" == "--skip-compile" ]]; then
  :
else
  bash -c "cd \"$DIR/..\" && rm -rf ./target && lein javac"     2>&1 | ./prefix.py "lein javac"
  bash -c "cd \"$DIR/..\" && ./test.sh --skip-compile"          2>&1 | ./prefix.py "test.sh"
  bash -c "cd \"$DIR/..\" && ./test_replay.sh --skip-compile"   2>&1 | ./prefix.py "test_replay.sh"
  bash -c "cd \"$DIR/..\" && ./test_truncate.sh --skip-compile" 2>&1 | ./prefix.py "test_truncate.sh"
  bash -c "cd \"$DIR/..\" && lein install"                      2>&1 | ./prefix.py "lein install"
  printf "\e[32m%s\e[0m\n" "Tests passed and install OK"             | ./prefix.py "$SELF_NAME"
#  printf '\033[3J' # clear scrollback
#  printf '\033[2J' # clear whole screen without moving the cursor
#  printf '\033[H' # move cursor to top left of the screen
fi

echo 'Truncate' > debug.log
echo 'Truncate' > debug2.log

printf "\e[0;33m%s\e[0m\n" "Starting nREPL server ... " | ./prefix.py "$SELF_NAME"

env ReplayConsumePrintStreamDebug='TRUE' clojure -X:run-server 2>&1 | ./prefix.py "clojure -X:run-server" &

tail -f ./debug.log 2>&1 | ./prefix.py "debug.log" &
tail -f ./debug2.log 2>&1 | ./prefix.py "debug2.log" &

bash -c "./wait_nrepl.sh" 2>&1 | ./prefix.py "wait_nrepl.sh"

printf "\e[32m%s\e[0m\n" "nREPL server up" | ./prefix.py "$SELF_NAME"

printf "\e[0;33m%s\e[0m\n" "All set up. Starting nREPL client ... " | ./prefix.py "$SELF_NAME"
#clojure -X:run-client 2>&1 | ./prefix.py "clojure -X:run-client"

#cat ./client.clj | clj -M -m nrepl.cmdline --connect --host localhost --port 7888 | ./prefix.py "nrepl-client"
#
#file1="./debug.log"
#file2="./expected_debug.log"
#
#if [ ! -f "$file1" ]; then
#  printf 'The file "%s" does not exist\n' "$file1" | ./prefix.py "$0"
#  echo -e "\e[31mTest FAIL\e[0m" | ./prefix.py "$0"
#  exit 1
#elif [ ! -f "$file2" ]; then
#  printf 'The file "%s" does not exist\n' "$file2" | ./prefix.py "$0"
#  echo -e "\e[31mTest FAIL\e[0m" | ./prefix.py "$0"
#  exit 1
#elif cmp -s "$file1" "$file2"; then
#  printf 'The file "%s" is the same as "%s"\n' "$file1" "$file2" | ./prefix.py "$0"
#  echo -e "\e[32mTest OK\e[0m" | ./prefix.py "$0"
#else
#  printf 'The file "%s" is different from "%s"\n' "$file1" "$file2" | ./prefix.py "$0"
#  echo -e "\e[31mTest FAIL\e[0m" | ./prefix.py "$0"
#  exit 1
#fi
#
#printf "\e[0m%s\e[0m\n" "Contents of debug.log:" | ./prefix.py "$0"
#cat ./debug.log | ./prefix.py "cat ./debug.log"
#
#printf "\e[0m%s\e[0m\n" "Contents of debug2.log:" | ./prefix.py "$0"
#cat ./debug2.log | ./prefix.py "cat ./debug2.log"

printf "\e[0m%s\e[0m\n" "Waiting for background processes to exit ..." | ./prefix.py "$SELF_NAME"
wait $(jobs -p)

EXITCOLOR='32'
