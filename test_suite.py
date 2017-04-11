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