/******************************************************************************
 * Mapper and Reducer for Hadoop Project
 *
 * First Map-Reduce job
 * Create N-grams and corresponding counts based on raw input
 *
 ******************************************************************************/

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.lib.db.DBConfiguration;
import org.apache.hadoop.mapred.lib.db.DBOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Driver {
    public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException {

        // Hadoop job 1
        Configuration conf1 = new Configuration();
        conf1.set("textinputformat.record.delimiter", "[.,!?;]");
        conf1.set("noOfGram", args[2]);  // number of grams to keep

        Job job1 = Job.getInstance(conf1);
        job1.setJobName("WordsToCount");
        job1.setJarByClass(Driver.class);

        job1.setMapperClass(WordsToCount.WordsMapper.class);
        job1.setReducerClass(WordsToCount.WordsReducer.class);

        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);

        job1.setInputFormatClass(TextInputFormat.class);
        job1.setOutputFormatClass(TextOutputFormat.class);

        TextInputFormat.setInputPaths(job1, new Path(args[0]));
        TextOutputFormat.setOutputPath(job1, new Path(args[1]));

        job1.waitForCompletion(true);

        // Hadoop job 2
        Configuration conf2 = new Configuration();
        conf2.set("threshold", args[3]);  // threshold for job 2
        conf2.set("k", args[4]);  // top k elements to keep

        // configure the DataBase connection
        DBConfiguration.configureDB(conf2,
                "com.mysql.jdbc.Driver",
                "jdbc:mysql://10.192.51.78:8889/JifuTest",
                "root",
                "root");

        Job job2 = Job.getInstance(conf2);
        job2.setJobName("CountToNgram");
        job2.setJarByClass(Driver.class);

        job2.addArchiveToClassPath(new Path("/mysql/mysql-connector-java-5.1.39-bin.jar"));

        job2.setMapOutputKeyClass(Text.class);
        job2.setMapOutputValueClass(Text.class);
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(NullWritable.class);

        job2.setMapperClass(CountToNgram.NgramMapper.class);
        job2.setReducerClass(CountToNgram.NgramReducer.class);

        job2.setInputFormatClass(TextInputFormat.class);
        job2.setOutputFormatClass(TextOutputFormat.class);

        DBOutputFormat.setOutput(
                job2,
                "output",
                new String[] {"Starting_words", "Following_word", "Count"}
        );

        TextInputFormat.setInputPaths(job2, args[1]);

        job2.waitForCompletion(true);

    }
}
