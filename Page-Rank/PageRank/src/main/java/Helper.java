/**
 *  Java helper code to transform page-rank results into csv file
 */

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Helper {
    public static void main(String[] args) throws IOException {
        String prPath = "/Users/jifu/GitHub/JiuZhang/Page-Rank/PageRank/src/main/resources/pr30.txt";
        String transitionPath = "/Users/jifu/GitHub/JiuZhang/Page-Rank/PageRank/src/main/resources/transition.txt";
        String resultPath = "/Users/jifu/GitHub/JiuZhang/Page-Rank/PageRank/src/main/resources/result30.csv";

        BufferedReader pr = new BufferedReader(new FileReader(prPath));
        BufferedReader transition = new BufferedReader(new FileReader(transitionPath));
        FileWriter fileWriter = new FileWriter(resultPath);

        Map<String, String> page_pr = new HashMap<String, String>();

        String line = pr.readLine();
        while (line != null) {
            page_pr.put(line.split("\t")[0], line.split("\t")[1]);
            line = pr.readLine();
        }
        pr.close();

        line = transition.readLine();
        fileWriter.write("source,target,value\n");

        while (line != null) {
            String[] from_tos = line.split("\t");
            String[] tos = from_tos[1].split(",");
            for (String to: tos) {
                String value = page_pr.get(to);
                fileWriter.write(from_tos[0] + "," + to + "," + value + "\n");
            }
            line = transition.readLine();
        }

        transition.close();
        fileWriter.close();
    }
}
