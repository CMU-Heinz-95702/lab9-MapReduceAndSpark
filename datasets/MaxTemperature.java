package edu.cmu.project5.task3;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MaxTemperature
        extends Configured
        implements Tool {

    @Override
    public int run(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println(
                "Usage: MaxTemperature <input-path> <output-path>");
            return 2;
        }

        Job job =
                Job.getInstance(
                        getConf(),
                        "Max temperature");

        job.setJarByClass(MaxTemperature.class);

        job.setMapperClass(
                MaxTemperatureMapper.class);

        job.setReducerClass(
                MaxTemperatureReducer.class);

        job.setOutputKeyClass(Text.class);

        job.setOutputValueClass(
                IntWritable.class);

        FileInputFormat.addInputPath(
                job,
                new Path(args[0]));

        FileOutputFormat.setOutputPath(
                job,
                new Path(args[1]));

        return job.waitForCompletion(true)
                ? 0
                : 1;
    }

    public static void main(String[] args)
            throws Exception {

        int result =
                ToolRunner.run(
                        new Configuration(),
                        new MaxTemperature(),
                        args);

        System.exit(result);
    }
}
