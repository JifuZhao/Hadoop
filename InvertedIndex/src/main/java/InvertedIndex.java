/**
 * Simple implementation of word count program through Hadoop MapReduce
 */

import java.util.*;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;

public class InvertedIndex {
    public static class InvertedIndexMapper extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            // input: line of words
            // output: < key=word, value=file name >

            String fileName = ((FileSplit) context.getInputSplit()).getPath().getName();
            String[] tokens = value.toString().trim().split("\\s+");

            for (String word: tokens) {
                context.write(new Text(word.toLowerCase()), new Text(fileName));
            }
        }
    }

    public static class InvertedIndexReducer extends Reducer<Text, Text, Text, Text> {
        @Override
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            // input: < key=word, value=<fileName1, fileName2, ..., fileNameN >>
            // output: < key=word, value=sorted list of file name according frequency >

            // calculate the frequency of each word in each file
            Map<String, Integer> hashMap = new HashMap<String, Integer>();

            for (Text value: values) {
                String fileName = value.toString().trim();

                if (hashMap.containsKey(fileName)) {
                    hashMap.put(fileName, hashMap.get(fileName) + 1);
                } else {
                    hashMap.put(fileName, 1);
                }
            }

            // sort file names according frequency
            TreeMap<Integer, List<String>> treeMap = new TreeMap<Integer, List<String>>(Collections.reverseOrder());
            for (String fileName: hashMap.keySet()) {
                int count = hashMap.get(fileName);
                if (treeMap.containsKey(count)) {
                    treeMap.get(count).add(fileName);
                } else {
                    List<String> list = new ArrayList<String>();
                    list.add(fileName);
                    treeMap.put(count, list);
                }
            }

            // build the final output
            StringBuilder sb = new StringBuilder();
            for (int count: treeMap.keySet()) {
                List<String> words = treeMap.get(count);
                for (String fileName: words) {
                    sb.append("->");
                    sb.append(fileName);
                }
            }

            // write to the output
            context.write(key, new Text(sb.toString().replaceFirst("->", "")));
        }

    }
}
