import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.dbcp2.BasicDataSource;

public class DBCPDataSource {

  private static final ThreadLocal<Connection> connHolder;
  private static final BasicDataSource dataSource;
//  private static final String HOST_NAME = "databaseskier.c6cajavlleea.us-west-2.rds.amazonaws.com";
  private static final String HOST_NAME = "localhost";
  private static final String PORT = "3306";
  private static final String DATABASE = "databaseskier";

  static {
    connHolder = new ThreadLocal<Connection>();
    dataSource = new BasicDataSource();
    try {
      Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    String url = String.format("jdbc:mysql://%s:%s/%s?serverTimezone=UTC", HOST_NAME, PORT, DATABASE);
    dataSource.setUrl(url);
    dataSource.setUsername("root");
    dataSource.setPassword("813300Hw!");
    dataSource.setInitialSize(20);
    dataSource.setMaxTotal(60);

  }

  public static BasicDataSource getDataSource() {
    return dataSource;
  }


  public static Connection getConnection() {
    Connection conn = connHolder.get();
    if (conn == null) {
      try {
        conn = dataSource.getConnection();
        System.out.println("get connection successfully");
      } catch (SQLException e) {
        System.out.println("get connection failed:" + e);
      } finally {
        connHolder.set(conn);
      }
    }
    return conn;
  }

  public static void closeConnection() {
    Connection conn = connHolder.get();
    if (conn != null) {
      try {
        conn.close();
        System.out.println("close connection successfully");
      } catch (SQLException e) {
        System.out.println("close connection failed:" + e);
        throw new RuntimeException(e);
      } finally {
        connHolder.remove();
      }
    }
  }


}
