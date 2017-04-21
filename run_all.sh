#!/bin/bash
./run 4 16 100000 runners/runner.py 1 10 12 unsat count 4
./run 4 16 100000 runners/runner.py 2 10 17 unsat bfs count 4
./run 4 16 100000 runners/runner.py 3 10 18 unsat bfs max_deg count 4
./run 4 16 100000 runners/runner.py 4 10 19 unsat bfs max_deg weights count 4
./run 4 16 100000 runners/runner.py 5 10 19 unsat bfs max_deg weights degs count 4
./run 4 16 100000 runners/runner.py 6 10 17 unsat lex count 4
./run 4 16 100000 runners/runner.py 7 12 13 unsat unavoid count 4
