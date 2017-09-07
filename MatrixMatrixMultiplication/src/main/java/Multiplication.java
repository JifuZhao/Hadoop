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

    public static class Matrix1Mapper extends Mapper<LongWritable, Text, Text, Text> {
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

    public static class Matrix2Mapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: row, value
            // output: < key=row, value="col:value" >

            String[] input = value.toString().trim().split(",");

            String row = input[0];
            String col = input[1];
            String val = input[2];

            context.write(new Text(row), new Text(col + ":" + val));
        }
    }

    public static class MultiplicationReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // input: < key=col, value=< row1=matrix1_value1, row2=matrix1_value2, ...,
            //                           col1:matrix2_value1, col2:matrix2_value2, ... > >
            // output: < key="row,col", value=matrix1_value * matrix2_value >

            List<String> matrix1Cells = new ArrayList<String>();
            List<String> matrix2Cells = new ArrayList<String>();

            for (Text value: values) {
                if (value.toString().contains("=")) {
                    matrix1Cells.add(value.toString());
                } else {
                    matrix2Cells.add(value.toString());
                }
            }

            for (String rowCell: matrix1Cells) {
                String row = rowCell.split("=")[0];
                double rowValue = Double.parseDouble(rowCell.split("=")[1]);

                for (String colCell: matrix2Cells) {
                    String col = colCell.split(":")[0];
                    double colValue = Double.parseDouble(colCell.split(":")[1]);

                    // calculate the element-wise multiplication and write to context
                    String outputKey = row + "," + col;
                    String outputVal = String.valueOf(rowValue * colValue);
                    context.write(new Text(outputKey), new Text(outputVal));
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {

        // args[0]: path to the first matrix input, e.g., /matrix1
        // args[1]: path to the second matrix input, e.g., /matrix2
        // args[2]: path to the output from MapReduce Job 1, e.g., /cell

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Cell Summation");

        job.setJarByClass(Multiplication.class);
        job.setReducerClass(MultiplicationReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, Matrix1Mapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, Matrix2Mapper.class);

        TextOutputFormat.setOutputPath(job, new Path(args[2]));

        job.waitForCompletion(true);

    }
}
