package part1;

import io.swagger.client.model.LiftRide;

public class NewLiftRide {
  private LiftRide body;
  private Integer resortID;
  private String seasonID;
  private String dayID;
  private Integer skierID;

  public NewLiftRide(LiftRide body, Integer resortID, String seasonID, String dayID,
      Integer skierID) {
    this.body = body;
    this.resortID = resortID;
    this.seasonID = seasonID;
    this.dayID = dayID;
    this.skierID = skierID;
  }


  public LiftRide getBody() {
    return body;
  }

  public void setBody(LiftRide body) {
    this.body = body;
  }

  public Integer getResortID() {
    return resortID;
  }

  public void setResortID(Integer resortID) {
    this.resortID = resortID;
  }

  public String getSeasonID() {
    return seasonID;
  }

  public void setSeasonID(String seasonID) {
    this.seasonID = seasonID;
  }

  public String getDayID() {
    return dayID;
  }

  public void setDayID(String dayID) {
    this.dayID = dayID;
  }

  public Integer getSkierID() {
    return skierID;
  }

  public void setSkierID(Integer skierID) {
    this.skierID = skierID;
  }
}
