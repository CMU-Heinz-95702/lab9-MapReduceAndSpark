// Derived from Tom White, Hadoop: The Definitive Guide, Second Edition (2011).
package edu.cmu.project5.task3;

import java.io.IOException;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

public class MaxTemperature {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: MaxTemperature <input> <output>");
            System.exit(2);
        }

        JobConf configuration = new JobConf(MaxTemperature.class);
        configuration.setJobName("Max temperature");
        FileInputFormat.addInputPath(configuration, new Path(args[0]));
        FileOutputFormat.setOutputPath(configuration, new Path(args[1]));
        configuration.setMapperClass(MaxTemperatureMapper.class);
        configuration.setReducerClass(MaxTemperatureReducer.class);
        configuration.setOutputKeyClass(Text.class);
        configuration.setOutputValueClass(IntWritable.class);
        JobClient.runJob(configuration);
    }
}
