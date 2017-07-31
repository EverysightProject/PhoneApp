package everysight.phoneapp;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import BluetoothConnection.BluetoothCommunicator;
import BluetoothConnection.DirectionsMessage;

public class DirectionTest extends AppCompatActivity {

    private String mDirection = "";
    private String mDistance = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direction_test);
    }

    public void onClickForward(View v)
    {
        mDirection = "Forward";
        Button b = (Button) v.findViewById(R.id.Forward);
        b.setBackgroundColor(Color.RED);
        b = (Button) this.findViewById(R.id.Right);
        b.setBackgroundColor(Color.WHITE);
        b = (Button) this.findViewById(R.id.Left);
        b.setBackgroundColor(Color.WHITE);
    }

    public void onClickLeft(View v)
    {
        mDirection = "Left";
        Button b = (Button) v.findViewById(R.id.Left);
        b.setBackgroundColor(Color.RED);
        b = (Button) this.findViewById(R.id.Right);
        b.setBackgroundColor(Color.WHITE);
        b = (Button) this.findViewById(R.id.Forward);
        b.setBackgroundColor(Color.WHITE);
    }

    public void onClickRight(View v)
    {
        mDirection = "Right";
        Button b = (Button) v.findViewById(R.id.Right);
        b.setBackgroundColor(Color.RED);
        b = (Button) this.findViewById(R.id.Forward);
        b.setBackgroundColor(Color.WHITE);
        b = (Button) this.findViewById(R.id.Left);
        b.setBackgroundColor(Color.WHITE);
    }

    public void onClickSend(View v) {
        try
        {
            DirectionsMessage dm = new DirectionsMessage();
            dm.Direction = mDirection;

            EditText distance = (EditText) this.findViewById(R.id.Distance);
            String dist = distance.getText().toString();
            if(!dist.equals(""))
                dm.DistanceMeter = Integer.parseInt(dist);

            BluetoothCommunicator bt = BluetoothCommunicator.getInstance();
            if(bt.isConnected())
            {
                Gson gson = new Gson();
                String message = gson.toJson(dm);
                bt.write(message);
            }
            else
            {
                Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_LONG).show();
            }
        }
        catch(Exception e)
        {
            Toast.makeText(this, "Enter distance in meters", Toast.LENGTH_LONG).show();
        }
    }
}
