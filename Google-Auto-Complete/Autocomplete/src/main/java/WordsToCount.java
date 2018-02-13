/******************************************************************************
 * Mapper and Reducer for Hadoop Project
 *
 * First Map-Reduce job
 * Create N-grams and corresponding counts based on raw input
 *
 ******************************************************************************/

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

public class WordsToCount {

    // Hadoop Mapper class
    public static class WordsMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

        int noOfGram;  // maximum number of grams to be used, default value 5

        @Override
        public void setup(Context context) throws IOException {
            Configuration conf = context.getConfiguration();
            noOfGram = conf.getInt("noOfGram", 5);
        }

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            // read the sentence from hdfs
            // transform sentence into N-gram -> counts
            String line = value.toString().trim().toLowerCase();
            line = line.replaceAll("[^a-z]+", " ");  // replace non-characters with " "
            String[] words = line.split("\\s+");  // split line into words

            // skip the useless case
            if (words.length < 2) {
                return;
            }

            // write N-gram -> counts into context
            StringBuilder builder;
            for (int i = 0; i < words.length; i++) {
                builder = new StringBuilder();
                for (int j = 0; i + j < words.length && j < noOfGram; j++) {
                    builder.append(words[i + j]);
                    builder.append(" ");
                    context.write(new Text(builder.toString().trim()), new IntWritable(1));
                }
            }
        }
    }

    // Hadoop Reducer class
    public static class WordsReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            // get the N-gram -> <1, 1, 1> from Mapper and calculate the sum
            int sum = 0;
            for (IntWritable value: values) {
                sum += value.get();
            }
            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) {
        // test, skipped
    }
}
