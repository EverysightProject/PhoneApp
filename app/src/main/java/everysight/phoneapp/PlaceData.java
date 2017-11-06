package everysight.phoneapp;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

/**
 * Created by t-aryehe on 11/6/2017.
 */

public class PlaceData {
    @SerializedName("Name")
    public String Name = null;

    @SerializedName("GeoLocation")
    public Location GeoLocation = null;
}
