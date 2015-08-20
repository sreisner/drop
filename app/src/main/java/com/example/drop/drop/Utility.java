package com.example.drop.drop;

import android.content.Context;

import com.appspot.drop_web_service.dropApi.DropApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utility {
    private static String LOG_TAG = Utility.class.getSimpleName();

    private static int EARTHS_RADIUS = 6378137;
    private static final String DISPLAY_DATE_FORMAT = "K:mmaa -  MMMM dd, yyyy";

    public static synchronized GoogleApiClient buildGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
    }

    public static String formatDateForDisplay(long seconds) {
        Date date = new Date(seconds);
        SimpleDateFormat formatter = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getDefault());
        return formatter.format(date);
    }

    public static DropApi getDropBackendApiService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = new AndroidJsonFactory();
        return new DropApi.Builder(transport, jsonFactory, null)
                .setRootUrl("https://drop-web-service.appspot.com/_ah/api/")
                .setServicePath("dropApi/v1/")
                .build();
    }
}
