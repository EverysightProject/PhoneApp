package BluetoothConnection;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import everysight.phoneapp.FriendData;

/**
 * Created by t-aryehe on 11/6/2017.
 */

public class FriendsMessage extends BtMessage {
    @SerializedName("FriendsData")
    public List<FriendData> FriendsData = null;
}
