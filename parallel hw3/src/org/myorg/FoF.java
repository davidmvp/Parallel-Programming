package org.myorg;
import java.io.IOException;

import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * 
 * @author Kyle Mercer
 * 
 *         April 6th, 2013 Parallel Programming - 600.320
 * 
 *         This file contains Map/Reduce classes and functions for finding
 *         friends of friends (FoF) relationships.
 */

public class FoF {

	/** Static Map class wrapper for map function. */
	public static class Map extends Mapper<Object, Text, Text, NullWritable> {

		/** This allows us to attach a null value since our entire key is the triplet. */
		private final static NullWritable myNullWritable = NullWritable.get();

		/**
		 * Map function.
		 * @param key
		 * 				input key, not sure what this is in terms of the input.
		 * @param value
		 * 				line value input
		 * @param context
		 * 				the context to output to mapper
		 * @throws IOException
		 * @throws InterruptedException
		 */
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			//Get a line
			String line = value.toString();
			System.out.println("LINE:" + line);
			StringTokenizer tokenizer = new StringTokenizer(line);

			//Careful: See countTokens() doc..
			int listSize = tokenizer.countTokens();
			int rootFriend = Integer.parseInt(tokenizer.nextToken());

			int[] friends = new int[listSize];

			//Set friends[0] because nextToken already passed it..
			friends[0] = rootFriend;
			for (int i = 1; i < listSize; i++) {
				friends[i] = Integer.parseInt(tokenizer.nextToken());
			}

			//Do the same as in the streaming version
			for (int i = 1; i < listSize; i++) {
				for (int j = i+1; j < listSize; j++) {
					Text keyOut;

					if (rootFriend < friends[j]) {
						keyOut = new Text(friends[i] + " " + rootFriend + " " + friends[j]);
					} else {
						keyOut = new Text(friends[i] + " " + friends[j] + " " + rootFriend);
					}
					//write out the key/value (really just the key)
					context.write(keyOut, myNullWritable);
					System.out.println(keyOut);

					if (rootFriend < friends[i]) {
						keyOut = new Text(friends[j] + " " + rootFriend + " " + friends[i]);
					} else {
						keyOut = new Text(friends[j] + " " + friends[i] + " " + rootFriend);
					}
					//write out the key/value (really just the key)
					context.write(keyOut, myNullWritable);
					System.out.println(keyOut);
				}
			}
		}
	}

	/** Static Reduce class wrapper for reduce function. */
	public static class Reduce extends Reducer<Text, NullWritable, Text, NullWritable> {

		/** Dummy results from the mapper. */
		private NullWritable result = NullWritable.get();

		/**
		 * Reduce function.
		 * @param key
		 * 			The key (triplet) of the FoF
		 * @param values
		 * 				A list of null values (1 per key)
		 * @param context
		 * 				The context to write out to.
		 */
		public void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
			int total = 0;

			//Count how many of each key there are.
			for (NullWritable nw : values) {
				total++;
			}

			// If two or more of each key, we know we have mutual friends.
			if (total >= 2) {
				context.write(key, result);
				System.out.println(key);
			}
		}
	}

	/**
	 * Program entry point.
	 * 
	 * @param args
	 *            Command line args
	 * @throws Exception
	 * 					Handles a multitude of exceptions 
	 */
	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();

		Job job = new Job(conf, "FoF");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		job.setJarByClass(FoF.class);

		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);
	}
}