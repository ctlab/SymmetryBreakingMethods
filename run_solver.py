from subprocess import call
import time


def run_solver(filename, max_seconds=3600, solver="treengeling"):
    cmd = ["timeout", str(max_seconds), "solvers/" + solver, filename]
    start = time.time()
    ret_code = call(cmd)
    # ret_code: 124 -- timeout
    #           10  -- SAT
    #           20  -- UNSAT
    if ret_code == 124: 
        return None
    end = time.time()
    return end - start, ret_code == 10

if __name__ == '__main__':
    print run_solver("models/0.dimacs")