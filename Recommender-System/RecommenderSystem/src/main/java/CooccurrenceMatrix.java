import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

public class CooccurrenceMatrix {
    public static class CooccurrenceMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: userID \t "movie1: rating1, movie2: rating2, ..."
            // output: < key="movie_A: movie_B", value=1 >

            String[] input = value.toString().trim().split("\t");

            // invalid input
            if (input.length < 2) {
                return;
            }

            String[] movieRatingList = input[1].trim().split(",");

            for (int i = 0; i < movieRatingList.length; i++) {
                String movieA = movieRatingList[i].trim().split(":")[0];

                for (int j = 0; j < movieRatingList.length; j++) {
                    String movieB = movieRatingList[j].trim().split(":")[0];
                    String outputKey = movieA + ":" + movieB;
                    context.write(new Text(outputKey), new IntWritable(1));
                }
            }

        }
    }

    public static class  CooccurrenceReducer extends Reducer<Text, IntWritable, Text, IntWritable> {
        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            // input: < key="movie_A: movie_B", value=1, 1, 1, ... >
            // output: < key="movie_A: movie_B", value=count >
            int sum = 0;
            for (IntWritable value: values) {
                sum += value.get();
            }

            context.write(key, new IntWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: data divided by user folder, e.g., /dataDividedByUser
        // args[1]: un-normalized cooccurrence matrix folder, e.g., /coOccurrenceMatrix

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Build Cooccurrence Matrix");

        job.setJarByClass(CooccurrenceMatrix.class);
        job.setMapperClass(CooccurrenceMapper.class);
        job.setReducerClass(CooccurrenceReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
