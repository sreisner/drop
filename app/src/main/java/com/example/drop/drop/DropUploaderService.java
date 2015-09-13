package com.example.drop.drop;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.drop.drop.backend.dropApi.DropAPI;
import com.example.drop.drop.backend.dropApi.model.Drop;
import com.example.drop.drop.backend.dropApi.model.GeoPt;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DropUploaderService extends IntentService {
    private static final String LOG_TAG = DropUploaderService.class.getSimpleName();

    private static final float DEFAULT_LATITUDE = 0.0f;
    private static final float DEFAULT_LONGITUDE = 0.0f;

    private Bitmap mImage;
    private DropAPI mDropService;

    public DropUploaderService() {
        super("DropUploaderServiceWorker");
    }

    private String createUploadUrl() {
        String uploadUrl;
        try {
            uploadUrl = mDropService.createUploadUrl().execute().getData();
            Log.d(LOG_TAG, "Upload URL created:  " + uploadUrl);
        } catch(IOException e) {
            // TODO:  Create a notification.
            Log.d(LOG_TAG, "Failure creating upload URL.");
            Log.d(LOG_TAG, "" + e.getMessage());
            uploadUrl = null;
        }
        return uploadUrl;
    }

    private byte[] getJpeg() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    private String uploadImage(String uploadUrl) {
        HttpClient httpClient = new DefaultHttpClient();

        HttpPost request = new HttpPost(uploadUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(getJpeg());
        builder.addBinaryBody("file", inputStream);
        HttpEntity entity = builder.build();
        request.setEntity(entity);
        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch(IOException e) {
            Log.d(LOG_TAG, "Exception occurred POSTing image.\n" + e.getMessage());
            return null;
        }
        HttpEntity urlEntity = response.getEntity();
        String responseString = "";
        try {
            InputStream in = urlEntity.getContent();
            while (true) {
                int ch = in.read();
                if (ch == -1)
                    break;
                responseString += (char) ch;
            }
        } catch(IOException e) {
            Log.d(LOG_TAG, "Image uploaded but failure getting response.");
            Log.d(LOG_TAG, "" + e.getMessage());
            return null;
        }
        Log.d(LOG_TAG, "Image upload success: " + responseString);
        return responseString;
    }

    private long uploadDrop(Drop toUpload) {
        long id = -1;
        try {
            id = mDropService.insert(toUpload).execute().getId();
            Log.d(LOG_TAG, "Created drop successfully.  Drop ID = " + id);
        } catch(IOException e) {
            // TODO:  Generate notification.
            Log.d(LOG_TAG, "Failed to create drop: " + e.getMessage());
        }
        return id;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mImage = intent.getParcelableExtra("image");

        float latitude = intent.getFloatExtra("latitude", DEFAULT_LATITUDE);
        float longitude = intent.getFloatExtra("longitude", DEFAULT_LONGITUDE);
        String caption = intent.getStringExtra("caption");

        Drop drop = new Drop();
        drop.setLocation(new GeoPt()
                .setLatitude(latitude)
                .setLongitude(longitude));
        drop.setCaption(caption);

        mDropService = Utility.getDropBackendApiService();
        String uploadUrl = createUploadUrl();
        if(uploadUrl != null) {
            uploadImage(uploadUrl);
        }
    }
}
