package com.example.drop.drop;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

import com.example.drop.drop.backend.dropApi.DropAPI;
import com.example.drop.drop.backend.dropApi.model.Drop;
import com.example.drop.drop.backend.dropApi.model.GeoPt;

import java.io.IOException;
import java.util.Date;

public class DropUploaderService extends IntentService {
    private static final String LOG_TAG = DropUploaderService.class.getSimpleName();

    private static final float DEFAULT_LATITUDE = 0.0f;
    private static final float DEFAULT_LONGITUDE = 0.0f;

    public DropUploaderService() {
        super("DropUploaderServiceWorker");
    }

    private long uploadDrop(Drop toUpload) {
        long id = -1;
        DropAPI dropService = Utility.getDropBackendApiService();
        try {
            id = dropService.insert(toUpload).execute().getId();
            Log.d(LOG_TAG, "Created drop successfully.  Drop ID = " + id);
        } catch(IOException e) {
            // TODO:  Generate notification.
            Log.d(LOG_TAG, "Failed to create drop: " + e.getMessage());
        }
        return id;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bitmap image = intent.getParcelableExtra("image");

        float latitude = intent.getFloatExtra("latitude", DEFAULT_LATITUDE);
        float longitude = intent.getFloatExtra("longitude", DEFAULT_LONGITUDE);
        String caption = intent.getStringExtra("caption");

        Drop drop = new Drop();
        drop.setLocation(new GeoPt()
                .setLatitude(latitude)
                .setLongitude(longitude));
        drop.setCaption(caption);
        drop.setCreatedOnUTCSeconds(new Date().getTime());
        drop.setImage(Utility.convertBitmapToJpegBase64String(image));
        uploadDrop(drop);
    }
}
