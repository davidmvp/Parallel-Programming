package org.myorg;

import java.io.IOException;
import java.util.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

	public class ThreesCompany {
	
    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
      private final static IntWritable one = new IntWritable(1);
      private Text word = new Text();
	
	      public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
	        String line = value.toString();
	        StringTokenizer tokenizer = new StringTokenizer(line);
	        int size = tokenizer.countTokens();
	        int root = Integer.parseInt(tokenizer.nextToken());
	        int[] friends = new int[size];
	        friends[0] = root;
	        for (int i = 1; i < size; i++) {
	        	friends[i] = Integer.parseInt(tokenizer.nextToken());
	        }
	        for (int i = 1; i < size; i++) {
	        	for (int j = i+1; j < size; j++) {
	        		int[] a = new int[3];
	        		a[0] = root;
	        		a[1] = friends[i];
	        		a[2] = friends[j];
	        		
	        		Arrays.sort(a);
	        		String d = a[0] + " " + a[1] + " " + a[2];
	        		output.collect(d,one);
	        	}
	        }
      }
    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
      public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
        int sum = 0;
        while (values.hasNext()) {
          sum += values.next().get();
        }
        if (sum == 3) {
        	StringTokenizer tokenizer = new StringTokenizer(key);
	        int size = tokenizer.countTokens();
	        int[] friends = new int[size];
	        for (int i = 0; i < size; i++) {
	        	friends[i] = Integer.parseInt(tokenizer.nextToken());
	        }
	        String s = friends[0] + " " + friends[1] + " " + friends[2];
            output.collect(s, new IntWritable(sum));
            s = friends[1] + " " + friends[0] + " " + friends[2];
            output.collect(s, new IntWritable(sum));
            s = friends[2] + " " + friends[0] + " " + friends[1];
            output.collect(s, new IntWritable(sum));
        }
      }
      }

	    public static void main(String[] args) throws Exception {
	      JobConf conf = new JobConf(ThreesCompany.class);
	      conf.setJobName("wordcount");
	
	      conf.setOutputKeyClass(Text.class);
	      conf.setOutputValueClass(IntWritable.class);
	
	      conf.setMapperClass(Map.class);
	      conf.setCombinerClass(Reduce.class);
	      conf.setReducerClass(Reduce.class);
	
	      conf.setInputFormat(TextInputFormat.class);
	      conf.setOutputFormat(TextOutputFormat.class);
	
	      FileInputFormat.setInputPaths(conf, new Path(args[0]));
	      FileOutputFormat.setOutputPath(conf, new Path(args[1]));
	
	      JobClient.runJob(conf);
	    }
	}