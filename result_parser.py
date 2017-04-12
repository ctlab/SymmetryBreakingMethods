from __future__ import print_function

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


def get(list, ident, n):
    for elem in list:
        if elem[0] == n and elem[5] == ident:
            return elem
    return None


def print_table_unsat(filename):
    res = list(filter(lambda x: x[2], parse_results(filename)))
    min_n = min(map(lambda x: x[0], res))
    max_n = max(map(lambda x: x[0], res))
    idents = ['000000', '100000', '110000', '111000', '111100', '000010', '000001']
    print('    n &      no-sb &        bfs &   bfs + md &   bfs,md,w & bfs,md,w,d &        lex &    unavoid \\\\ ')
    for n in xrange(min_n, max_n + 1):
        print('{:5d}'.format(n) + ' & ', end='')
        for ident in idents:
            elem = get(res, ident, n)
            sep = ' \\\\' if ident == idents[-1] else ' & '
            if elem is None:
                print(' ' * 10 + sep, end='')
            else:
                if elem[6] is None:
                    print ('{:9.4f}+'.format(elem[3]) + sep, end='')
                else:
                    print('{:10.4f}'.format(elem[6]) + sep, end='')
        print()


if __name__ == '__main__':
    print_table_unsat('dumps/dump1')
