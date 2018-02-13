/**
 * MapReduce job 2, sum the result from MapReduce job 1
 */
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.text.DecimalFormat;

public class PageRankSummation {
    public static class SumMapper extends Mapper<Object, Text, Text, DoubleWritable> {
        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            // input key: to page
            // input value: to page matrix multiplication value
            // output format: to page \t to page value
            String[] line = value.toString().split("\t");
            double outputValue = Double.parseDouble(line[1]);
            context.write(new Text(line[0]), new DoubleWritable(outputValue));
        }
    }

    public static class BetaMapper extends Mapper<Object, Text, Text, DoubleWritable> {

        float beta;

        @Override
        public void setup(Context context) {
            // get beta value, default 0.15
            Configuration conf = context.getConfiguration();
            beta = conf.getFloat("beta", 0.15f);
        }

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            // input format: page \t page rank value
            // E.g. 1   0.25
            // output format: <1, 0.25>
            String line = value.toString().trim();
            String[] tokens = line.split("\t");
            double prValue = Double.parseDouble(tokens[1]) * beta;
            context.write(new Text(tokens[0]), new DoubleWritable(prValue));
        }
    }

    public static class SumReducer extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        @Override
        public void reduce(Text key, Iterable<DoubleWritable> values, Context context)
                throws IOException, InterruptedException {
            // input key = to page
            // input value = list of values
            double sum = 0;
            for (DoubleWritable value: values) {
                sum += value.get();
            }

            DecimalFormat dataFormat = new DecimalFormat("#.00000");
            sum = Double.valueOf(dataFormat.format(sum));
            context.write(key, new DoubleWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setFloat("beta", Float.parseFloat(args[3]));
        Job job = Job.getInstance(conf);
        job.setJarByClass(PageRankSummation.class);

        // chain two mappers
        ChainMapper.addMapper(job, SumMapper.class, Object.class, Text.class, Text.class, DoubleWritable.class, conf);
        ChainMapper.addMapper(job, BetaMapper.class, Text.class, DoubleWritable.class, Text.class, DoubleWritable.class, conf);

        job.setReducerClass(SumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        // multiple inputs setting
        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, SumMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, BetaMapper.class);

        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.waitForCompletion(true);
    }
}
