package edu.cmu.project5.task3;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MaxTemperatureMapper
        extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final int MISSING = 9999;

    private final Text yearText = new Text();

    @Override
    protected void map(LongWritable key,
                       Text value,
                       Context context)
            throws IOException, InterruptedException {

        // Convert Hadoop Text to Java String.
        String line = value.toString();

        // Extract year from positions 15-18.
        String year = line.substring(15, 19);

        // Extract air temperature.
        int airTemperature;

        if (line.charAt(87) == '+') {
            airTemperature =
                    Integer.parseInt(line.substring(88, 92));
        } else {
            airTemperature =
                    Integer.parseInt(line.substring(87, 92));
        }

        // Extract quality code.
        String quality = line.substring(92, 93);

        // Emit (year, temperature) if temperature is valid.
        if (airTemperature != MISSING
                && quality.matches("[01459]")) {

            yearText.set(year);

            context.write(
                    yearText,
                    new IntWritable(airTemperature));
        }
    }
}
