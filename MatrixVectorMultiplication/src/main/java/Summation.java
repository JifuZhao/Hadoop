import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

public class Summation {

    public static class SummationMapper extends Mapper<LongWritable, Text, Text, DoubleWritable> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: row \t value
            // output: < key=row, value=value >

            String[] input = value.toString().trim().split("\t");

            String row = input[0];
            double val = Double.parseDouble(input[1]);

            context.write(new Text(row), new DoubleWritable(val));
        }
    }

    public static class SummationReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context) throws IOException, InterruptedException {
            // input: < key=row, value=<val1, val2, ... > >
            // output: < key=row, value=sum(val1, val2, ...) >

            double sum = 0;
            for (DoubleWritable value: values) {
                sum += value.get();
            }

            context.write(key, new DoubleWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: path to the output from MapReduce Job 1, e.g., /cell
        // args[1]: path to the output, e.g., /output

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Cell Summation");

        job.setJarByClass(Summation.class);
        job.setMapperClass(SummationMapper.class);
        job.setReducerClass(SummationReducer.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        TextInputFormat.setInputPaths(job, new Path(args[0]));
        TextOutputFormat.setOutputPath(job, new Path(args[1]));

        job.waitForCompletion(true);

    }
}
