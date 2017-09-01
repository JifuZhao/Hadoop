/**
 * Driver to run the word count example
 *
 * To run the code:
 *      first start your Hadoop module
 *      then enter the WordCount file by cd WordCount
 *      $: hdfs dfs -mkdir /input
 *      $: hdfs dfs -put input/* /input/
 *      $: cd src/main/java/
 *      $: hadoop com.sun.tools.javac.Main -d class *.java
 *      $: cd class
 *      $: jar cf wordCount.jar *.class
 *      $: hadoop jar wordCount.jar Driver /input /output
 *      $: hdfs dfs -cat /output/*
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Driver {
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Word Count Demo");

        job.setJarByClass(WordCount.class);
        job.setMapperClass(WordCount.WordCountMapper.class);
        job.setReducerClass(WordCount.WordCountReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
