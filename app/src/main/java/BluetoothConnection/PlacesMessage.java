package BluetoothConnection;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import everysight.phoneapp.PlaceData;

/**
 * Created by t-aryehe on 11/6/2017.
 */

public class PlacesMessage extends BtMessage {
    @SerializedName("PlacesData")
    public List<PlaceData> PlacesData = null;
}
