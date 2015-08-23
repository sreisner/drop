package com.example.drop.drop;

import android.os.AsyncTask;
import android.util.Log;

import com.appspot.drop_web_service.dropApi.DropApi;
import com.appspot.drop_web_service.dropApi.model.Drop;
import com.appspot.drop_web_service.dropApi.model.GeoPt;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CreateDropTask extends AsyncTask<Object, Integer, Long> {
    private static final String LOG_TAG = CreateDropTask.class.getSimpleName();

    private void uploadImage(String uploadUrl, byte[] imageData) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost(uploadUrl);

        ByteArrayInputStream stream = new ByteArrayInputStream(imageData);
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent(stream);
        request.setEntity(entity);

        try {
            HttpResponse response = client.execute(request);
        } catch (IOException e) {
            Log.d(LOG_TAG, "Couldn't upload image. " + e.getMessage());
        }
    }

    @Override
    protected Long doInBackground(Object... params) {
        DropApi dropService = Utility.getDropBackendApiService();

        float latitude = (float)params[0];
        float longitude = (float)params[1];
        String caption = (String)params[2];
        byte[] imageData = (byte[])params[3];

        Drop toCreate = new Drop();
        toCreate.setLocation(new GeoPt()
                .setLatitude(latitude)
                .setLongitude(longitude));
        toCreate.setCaption(caption);

        try {
            toCreate = dropService.post(toCreate).execute();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failed to create drop: " + e.getMessage());
        }

        String uploadUrl = toCreate.getUploadUrl();
        uploadImage(uploadUrl, imageData);

        return toCreate.getId();
    }
}
