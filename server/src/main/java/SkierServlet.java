import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "SkierServlet", value = "/SkierServlet")
public class SkierServlet extends HttpServlet {

  private static final String RMQ_IP = "34.121.727.153";
  private static final String SPACE = " ";
  private static final String EXCHANGE_NAME = "exchanger";
  private static final int CHANNEL = 200;
  private Gson gson = new Gson();
  private RMQChannelPool channelPool;
  private static Connection connection;

  @Override
  public void init() {
    ConnectionFactory factory = new ConnectionFactory();
    //factory.setHost(RMQ_IP);
    factory.setHost("localhost");
    factory.setPort(5672);
    factory.setUsername("admin");
    factory.setPassword("password");
    try {
      connection = factory.newConnection();
    } catch (IOException | TimeoutException e) {
      System.out.println("connection error");
      e.printStackTrace();
    }
    ChannelFactory channelFactory = new ChannelFactory(connection);
    channelPool = new RMQChannelPool(CHANNEL, channelFactory);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws IOException {

    ResponseBody responseBody;
    String urlPath = req.getPathInfo();
    PrintWriter out = res.getWriter();
    StringBuilder urlInfo = new StringBuilder();

    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      responseBody = new ResponseBody("missing paramterers");
      sendResponse(res, out, responseBody);
    }

    String[] urlParts = urlPath.split("/");

    int urlCheckResult = isUrlValid(urlParts, urlInfo);
    if (urlCheckResult == 2) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      responseBody = new ResponseBody("missing paramterers");
      sendResponse(res, out, responseBody);

    } else if (urlCheckResult == 1) {
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      responseBody = new ResponseBody("invalid url");
      sendResponse(res, out, responseBody);
    } else {

      try {
        StringBuilder sb = new StringBuilder();
        String s;
        while ((s = req.getReader().readLine()) != null) {
          sb.append(s);
        }
        SeasonInfo requestBody = (SeasonInfo) gson.fromJson(sb.toString(), SeasonInfo.class);

        if (requestBody.getLiftID() < 1 || requestBody.getLiftID() > 40
            || requestBody.getTime() < 1 || requestBody.getTime() > 360) {
          res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
          responseBody = new ResponseBody("invalid request body data");
          sendResponse(res, out, responseBody);
        } else {
          sb.delete(0, sb.length());
          sb.append(requestBody);
          sb.append(urlInfo);
          String message = sb.toString();
          System.out.println(message);
          Channel channel = null;
          try {
            channel = channelPool.borrowObject();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
          } catch (Exception e) {
            throw new RuntimeException("fail to borrow channel from pool" + e);
          } finally {
            try {
              if (null != channel) {
                channelPool.returnObject(channel);
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          res.setStatus(HttpServletResponse.SC_CREATED);
          responseBody = new ResponseBody("Successful posted a lift ride for the skier");
          sendResponse(res, out, responseBody);
        }

      } catch (Exception ex) {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        responseBody = new ResponseBody("missing parameters");
        sendResponse(res, out, responseBody);
      }

    }
  }


  private void sendResponse(HttpServletResponse res, PrintWriter out, ResponseBody responseBody) {
    String responseBodyString = this.gson.toJson(responseBody);
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    out.print(responseBodyString);
    out.flush();
  }

  private int isUrlValid(String[] urlPath, StringBuilder urlInfo) {
    /*
    return 0: 201
    return 1: 400	Invalid inputs
    return 2: 404 Data not found
     urlPath  = "/1/seasons/2019/day/1/skier/123"
    POST/skiers/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}
     urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
     */

    if (urlPath.length != 8) {
      return 2;
    }

    System.out.println(Arrays.toString(urlPath));

    try {
      int resortId = Integer.parseInt(urlPath[1]);
      String season = urlPath[2];
      int seasonID = Integer.parseInt(urlPath[3]);
      String day = urlPath[4];
      int dayID = Integer.parseInt(urlPath[5]);
      String skier = urlPath[6];
      int skierId = Integer.parseInt(urlPath[7]);

      if (resortId < 1 || resortId > 10 || seasonID != 2022 || dayID != 1 || skierId < 1
          || skierId > 100000 || !season.equals("seasons") || !day.equals("days")
          || !skier.equals("skiers")) {
        return 1;
      } else {
        //valid info and pack it into a StringBuilder for queue message
        urlInfo.append(resortId).append(SPACE).append(seasonID).append(SPACE).append(dayID)
            .append(SPACE).append(skierId);
        return 0;
      }
    } catch (Exception e) {
      return 1;

    }
  }
}
