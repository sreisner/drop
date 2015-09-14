package com.example.drop.drop;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.util.Base64;

import com.example.drop.drop.backend.dropApi.DropAPI;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utility {
    private static String LOG_TAG = Utility.class.getSimpleName();

    private static final String DISPLAY_DATE_FORMAT = "K:mmaa - MMMM dd, yyyy";

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

    public static DropAPI getDropBackendApiService() {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        AndroidJsonFactory jsonFactory = new AndroidJsonFactory();
        return new DropAPI.Builder(transport, jsonFactory, null)
                .setRootUrl("https://drop-web-service.appspot.com/_ah/api/")
                .setServicePath("dropApi/v1/")
                .build();
    }

    public static boolean isLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static String convertBitmapToJpegBase64String(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] rawJpeg = stream.toByteArray();
        return Base64.encodeToString(rawJpeg, Base64.DEFAULT);
    }
}
