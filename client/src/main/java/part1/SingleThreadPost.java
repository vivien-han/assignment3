package part1;


import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class SingleThreadPost implements Runnable {

  private LinkedBlockingQueue<NewLiftRide> q;
  private CountDownLatch endCountDown;
  private Counter successfulCount;
  private Counter failedCount;
  private int totalPost;

//  public static final String BASE_PATH = "http://localhost:8080/lab2_war_exploded";
  public static String BASE_PATH = "http://18.237.15.212:8080/lab2_war_exploded";
  public static final int TOTAL_RETRY = 5;
  public static final int SUCCESS_CODE = 201;

  public SingleThreadPost(LinkedBlockingQueue<NewLiftRide> q,
      CountDownLatch endCountDown, Counter successfulCount, Counter failedCount, int numOfPost) {
    this.q = q;
    this.endCountDown = endCountDown;
    this.successfulCount = successfulCount;
    this.failedCount = failedCount;
    this.totalPost = numOfPost;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(BASE_PATH);
    SkiersApi skiersApi = new SkiersApi(apiClient);

    for (int i = 0; i < this.totalPost; i++) {
      NewLiftRide newLiftRide = q.poll();
      try {
        ApiResponse<Void> res = skiersApi.writeNewLiftRideWithHttpInfo(newLiftRide.getBody(),
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
    }
  }

}
