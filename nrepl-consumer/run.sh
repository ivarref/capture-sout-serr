#!/usr/bin/env bash

set -euo pipefail

trap "trap - SIGTERM && echo 'run.sh exiting' && kill -- -$$" SIGHUP SIGINT SIGTERM EXIT

# https://stackoverflow.com/questions/59895/how-do-i-get-the-directory-where-a-bash-script-is-located-from-within-the-script
SOURCE=${BASH_SOURCE[0]}
while [ -L "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )
  SOURCE=$(readlink "$SOURCE")
  [[ $SOURCE != /* ]] && SOURCE=$DIR/$SOURCE # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR=$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )

cd "$DIR"

bash -c "cd \"$DIR/..\" && ./test_all.sh"

bash -c "cd \"$DIR/..\" && lein install" 2>&1 | sed -u -e "s/.*/lein install &/"

printf "\e[32m%s\e[0m\n" "Tests passed and install OK"

clojure -X:run-server 2>&1 | sed -u -e "s/.*/clojure -X:run-server &/" &

bash -c "./wait_nrepl.sh" 2>&1 | sed -u -e "s/.*/wait_nrepl.sh &/"

printf "\e[32m%s\e[0m\n" "nREPL server up"

clojure -X:run-client 2>&1 | sed -u -e "s/.*/clojure -X:run-client &/"

echo "run.sh about to exit"