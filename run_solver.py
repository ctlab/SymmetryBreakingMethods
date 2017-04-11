from subprocess import call
import time


def run_solver(filename, max_seconds=3600, solver="treengeling"):
    cmd = ["timeout", str(max_seconds), "solvers/" + solver, filename]
    start = time.time()
    if call(cmd):
        return None
    end = time.time()
    return end - start

if __name__ == '__main__':
    print run_solver("models/0.dimacs")