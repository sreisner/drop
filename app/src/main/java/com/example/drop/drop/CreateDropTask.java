package com.example.drop.drop;

import android.os.AsyncTask;
import android.util.Log;

import com.example.drop.drop.backend.dropApi.DropAPI;

import java.io.IOException;

public class CreateDropTask extends AsyncTask<Object, Integer, String> {
    private static final String LOG_TAG = CreateDropTask.class.getSimpleName();

    @Override
    protected String doInBackground(Object... params) {
        DropAPI dropService = Utility.getDropBackendApiService();
        String uploadUrl = null;
        try {
            uploadUrl = dropService.createUploadUrl().execute().getData();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failure creating upload URL.");
            Log.d(LOG_TAG, e.getMessage());
        }

        return uploadUrl;
    }
}