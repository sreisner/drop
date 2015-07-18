package com.example.drop.drop;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class Utility {

    private static int EARTHS_RADIUS = 6378137;

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
}
