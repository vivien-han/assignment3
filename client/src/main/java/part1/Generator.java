package part1;

import io.swagger.client.model.LiftRide;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Generator implements Runnable {

  private LinkedBlockingQueue<NewLiftRide> q;
  private int totalRequest;
  public static final String SEASON_ID = "2022";
  public static final String DAY_ID = "1";

  public Generator(LinkedBlockingQueue<NewLiftRide> q, int totalRequest) {
    this.q = q;
    this.totalRequest = totalRequest;
  }

  @Override
  public void run() {
    for (int i = 0; i < totalRequest; i++) {
      Integer skierID = ThreadLocalRandom.current().nextInt(100000) + 1;
      Integer resortID = ThreadLocalRandom.current().nextInt(10) + 1;
      Integer liftID = ThreadLocalRandom.current().nextInt(40) + 1;
      Integer time = ThreadLocalRandom.current().nextInt(360) + 1;
      LiftRide body = new LiftRide();
      body.setTime(time);
      body.setLiftID(liftID);
      NewLiftRide newLiftRide = new NewLiftRide(body, resortID, SEASON_ID, DAY_ID, skierID);
      this.q.offer(newLiftRide);
    }

  }
}
