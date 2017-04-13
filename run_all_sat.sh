#!/bin/bash
./run 4 64 1800000 runners/runner.py 1 18 28 count 50
./run 4 64 1800000 runners/runner.py 2 18 28 bfs count 50
./run 4 64 1800000 runners/runner.py 3 18 28 bfs max_deg count 50
./run 4 64 1800000 runners/runner.py 4 18 28 bfs max_deg weights count 50
./run 4 64 1800000 runners/runner.py 5 18 28 bfs max_deg weights degs count 50
./run 4 64 1800000 runners/runner.py 6 18 28 lex count 50
./run 4 64 1800000 runners/runner.py 7 18 28 unavoid count 50
