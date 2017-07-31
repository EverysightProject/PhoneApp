package everysight.phoneapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.maps.model.DirectionsResult;

import java.io.StringReader;

import BluetoothConnection.ConnectThread;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                GoogleApiClient.ConnectionCallbacks,
                                                 GoogleApiClient.OnConnectionFailedListener,
                                                                                LocationListener {

    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_ENABLE_BT = 1 ;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 2 ;
    private static final int REQUEST_DIRECTIONS = 3;
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;

    private String origin = "";
    private String Destination = "";

    private Location location;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mBThandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1000); // 1 second, in milliseconds

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(TAG,"Device does not support Bluetooth");
        }
        mBThandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            location =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if ( location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            else {
                handleNewLocation( location);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if(requestCode==REQUEST_CONNECT_DEVICE_SECURE)
        {
            String address = data.getExtras()
                    .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            ConnectThread connectThread = new ConnectThread(device, mBThandler);
            connectThread.start();
        }

        if(requestCode == REQUEST_DIRECTIONS && resultCode == RESULT_OK)
        {
            try
            {
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new StringReader(data.getExtras().getString("Result")));
                reader.setLenient(true);
                DirectionsResult dr = gson.fromJson(reader, DirectionsResult.class);
                HandleRoute(dr);
            }
            catch(Exception exception)
            {
                System.out.print(exception.toString());
            }
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("You are here"));
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // We can now safely use the API we requested access to
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                    handleNewLocation(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            } else {
                // Permission was denied or request was cancelled
                Log.i(TAG, "Location services permission denied.");
            }
        }
    }

    public void onDirectionsButtonClick(View v)
    {
        try {
            Intent serverIntent = new Intent(this, DirectionsSettingsActivity.class);
            startActivityForResult(serverIntent, REQUEST_DIRECTIONS);
        }
        catch( Exception e)
        {
            Log.e(TAG,e.toString());
        }
    }

    public void onBluetoothButtonClick(View v)
    {
        try {
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
        }
        catch( Exception e)
        {
            Log.e(TAG,e.toString());
        }
    }


    public void onJoystickButtonCick(View v)
    {
        try {
            Intent intent = new Intent(this, DirectionTest.class);
            startActivity(intent);
        }
        catch( Exception e)
        {
            Log.e(TAG,e.toString());
        }
    }

    private void HandleRoute(DirectionsResult dr)
    {

    }

    private void onBluetoothConnected()
    {
        Toast.makeText(this, "Bluetooth Connected", Toast.LENGTH_LONG).show();
    }
}
