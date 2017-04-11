import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Problem34 {
    public static final int[] f4 = {0, 0, 1, 2, 3, 5, 6, 8, 10, 12, 15, 16, 18, 21, 23, 26, 28, 31,
            34, 38, 41, 44, 47, 50, 54, 57, 61, 65, 68, 72, 76, 80, 85};

    @Option(name = "--bee", metaVar = "PATH", usage = "path to BEE root")
    private String beePath = "." + File.separatorChar + "bee20170408";

    @Option(name = "--bfs", forbids = {"--lex", "--unavoidable"}, usage = "force bfs enumeration")
    private boolean sbBfsBase = false;

    @Option(name = "--start-max-deg", depends = {"--bfs"}, usage = "force start vertex to have maximum degree")
    private boolean sbStartMaxDeg = false;

    @Option(name = "--sorted-weights", depends = {"--bfs"}, usage = "force weights in each layer to be sorted")
    private boolean sbSortedWeights = false;

    @Option(name = "--sorted-degs", depends = {"--sorted-weights"}, usage = "force (weight, degree) in each layer to be sorted")
    private boolean sbSortedWeightsDegs = false;

    @Option(name = "--lex", forbids = {"--bfs", "--unavoidable"}, usage = "enables quadratic lex sorted constraint")
    private boolean sbLex = false;

    @Option(name = "--unavoidable", forbids = {"--bfs", "--lex"}, usage = "enumerate K12 unavoidable subgraph")
    private boolean sbUnavoidable = false;

    @Option(name = "--unsat", forbids = {"--nbEdges"}, usage = "set number of edges to f4[n] + 1")
    private boolean unsat;

    @Option(name = "--nbNodes", aliases = {"-n"}, required = true, usage = "number of nodes")
    private int nbNodes;

    @Option(name = "--nbEdges", aliases = {"-m"}, forbids = {"--unsat"}, usage = "number of edges")
    private int nbEdges = 0;

    @Option(name = "--output", aliases = {"-o"}, metaVar = "PATH", usage = "file to write resulting model")
    private String outputPath = null;
    
    @Option(name = "--help", aliases = {"-h", "--usage", "-help", "-usage"}, help = true)
    private boolean showUsage = false;

    private Problem34(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        parser.getProperties()
                .withUsageWidth(120);
        try {
            parser.parseArgument(args);
            if (showUsage) {
                parser.printUsage(System.out);
                System.exit(0);
            }
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.exit(1);
        }
    }

    private void start() throws FileNotFoundException {
        if (nbEdges == 0) {
            nbEdges = unsat ? f4[nbNodes] + 1 : f4[nbNodes];
        }
        PrintWriter out = outputPath == null
                ? new PrintWriter(System.out)
                : new PrintWriter(outputPath);
        
        declareAdjacencyMatrix(out);
        addNoThreeFourCyclesConstraint(out);
        declareDegrees(out);
        
        if (sbLex) {
            addLexConstraint(out);
        }
        if (sbUnavoidable) {
            if (nbNodes < 12) {
                System.err.println("Unavoidable symmetry break available only for graphs with 12 or more nodes");
                System.exit(1);
            }
            addUnavoidableConstraint(out);
        }
        if (sbBfsBase) {
            addAllBFSConstraints(out);
        }
        
        out.println("solve satisfy");
        out.close();
    }

    private void addBFSConstraint(PrintWriter out) {
        // Declare parents in BFS tree, p[i]: [1..i - 1]
        for (int i = 1; i < nbNodes; i++) {
            out.println("new_int(" + var("p", i) + ", 0, " + (i - 1) + ")");
        }

        // Define parents, FORALL i, j. p[j] = i <=> (A[i, j] && !(EXISTS k < i. A[k, j]))
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 1; j < nbNodes; j++) {
                String X1 = nextBool(out);
                String X2 = nextBool(out);

                // p[j] = i <=> X1
                out.println("int_eq_reif(" + var("p", j) + ", " + i + ", " + X1 + ")");

                // EXISTS k < i. A[k, j] <=> X2
                List<String> list = new ArrayList<>();
                for (int k = 0; k < i; k++) {
                    list.add(var("A", k, j));
                }
                out.println("bool_array_or_reif(" + list + ", " + X2 + ")");

                // A[i, j] && -X2 <=> X1
                out.println("bool_and_reif(" + var("A", i, j) + ", -" + X2 + ", " + X1 + ")");
            }
        }

        // Constraint for ordering parents, p[i] <= p[i + 1]
        for (int i = 1; i < nbNodes - 1; i++) {
            out.println("int_leq(" + var("p", i) + ", " + var("p", i + 1) + ")");
        }
    }

    private void addStartMaxDegConstraint(PrintWriter out) {
        // Constraint for start node, degree[0] == max_deg
        out.println("int_eq(" + var("degree", 0) + ", max_deg)");
    }

    private void addSortedWeightsConstraint(PrintWriter out) {
        // Declare weights of subtree, w[i]: [1..nbNodes - i]
        for (int i = 0; i < nbNodes; i++) {
            out.println("new_int(" + var("w", i) + ", 1, " + (nbNodes - i) + ")");
        }

        // Constraint for sorted weights, (p[i] == p[i + 1]) => (w[i] >= w[i + 1])
        for (int i = 1; i < nbNodes - 1; i++) {
            String X1 = nextBool(out);
            String X2 = nextBool(out);
            out.println("int_eq_reif(" + var("p", i) + ", " + var("p", i + 1) + ", " + X1 + ")");
            out.println("int_geq_reif(" + var("w", i) + ", " + var("w", i + 1) + ", " + X2 + ")");
            out.println("bool_or(-" + X1 + ", " + X2 + ")");
        }

        // Define weights of subtree, w[i] = 1 + sum(w[j] * bool2int(p[j] == i), j = i+1..n-1)
        for (int i = 0; i < nbNodes; i++) {
            List<String> list = new ArrayList<>();
            for (int j = i + 1; j < nbNodes; j++) {
                String X1 = nextBool(out);
                String X2 = nextInt(out, 0, 1);
                String X3 = nextInt(out, 0, nbNodes);
                out.println("int_eq_reif(" + var("p", j) + ", " + i + ", " + X1 + ")");
                out.println("bool2int(" + X1 + ", " + X2 + ")");
                out.println("int_times(" + var("w", j) + ", " + X2 + ", " + X3 + ")");
                list.add(X3);
            }
            list.add("1");
            out.println("int_array_sum_eq(" + list + ", " + var("w", i) + ")");
        }
    }

    private void addSortedDegsConstraint(PrintWriter out) {
        // Constraint order of vertices in layer, (p[i] == p[i + 1] && w[i] == w[i + 1]) => (degree[i] >= degree[i + 1])
        for (int i = 1; i < nbNodes - 1; i++) {
            String X1 = nextBool(out);
            String X2 = nextBool(out);
            String X3 = nextBool(out);
            out.println("int_eq_reif(" + var("p", i) + ", " + var("p", i + 1) + ", " + X1 + ")");
            out.println("int_eq_reif(" + var("w", i) + ", " + var("w", i + 1) + ", " + X2 + ")");
            out.println("int_geq_reif(" + var("degree", i) + ", " + var("degree", i + 1) + ", " + X3 + ")");
            out.println("bool_array_or([-" + X1 + ", -" + X2 + ", " + X3 + "])");
        }
    }
    
    private void addAllBFSConstraints(PrintWriter out) {
        addBFSConstraint(out);
        if (sbStartMaxDeg) {
            addStartMaxDegConstraint(out);
        }
        if (sbSortedWeights) {
            addSortedWeightsConstraint(out);
            if (sbSortedWeightsDegs) {
                addSortedDegsConstraint(out);
            }
        }
    }
    
    private void addUnavoidableConstraint(PrintWriter out) {
        // Edges of unavoidable subgraph of K12 
        int[][] edges = {
                {0, 6},       
                {0, 7},       
                {1, 6},       
                {1, 7},       
                {1, 8},       
                {2, 6},       
                {2, 7},       
                {2, 8},       
                {3, 7},       
                {3, 8},       
                {4, 8},       
                {5, 8}      
        };
        // Constraint for simultaneous presence or absence of all edges 
        for (int i = 0; i < edges.length; i++) {
            int[] edge = edges[i];
            int[] nextEdge = edges[(i + 1) % edges.length];
            out.println("bool_array_or([-" + var("A", edge[0], edge[1]) + ", " + var("A", nextEdge[0], nextEdge[1]) + ")");
        }
    }

    private void addLexConstraint(PrintWriter out) {
        for (int i = 0; i < nbNodes; i++) {
            for (int j = i + 1; j < nbNodes; j++) {
                if (j - i != 2) {
                    List<String> listI = new ArrayList<>();
                    List<String> listJ = new ArrayList<>();
                    for (int k = 0; k < nbNodes; k++) {
                        if (k != i && k != j) {
                            listI.add(var("A", i, k));
                            listJ.add(var("A", j, k));
                        }
                    }
                    out.println("bool_arrays_lex(" + listI + ", " + listJ + ")");
                }
            }
        }
    }
    
    private void declareDegrees(PrintWriter out) {
        // Declare degree variables, degree[i]: [1..nbNodes]
        for (int i = 0; i < nbNodes; i++) {
            out.println("new_int(" + var("degree", i) + ", 1, " + nbNodes + ")");
        }
        // Declare minimum, maximum degrees and (min * max) degree 
        out.println("new_int(min_deg, 1, " + nbNodes + ")");
        out.println("new_int(max_deg, 1, " + nbNodes + ")");
        out.println("new_int(min_deg_times_max_deg, 1, " + nbNodes * nbNodes + ")");
        out.println("int_times(min_deg, max_deg, min_deg_times_max_deg)");
        
        // General degrees properties
        // min_deg <= max_deg
        out.println("int_leq(min_deg, max_deg)");
        // max_deg >= 2 * m / n;
        out.println("int_geq(max_deg, " + ceilDiv(2 * nbEdges, nbNodes) + ")");

        // Constraints from 3,4-cycles absence
        if (nbEdges == f4[nbNodes]) {
            // min_deg * max_deg <= n - 1
            out.println("int_leq(min_deg_times_max_deg, " + (nbNodes - 1) + ")");
            // min_deg >= m - f4[n - 1];
            out.println("int_geq(min_deg, " + (nbEdges - f4[nbNodes - 1]) + ")");
        }

        // Definition of degree
        for (int i = 0; i < nbNodes; i++) {
            // degree[i] = sum(A[i])
            out.print("bool_array_sum_eq([");
            for (int j = 0; j < nbNodes; j++) {
                out.print(var("A", i, j));
                if (j != nbNodes - 1)
                    out.print(", ");
            }
            out.println("], " + var("degree", i) + ")");

            // degree[i] <= max_deg
            out.println("int_leq(" + var("degree", i) + ", max_deg)");

            // degree[i] >= min_deg
            out.println("int_geq(" + var("degree", i) + ", min_deg)");
        }

        // Define minimum degree, min_deg = min(degree)
        out.print("int_array_min([");
        for (int j = 0; j < nbNodes; j++) {
            out.print(var("degree", j));
            if (j != nbNodes - 1)
                out.print(", ");
        }
        out.println("], min_deg)");

        // Define maximum degree, max_deg = max(degree)
        out.print("int_array_max([");
        for (int j = 0; j < nbNodes; j++) {
            out.print(var("degree", j));
            if (j != nbNodes - 1)
                out.print(", ");
        }
        out.println("], max_deg)");

        // Sum of degrees, Σ_i degree[i] = 2 * nbEdges
        out.print("bool_array_sum_eq([");
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                out.print(var("A", i, j));
                if (i != nbNodes - 1 || j != nbNodes - 1)
                    out.print(", ");
            }
        }
        out.println("], " + 2 * nbEdges + ")");
    }
    
    private void addNoThreeFourCyclesConstraint(PrintWriter out) {
        // Declare auxiliary variables, x[i, j, k] = A[i, j] && A[j, k] 
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                for (int k = i + 1; k < nbNodes; k++) {
                    if (i != j && k != j) {
                        out.println("new_bool(" + var("x", i, j, k) + ")");
                        out.println("bool_and_reif(" + var("A", i, j) + ", " + var("A", j, k) + ", " + var("x", i, j, k) + ")");
                    }
                }
            }
        }

        // Declare auxiliary variables, x[i, k] = ||{x[i, j, k] | j != i, j != k}
        for (int i = 0; i < nbNodes; i++) {
            for (int k = i + 1; k < nbNodes; k++) {
                List<String> list = new ArrayList<>();
                for (int j = 0; j < nbNodes; j++) {
                    if (i != j && k != j) {
                        list.add(var("x", i, j, k));
                    }
                }
                out.println("new_bool(" + var("x", i, k) + ")");
                out.println("bool_array_or_reif(" + list + ", " + var("x", i, k) + ")");
            }
        }

        // Constraint for absence of 3-cycles, !A[i, k] || !x[i, k]
        for (int i = 0; i < nbNodes; i++) {
            for (int k = i + 1; k < nbNodes; k++) {
                out.println("bool_or(-" + var("A", i, k) + ", -" + var("x", i, k) + ")");
            }
        }

        // Constraint for absence of 4-cycles, Σ_j A[i, j, k] < 2
        for (int i = 0; i < nbNodes; i++) {
            for (int k = i + 1; k < nbNodes; k++) {
                List<String> list = new ArrayList<>();
                for (int j = 0; j < nbNodes; j++) {
                    if (j != i && j != k) {
                        list.add(var("x", i, j, k));
                    }
                }
                out.println("bool_array_sum_lt(" + list + ", 2)");
            }
        }
    }
    
    private void declareAdjacencyMatrix(PrintWriter out) {
        // Declare variables for adjacency matrix, A[u, v]: bool
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                out.println("new_bool(" + var("A", i, j) + ")");
            }
        }

        // Constraint for symmetry of matrix, A[u, v] = A[v, u]
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                if (i < j) {
                    out.println("bool_eq(" + var("A", i, j) + ", " + var("A", j, i) + ")");
                }
            }
        }

        // Constraint for absence of loops, A[v, v] = 0
        for (int i = 0; i < nbNodes; i++) {
            out.println("bool_eq(" + var("A", i, i) + ", false)");
        }
    }

    private String var(String prefix, int... indices) {
        return prefix + Arrays.stream(indices)
                .mapToObj(x -> "_" + x)
                .collect(Collectors.joining());
    }

    private int ceilDiv(int n, int m) {
        return (int) Math.ceil((double) n / m);
    }

    private int lastTemp = 0;
    private String nextBool(PrintWriter out) {
        out.println("new_bool(temp_" + lastTemp + ")");
        return "temp_" + lastTemp++;
    }

    private String nextInt(PrintWriter out, int a, int b) {
        out.println("new_int(temp_" + lastTemp + ", " + a + ", " + b + ")");
        return "temp_" + lastTemp++;
    }
    
    public static void main(String[] args) throws FileNotFoundException {
        new Problem34(args).start();
    }
}
 