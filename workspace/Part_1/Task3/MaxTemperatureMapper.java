package edu.cmu.project5.task3;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * MaxTemperatureMapper
 *
 * Reads weather records and emits:
 *
 *     (year, temperature)
 *
 * for each valid temperature observation.
 *
 * The year is stored in positions 15-18 of each record.
 * The temperature is stored beginning at position 87.
 */
public class MaxTemperatureMapper
        extends Mapper<LongWritable, Text, Text, IntWritable> {

    // Value indicating that the temperature is missing.
    private static final int MISSING = 9999;

    // Reusable output key object.
    private final Text yearText = new Text();

    // Reusable output value object.
    private final IntWritable temperature = new IntWritable();

    /**
     * Called once for each input record.
     *
     * key     = byte offset of the record within the file
     * value   = a single weather record
     * context = used to emit mapper output
     */
    @Override
    protected void map(LongWritable key,
                       Text value,
                       Context context)
            throws IOException, InterruptedException {

        // Convert the Hadoop Text object into a Java String.
        String line = value.toString();

        // Ignore malformed records.
        if (line.length() < 93) {
            return;
        }

        // Extract the year from positions 15-18.
        String year = line.substring(15, 19);

        // Extract the air temperature.
        int airTemperature;

        // Handle temperatures beginning with '+'.
        if (line.charAt(87) == '+') {
            airTemperature = Integer.parseInt(
                    line.substring(88, 92));
        } else {
            airTemperature = Integer.parseInt(
                    line.substring(87, 92));
        }

        // Extract the quality code.
        String quality = line.substring(92, 93);

        // Emit only valid temperature readings.
        if (airTemperature != MISSING
                && quality.matches("[01459]")) {

            // Set the output key to the year.
            yearText.set(year);

            // Set the output value to the temperature.
            temperature.set(airTemperature);

            // Emit (year, temperature).
            context.write(yearText, temperature);
        }
    }
}
