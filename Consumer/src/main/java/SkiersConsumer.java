import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkiersConsumer {

  private static final String RMQ_IP = "34.121.727.153";
  private static final int THREAD_POOL_SIZE = 20;
  private static final int PER_THREAD_CHANNEL_SIZE = 10;
  private final static String QUEUE_NAME = "skiersQueue";
  private static final String EXCHANGE_NAME = "exchanger";
  private static ConcurrentLinkedQueue<String> q = new ConcurrentLinkedQueue<>();

  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("localhost");
//    factory.setHost(RMQ_IP);
    factory.setPort(5672);
    factory.setUsername("admin");
    factory.setPassword("password");

    Connection connection = factory.newConnection();

    Runnable runnable = () -> {
      for (int i = 0; i < PER_THREAD_CHANNEL_SIZE; i++) {
        try {
          Channel channel = connection.createChannel();
          channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
          channel.queueDeclare(QUEUE_NAME, false, false, false, null);
          channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "");
          channel.basicQos(1);

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            q.offer(message);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
          });
        } catch (IOException ex) {
          Logger.getLogger(SkiersConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };

    ExecutorService consumerPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    long start = System.currentTimeMillis();
    for (int i = 0; i < THREAD_POOL_SIZE; i++) {
      consumerPool.execute(runnable);
    }

    //insert messages to the database
    ExecutorService consumerPool2 = Executors.newFixedThreadPool(50);
    Thread.sleep(1000);
    int maxQueueSize = 0;
    while (true) {
      for (int i = 0; i < 100; i++) {
        maxQueueSize = Math.max(maxQueueSize, q.size());
        System.out.println("max q size: " + maxQueueSize);
        System.out.println("thread: " + i);
        Thread.sleep(1000);
        consumerPool2.execute(LiftRideDao.createLiftRide(q));
      }
      if (q.isEmpty()) {
        break;
      }
    }
    long end = System.currentTimeMillis();
    System.out.println("total time: " + (end - start) / 1000);//127s
    System.out.println("max cache queue size: " + maxQueueSize);//69943
  }

}
