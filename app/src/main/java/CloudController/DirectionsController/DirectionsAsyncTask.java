package CloudController.DirectionsController;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.internal.LatLngAdapter;
import com.google.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class DirectionsAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {
    private Context context;
    private RouteParameters routeParameters = null;
    private Activity mCaller;

    public DirectionsAsyncTask(Activity caller)
    {
        mCaller = caller;
    }

    @Override
    protected String doInBackground(Pair<Context, String>... params) {
        context = params[0].first;

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LatLng.class, new LatLngAdapter())
                .create();

        try {
            // Set up the request
            URL url = new URL("https://everysightbackendapp.appspot.com/directions");
         //   URL url = new URL("http://192.168.1.18/directions");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Execute HTTP Post
            OutputStream outputStream = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            writer.write(gson.toJson(routeParameters));
            writer.flush();
            writer.close();
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

    private String buildPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    @Override
    protected void onPostExecute(String result)
    {
            Intent data = new Intent();
            data.putExtra("Result",result);
            mCaller.setResult(mCaller.RESULT_OK,data);
            mCaller.finish();
    }

    public void setRouteParameters(RouteParameters routeParameters)
    {
        this.routeParameters = routeParameters;
    }

    public RouteParameters getRouteParameters()
    {
        return this.routeParameters;
    }
}