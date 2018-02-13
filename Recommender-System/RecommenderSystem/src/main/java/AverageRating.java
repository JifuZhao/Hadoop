import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

public class AverageRating {

    public static class UserRatingReader extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: userID, movieID, rating
            // output: < key=userID, value=rating >

            String[] tokens = value.toString().trim().split(",");
            String userID = tokens[0];
            String rating = tokens[2];

            context.write(new Text(userID), new DoubleWritable(Double.parseDouble(rating)));
        }
    }

    public static class AverageRatingReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {
            // input: < key=userID, value=< rating1, rating2, ... > >
            // output: < key=userID, value=average rating >

            int count = 0;
            double sum = 0;

            for (DoubleWritable value: values) {
                sum += value.get();
                count += 1;
            }

            context.write(key, new DoubleWritable(sum / count));
        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: original user rating folder, e.g., /input
        // args[1]: averaged user rating list folder, e.g., /averageRating

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Calculate User Average Rating");

        job.setJarByClass(AverageRating.class);
        job.setMapperClass(AverageRating.UserRatingReader.class);
        job.setReducerClass(AverageRating.AverageRatingReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
