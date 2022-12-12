import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LiftRideDao {

  private static final String SPACE = " ";

  public static Runnable createLiftRide(ConcurrentLinkedQueue<String> q) {
    return () -> {
      String insertQuery = "INSERT INTO liftRides (skierId, resortId, seasonId, dayId, time, liftId) " +
          "VALUES (?,?,?,?,?,?)";
      Connection conn = null;
      PreparedStatement preparedStatement = null;
      try {
        conn = DBCPDataSource.getConnection();
        preparedStatement = conn.prepareStatement(insertQuery);

        for (int i = 1; i <= 20000; i++) {
          String message = q.poll();
          if (message == null) {
            continue;
          }
          String[] parameters = message.split(SPACE);
          int time = Integer.parseInt(parameters[0]);
          int liftID = Integer.parseInt(parameters[1]);
          int resortID = Integer.parseInt(parameters[2]);
          int seasonID = Integer.parseInt(parameters[3]);
          int dayID = Integer.parseInt(parameters[4]);
          int skierID = Integer.parseInt(parameters[5]);

          preparedStatement.setInt(1, skierID);
          preparedStatement.setInt(2, resortID);
          preparedStatement.setInt(3, seasonID);
          preparedStatement.setInt(4, dayID);
          preparedStatement.setInt(5, time);
          preparedStatement.setInt(6, liftID);
          preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        preparedStatement.clearBatch();
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        try {
          DBCPDataSource.closeConnection();
          if (preparedStatement != null) {
            preparedStatement.close();
          }
        } catch (SQLException se) {
          se.printStackTrace();
        }
      }
    };
  }

}
