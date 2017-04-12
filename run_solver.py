from subprocess import call
import time


def run_solver(filename, max_seconds, solver):
    cmd = ["timeout", str(max_seconds), "solvers/" + solver, filename]
    start = time.time()
    ret_code = call(cmd)
    # ret_code: 124 -- timeout
    #           10  -- SAT
    #           20  -- UNSAT
    if ret_code == 124:
        return None, None
    end = time.time()
    return end - start, ret_code == 10
