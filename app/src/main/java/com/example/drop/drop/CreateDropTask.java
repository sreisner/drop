package com.example.drop.drop;

import android.os.AsyncTask;
import android.util.Log;

import com.appspot.drop_web_service.dropApi.DropApi;

import java.io.IOException;

public class CreateDropTask extends AsyncTask<String, Integer, Void> {
    private static final String LOG_TAG = CreateDropTask.class.getSimpleName();

    @Override
    protected Void doInBackground(String... params) {
        DropApi service = Utility.getDropBackendApiService();

        float latitude = Float.parseFloat(params[0]);
        float longitude = Float.parseFloat(params[1]);
        String caption = params[2];

        try {
            service.create(latitude, longitude, caption).execute();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failed to create drop: " + e.getMessage());
        }

        return null;
    }
}
