import os
import platform
from subprocess import call


def compile_model(filename, bee_path="bee20170408"):
    executable = bee_path + os.sep + "BumbleBEE" + (".exe" if platform.system() == "Windows" else "")
    dimacs_file = filename[:-3] + "dimacs"
    map_file = filename[:-3] + "map"
    if call([executable, filename, "-dimacs", dimacs_file, map_file]):
        return None
    return dimacs_file, map_file


if __name__ == "__main__":
    print compile_model("models/0.bee")