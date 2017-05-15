import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Vyacheslav Moklev
 */
public class ResultExtractor {
    private static final Pattern BOOL_VAR = Pattern.compile("\\(([^,]+),bool,(-?[0-9]+)\\)");
    private static final Pattern ADJ_VAR = Pattern.compile("A_([0-9]+)_([0-9]+)");
    
    public static void main(String[] args) {
        String mapFile = args[0];
        String resultFile = args[1];
        Map<String, Boolean> varMap = getVariableValues(mapFile, resultFile);
        printGraph(varMap);
    }
    
    private static void printGraph(Map<String, Boolean> map) {
        System.out.println("graph {");
        map.forEach((key, val) -> {
            if (val) {
                Matcher matcher = ADJ_VAR.matcher(key);
                if (matcher.find()) {
                    int u = Integer.parseInt(matcher.group(1));
                    int v = Integer.parseInt(matcher.group(2));
                    if (u < v) {
                        System.out.println("    " + u + " -- " + v + ";");
                    }
                }
            }
        });
        System.out.println("}");
    }

    private static Map<String, Boolean> getVariableValues(String mapFile, String resultFile) {
        Map<String, Integer> boolMap = new HashMap<>();
        Map<Integer, Boolean> resultMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(mapFile))) {
            br.lines().forEach(s -> {
                Matcher matcher = BOOL_VAR.matcher(s);
                if (matcher.find()) {
                    boolMap.put(matcher.group(1), Integer.valueOf(matcher.group(2)));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader br = new BufferedReader(new FileReader(resultFile))) {
            br.lines().forEach(s -> {
                if (!s.startsWith("v"))
                    return;
                String[] parts = s.substring(1).trim().split(" +");
                for (String part: parts) {
                    int value = Integer.parseInt(part);
                    if (value == 0)
                        continue;
                    resultMap.put(Math.abs(value), value > 0);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return boolMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> resultMap.get(Math.abs(e.getValue()))
                ));
    }
}
