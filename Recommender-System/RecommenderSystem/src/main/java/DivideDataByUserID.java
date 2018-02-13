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

public class DivideDataByUserID {
    public static class DivideDataMapper extends Mapper<LongWritable, Text, IntWritable, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: userID, movieID, rating
            // output: < key=userID, value=movieID: rating >

            String line = value.toString().trim();
            String[] tokens = line.split(",");
            String userID = tokens[0];
            String movieID = tokens[1];
            String rating = tokens[2];

            context.write(new IntWritable(Integer.parseInt(userID)), new Text(movieID + ":" + rating));
        }
    }

    public static class  DivideDataReducer extends Reducer<IntWritable, Text, IntWritable, Text> {
        @Override
        public void reduce(IntWritable key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // input: < key=userID, value=< movie1: rating1, movie2: rating2, ... > >
            // output: < key=userID, value="movie1: rating1, movie2: rating2, ..." >

            StringBuilder builder = new StringBuilder();
            for (Text value: values) {
                builder.append(value.toString() + ",");
            }

            String outputValue = builder.deleteCharAt(builder.length() - 1).toString();
            context.write(key, new Text(outputValue));

        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: original user rating folder, e.g., /input
        // args[1]: output of the data divided by userID, e.g., /dataDividedByUser

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Divide Input By User");

        job.setJarByClass(DivideDataByUserID.class);
        job.setMapperClass(DivideDataMapper.class);
        job.setReducerClass(DivideDataReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(Text.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
