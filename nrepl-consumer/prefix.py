#!/usr/bin/env python3
import sys

if __name__ == "__main__":
    assert 2 == len(sys.argv)
    with open('./.max_prefix.txt') as fd:
        max_prefix = fd.read().strip()
    prefix = sys.argv[1]
    spaces = len(max_prefix) - len(prefix)

    for line in sys.stdin:
        line = line.rstrip('\n')
        print(prefix, end='')
        if spaces > 0:
            print(' ' * spaces, end='')
        print(' | ', end='')
        print(line, flush=True)
