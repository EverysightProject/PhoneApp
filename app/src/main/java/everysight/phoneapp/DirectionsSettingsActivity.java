package everysight.phoneapp;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.maps.model.TravelMode;

import CloudController.DirectionsController.DirectionsAsyncTask;
import CloudController.DirectionsController.RouteParameters;

public class DirectionsSettingsActivity extends AppCompatActivity {

    private Location location;
    private static final int TAB_DRIVE = 0;
    private static final int TAB_TRANSIT = 1;
    private static final int TAB_WALK = 2;
    private static final int TAB_CYCLE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions_settings);
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_CANCELED);
        finish();
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
        EditText locationInput = (EditText) findViewById(R.id.currentLocation);
        EditText destinationInput = (EditText) findViewById(R.id.destination);

        String locInput = locationInput.getText().toString();
        String destInput = destinationInput.getText().toString();

        if (destInput.equals(""))
        {
            Toast toast = Toast.makeText(this, "Please Enter Destination!", Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
        RouteParameters routeParameters = new RouteParameters(locInput,destInput);

        if(locInput.equals("") )
        {
            //TODO - get place id by location
            routeParameters.setOriginName(locInput);
            routeParameters.setOrigin(new com.google.maps.model.LatLng(location.getLatitude(),location.getLongitude()));
        }
        routeParameters.setAlternatives(true);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.rideOptions);
        int selected = tabLayout.getSelectedTabPosition();
        switch (selected)
        {
            case TAB_DRIVE: routeParameters.setTravelMode(TravelMode.DRIVING); break;
            case TAB_TRANSIT: routeParameters.setTravelMode(TravelMode.TRANSIT); break;
            case TAB_WALK: routeParameters.setTravelMode(TravelMode.WALKING); break;
            case TAB_CYCLE: routeParameters.setTravelMode(TravelMode.BICYCLING); break;
            default: routeParameters.setTravelMode(TravelMode.WALKING); break;
        }
        return routeParameters;
    }
}
