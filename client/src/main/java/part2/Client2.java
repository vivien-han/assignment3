package part2;

import static part1.SkiersClient.FIRST_PHASE_POST;
import static part1.SkiersClient.FIRST_PHASE_THREADS;
import static part1.SkiersClient.SECOND_PHASE_POST;
import static part1.SkiersClient.SECOND_PHASE_THREADS;
import static part1.SkiersClient.TOTAL_REQUESTS;

import part1.Counter;
import part1.Generator;
import part1.NewLiftRide;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Client2 {

  public static void main(String[] args) throws InterruptedException {

    LinkedBlockingQueue<NewLiftRide> q = new LinkedBlockingQueue<>();
    Generator generator = new Generator(q, TOTAL_REQUESTS);
    Counter successfulCount = new Counter();
    Counter failedCounter = new Counter();
    ExecutorService threadPool = Executors.newFixedThreadPool(FIRST_PHASE_THREADS + SECOND_PHASE_THREADS);
    CountDownLatch endCountDown = new CountDownLatch(TOTAL_REQUESTS);
    DataProcessor result = new DataProcessor(successfulCount, failedCounter);

    result.start();
    Thread producer = new Thread(generator);
    producer.start();

    // first phase: 32 threads with 1000 requests
    SingleThreadPost2 firstPoster = new SingleThreadPost2(q, endCountDown, successfulCount, failedCounter,
        FIRST_PHASE_POST, result);

    Thread firstConsumer = new Thread(firstPoster);
    firstConsumer.setPriority(Thread.MAX_PRIORITY);
    firstConsumer.start();

    for (int i = 0; i < FIRST_PHASE_THREADS; i++) {
      SingleThreadPost2 poster = new SingleThreadPost2(q, endCountDown, successfulCount, failedCounter,
          FIRST_PHASE_POST, result);
      threadPool.execute(poster);
    }
    firstConsumer.join();

    //second phase: 336 threads with 500 requests
    for (int i = 0; i < SECOND_PHASE_THREADS; i++) {
      SingleThreadPost2 poster = new SingleThreadPost2(q, endCountDown, successfulCount, failedCounter,
          SECOND_PHASE_POST, result);
      threadPool.execute(poster);
    }

    endCountDown.await();
    result.end();
    threadPool.shutdown();
    threadPool.awaitTermination(30, TimeUnit.SECONDS);
    result.process();
  }
}
