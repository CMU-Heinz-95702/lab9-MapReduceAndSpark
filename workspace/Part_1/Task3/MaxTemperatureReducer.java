// Derived from Tom White, Hadoop: The Definitive Guide, Second Edition (2011).
package edu.cmu.project5.task3;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * MaxTemperatureReducer
 *
 * Receives:
 *
 *     (year, [temperature, temperature, temperature, ...])
 *
 * for each year produced by the mapper.
 *
 * The reducer finds the maximum temperature associated
 * with each year and emits:
 *
 *     (year, maxTemperature)
 */
public class MaxTemperatureReducer
        extends Reducer<Text, IntWritable, Text, IntWritable> {

    // Reusable output object.
    private final IntWritable result = new IntWritable();

    /**
     * Called once for each unique year.
     *
     * key     = year
     * values  = all temperatures associated with that year
     * context = used to emit reducer output
     */
    @Override
    protected void reduce(Text key,
                          Iterable<IntWritable> values,
                          Context context)
            throws IOException, InterruptedException {

        // Initialize to the lowest possible integer.
        int maxTemperature = Integer.MIN_VALUE;

        // Examine all temperatures associated with this year.
        for (IntWritable value : values) {

            // Keep the largest temperature seen so far.
            maxTemperature = Math.max(
                    maxTemperature,
                    value.get());
        }

        // Store the maximum temperature in the writable object.
        result.set(maxTemperature);

        // Emit (year, maximum temperature).
        context.write(key, result);
    }
}
