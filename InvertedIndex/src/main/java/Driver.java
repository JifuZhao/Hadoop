/**
 * Driver to run the word count example
 *
 * To run the code:
 *      first start your Hadoop module
 *      then enter the InvertedIndex file by
 *      $: cd InvertedIndex/
 *      $: hdfs dfs -mkdir /input
 *      $: hdfs dfs -put input/* /input/
 *      $: cd src/main/java/
 *      $: hadoop com.sun.tools.javac.Main -d class *.java
 *      $: cd class/
 *      $: jar cf invertedIndex.jar *.class
 *      $: hadoop jar invertedIndex.jar Driver /input /output
 *      $: hdfs dfs -cat /output/*
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Driver {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Interted Index Demo");

        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(InvertedIndex.InvertedIndexMapper.class);
        job.setReducerClass(InvertedIndex.InvertedIndexReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
