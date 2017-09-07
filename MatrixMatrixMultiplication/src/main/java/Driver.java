/**
 * Driver to run the word count example
 *
 * To run the code:
 *      first start your Hadoop module, then enter the MatrixMatrixMultiplication file by
 *      $: cd MatrixMatrixMultiplication/
 *      $: hdfs dfs -mkdir /matrix1
 *      $: hdfs dfs -put matrix1/* /matrix1/
 *      $: hdfs dfs -mkdir /matrix2
 *      $: hdfs dfs -put matrix2/* /matrix2/
 *      $: cd src/main/java/
 *      $: hadoop com.sun.tools.javac.Main -d class *.java
 *      $: cd class
 *      $: jar cf matrixMultiplication.jar *.class
 *      $: hadoop jar matrixMultiplication.jar Driver /matrix1 /matrix2 /cell /output
 *      $: hdfs dfs -cat /output/*
 */

public class Driver {
    public static void main(String[] args) throws Exception {

        // args[0]: path to the first atrix input, e.g., /matrix1
        // args[1]: path to the second matrix input, e.g., /matrix2
        // args[2]: path to the output from MapReduce Job 1, e.g., /cell
        // args[3]: path to the output, e.g., /output

        Multiplication multiplication = new Multiplication();
        Summation summation = new Summation();

        String matrix1Path = args[0];
        String matrix2Path = args[1];
        String cellPath = args[2];
        String outputPath = args[3];

        // MapReduce Job 1
        String[] multiplicationArgs = {matrix1Path, matrix2Path, cellPath};
        multiplication.main(multiplicationArgs);

        // MapReduce Job 2
        String[] summationArgs = {cellPath, outputPath};
        summation.main(summationArgs);

    }
}