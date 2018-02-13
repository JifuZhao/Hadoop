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

public class MatrixSum {
    public static class SumMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: userID: movieID \t ratio * rating
            // output: < key="userID: movieID", value=ratio * rating >

            String[] tokens = value.toString().trim().split("\t");
            context.write(new Text(tokens[0]), new DoubleWritable(Double.parseDouble(tokens[1])));
        }
    }

    public static class  SumReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {
            // input: < key="userID: movieID", value=< subRating1, subRating2, ... > >
            // output: < key="userID: movieID", value=rating prediction >

            double sum = 0;
            for (DoubleWritable value: values) {
                sum += value.get();
            }

            context.write(key, new DoubleWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: matrix multiplication output folder, e.g., /Multiplication
        // args[1]: matrix multiplication sum output folder, e.g., /Sum

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Summation");

        job.setJarByClass(MatrixSum.class);
        job.setMapperClass(SumMapper.class);
        job.setReducerClass(SumReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);
    }
}
