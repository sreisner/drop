package com.example.drop.drop;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utility {
    private static String LOG_TAG = Utility.class.getSimpleName();

    private static int EARTHS_RADIUS = 6378137;
    private static final String DISPLAY_DATE_FORMAT = "K:mmaa -  MMMM dd, yyyy";
    private static final String DB_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static synchronized GoogleApiClient buildGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .build();
    }

    public static double getOffsetLatitude(double latitude, double offsetMeters) {
        double latDelta = offsetMeters / EARTHS_RADIUS;
        return latitude + latDelta * 180 / Math.PI;
    }

    public static double getOffsetLongitude(double latitude, double longitude, double offsetMeters) {
        double longDelta = offsetMeters / (EARTHS_RADIUS * Math.cos(Math.PI * latitude / 180));
        return longitude + longDelta * 180 / Math.PI;
    }

    public static Date parseDate(String date) {
        Date createdOn;

        try {
            createdOn = new SimpleDateFormat(DB_DATE_FORMAT).parse(date);
        } catch (ParseException e) {
            Log.d(LOG_TAG, "Failed to parse date.", e);
            createdOn = new Date(0);
        }

        return createdOn;
    }

    public static String formatDateForDisplay(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }
}
