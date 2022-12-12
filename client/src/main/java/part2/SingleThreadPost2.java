package part2;

import static part1.SingleThreadPost.BASE_PATH;
import static part1.SingleThreadPost.SUCCESS_CODE;
import static part1.SingleThreadPost.TOTAL_RETRY;

import part1.Counter;
import part1.NewLiftRide;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class SingleThreadPost2 implements Runnable{
  private DataProcessor result;
  private LinkedBlockingQueue<NewLiftRide> q;
  private CountDownLatch endCountDown;
  private Counter successfulCount;
  private Counter failedCount;
  private int totalPost;


  public SingleThreadPost2(LinkedBlockingQueue<NewLiftRide> q, CountDownLatch endCountDown,
      Counter successfulCount, Counter failedCount, int numOfPost, DataProcessor result) {
    this.q = q;
    this.endCountDown = endCountDown;
    this.successfulCount = successfulCount;
    this.failedCount = failedCount;
    this.totalPost = numOfPost;
    this.result = result;
  }


  @Override
  public void run() {

    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    SkiersApi skiersApi = new SkiersApi(apiClient);
    List<Long> durations = new ArrayList<>();
    StringBuilder line = new StringBuilder();

    for (int i = 0; i < totalPost; i++) {
      NewLiftRide newLiftRide = q.poll();
      long start = System.currentTimeMillis();
      ApiResponse<Void> res = null;
      try {
          res = skiersApi.writeNewLiftRideWithHttpInfo(newLiftRide.getBody(),
            newLiftRide.getResortID(), newLiftRide.getSeasonID()
            , newLiftRide.getDayID(), newLiftRide.getSkierID());

        int retry = 0;

        while (res.getStatusCode() != SUCCESS_CODE && retry < TOTAL_RETRY) {
          res = skiersApi.writeNewLiftRideWithHttpInfo(newLiftRide.getBody(),
              newLiftRide.getResortID(), newLiftRide.getSeasonID()
              , newLiftRide.getDayID(), newLiftRide.getSkierID());
          retry++;
        }
        if (res.getStatusCode() == SUCCESS_CODE) {
          this.endCountDown.countDown();
          this.successfulCount.increase();
        } else {
          this.failedCount.increase();
        }

      } catch (ApiException e) {
        throw new RuntimeException(e);
      }

      long end = System.currentTimeMillis();
      long duration = end - start;
      durations.add(duration);

      line.append(start).append(",POST,").append(duration).append(",").append(res.getStatusCode())
          .append("\n");
    }

    result.addLine(line.toString());
    result.addDurationOfPoster(durations);
  }
}
