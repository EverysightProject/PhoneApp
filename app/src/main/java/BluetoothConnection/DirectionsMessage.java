package BluetoothConnection;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

/**
 * Created by t-aryehe on 7/17/2017.
 */

public class DirectionsMessage extends BtMessage{

    @SerializedName("Direction")
    public String Direction = null;

    @SerializedName("DistanceMeter")
    public int DistanceMeter = 0;

    @SerializedName("AzimuthDeg")
    public double AzimuthDeg = 0;
}
