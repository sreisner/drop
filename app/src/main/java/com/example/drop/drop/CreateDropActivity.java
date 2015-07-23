package com.example.drop.drop;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


public class CreateDropActivity extends ActionBarActivity {

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drop);

        mGoogleApiClient = Utility.buildGoogleApiClient(this);
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_drop, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if(id == R.id.action_coordinates) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (lastLocation != null) {
                EditText latitudeEditText = (EditText)findViewById(R.id.latitude);
                EditText longitudeEditText = (EditText)findViewById(R.id.longitude);
                latitudeEditText.setText(String.valueOf(lastLocation.getLatitude()));
                longitudeEditText.setText(String.valueOf(lastLocation.getLongitude()));
            } else {
                Toast.makeText(this, "Location not found.", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void createDrop(View view) {
        EditText latitudeEditText = (EditText)findViewById(R.id.latitude);
        EditText longitudeEditText = (EditText)findViewById(R.id.longitude);
        EditText dropTextEditText = (EditText)findViewById(R.id.drop_text);

        String latitude = latitudeEditText.getText().toString();
        String longitude = longitudeEditText.getText().toString();
        String text = dropTextEditText.getText().toString();

        new CreateDropTask().execute(latitude, longitude, text);
    }
}
