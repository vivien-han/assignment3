import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySQLTableCreator {

  private static final String HOST_NAME = "localhost";
  private static final String PORT = "3306";
  private static final String DATABASE = "databaseskier";
  private static final String USERNAME = "root";
  private static final String PASSWORD = "813300Hw!";

  private static String getMySQLAddress() {
    return String.format(
        "jdbc:mysql://%s:%s/%s?user=%s&password=%s&autoReconnect=true&serverTimezone=UTC&createDatabaseIfNotExist=true",
        HOST_NAME, PORT, DATABASE, USERNAME, PASSWORD);
  }

  public static void main(String[] args) {
    try {
      System.out.println("Connecting to " + getMySQLAddress());
      Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
      Connection conn = DriverManager.getConnection(getMySQLAddress());
      if (conn == null) {
        System.out.println("Driver manager get connection failed");
        return;
      }
      // Drop table if they exists.
      Statement statement = conn.createStatement();
      String sql = "DROP TABLE IF EXISTS liftRides";
      statement.executeUpdate(sql);
      // Create new table.
      sql = "CREATE TABLE liftRides ("
          + "id INT NOT NULL AUTO_INCREMENT,"
          + "skierId INT,"
          + "resortId INT,"
          + "seasonId INT,"
          + "dayId INT,"
          + "time INT,"
          + "liftId INT,"
          + "PRIMARY KEY (id)"
          + ")";
      statement.executeUpdate(sql);
      conn.close();
      System.out.println("Import successfully");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}
