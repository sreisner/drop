package com.example.drop.drop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.drop.drop.backend.dropApi.DropAPI;
import com.example.drop.drop.backend.dropApi.model.BlobKey;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateDropActivity
        extends AppCompatActivity
        implements Response.Listener<String>,
                   Response.ErrorListener {
    private static final String LOG_TAG =  CreateDropActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private GoogleApiClient mGoogleApiClient;
    private Bitmap mImage;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            mImage = (Bitmap) extras.get("data");

            Button takePictureButton = (Button)findViewById(R.id.start_camera_button);
            ImageView imageThumbnail = (ImageView)findViewById(R.id.image_thumbnail);
            imageThumbnail.setImageBitmap(mImage);

            takePictureButton.setVisibility(View.GONE);
            imageThumbnail.setVisibility(View.VISIBLE);
        }
    }

    private String getLatitudeText() {
        EditText latitudeEditText = (EditText)findViewById(R.id.latitude);
        return latitudeEditText.getText().toString();
    }

    private String getLongitudeText() {
        EditText longitudeEditText = (EditText)findViewById(R.id.longitude);
        return longitudeEditText.getText().toString();
    }

    private String getCaption() {
        EditText captionEditText = (EditText)findViewById(R.id.caption);
        return captionEditText.getText().toString();
    }

    private byte[] getPngImage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mImage.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    public void dispatchCameraIntent(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    public void createDrop(View view) {
        Log.d(LOG_TAG, "Creating drop...");

        DropAPI dropService = Utility.getDropBackendApiService();
        RequestQueue queue = Volley.newRequestQueue(this);
        String uploadUrl;
        try {
            uploadUrl = dropService.createUploadUrl().execute().getData();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failure creating upload URL.");
            Log.d(LOG_TAG, e.getMessage());
            Toast.makeText(this, "Failed to upload drop.  See logs for details.", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, uploadUrl, this, this) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("pngImage", new String(getPngImage()));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "multipart/form-data");
                return headers;
            }
        };
        queue.add(request);
    }

    @Override
    public void onResponse(String blobKeyString) {
        Log.d(LOG_TAG, blobKeyString);
        Toast.makeText(this, "Image uploaded with blob key " + blobKeyString, Toast.LENGTH_LONG).show();

        CreateDropTask task = new CreateDropTask();

        float latitude = Float.parseFloat(getLatitudeText());
        float longitude = Float.parseFloat(getLongitudeText());
        String caption = getCaption();
        BlobKey blobKey = new BlobKey().setKeyString(blobKeyString);

        task.execute(latitude, longitude, caption, blobKey);
    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        Log.d(LOG_TAG, volleyError.getMessage());
        Toast.makeText(this, "Error uploading image.  See logs for details.", Toast.LENGTH_LONG).show();
    }
}
