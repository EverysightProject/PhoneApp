package everysight.phoneapp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.maps.internal.ExceptionsAllowedToRetry;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsStep;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import BluetoothConnection.BluetoothCommunicator;
import BluetoothConnection.ConnectThread;
import BluetoothConnection.DirectionsMessage;
import BluetoothConnection.FriendsMessage;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
                                                GoogleApiClient.ConnectionCallbacks,
                                                 GoogleApiClient.OnConnectionFailedListener,
                                                                                LocationListener {
    private static final int REQUEST_LOCATION = 0;
    private static final int REQUEST_ENABLE_BT = 1 ;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 2 ;
    private static final int REQUEST_DIRECTIONS = 3;
    private static final int REQUEST_LOGIN = 4;
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private LocationRequest mLocationRequest;

    private Location location;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mBThandler;

    private boolean waitForConnection = false;

    private Thread dirThread;
    private Thread friendThread;

    private String Username = "";

    private boolean FriendsOn = false;
    private boolean BluetoothOn = false;
    private boolean PlacesOn = false;
    private boolean DirectionsOn = false;
    private Map<String,LatLng> friends = new HashMap<String, LatLng>();

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


        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String name = sharedPref.getString("Name","None");

        if (name.equals("None")) {
            try {
                Intent serverIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(serverIntent, REQUEST_LOGIN);
            }
            catch( Exception e)
            {
                Log.e(TAG,e.toString());
            }
        }
        else
        {
            Username = name;
        }
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
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("You are here"));
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
        this.location = location;
        handleNewLocation(location);
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data)
    {
        if(requestCode==REQUEST_CONNECT_DEVICE_SECURE)
        {
            try
            {
                String address = data.getExtras()
                        .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BluetoothDevice object
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                ConnectThread connectThread = new ConnectThread(device,this, mBThandler);
                connectThread.start();
            }
            catch (Exception e){

            }
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

        if (requestCode == REQUEST_LOGIN)
        {
            Username = data.getStringExtra("Name");
            String email = data.getStringExtra("Email");
            String pass = data.getStringExtra("Password");

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("Name", Username);
            editor.putString("Email", email);
            editor.putString("Password", pass);
            editor.commit();
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
        ImageButton b = (ImageButton) findViewById(R.id.DirectionsButton);
        if(DirectionsOn)
        {
            Drawable myIcon = getResources().getDrawable( R.drawable.sign);
            b.setBackground(myIcon);
            dirThread.interrupt();
            DirectionsOn = false;
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("You are here"));
        }
        else {
            try {
                Intent serverIntent = new Intent(this, DirectionsSettingsActivity.class);
                startActivityForResult(serverIntent, REQUEST_DIRECTIONS);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            Drawable myIcon = getResources().getDrawable( R.drawable.signpressed);
            b.setBackground(myIcon);
            DirectionsOn = true;
        }
    }

    public void onBluetoothButtonClick(View v)
    {
        ImageButton b = (ImageButton) findViewById(R.id.BluetoothButton);
        if (BluetoothOn)
        {
            Drawable myIcon = getResources().getDrawable( R.drawable.bluetooth);
            b.setBackground(myIcon);
            BluetoothOn = false;
        }
        else
        {
            try {
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
            catch( Exception e)
            {
                Log.e(TAG,e.toString());
            }
            Drawable myIcon = getResources().getDrawable( R.drawable.bluetoothpressed);
            b.setBackground(myIcon);
            BluetoothOn = true;
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

    private void HandleRoute(final DirectionsResult dr)
    {
        final int selectedRoute = 0;
        //TODO - choose path

        for (int i=0;i<dr.routes[0].legs.length;i++)
        {
            for (int j=0; j< dr.routes[0].legs[i].steps.length;j++)
            {
                com.google.maps.model.LatLng point = dr.routes[0].legs[i].steps[j].startLocation;

                CircleOptions circleOptions = new CircleOptions()
                        .center(new LatLng(point.lat,point.lng))
                        .fillColor(Color.BLUE)
                        .radius(1); // In meters

                // Get back the mutable Circle
                Circle circle = mMap.addCircle(circleOptions);

                point = dr.routes[0].legs[i].steps[j].endLocation;

                circleOptions = new CircleOptions()
                        .center(new LatLng(point.lat,point.lng))
                        .fillColor(Color.BLUE)
                        .radius(1); // In meters

                // Get back the mutable Circle
                circle = mMap.addCircle(circleOptions);
            }
        }

        Runnable directionsThread = new Runnable() {

            private boolean stop = false;

            @Override
            public void run() {
                for(int i=0; i<dr.routes[selectedRoute].legs.length;i++)
                {
                    for( int j=0;j<dr.routes[selectedRoute].legs[i].steps.length;j++)
                    {
                        doStep(dr.routes[selectedRoute].legs[i].steps[j]);
                        if(stop)
                            return;
                    }
                }
            }

            public void doStep(DirectionsStep directionStep)
            {
                Location endLocation = new Location("");
                endLocation.setLatitude(directionStep.endLocation.lat);
                endLocation.setLongitude(directionStep.endLocation.lng);
                DirectionsMessage dm = new DirectionsMessage();

                String mDirection = "Forward";
                if(directionStep.maneuver != null) {
                    if (directionStep.maneuver.endsWith("right")) {
                        mDirection = "Right";
                    } else if (directionStep.maneuver.endsWith("left")) {
                        mDirection = "Left";
                    } else if (directionStep.maneuver.endsWith("straight")) {
                        mDirection = "Forward";
                    }
                }

                do {
                    try {
                        dm.Direction = mDirection;
                        dm.DistanceMeter = (int) location.distanceTo(endLocation);
                        if(dm.DistanceMeter > 50)
                            dm.Direction = "Forward";

                        dm.MessageType = "Directions";
                        dm.GeoLocation = location;
                        dm.AzimuthDeg = (double) location.bearingTo(endLocation);

                        BluetoothCommunicator bt = BluetoothCommunicator.getInstance();
                        if(bt.isConnected())
                        {
                            Gson gson = new Gson();
                            String message = gson.toJson(dm);
                            bt.write(message);
                            Log.i("Directions Thread","Sent:" + message);
                        }
                        if (Thread.interrupted())
                        {
                            stop = true;
                            break;
                        }
                        else
                            Thread.sleep(1000);
                    }
                    catch (Exception e)
                    {
                        Log.i("Direction Tread", e.toString());
                    }
                }while(dm.DistanceMeter >= 10);
            }
        };

        dirThread = new Thread(directionsThread);
        dirThread.start();
        }


    public void OnFriendsButtonClick(View v)
    {
        ImageButton b = (ImageButton) findViewById(R.id.FriendsButton);
        if (FriendsOn)
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("Friends").child(Username);

            myRef.setValue(0,0);
            Drawable myIcon = getResources().getDrawable( R.drawable.friends);
            b.setBackground(myIcon);
            FriendsOn = false;

            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title(Username));
            friendThread.interrupt();
        }
        else
        {
            FriendsOn = true;
            Drawable myIcon = getResources().getDrawable( R.drawable.friendspressed);
            b.setBackground(myIcon);

            Runnable friendsThread = new Runnable() {
                @Override
                public void run() {
                    while(FriendsOn)
                    {
                        try
                        {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("Friends").child(Username);

                            myRef.setValue(new LatLng(location.getLatitude(),location.getLongitude()));

                            myRef = database.getReference("Friends");
                            myRef.addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            try
                                            {
                                                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                                                    if (dsp.getKey().equals(Username))
                                                        continue;
                                                    Map<String,Double> location = (HashMap<String,Double>) dsp.getValue();
                                                    double lat = location.get("latitude");
                                                    double lon = location.get("longitude");
                                                    friends.put(dsp.getKey(), new LatLng(lat,lon));

                                                    mMap.clear();
                                                    mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(lat, lon))
                                                            .title(dsp.getKey()));
                                                }
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                                        .title(Username));

                                                FriendsMessage fm = new FriendsMessage();
                                                fm.GeoLocation = location;
                                                fm.MessageType = "Friends";
                                                fm.FriendsData = friends;
                                                BluetoothCommunicator bt = BluetoothCommunicator.getInstance();
                                                if(bt.isConnected())
                                                {
                                                    Gson gson = new Gson();
                                                    String message = gson.toJson(fm);
                                                    bt.write(message);
                                                    Log.i("Friends Thread","Sent:" + message);
                                                }
                                            }
                                            catch(Exception e)
                                            {
                                            }
                                        }
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            //handle databaseError
                                        }
                                    });
                            if(Thread.interrupted())
                                break;
                            else
                                Thread.sleep(1000);
                        }
                        catch(Exception e)
                        {

                        }
                    }
                }
            };

            friendThread = new Thread(friendsThread);
            friendThread.start();
        }
    }

    public void OnPlacesButtonClick(View v)
    {
        ImageButton b = (ImageButton) findViewById(R.id.PlacesButton);
        if (PlacesOn)
        {
            Drawable myIcon = getResources().getDrawable( R.drawable.places);
            b.setBackground(myIcon);
            PlacesOn = false;
        }
        else
        {
            Drawable myIcon = getResources().getDrawable( R.drawable.placespressed);
            b.setBackground(myIcon);
            PlacesOn = true;
        }
    }
}


