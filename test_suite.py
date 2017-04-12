from make_model import make_model_34
from compile_bee import compile_model
from run_solver import run_solver


def test(n, m=None, bfs=False, max_deg=False, weights=False, degs=False,
         lex=False, unavoid=False, unsat=False, max_seconds=3600, solver="treengeling"):
    model = make_model_34(n, m, bfs, max_deg, weights, degs, lex, unavoid, unsat)
    dimacs, map_file = compile_model(model)
    time, sat = run_solver(dimacs, max_seconds, solver)
    if sat == unsat:
        raise Exception("Model should be " + ("UNSAT" if unsat else "SAT") + ", but solver reports the opposite result")
    return time, sat


def run_test(filename, n, m=None, bfs=False, max_deg=False, weights=False, degs=False,
             lex=False, unavoid=False, unsat=False, max_seconds=3600, solver="treengeling"):
    time, sat = test(n, m, bfs, max_deg, weights, degs, lex, unavoid, unsat, max_seconds, solver)
    with open(filename, "a") as f:
        f.write("[test]\n")
        f.write("n = " + str(n) + "\n")
        f.write("m = " + str(m) + "\n")
        f.write("bfs = " + str(m) + "\n")
        f.write("max_deg = " + str(max_deg) + "\n")
        f.write("weights = " + str(weights) + "\n")
        f.write("degs = " + str(degs) + "\n")
        f.write("lex = " + str(lex) + "\n")
        f.write("unavoid = " + str(unavoid) + "\n")
        f.write("unsat = " + str(unsat) + "\n")
        f.write("max_seconds = " + str(max_seconds) + "\n")
        f.write("solver = " + str(solver) + "\n")
        f.write("time = " + str(time) + "\n")
        f.write("sat = " + str(sat) + "\n")
        f.write("[end]")

