#!/bin/bash
./run 4 64 100000 runners/runner.py 1 10 12 unsat 
./run 4 64 100000 runners/runner.py 2 10 17 unsat bfs
./run 4 64 100000 runners/runner.py 3 10 18 unsat bfs max_deg
./run 4 64 100000 runners/runner.py 4 10 19 unsat bfs max_deg weights
./run 4 64 100000 runners/runner.py 5 10 19 unsat bfs max_deg weights degs
./run 4 64 100000 runners/runner.py 6 10 17 unsat lex
./run 4 64 100000 runners/runner.py 7 12 13 unsat unavoid
