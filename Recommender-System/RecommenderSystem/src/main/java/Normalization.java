import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Normalization {
    public static class NormalizeMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: movie_A: movie_B \t count
            // output: < key=movie_A, value="movie_B=count" >
            String movieCount = value.toString().trim();
            String[] tokens = movieCount.split("\t");
            String[] movieAB = tokens[0].trim().split(":");
            context.write(new Text(movieAB[0]), new Text(movieAB[1] + "=" + tokens[1]));
        }
    }

    public static class  NormalizeReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // input: < key=movie_A, value=< movie_B=count, movie_C=count, ... > >
            // output: < key=movie_B, value="movieA=count/total" >

            int denominator = 0;
            Map<String, Integer> movieCountMap = new HashMap<String, Integer>();
            for (Text value: values) {
                String[] movieCount = value.toString().trim().split("=");
                int count = Integer.parseInt(movieCount[1]);
                denominator += count;
                movieCountMap.put(movieCount[0], count);
            }

            for (Map.Entry<String, Integer> entry: movieCountMap.entrySet()) {
                // key: movieB
                // value: count
                double ratio = (double) entry.getValue() / denominator;
                context.write(new Text(entry.getKey()), new Text(key + "=" + ratio));
            }

            /* Another choice
            Iterator iterator = movieCountMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iterator.next();
                // key: movieB
                // value: count
                double ratio = (double) entry.getValue() / denominator;
                context.write(new Text(entry.getKey()), new Text(key + "=" + ratio));
            }
            */
        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: un-normalized cooccurrence matrix folder, e.g., /coOccurrenceMatrix
        // args[1]: normalized cooccurrence matrix folder, e.g., /Normalize

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Normalize Cooccurrence Matrix");

        job.setJarByClass(Normalization.class);
        job.setMapperClass(NormalizeMapper.class);
        job.setReducerClass(NormalizeReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
