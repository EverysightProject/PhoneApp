package CloudController.PlacesController;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.maps.internal.LatLngAdapter;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import everysight.phoneapp.MapsActivity;

/**
 * Created by t-aryehe on 11/11/2017.
 */

public class PlacesAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {
    private Context context;
    private NearByParameters nearByParameters = null;
    private Activity mCaller;

    public PlacesAsyncTask(Activity caller)
    {
        mCaller = caller;
    }

    @Override
    protected String doInBackground(Pair<Context, String>[] params) {
        context = params[0].first;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LatLng.class, new LatLngAdapter())
                .create();

        try {
            // Set up the request
            URL url = new URL("https://everysightbackendapp.appspot.com/places");
            //   URL url = new URL("http://192.168.1.18/directions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Execute HTTP Post
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String message = gson.toJson(nearByParameters);
            writer.write(message);
            writer.flush();
            outputStream.close();
            connection.connect();

            // Read response
            int responseCode = connection.getResponseCode();
            StringBuilder response = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                return response.toString();
            }
            return "Error: " + responseCode + " " + connection.getResponseMessage();

        } catch (IOException e) {
            return e.getMessage();
        }
    }

    protected void onPostExecute(String result)
    {
        MapsActivity activity = (MapsActivity) mCaller;
        activity.setPlaceResponse(result);
    }

    public void setNearByParameters(NearByParameters nearByParameters)
    {
        this.nearByParameters = nearByParameters;
    }

    public NearByParameters getNearByParameters()
    {
        return this.nearByParameters;
    }
}
