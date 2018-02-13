/******************************************************************************
 * Mapper and Reducer for Hadoop Project
 *
 * Second Map-Reduce job
 * Create pre-words, following-word, count model based on output from
 * the first Map-Reduce job
 *
 ******************************************************************************/

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.util.Comparator;
import java.util.PriorityQueue;

public class CountToNgram {

    // Hadoop Mapper class
    public static class NgramMapper extends Mapper<LongWritable, Text, Text, Text> {

        int threshold;  // threshold to keep

        @Override
        public void setup(Context context) throws IOException {
            Configuration conf = context.getConfiguration();
            threshold = conf.getInt("threshold", 20);
        }

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            if (value == null || (value.toString().trim()).length() == 0) {
                return;
            }

            // read input data from the first Map-Reduce job
            // format: <words \t count>
            String line = value.toString().trim();
            String[] input = line.split("\t");
            if (input.length < 2) {
                return;
            }

            String[] tokens = input[0].split("\\s+");
            int count = Integer.valueOf(input[1]);

            if (count < threshold) {
                return;
            }

            // build the new string for output key and value
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < tokens.length - 1; i++) {
                builder.append(tokens[i]);
                builder.append(" ");
            }
            String outputKey = builder.toString().trim();
            String outputValue = tokens[tokens.length - 1];

            if (outputKey != null && outputKey.length() >= 1) {
                context.write(new Text(outputKey), new Text(outputValue + ":" + count));
            }

        }
    }

    // Hadoop Reducer class
    public static class NgramReducer extends Reducer<Text, Text, DBOutputWritable, NullWritable> {

        int k;  // only keep the top k key-value pairs

        static class WordsPair {
            String word;
            int count;

            public WordsPair(String value) {
                String[] line = value.split(":");
                word = line[0].trim();
                count = Integer.valueOf(line[1].trim());
            }
        }

        @Override
        public void setup(Context context) throws IOException {
            Configuration conf = context.getConfiguration();
            k = conf.getInt("k", 10);
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // create PriorityQueue to keep the top-k elements
            // is in fact a min-heap
            PriorityQueue<WordsPair> minHeap = new PriorityQueue<WordsPair>(k, new Comparator<WordsPair>() {
                public int compare(WordsPair w1, WordsPair w2) {
                    if (w1.count > w2.count) {
                        return 1;
                    } else if (w1.count < w2.count) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            for (Text value: values) {
                String curValue = value.toString().trim();
                minHeap.add(new WordsPair(curValue));

                if (minHeap.size() > k) {
                    minHeap.poll();
                }
            }

            for (WordsPair wp: minHeap) {
                context.write(new DBOutputWritable(key.toString(), wp.word, wp.count), NullWritable.get());
            }

        }
    }

    public static void main(String[] args) {
        // test, skipped
    }
}
