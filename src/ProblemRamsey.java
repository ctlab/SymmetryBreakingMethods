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

import org.apache.commons.math3.util.Combinations;

/**
 * @author Moklev Vyacheslav
 */
public class ProblemRamsey {
    public static final int[][] R = {
            {1,  1,  1,  1,  1,  1,  1,  1,  1, 1},
            {1,  2,  3,  4,  5,  6,  7,  8,  9, 10},
            {1,  3,  6,  9,  14, 18, 23, 28, 36},
            {1,  4,  9,  18, 25},
            {1,  5,  14, 25},
            {1,  6,  18},
            {1,  7,  23},
            {1,  8,  28},
            {1,  9,  36},
            {1,  10}
    };

    @Option(name = "--bee", metaVar = "PATH", usage = "path to BEE root")
    private String beePath = "." + File.separatorChar + "bee20170408";

    @Option(name = "--bfs", forbids = {"--lex", "--unavoidable"}, usage = "force bfs enumeration")
    private boolean sbBfsBase = false;

    @Option(name = "--start-max-deg", depends = {"--bfs"}, forbids = {"--start-lex-min"}, usage = "force start vertex to have maximum degree")
    private boolean sbStartMaxDeg = false;

    @Option(name = "--start-lex-min", depends = {"--bfs"}, forbids = {"--start-max-deg"}, usage = "force start vertex to have lexicographically minimal adjacency row")
    private boolean sbStartLexMin = false;

    @Option(name = "--sorted-weights", depends = {"--bfs"}, usage = "force weights in each layer to be sorted")
    private boolean sbSortedWeights = false;

    @Option(name = "--sorted-degs", depends = {"--sorted-weights"}, usage = "force (weight, degree) in each layer to be sorted")
    private boolean sbSortedWeightsDegs = false;

    @Option(name = "--lex", forbids = {"--bfs", "--unavoidable"}, usage = "enables quadratic lex sorted constraint")
    private boolean sbLex = false;

    @Option(name = "--unavoidable", forbids = {"--bfs", "--lex"}, usage = "enumerate K12 unavoidable subgraph")
    private boolean sbUnavoidable = false;

    @Option(name = "--unsat", forbids = {"--nbEdges"}, usage = "set number of edges to R[k, l] + 1")
    private boolean unsat;

    @Option(name = "--nbNodes", aliases = {"-n"}, required = true, usage = "number of nodes")
    private int nbNodes;
    
    @Option(name = "--redCliqueSize", aliases = {"-k"}, required = true, usage = "size of red forbidden clique")
    private int redCliqueSize;

    @Option(name = "--blueCliqueSize", aliases = {"-l"}, required = true, usage = "size of blue forbidden clique")
    private int blueCliqueSize;

    @Option(name = "--output", aliases = {"-o"}, metaVar = "PATH", usage = "file to write resulting model")
    private String outputPath = null;

    @Option(name = "--help", aliases = {"-h", "--usage", "-help", "-usage"}, help = true)
    private boolean showUsage = false;

    private ProblemRamsey(String[] args) {
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
        PrintWriter out = outputPath == null
                ? new PrintWriter(System.out)
                : new PrintWriter(outputPath);
        
        declareAdjacencyMatrix(out);
        denyColoredClique(out);

        if (sbLex) {
            addLexConstraint(out);
        }
        
        if (sbBfsBase) {
            addBFSConstraint(out);
        }
        
        out.println("solve satisfy");
        out.close();
    }

    private void denyColoredClique(PrintWriter out) {
        for (int[] mask: new Combinations(nbNodes, redCliqueSize)) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < mask.length; i++) {
                for (int j = i + 1; j < mask.length; j++) {
                    list.add("-" + var("A", mask[i], mask[j]));
                }
            }
            out.println("bool_array_or(" + list + ")");
        }

        for (int[] mask: new Combinations(nbNodes, blueCliqueSize)) {
            List<String> list = new ArrayList<>();
            for (int i = 0; i < mask.length; i++) {
                for (int j = i + 1; j < mask.length; j++) {
                    list.add(var("A", mask[i], mask[j]));
                }
            }
            out.println("bool_array_or(" + list + ")");
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

    private void addBFSConstraint(PrintWriter out) {
        // Declare parents in BFS tree, p[i]: [1..i]
        for (int i = 0; i < nbNodes; i++) {
            out.println("new_int(" + var("p", i) + ", 0, " + i + ")");
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
    
    private String var(String prefix, int... indices) {
        return prefix + Arrays.stream(indices)
                .mapToObj(x -> "_" + x)
                .collect(Collectors.joining());
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
        new ProblemRamsey(args).start();
    }
}
