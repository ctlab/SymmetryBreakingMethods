import itertools
import platform
from os import path, makedirs
from subprocess import call


def make_model_34(n, start, m=None, bfs=False, max_deg=False, weights=False, degs=False, weights_lex=False, lex=False, unavoid=False, unsat=False):
    cmd = ["run34.bat" if platform.system() == "Windows" else "./run34.sh", "-n", str(n)]
    if m:
        cmd.append("-m")
        cmd.append(str(n))
    if bfs:
        cmd.append("--bfs")
    if max_deg:
        cmd.append("--start-max-deg")
    if weights:
        cmd.append("--sorted-weights")
    if degs:
        cmd.append("--sorted-degs")
    if weights_lex:
        cmd.append("--sorted-weights-lex")
    if lex:
        cmd.append("--lex")
    if unavoid:
        cmd.append("--unavoidable")
    if unsat:
        cmd.append("--unsat")
    if not path.exists("models/"):
        makedirs("models/")
    index = 0
    for i in itertools.count(start=start, step=100):
        if not path.exists("models/" + str(i) + ".bee"):
            index = i
            break
    cmd.append("-o")
    cmd.append("models/" + str(index) + ".bee")
    print cmd
    if call(cmd):
        return None
    return "models/" + str(index) + ".bee"


if __name__ == "__main__":
    print make_model_34(10)
