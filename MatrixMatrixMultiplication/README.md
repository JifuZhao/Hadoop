# Matrix and Vector Multiplication

## Goal:
Implement matrix and matrix multiplication through Hadoop MapReduce

## Example
![equation](https://latex.codecogs.com/gif.latex?%5Cdpi%7B200%7D%20%5Cbg_white%20%5Cleft%20%28%20%5Cbegin%7Barray%7D%7Bccc%7D%201%20%26%202%20%26%203%20%5C%5C%204%20%26%205%20%26%206%20%5C%5C%207%20%26%208%20%26%209%20%5C%5C%2010%20%26%2011%20%26%2012%5C%5C%20%5Cend%7Barray%7D%20%5Cright%20%29%20%5Ctimes%20%5Cleft%20%28%20%5Cbegin%7Barray%7D%7Bccc%7D%201%20%26%204%20%26%207%20%5C%5C%202%20%26%205%20%26%208%20%5C%5C%203%20%26%206%20%26%209%20%5C%5C%20%5Cend%7Barray%7D%20%5Cright%20%29%20%3D%20%5Cleft%20%28%20%5Cbegin%7Barray%7D%7Bccc%7D%2014%20%26%2032%20%26%2050%20%5C%5C%2032%20%26%2077%20%26%20122%20%5C%5C%2050%20%26%20122%20%26%20194%20%5C%5C%2068%20%26%20167%20%26%20266%20%5C%5C%20%5Cend%7Barray%7D%20%5Cright%20%29)

### Input:
* Matrix 1 input: row, col, value <br>
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

* Matrix 2 input: row, col, value <br>
1,1,1 <br>
1,2,4 <br>
1,3,7 <br>
2,1,2 <br>
2,2,5 <br>
2,3,8 <br>
3,1,3 <br>
3,2,6 <br>
3,3,9 <br>

## Workflow
+ MapReduce Job 1: Multiplication.java
    - Mapper 1: read raw matrix 1 input
        - input: < offset, row,col,value >
        - ouput: < key=col, value="row=value" >

    - Mapper 2: read raw matrix 2 input
        - input: < offset, row,col,value >
        - ouput: < key=row, value="col:value" >

    - Reducer: multiply matrix 1 and matrix 2
        - input: < key=col, value=< row1=matrix1_value1, row2=matrix1_value2, ..., col1:matrix2_value1, col2:matrix2_value2, ... > >
        - output: < key="row,col", value=matrix1_value * matrix2_value >


+ MapReduce Job 2: Summation.java
    - Mapper: read cell result from MapReduce Job 1
        - input: < offset, "row,col" \t value >
        - ouput: < key="row,col", value=value >

    - Reducer: calculate the sum of each row
        - input: < key="row,col", value=< value1, value2, ... > >
        - output: < key="row,col", value=sum >


## Run code:
First start your Hadoop module, then enter the MatrixVectorMultiplication file by

    $: cd MatrixMatrixMultiplication/
    $: hdfs dfs -mkdir /matrix1
    $: hdfs dfs -put matrix1/* /matrix1/
    $: hdfs dfs -mkdir /matrix2
    $: hdfs dfs -put matrix2/* /matrix2/
    $: cd src/main/java/
    $: hadoop com.sun.tools.javac.Main -d class *.java
    $: cd class/
    $: jar cf matrixMultiplication.jar *.class
    $: hadoop jar matrixMultiplication.jar Driver /matrix1 /matrix2 /cell /output
    $: hdfs dfs -cat /output/*

### Demo of output
<img src="./output/result.png" width=700>
