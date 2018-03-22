package everysight.phoneapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.maps.model.TravelMode;

import CloudController.DirectionsController.DirectionsAsyncTask;
import CloudController.DirectionsController.RouteParameters;

public class DirectionsSettingsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_CUR = 1 ;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST = 2 ;
    private static final int REQUEST_LOCATION = 0;
    private static final int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private Location currentLocation;
    private static final int TAB_DRIVE = 0;
    private static final int TAB_TRANSIT = 1;
    private static final int TAB_WALK = 2;
    private static final int TAB_CYCLE = 3;

    private Place OriginLocation;
    private Place Destination;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions_settings);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi( Places.PLACE_DETECTION_API )
                .build();

        mGoogleApiClient.connect();
       // getCurrentPlace();
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_CUR) {
            if (resultCode == RESULT_OK) {
                OriginLocation = PlaceAutocomplete.getPlace(this, data);
                EditText locationInput = (EditText) findViewById(R.id.currentLocation);
                locationInput.setText(OriginLocation.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
        else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST) {
            if (resultCode == RESULT_OK) {
                Destination = PlaceAutocomplete.getPlace(this, data);
                EditText destinationInput = (EditText) findViewById(R.id.destination);
                destinationInput.setText(Destination.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private String getCurrentPlace() {
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        } else {
            // permission has been granted, continue as usual
            location =
                    LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

             if ( location == null)
             {
                 PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
                 result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                     @Override
                     public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                         float likelihood = 0;
                         int index = 0;
                         for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                             if (placeLikelihood.getLikelihood() > likelihood) {
                                 likelihood = placeLikelihood.getLikelihood();
                                 index++;
                             }
                         }
                         PlaceLikelihood placeLike = likelyPlaces.get(index);
                         OriginLocation = placeLike.getPlace();
                     }
                 });
                 return OriginLocation.getName().toString();
             }
        }

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        return String.valueOf(lat) + "," + String.valueOf(lng);
    }

    public void onCurrentLocationClick(View v)
    {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_CUR);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void onDestinationClick(View v)
    {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE_DEST);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void onGetDirectionsButtonClick(View v)
    {
        RouteParameters routeParameters = fillParameters();
        if(routeParameters == null)
            return;
        DirectionsAsyncTask directionsAsyncTask = new DirectionsAsyncTask(this);
        directionsAsyncTask.setRouteParameters(routeParameters);
        directionsAsyncTask.execute(new Pair<Context, String>(this, ""));
    }

    private RouteParameters fillParameters()
    {
        String locInput = getCurrentPlace();
        String destInput = Destination.getName().toString();

        if (destInput.equals(""))
        {
            Toast toast = Toast.makeText(this, "Please Enter Destination!", Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
        RouteParameters routeParameters = new RouteParameters(locInput,destInput);
        return routeParameters;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

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

        }
    }
}
