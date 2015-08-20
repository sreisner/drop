package com.example.drop.drop;

import android.os.AsyncTask;
import android.util.Log;

import com.appspot.drop_web_service.dropApi.DropApi;
import com.appspot.drop_web_service.dropApi.model.Drop;
import com.appspot.drop_web_service.dropApi.model.GeoPt;

import java.io.IOException;

public class CreateDropTask extends AsyncTask<Drop, Integer, Void> {
    private static final String LOG_TAG = CreateDropTask.class.getSimpleName();

    @Override
    protected Void doInBackground(Drop... params) {
        DropApi service = Utility.getDropBackendApiService();

        Drop toCreate = params[0];

        try {
            service.create(toCreate).execute();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failed to create drop: " + e.getMessage());
        }

        return null;
    }
}
