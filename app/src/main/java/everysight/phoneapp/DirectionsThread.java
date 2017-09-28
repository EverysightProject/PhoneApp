package everysight.phoneapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;

import BluetoothConnection.BluetoothCommunicator;
import BluetoothConnection.DirectionsMessage;

/**
 * Created by t-aryehe on 7/27/2017.
 */

public class DirectionsThread extends Thread {

    private final DirectionsRoute mDirectionRoute;
    private GoogleApiClient mGoogleApiClient;
    private final Context mContext;
    private Location mLocation;
    private String mDirection = "";

    public DirectionsThread(DirectionsRoute directionsRoute, Context context,
                            GoogleApiClient googleApiClient)
    {
        mDirectionRoute = directionsRoute;
        mGoogleApiClient = googleApiClient;
        mContext = context;
    }

    public void run()
    {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            for(int i=0; i<mDirectionRoute.legs.length;i++)
            {
                for( int j=0;j<mDirectionRoute.legs[i].steps.length;j++)
                {
                    doStep(mDirectionRoute.legs[i].steps[j]);
                }
            }
        }
    }

    public void cancel()
    {

    }

    public void doStep(DirectionsStep directionStep)
    {
        Location endLocation = new Location("");
        endLocation.setLatitude(directionStep.endLocation.lat);
        endLocation.setLongitude(directionStep.endLocation.lng);

        DirectionsMessage dm = new DirectionsMessage();

        mDirection = "Forward";
        if(directionStep.maneuver != null) {
            if (directionStep.maneuver.endsWith("right")) {
                mDirection = "Right";
            } else if (directionStep.maneuver.endsWith("left")) {
                mDirection = "Left";
            } else if (directionStep.maneuver.endsWith("straight")) {
                mDirection = "Forward";
            }
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            do {
                try {
                    mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    dm.Direction = mDirection;
                    dm.DistanceMeter = (int) mLocation.distanceTo(endLocation);
                    if(dm.DistanceMeter > 50)
                        dm.Direction = "Forward";

                    dm.GeoLocation = mLocation;
                    dm.AzimuthDeg = (double) mLocation.bearingTo(endLocation);

                    BluetoothCommunicator bt = BluetoothCommunicator.getInstance();
                    if(bt.isConnected())
                    {
                        Gson gson = new Gson();
                        String message = gson.toJson(dm);
                        bt.write(message);
                    }
                    else
                    {
                        Toast.makeText(mContext, "Bluetooth is not connected", Toast.LENGTH_LONG).show();
                    }
                    this.sleep(1000);
                }
                catch (Exception e)
                {

                }
            }while(dm.DistanceMeter >= 10);


        }

    }

}
