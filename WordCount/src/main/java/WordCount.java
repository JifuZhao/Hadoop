/**
 * Simple implementation of word count program through Hadoop MapReduce
 */

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

public class WordCount {

    public static class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: line of words
            // output: < key=word, value=1 >
            String[] tokens = value.toString().trim().split("\\s+");

            for (String word: tokens) {
                context.write(new Text(word), new IntWritable(1));
            }
        }
    }

    public static class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            // input: < key=word, value=<1, 1, ..., 1> >
            // output: < key=word, value=sum >
            int sum = 0;
            for (IntWritable value: values) {
                sum += value.get();
            }

            context.write(key, new IntWritable(sum));
        }
    }
}
