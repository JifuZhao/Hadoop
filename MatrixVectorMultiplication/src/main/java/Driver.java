/**
 * Driver to run the word count example
 *
 * To run the code:
 *      first start your Hadoop module, then enter the MatrixVectorMultiplication file by
 *      $: cd MatrixVectorMultiplication/
 *      $: hdfs dfs -mkdir /matrix
 *      $: hdfs dfs -put matrix/* /matrix/
 *      $: hdfs dfs -mkdir /vector
 *      $: hdfs dfs -put vector/* /vector/
 *      $: cd src/main/java/
 *      $: hadoop com.sun.tools.javac.Main -d class *.java
 *      $: cd class
 *      $: jar cf matrixMultiplication.jar *.class
 *      $: hadoop jar matrixMultiplication.jar Driver /matrix /vector /cell /output
 *      $: hdfs dfs -cat /output/*
 */

public class Driver {
    public static void main(String[] args) throws Exception {

        // args[0]: path to the matrix input, e.g., /matrix
        // args[1]: path to the vector input, e.g., /vector
        // args[2]: path to the output from MapReduce Job 1, e.g., /cell
        // args[3]: path to the output, e.g., /output

        Multiplication multiplication = new Multiplication();
        Summation summation = new Summation();

        String matrixPath = args[0];
        String vectorPath = args[1];
        String cellPath = args[2];
        String outputPath = args[3];

        // MapReduce Job 1
        String[] multiplicationArgs = {matrixPath, vectorPath, cellPath};
        multiplication.main(multiplicationArgs);

        // MapReduce Job 2
        String[] summationArgs = {cellPath, outputPath};
        summation.main(summationArgs);

    }
}