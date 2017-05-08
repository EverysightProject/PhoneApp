/**
 * My Directions API
 * No description provided (generated by Swagger Codegen https://github.com/swagger-api/swagger-codegen)
 *
 * OpenAPI spec version: 0.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package io.swagger.client.model;

import io.swagger.annotations.*;
import com.google.gson.annotations.SerializedName;

@ApiModel(description = "")
public class GeoLocation {
  
  @SerializedName("Latitude")
  private Double latitude = null;
  @SerializedName("Longitude")
  private Double longitude = null;

  /**
   **/
  @ApiModelProperty(value = "")
  public Double getLatitude() {
    return latitude;
  }
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  public Double getLongitude() {
    return longitude;
  }
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GeoLocation geoLocation = (GeoLocation) o;
    return (this.latitude == null ? geoLocation.latitude == null : this.latitude.equals(geoLocation.latitude)) &&
        (this.longitude == null ? geoLocation.longitude == null : this.longitude.equals(geoLocation.longitude));
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (this.latitude == null ? 0: this.latitude.hashCode());
    result = 31 * result + (this.longitude == null ? 0: this.longitude.hashCode());
    return result;
  }

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class GeoLocation {\n");
    
    sb.append("  latitude: ").append(latitude).append("\n");
    sb.append("  longitude: ").append(longitude).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
