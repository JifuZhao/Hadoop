/**
 * Main driver for whole job
 */

public class Driver {

    public static void main(String[] args) throws Exception {

        PageRankMultiplication multiplication = new PageRankMultiplication();
        PageRankSummation sum = new PageRankSummation();

        //args0: dir of transition.txt
        //args1: dir of pageRank.txt
        //args2: dir of PageRankMultiplication result
        //args3: times of convergence
        //args4: pre-defined beta value
        String transitionMatrix = args[0];
        String prMatrix = args[1];
        String unitState = args[2];
        int count = Integer.parseInt(args[3]);
        String beta = args[4];

        for(int i = 0; i < count; i++) {
            String[] args1 = {transitionMatrix, prMatrix + i, unitState + i, beta};
            multiplication.main(args1);
            String[] args2 = {unitState + i, prMatrix + i, prMatrix + (i + 1), beta};
            sum.main(args2);
        }
    }
}