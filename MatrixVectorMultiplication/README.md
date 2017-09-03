# Matrix and Vector Multiplication

## Goal:
Implement matrix and vector multiplication through Hadoop MapReduce

## Example
![equation](https://latex.codecogs.com/png.latex?%5Cdpi%7B200%7D%20%5Cbg_white%20%5Cbegin%7Bpmatrix%7D%201%20%26%202%20%26%203%20%5C%5C%204%20%26%205%20%26%206%20%5C%5C%207%20%26%208%20%26%209%20%5C%5C%2010%20%26%2011%20%26%2012%20%5C%5C%20%5Cend%7Bpmatrix%7D%20%5Ctimes%20%5Cbegin%7Bpmatrix%7D%201%20%5C%5C%202%20%5C%5C%203%20%5C%5C%20%5Cend%7Bpmatrix%7D%20%3D%20%5Cbegin%7Bpmatrix%7D%2014%20%5C%5C%2032%20%5C%5C%2050%20%5C%5C%2068%20%5C%5C%20%5Cend%7Bpmatrix%7D)

### Input:
* Matrix input: row, col, value <br>
1,1,1 <br>
1,2,2 <br>
1,3,3 <br>
2,1,4 <br>
2,2,5 <br>
2,3,6 <br>
3,1,7 <br>
3,2,8 <br>
3,3,9 <br>
4,1,10 <br>
4,2,11 <br>
4,3,12 <br>

* Vector input: row, value <br>
1,1 <br>
2,2 <br>
3,3 <br>

## Workflow
+ MapReduce Job 1: Multiplication.java
    - Mapper 1: read raw matrix input
        - input: < offset, row,col,value >
        - ouput: < key=col, value="row=value" >

    - Mapper 2: read raw vector input
        - input: < offset, row,value >
        - ouput: < key=row, value="value" >

    - Reducer: multiply matrix and vector
        - input: < key=col, value=< row1=matrixValue1, row2=matrixValue2, ..., vectorValue > >
        - output: < key=row, value=matrixValue * vectorValue >


+ MapReduce Job 2: Summation.java
    - Mapper: read cell result from MapReduce Job 1
        - input: < offset, row \t value >
        - ouput: < key=row, value=value >

    - Reducer: calculate the sum of each row
        - input: < key=row, value=< value1, value2, ... > >
        - output: < key=row, value=sum >


## Run code:
First start your Hadoop module, then enter the MatrixVectorMultiplication file by

    $: cd MatrixVectorMultiplication/
    $: hdfs dfs -mkdir /matrix
    $: hdfs dfs -put matrix/* /matrix/
    $: hdfs dfs -mkdir /vector
    $: hdfs dfs -put vector/* /vector/
    $: cd src/main/java/
    $: hadoop com.sun.tools.javac.Main -d class *.java
    $: cd class/
    $: jar cf matrixMultiplication.jar *.class
    $: hadoop jar matrixMultiplication.jar Driver /matrix /vector /cell /output
    $: hdfs dfs -cat /output/*

### Demo of output
<img src="./output/result.png" width=700>
