package com.example.drop.drop;

import android.os.AsyncTask;
import android.util.Log;

import com.example.drop.drop.backend.dropApi.DropAPI;
import com.example.drop.drop.backend.dropApi.model.BlobKey;
import com.example.drop.drop.backend.dropApi.model.ByteArrayInputStream;
import com.example.drop.drop.backend.dropApi.model.Drop;
import com.example.drop.drop.backend.dropApi.model.GeoPt;

import java.io.IOException;

public class CreateDropTask extends AsyncTask<Object, Integer, Long> {
    private static final String LOG_TAG = CreateDropTask.class.getSimpleName();

    @Override
    protected Long doInBackground(Object... params) {
        DropAPI dropService = Utility.getDropBackendApiService();

        float latitude = (float)params[0];
        float longitude = (float)params[1];
        String caption = (String)params[2];
        byte[] imageData = (byte[])params[3];

        BlobKey imageKey = dropService.uploadImage(imageData).execute();

        Drop toCreate = new Drop();
        toCreate.setLocation(new GeoPt()
                .setLatitude(latitude)
                .setLongitude(longitude));
        toCreate.setCaption(caption);
        toCreate.setImageKey(imageKey);

        try {
            toCreate = dropService.insert(toCreate).execute();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failed to create drop: " + e.getMessage());
        }

        return toCreate.getId();
    }
}
