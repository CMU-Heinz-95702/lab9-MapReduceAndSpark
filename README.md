# 95-702 Distributed Systems

## Lab 9: HDFS, MapReduce, and Spark

This is a two-part lab exercise. Part 1 uses Docker to prepare an HDFS environment for Project 5. Part 2 provides the initial setup for running Spark applications in IntelliJ, again for Project 5.

### Part 1 Overview of the Hadoop environment

Part 1 runs in a local Docker container that provides Hadoop 3.5.0 in single-node
pseudo-distributed mode. The container runs the same core Hadoop services found
in a production cluster, including the NameNode (HDFS metadata management),
DataNode (HDFS block storage), ResourceManager (YARN cluster scheduling),
NodeManager (task execution), and JobHistoryServer (completed MapReduce job history).

In a multi-node Hadoop cluster, these services are distributed across many
machines.  In this lab environment, all of these services run as separate
Java processes inside a single Docker container, allowing us to use HDFS,
YARN, and MapReduce locally while working with the same Hadoop architecture
and administrative interfaces used in larger clusters.

### Supported systems

- Windows 10/11 with Docker Desktop
- macOS with Docker Desktop, on either Intel or Apple Silicon
- At least 4 GB of memory available to Docker and approximately 6 GB of free
  disk space

In what follows, we will use the term 'host' to refer to your computer.

### Filesystems

There are three distinct storage locations used in this lab:

### 1. Host Filesystem

- Example: workspace/

    - Located on your computer outside Docker.
    - Appears inside the container as /home/student/Project5.
    - Files remain available even if the container is stopped, removed, or recreated.


- Example: datasets/

    - Located on your computer outside Docker.
    - Appears inside the container as /home/public.
    - Contains instructor-provided datasets.
    - Read-only inside the container.

### 2. Container Linux Filesystem

- Examples: /home/student, /home/public

    - Exists inside the Docker container.
    - Used for temporary files, compilation, testing, and local input/output files.
    - Files remain available if the container is stopped and restarted.
    - Files are lost if the container is removed and recreated (for example, by running docker compose down followed by docker compose up).
    - Note: /home/public is visible inside the container but differs from most other
  directories under /home. Its contents come from the host machine's datasets/
  directory and are available inside the container as read-only files.
  Students can read files in /home/public but cannot modify them.

### 3. HDFS (Hadoop Distributed File System)

- Examples: /user/student/input, /user/student/output

    - Managed by Hadoop.
    - MapReduce jobs read input from and write output to HDFS.
    - HDFS data is stored in a Docker volume separate from the container filesystem.
    - HDFS data persists across docker compose stop, docker compose start, and docker compose down because it is stored in a Docker volume. HDFS data is removed only when the volume itself is deleted, for example by running docker compose down -v.
    - To delete all HDFS data and start over, run: docker compose down -v
    - Note: Students will frequently copy files from `/home/public` or `/home/student` into HDFS using `hdfs dfs` commands before running MapReduce jobs.


### Start the environment

In a terminal window, clone this repository to a directory named lab9-MapReduceAndSpark.

``` console
git clone https://github.com/CMU-Heinz-95702/lab9-MapReduceAndSpark.git

```

Look over this directory and review the contents in subdirectories.


Within the lab9-MapReduceAndSpark directory, start the Hadoop container
in the background and wait until its health check reports healthy.

```console

docker compose up -d --wait

```

### Troubleshooting (probably not needed)

If startup does not become healthy:

```console
docker compose ps
docker compose logs hadoop
```

On first startup Docker must download the course image. A slow or interrupted
download can be retried with:

```console
docker compose pull
docker compose up -d --wait
```

### Enter the Hadoop container

Open an interactive Bash shell inside the running Hadoop container.

```
docker compose exec hadoop bash

```

The shell opens in `/home/student/Project5`. Files in that directory are the
same files as the host filesystem's `workspace/` directory, so they can be edited
with an IDE on the host or with `nano` inside the container.

### Helpful Linux commands

Note that the Tab key is useful for command and file-name completion. Previously entered commands can be recalled using the up-arrow key or by using the history command.

Note too that there is a Unix help sheet on Canvas. It is named
"UNIX Commands Quick List". Note too that we have provided a short
Linux tutorial. See Canvas and look for LinuxTutorial.mp4.


### Useful commands from the host machine:

Show the status of all services defined in compose.yaml:
```console
docker compose ps
```
Display log output from the Hadoop container.
```console
docker compose logs hadoop
```
Show the status of Hadoop services and the lab environment inside the container.
```console
docker compose exec hadoop lab-status
```
Stop the Hadoop container while preserving HDFS data and other persistent storage.
```console
docker compose stop
```

`docker compose stop` and `docker compose down` preserve HDFS data. To erase
HDFS and start over, run `docker compose down -v`. This permanently deletes
the Docker volume; files under `workspace/` on the host are not deleted.

### Web interfaces

The following web interfaces can be accessed from a browser on your local computer:

    HDFS NameNode: http://localhost:9870/
    YARN ResourceManager: http://localhost:8088/
    MapReduce JobHistory: http://localhost:19888/

These services are bound to 127.0.0.1 and are reachable only from the local
computer running Docker.

If one of these ports is already in use, stop the conflicting program before
starting the lab.


### Verify that the instructor-supplied datasets are present inside the container:

```console
ls /home/public

```

The output should include the following datasets. Some sample MapReduce implementations
are also provided: WordCount.java, MaxTemperature.java, MaxTemperatureMapper.java, and MaxTemperatureReducer.java.

```text

words.txt
combinedYears.txt
P1V.txt
CrimeLatLonXYTabs.txt
WordCount.java
MaxTemperature.java
MaxTemperatureMapper.java
MaxTemperatureReducer.java

```

## Part 1: HDFS and MapReduce

For testing, run the bundled Pi example:

```console
hadoop jar /opt/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples.jar pi 10 100000
```

You may also compute pi with 20 mappers:

```console
hadoop jar /opt/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples.jar pi 20 100000

```

### Prepare input file for WordCount

Create input and output directories on the container's Linux filesystem (not HDFS):

```console
mkdir -p /home/student/input /home/student/output
```

Create a small input file on the Linux filesystem and copy it into HDFS:

#### Create a simple file named test
```console
printf 'the quick brown fox jumps over the lazy dog\n' > /home/student/input/test
```
#### Make a directory on HDFS
```console
hdfs dfs -mkdir -p /user/student/input
```
#### Copy the input file to HDFS (-f forces the copy if the file already exists )
```console
hdfs dfs -copyFromLocal -f /home/student/input/test /user/student/input/test
```
#### Use the 'ls' command to see if it is there
```console
hdfs dfs -ls /user/student/input
```
#### View the file on HDFS with 'cat'
```console
hdfs dfs -cat /user/student/input/test
```

### Run the WordCount MapReduce example:

#### Remove the previous output directory from HDFS.
```
hdfs dfs -rm -r -f /user/student/output
```

#### Run the WordCount job.
```
hadoop jar /opt/hadoop/share/hadoop/mapreduce/hadoop-mapreduce-examples.jar \
  wordcount /user/student/input /user/student/output
```

#### Display the output stored in HDFS.
```
hdfs dfs -cat /user/student/output/part-r-00000
```

#### Copy the output from HDFS to the container's Linux filesystem.
```
hdfs dfs -getmerge /user/student/output /home/student/output/mergedfile.txt
```

#### Display the merged output file.
```
cat /home/student/output/mergedfile.txt
```

The WordCount output is stored in HDFS. The hdfs dfs -getmerge command copies
all output partitions into a single file on the container's Linux filesystem for
easier viewing.

Important: If you run the WordCount job multiple times, first remove the existing
output directory. Hadoop will fail with an error if the output directory already
exists.

### Inspect running and completed jobs (optional):

Lists MapReduce jobs that are running.

```console
mapred job -list
```

List YARN applications in all states.

```console   
yarn application -list -appStates ALL  
```

Terminates a running MapReduce job.

```console
mapred job -kill JOB_ID  
```

Use the ResourceManager and JobHistory web interfaces to inspect completed jobs and task logs.
For small debugging tasks, output written with System.out.println() in Java typically
appears in the YARN task logs.

Check the Job History web site http://localhost:19888. Did your jobs run to completion?

### :checkered_flag: This is the .25 point checkpoint. Using the web site, show your TA that your jobs ran to completion.

### Ending work

Press Ctrl-D (or type exit) to leave the interactive shell. This does not stop the Hadoop container; it only closes your interactive session.

To reconnect:

```console
docker compose exec hadoop bash
```

To stop the container:
```console
docker compose stop
```

To stop and remove the container:
```
 docker compose down
```
Note: Do not use docker compose down -v as a general troubleshooting step. The -v option permanently deletes all data stored in HDFS. Use this command only when you intentionally want to start with a fresh Hadoop environment.

### Part 2. Apache Spark Using IntelliJ

For the Spark part of this lab, we are using JDK 8 or 1.8. More recent
versions of Java do not work as expected.

Be sure to download and install JDK 8 for the remainder of this lab.

0. Run IntelliJ and select File New project
1. Name the project Spark-Example-Lab9
2. Choose Java as the language
3. Choose Maven for the Build System
4. Use JDK 8 or JDK 1.8 (Same thing!)
5. Select Create and do the following to install the Spark library.
   a. Right click the project node and select Open Module Settings
   b. Choose Libraries/+/From Maven
   c. Enter org.apache.spark:spark-core_2.10:1.0.0
      Leave Transitive dependancies checked.
   d. Select OK, OK, Apply, OK
6. Drill down into src and right click the Java node and select new Java class
7. Create a Java class named WordCounter.java with the following content:


```java

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;

public class WordCounter {

    private static void wordCount(String fileName) {

        SparkConf sparkConf = new SparkConf().setMaster("local").setAppName("JD Word Counter");

        JavaSparkContext sparkContext = new JavaSparkContext(sparkConf);

        JavaRDD<String> inputFile = sparkContext.textFile(fileName);

        JavaRDD<String> wordsFromFile = inputFile.flatMap(content -> Arrays.asList(content.split(" ")));

        JavaPairRDD countData = wordsFromFile.mapToPair(t -> new Tuple2(t, 1)).reduceByKey((x, y) -> (int) x + (int) y);

        countData.saveAsTextFile("CountData");
    }

    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("No files provided.");
            System.exit(0);
        }

        wordCount(args[0]);
    }
}
```

Note: If you get an error on the import of java.utils.Arrays, select File/Invalidate Caches, select all of the options, and then select "Invalidate and Restart".

8. We need a file to process. Right click the project node and select "New File".
Give the file the name hadoop-lab.txt.

9. Use the file that you are currently reading and copy it to hadoop-lab.txt.

10. We need to specify the file name as a command line parameter.
Select the project node. From the Run menu, select Edit Configurations.

11. You may need to "Add new run configuration...".
    Choose "Application".
    The main class is WordCounter.
    Set the command line argument (CLI arguments to your application) to the name of this file: hadoop-lab.txt
    Check that the working directory points to the directory holding hadoop-lab.txt.
    The configuration's Build and Run should show java 8 SDK and the working
    directory will provide a path to hadoop-lab.txt.

12. Compile and run the Java application.

13. The output file will be in a directory named CountData in your working directory.

14. If you get a "file already exists exception", be sure to delete the output directory
named "CountData" in your working directory. Spark does this so that a large output file is
not accidentally erased.

### :checkered_flag: This is the .75 point checkpoint. Show your TA that your Spark job on IntelliJ ran to completion.
