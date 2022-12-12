package part1;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SkiersClient {

  public static final int FIRST_PHASE_THREADS = 31;
  public static final int FIRST_PHASE_POST = 1000;
  public static final int SECOND_PHASE_THREADS = 336;
  public static final int SECOND_PHASE_POST = 500;
  public static final int TOTAL_REQUESTS = 200000;

  public static void main(String[] args) throws InterruptedException {

    LinkedBlockingQueue<NewLiftRide> q = new LinkedBlockingQueue<>();
    Generator generator = new Generator(q, TOTAL_REQUESTS);
    Counter successfulCount = new Counter();
    Counter failedCount = new Counter();
    ExecutorService threadPool = Executors.newFixedThreadPool(FIRST_PHASE_THREADS + SECOND_PHASE_THREADS);
    CountDownLatch endCountDown = new CountDownLatch(TOTAL_REQUESTS);

    long start = System.currentTimeMillis();
    Thread thread = new Thread(generator);
    thread.start();

    // first phase: 32 threads with 1000 requests
    SingleThreadPost firstPoster = new SingleThreadPost(q, endCountDown, successfulCount, failedCount,
        FIRST_PHASE_POST);
    Thread firstConsumer = new Thread(firstPoster);
    firstConsumer.setPriority(Thread.MAX_PRIORITY);
    firstConsumer.start();
    for (int i = 0; i < FIRST_PHASE_THREADS; i++) {
        SingleThreadPost poster = new SingleThreadPost(q, endCountDown, successfulCount, failedCount, FIRST_PHASE_POST);
        threadPool.execute(poster);
    }
    firstConsumer.join();

    //second phase: 336 threads with 500 requests
    for (int i = 0; i < SECOND_PHASE_THREADS; i++) {
        SingleThreadPost poster = new SingleThreadPost(q, endCountDown, successfulCount, failedCount, SECOND_PHASE_POST);
        threadPool.execute(poster);
    }
    endCountDown.await();
    long end = System.currentTimeMillis();

    threadPool.shutdown();
    threadPool.awaitTermination(30, TimeUnit.SECONDS);
    long duration = end - start;
    System.out.println("number of successful requests sent: " + successfulCount.getValue());
    System.out.println("number of unsuccessful requests: " + failedCount.getValue());
    System.out.println("total runtime used in millisecond: " + duration);
    System.out.println("total throughput in requests per second: "
        + (double) (successfulCount.getValue() + failedCount.getValue()) / duration
        * 1000);
  }
}
