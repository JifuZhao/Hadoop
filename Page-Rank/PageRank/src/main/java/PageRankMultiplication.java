/**
 * MapReduce job 1, matrix multiplication
 */
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageRankMultiplication {
    public static class TransitionMapper extends Mapper<Object, Text, Text, Text> {

        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            // input format: fromPage \t toPage1,toPage2,toPage3
            // E.g.: 1  2,3,4,5
            // output format: <1, 2=1/4>
            String line = value.toString().trim();
            String[] tokens = line.split("\t");

            // invalid input
            if (tokens.length < 2) {
                return;
            }

            String fromPage = tokens[0];
            String[] toPages = tokens[1].split(",");

            for (String toPage: toPages) {
                context.write(new Text(fromPage), new Text(toPage + "=" + (double)1 / toPages.length));
            }

        }
    }

    public static class PageRankMapper extends Mapper<Object, Text, Text, Text> {
        @Override
        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {
            // input format: page \t page rank value
            // E.g. 1   0.25
            // output format: <1, 0.25>
            String line = value.toString().trim();
            String[] tokens = line.split("\t");
            context.write(new Text(tokens[0]), new Text(tokens[1]));
        }
    }

    public static class TransitionReducer extends Reducer<Text, Text, Text, Text> {

        float beta;

        @Override
        public void setup(Context context) {
            // get beta value, default 0.15
            Configuration conf = context.getConfiguration();
            beta = conf.getFloat("beta", 0.15f);
        }

        @Override
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            // input key: from page
            // input value: to page1 = prob, to page2 = prob, ......, from page PR value
            // output format: <to page, to page prob * from page PR value>
            List<String> list = new ArrayList<String>();
            double prePR = 0;

            // parse values
            for (Text value: values) {
                if (value.toString().contains("=")) {
                    list.add(value.toString());
                } else {
                    prePR = Double.parseDouble(value.toString());
                }
            }

            // calculate the matrix multiplication
            for (String pageProb: list) {
                String outputKey = pageProb.split("=")[0];
                double prob = Double.parseDouble(pageProb.split("=")[1]);
                String outputValue = String.valueOf((1 - beta) * prob * prePR);
                context.write(new Text(outputKey), new Text(outputValue));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        conf.setFloat("beta", Float.parseFloat(args[3]));
        Job job = Job.getInstance(conf);
        job.setJarByClass(PageRankMultiplication.class);

        // chain two mappers
        ChainMapper.addMapper(job, TransitionMapper.class, Object.class, Text.class, Text.class, Text.class, conf);
        ChainMapper.addMapper(job, PageRankMapper.class, Object.class, Text.class, Text.class, Text.class, conf);

        job.setReducerClass(TransitionReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        // multiple inputs setting
        MultipleInputs.addInputPath(job, new Path(args[0]), TextInputFormat.class, TransitionMapper.class);
        MultipleInputs.addInputPath(job, new Path(args[1]), TextInputFormat.class, PageRankMapper.class);

        FileOutputFormat.setOutputPath(job, new Path(args[2]));
        job.waitForCompletion(true);
    }
}
