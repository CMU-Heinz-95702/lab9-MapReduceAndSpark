package edu.cmu.project5.task0;

// Java classes used for input/output exceptions and tokenization.
import java.io.IOException;
import java.util.StringTokenizer;

// Hadoop configuration classes.
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;

// Hadoop filesystem path class.
import org.apache.hadoop.fs.Path;

// Hadoop writable data types used during MapReduce processing.
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;

// Hadoop MapReduce classes.
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

// Hadoop input and output format classes.
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

// Hadoop utility classes used to launch the job.
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * WordCount.java
 *
 * Author: Your Name Here
 *
 * Purpose:
 * Demonstrates a complete Hadoop MapReduce application.
 *
 * The mapper reads text and emits:
 *
 *     (word, 1)
 *
 * for each word in the input.
 *
 * The reducer receives:
 *
 *     (word, [1,1,1,...])
 *
 * and adds the values to produce:
 *
 *     (word, count)
 *
 * Input:
 *     Text file stored on HDFS.
 *
 * Output:
 *     Word frequencies written to HDFS.
 */
public class WordCount extends Configured implements Tool {

    /**
     * Mapper
     *
     * Receives one line of text at a time.
     * Emits (word,1) for every word found in the line.
     */
    public static class WordMapper
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        // Reusable constant that represents the value 1.
        private static final IntWritable ONE = new IntWritable(1);

        // Reusable Text object used as the output key.
        private final Text word = new Text();

        /**
         * Called once for each line of input.
         *
         * key     = byte offset of line within file
         * value   = contents of the line
         * context = mechanism for writing mapper output
         */
        @Override
        protected void map(LongWritable key,
                           Text value,
                           Context context)
                throws IOException, InterruptedException {

            // Convert the Hadoop Text object into a Java String.
            String line = value.toString();

            // Split the line into tokens separated by whitespace.
            StringTokenizer tokenizer = new StringTokenizer(line);

            // Process each word in the line.
            while (tokenizer.hasMoreTokens()) {

                // Store the current word in the Text object.
                word.set(tokenizer.nextToken());

                // Emit (word,1).
                context.write(word, ONE);
            }
        }
    }

    /**
     * Reducer
     *
     * Receives:
     *
     *     (word, [1,1,1,1,...])
     *
     * Adds the values together and emits:
     *
     *     (word,totalCount)
     */
    public static class WordReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        /**
         * Called once for each unique word.
         */
        @Override
        protected void reduce(Text key,
                              Iterable<IntWritable> values,
                              Context context)
                throws IOException, InterruptedException {

            // Running total for the current word.
            int sum = 0;

            // Add all counts associated with this word.
            for (IntWritable value : values) {
                sum += value.get();
            }

            // Emit the final count for the word.
            context.write(key, new IntWritable(sum));
        }
    }

    /**
     * Configures and launches the MapReduce job.
     *
     * args[0] = HDFS input path
     * args[1] = HDFS output path
     */
    @Override
    public int run(String[] args) throws Exception {

        // Verify that exactly two command-line arguments were supplied.
        if (args.length != 2) {
            System.err.println(
                    "Usage: WordCount <input-path> <output-path>");
            return 2;
        }

        // Create a new Hadoop job named "wordcount".
        Job job = Job.getInstance(getConf(), "wordcount");

        // Identify the class containing the main method.
        job.setJarByClass(WordCount.class);

        // Specify the mapper class.
        job.setMapperClass(WordMapper.class);

        // Specify the reducer class.
        job.setReducerClass(WordReducer.class);

        // Specify the mapper/reducer output key type.
        job.setOutputKeyClass(Text.class);

        // Specify the mapper/reducer output value type.
        job.setOutputValueClass(IntWritable.class);

        // Set the HDFS input directory or file.
        FileInputFormat.addInputPath(job, new Path(args[0]));

        // Set the HDFS output directory.
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        // Submit the job and wait for completion.
        return job.waitForCompletion(true) ? 0 : 1;
    }

    /**
     * Program entry point.
     */
    public static void main(String[] args) throws Exception {

        // Launch the Tool and return its exit code.
        int result =
                ToolRunner.run(
                        new Configuration(),
                        new WordCount(),
                        args);

        // Terminate the JVM with the result code.
        System.exit(result);
    }
}
