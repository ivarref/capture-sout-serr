#!/usr/bin/env bash

set -euo pipefail

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

echo "clojure -X:run-server" > ./.max_prefix.txt

if [[ "$#" -eq 1 && "$1" == "--skip-compile" ]]; then
  :
else
  bash -c "cd \"$DIR/../nrepl\" && rm -rf ./target && lein install" 2>&1 | ./prefix.py "nrepl install"
  bash -c "cd \"$DIR/..\" && rm -rf ./target && lein javac"         2>&1 | ./prefix.py "lein javac"
  bash -c "cd \"$DIR/..\" && ./test.sh --skip-compile"              2>&1 | ./prefix.py "test.sh"
  bash -c "cd \"$DIR/..\" && ./test_replay.sh --skip-compile"       2>&1 | ./prefix.py "test_replay.sh"
  bash -c "cd \"$DIR/..\" && ./test_truncate.sh --skip-compile"     2>&1 | ./prefix.py "test_truncate.sh"
  bash -c "cd \"$DIR/..\" && lein install"                          2>&1 | ./prefix.py "lein install"
  printf "\e[32m%s\e[0m\n" "Tests passed and install OK"                 | ./prefix.py "$SELF_NAME"
#  printf '\033[3J' # clear scrollback
#  printf '\033[2J' # clear whole screen without moving the cursor
#  printf '\033[H' # move cursor to top left of the screen
fi

echo 'Truncate' > debug.log

printf "\e[0;33m%s\e[0m\n" "Starting nREPL server ... " | ./prefix.py "$SELF_NAME"

rm -f ./.nrepl_client_done
rm -f ./nrepl_client_out.log

PROCESS_GROUP="$$"
{ env ReplayConsumePrintStreamDebug='TRUE' clojure -X:run-server 2>&1 \
  && echo 'Exited with exit code 0' \
  || { echo -e "\e[31mFailed with exit code $?\e[0m"; kill -- "-$PROCESS_GROUP"; }; } \
| ./prefix.py "clojure -X:run-server" &
CLOJURE_SERVER_PID="$!"

TAIL_PID="NONE"
{ { tail -f ./debug.log 2>&1 | ./prefix.py "debug.log"; } || true; } &
TAIL_PID="$!"

bash -c "./wait_nrepl.sh" 2>&1 | ./prefix.py "wait_nrepl.sh"

printf "\e[32m%s\e[0m\n" "nREPL server up" | ./prefix.py "$SELF_NAME"

printf "\e[0;33m%s\e[0m\n" "All set up. Starting nREPL client ... " | ./prefix.py "$SELF_NAME"
#clojure -X:run-client 2>&1 | ./prefix.py "clojure -X:run-client"

{ cat ./wait_output.clj | \
  env NREPL_FORWARD_STDOUT='TRUE' clj -M -m nrepl.cmdline \
  --connect --host localhost --port 7888 \
  && echo 'Exited with exit code 0' || echo -e "\e[31mFailed with exit code $?\e[0m";
  touch './.nrepl_client_done'; } \
2>&1 | tee -i ./nrepl_client_out.log | ./prefix.py "nrepl-client"

printf "Waiting for nREPL server process %s to exit ...\n" "$CLOJURE_SERVER_PID" | ./prefix.py "$SELF_NAME"

wait "$CLOJURE_SERVER_PID"
printf "Waiting for nREPL server process %s to exit ... OK\n" "$CLOJURE_SERVER_PID" | ./prefix.py "$SELF_NAME"

printf "Terminating tail command with pid %s ...\n" "$TAIL_PID" | ./prefix.py "$SELF_NAME"

if [[ "$TAIL_PID" == "NONE" ]]; then
  :
else
  set +e
  kill -SIGTERM "$TAIL_PID"
  wait "$TAIL_PID"
  set -e
fi

if [[ "" == "$(jobs -p)" ]]; then
  printf "%s\n" "No background jobs running" | ./prefix.py "$SELF_NAME"
else
  printf "Background jobs is:\n%s\n" "$(jobs -p)" | ./prefix.py "$SELF_NAME"

  printf "%s\n" "Waiting for background processes to exit ..." | ./prefix.py "$SELF_NAME"
  wait $(jobs -p)
  printf "\e[0m%s\e[0m\n" "Waiting for background processes to exit ... OK" | ./prefix.py "$SELF_NAME"
fi

printf "%s\n" "Checking output of nrepl client ..." | ./prefix.py "$SELF_NAME"

# https://stackoverflow.com/questions/1521462/looping-through-the-content-of-a-file-in-bash/
while IFS="" read -r p || [ -n "$p" ]
do
  if [[ "" == "$p" ]]; then
    :
  else
    if grep -q "$p" "./nrepl_client_out.log"; then
#      printf 'OK found line %s\n' "$p"
      :
    else
      printf "\e[31m%s '%s'\e[0m\n" "Did not find line" "$p" | ./prefix.py "$SELF_NAME"
      exit 1
    fi
  fi
done < ./expected_nrepl_client_out.log

printf "%s\n" "Checking output of nrepl client ... OK" | ./prefix.py "$SELF_NAME"

EXITCOLOR='32'
