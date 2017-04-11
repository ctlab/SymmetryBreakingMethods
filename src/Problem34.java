import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;

public class Problem34 {
    public static final int[] f4 = {0, 0, 1, 2, 3, 5, 6, 8, 10, 12, 15, 16, 18, 21, 23, 26, 28, 31,
            34, 38, 41, 44, 47, 50, 54, 57, 61, 65, 68, 72, 76, 80, 85};
 
    @Option(name = "--bee")
    private String beePath = "." + File.separatorChar + "bee20170408";
    
    private Problem34(String[] args) {
        CmdLineParser parser = new CmdLineParser(this);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
        }
    }
    
    private void start() {
        
    }
    
    public static void main(String[] args) {
        new Problem34(args).start();
    }
}
 