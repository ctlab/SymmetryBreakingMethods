from __future__ import print_function
import sys
from math import sqrt, ceil, floor


def int_none(s):
    if s == 'None':
        return 0
    return int(s)


def str_none(s):
    if s == 'None':
        return ""
    return s


def bool_none(s):
    if s == 'None':
        return False
    return s == 'True'


def float_none(s):
    if s == 'None':
        return None
    return float(s)


def parse_results(filename):
    with open(filename, 'r') as f:
        lines = f.readlines()
    n = None
    m = None
    bfs = False
    max_deg = False
    weights = False
    degs = False
    lex = False
    unavoid = False
    unsat = False
    max_seconds = 3600
    solver = 'treengeling'
    time = None
    sat = None
    results = []
    for line in lines:
        line = line.rstrip()
        if line == '[test]':
            continue
        if line == '[end]':
            ident = [bfs, max_deg, weights, degs, lex, unavoid]
            ident = "".join(map(lambda x: '1' if x else '0', ident))
            results.append((n, m, unsat, max_seconds, solver, ident, time, sat))
            continue
        arg, val = map(lambda x: x.strip(), line.split('=')[:2])
        if arg == 'n':
            n = int_none(val)
        elif arg == 'm':
            m = int_none(val)
        elif arg == 'max_seconds':
            max_seconds = int_none(val)
        elif arg == 'solver':
            solver = str_none(val)
        elif arg == 'bfs':
            bfs = bool_none(val)
        elif arg == 'max_deg':
            max_deg = bool_none(val)
        elif arg == 'weights':
            weights = bool_none(val)
        elif arg == 'degs':
            degs = bool_none(val)
        elif arg == 'lex':
            lex = bool_none(val)
        elif arg == 'unavoid':
            unavoid = bool_none(val)
        elif arg == 'unsat':
            unsat = bool_none(val)
        elif arg == 'sat':
            sat = bool_none(val)
        elif arg == 'time':
            time = float_none(val)
        else:
            print("Unknown argument: " + arg)
    return results


def get_all(list, ident, n):
    for elem in list:
        if elem[0] == n and elem[5] == ident:
            yield elem


def tl_str(x, tl):
    if x is None:
        return '{:6.2f}+'.format(tl)
    else:
        return '{:7.2f}'.format(x)


def q(s_elems, percent):
    pos = (len(s_elems) - 1) * percent
    l_pos = int(floor(pos))
    r_pos = int(ceil(pos))
    return 0.5 * (s_elems[l_pos] + s_elems[r_pos])


def stat(elems):
    elems = list(map(lambda x: x[3] if x[6] is None else x[6], elems))
    mean = sum(elems) / len(elems)
    s_elems = sorted(elems)
    return mean, q(s_elems, 0.5), q(s_elems, 0.25), q(s_elems, 0.75)


def print_table(res):
    min_n = min(map(lambda x: x[0], res))
    max_n = max(map(lambda x: x[0], res))
    idents = ['000000', '100000', '110000', '111000', '111100', '000010', '000001']
    print('    n &      no-sb &        bfs &   bfs + md &   bfs,md,w & bfs,md,w,d &        lex &    unavoid \\\\ ')
    for n in xrange(min_n, max_n + 1):
        print('{:5d}'.format(n) + ' & ', end='')
        for ident in idents:
            elems = list(get_all(res, ident, n))
            sep = ' \\\\' if ident == idents[-1] else ' & '
            if len(elems) == 0:
                print(' ' * 10 + sep, end='')
            else:
                tl = elems[0][3]
                mean, median, q25, q75 = stat(elems)
                print(tl_str(median, tl) + '(' + str(len(elems)) + ')' + sep, end='')
        print()


def print_table_unsat(filename):
    print_table(list(filter(lambda x: x[2], parse_results(filename))))


def print_table_sat(filename):
    print_table(list(filter(lambda x: not x[2], parse_results(filename))))


if __name__ == '__main__':
    if len(sys.argv) > 1:
        if sys.argv[1] == 'sat':
            print_table_sat('dumps/dump1')
        else:
            print_table_unsat('dumps/dump1')
    else:
        print('=' * 40 + ' UNSAT ' + 40 * '=')
        print_table_unsat('dumps/dump1')
        print('=' * 41 + ' SAT ' + 41 * '=')
        print_table_sat('dumps/dump1')
