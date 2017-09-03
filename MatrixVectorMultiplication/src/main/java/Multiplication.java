import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

public class Multiplication {

    public static class MatrixMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: row, col, value
            // output: < key=col, value="row=value" >

            String[] input = value.toString().trim().split(",");

            String row = input[0];
            String col = input[1];
            String val = input[2];

            context.write(new Text(col), new Text(row + "=" + val));
        }
    }

    public static class VectorMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: row, value
            // output: < key=row, value=value >

            String[] input = value.toString().trim().split(",");

            String row = input[0];
            String val = input[1];

            context.write(new Text(row), new Text(val));
        }
    }

    public static class MultiplicationReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // input: < key=col, value=< row1=matrixValue1, row2=matrixValue2, ..., vectorValue > >
            // output: < key=row, value=matrixValue * vectorValue >

            List<String> cells = new ArrayList<String>();
            double vectorVal = 0;

            for (Text value: values) {
                if (value.toString().contains("=")) {
                    cells.add(value.toString());
                } else {
                    vectorVal = Double.parseDouble(value.toString());
                }
            }

            for (String element: cells) {
                String row = element.split("=")[0];
                double tmp = Double.parseDouble(element.split("=")[1]);
                String outputVal = String.valueOf(tmp * vectorVal);
                context.write(new Text(row), new Text(outputVal));
            }
        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: path to the matrix input, e.g., /matrix
        // args[1]: path to the vector input, e.g., /vector
        // args[2]: path to the output from MapReduce Job 1, e.g., /cell

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Cell Summation");

        job.setJarByClass(Multiplication.class);
        job.setReducerClass(MultiplicationReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, MatrixMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, VectorMapper.class);

        TextOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);

    }
}
