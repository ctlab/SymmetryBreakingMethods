from make_model import make_model_34
from compile_bee import compile_model
from run_solver import run_solver
import fcntl


def test(n, start, m=None, bfs=False, max_deg=False, weights=False, degs=False, weights_lex=False,
         lex=False, unavoid=False, unsat=False, max_seconds=3600, solver="treengeling"):
    model = make_model_34(n, start, m, bfs, max_deg, weights, degs, weights_lex, lex, unavoid, unsat)
    dimacs, map_file = compile_model(model)
    time, sat = run_solver(dimacs, max_seconds, solver)
    if time is None:
        return None, None
    if sat == unsat:
        raise Exception("Model should be " + ("UNSAT" if unsat else "SAT") + ", but solver reports the opposite result")
    return time, sat


def run_test(filename, n, start, m=None, bfs=False, max_deg=False, weights=False, degs=False, weights_lex=False,
             lex=False, unavoid=False, unsat=False, max_seconds=3600, solver="treengeling"):
    time, sat = test(n=n, start=start, m=m, bfs=bfs, max_deg=max_deg, weights=weights, weights_lex=weights_lex, degs=degs, lex=lex, unavoid=unavoid, unsat=unsat, max_seconds=max_seconds, solver=solver)
    with open(filename, "a") as f:
        fcntl.flock(f, fcntl.LOCK_EX)
        f.write("[test]\n")
        f.write("n = " + str(n) + "\n")
        f.write("m = " + str(m) + "\n")
        f.write("bfs = " + str(bfs) + "\n")
        f.write("max_deg = " + str(max_deg) + "\n")
        f.write("weights = " + str(weights) + "\n")
        f.write("degs = " + str(degs) + "\n")
        f.write("weights_lex = " + str(weights_lex) + "\n")
        f.write("lex = " + str(lex) + "\n")
        f.write("unavoid = " + str(unavoid) + "\n")
        f.write("unsat = " + str(unsat) + "\n")
        f.write("max_seconds = " + str(max_seconds) + "\n")
        f.write("solver = " + str(solver) + "\n")
        f.write("time = " + str(time) + "\n")
        f.write("sat = " + str(sat) + "\n")
        f.write("[end]\n")
        fcntl.flock(f, fcntl.LOCK_UN)

