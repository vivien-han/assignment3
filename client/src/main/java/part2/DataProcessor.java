package part2;

import part1.Counter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataProcessor {

  private Long startTime;
  private Long endTime;
  private Counter successfulCount;
  private Counter failedCount;

  private ConcurrentLinkedQueue<List<Long>> durations;
  private ConcurrentLinkedQueue<String> lines;

  public DataProcessor(Counter successfulCount, Counter failedCount) {
    this.successfulCount = successfulCount;
    this.failedCount = failedCount;
    this.durations = new ConcurrentLinkedQueue<>();
    this.lines = new ConcurrentLinkedQueue<>();

  }

  public void start() {
    this.startTime = System.currentTimeMillis();
  }

  public void end() {
    this.endTime = System.currentTimeMillis();
  }

  public void addDurationOfPoster(List<Long> durationList) {
    this.durations.add(durationList);
  }

  public void addLine(String line) {
    this.lines.add(line);
  }

  public void process() {

    //Write out a record containing {start time, request type (ie POST), latency, response code} to csv file
    File file = new File("record.csv");
    try {
      FileOutputStream outputStream = new FileOutputStream(file);
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
      for (String line : lines) {
        writer.write(line);
        writer.newLine();
      }
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    List<Long> responseTimes = new ArrayList<>();
    for (List<Long> durationList : durations) {
      responseTimes.addAll(durationList);
    }

    int n = responseTimes.size();
    Collections.sort(responseTimes);

    double median = responseTimes.get(n / 2);
    long p99 = responseTimes.get((n - 1) * 99 / 100);
    long min = responseTimes.get(0);
    long max = responseTimes.get(n - 1);
    double sum = 0;
    for (long x : responseTimes) {
      sum += x;
    }
    double mean = sum / n;

    int numSuccess = successfulCount.getValue();
    int numFail = failedCount.getValue();
    long totalRuntime = this.endTime - this.startTime;
    double throughput = ((double) numSuccess + numFail) / totalRuntime * 1000;

    System.out.println("mean response time in millisecond: " + mean);
    System.out.println("median response time in millisecond: " + median);
    System.out.println("p99 response time in millisecond: " + p99);
    System.out.println("min response time in millisecond: " + min);
    System.out.println("max response time in millisecond: " + max);
    System.out.println("number of successful requests sent: " + numSuccess);
    System.out.println("number of unsuccessful requests: " + numFail);
    System.out.println("total runtime used in millisecond: " + totalRuntime);
    System.out.println("total throughput in requests per second: " + throughput);
  }
}
