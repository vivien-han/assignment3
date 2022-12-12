import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class ChannelFactory extends BasePooledObjectFactory<Channel> {

  private final Connection connection;
  private int count;

  public ChannelFactory(Connection connection) {
    this.connection = connection;
    count = 0;
  }

  @Override
   public Channel create() throws Exception {
    count ++;
    return connection.createChannel();
  }

  @Override
  public PooledObject<Channel> wrap(Channel channel) {
    return new DefaultPooledObject<>(channel);
  }

  public int getChannelCount() {
    return count;
  }

}
