package com.example.drop.drop;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CreateDropTask extends AsyncTask<String, Integer, Long> {
    private static final String LOG_TAG = CreateDropTask.class.getName();

    @Override
    protected Long doInBackground(String... params) {
        HttpClient client = new DefaultHttpClient();
        HttpPost request = new HttpPost("http://drop-web-service.appspot.com/drop");

        String latitude = params[0];
        String longitude = params[1];
        String text = params[2];

        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("latitude", latitude));
        parameters.add(new BasicNameValuePair("longitude", longitude));
        parameters.add(new BasicNameValuePair("text", text));

        try {
            request.setEntity(new UrlEncodedFormEntity(parameters));
        } catch(UnsupportedEncodingException e) {
            Log.d(LOG_TAG, e.getMessage());
        }

        try {
            HttpResponse response = client.execute(request);
            Log.d(LOG_TAG, "Created drop with ID: " + response.toString());
        } catch (IOException e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return (long)1;
    }
}
