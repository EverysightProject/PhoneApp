package CloudController.DirectionsController;

import com.google.gson.annotations.SerializedName;

public class RouteParameters {

  public RouteParameters()
  {

  }

  public RouteParameters(String origin,String destination)
  {
    this.originName = origin;
    this.destinationName = destination;
  }

  @SerializedName("originName")
  private String originName = null;
  @SerializedName("destinationName")
  private String destinationName = null;

  /**
   * origin by Name
   **/
  public String getOriginName() {
    return originName;
  }
  public void setOriginName(String originName) {
    this.originName = originName;
  }

  /**
   * destination by Name
   **/
  public String getDestinationName() {
    return destinationName;
  }
  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

}
