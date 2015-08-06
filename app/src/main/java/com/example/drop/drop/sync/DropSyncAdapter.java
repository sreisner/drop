package com.example.drop.drop.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.example.drop.drop.R;
import com.example.drop.drop.Utility;
import com.example.drop.drop.data.DropContract;
import com.google.android.gms.common.api.GoogleApiClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DropSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = DropSyncAdapter.class.getSimpleName();

    public static final double DISCOVER_RADIUS_METERS = 100;
    public static final double DOWNLOAD_BOUNDARY_METERS = DISCOVER_RADIUS_METERS * 3;

    private ContentResolver mContentResolver;
    private GoogleApiClient mGoogleApiClient;

    public DropSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        mContentResolver = context.getContentResolver();

        mGoogleApiClient = Utility.buildGoogleApiClient(context);
        mGoogleApiClient.connect();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {

        Log.d(LOG_TAG, "Performing sync.");

        double currentLatitude = extras.getDouble("latitude");
        double currentLongitude = extras.getDouble("longitude");

        double minLatitude = Utility.getOffsetLatitude(currentLatitude, -DOWNLOAD_BOUNDARY_METERS);
        double maxLatitude = Utility.getOffsetLatitude(currentLatitude, DOWNLOAD_BOUNDARY_METERS);
        double minLongitude = Utility.getOffsetLongitude(currentLatitude, currentLongitude, -DOWNLOAD_BOUNDARY_METERS);
        double maxLongitude = Utility.getOffsetLongitude(currentLatitude, currentLongitude, DOWNLOAD_BOUNDARY_METERS);

        HttpClient client = new DefaultHttpClient();
        String baseUrl = "http://drop-web-service.appspot.com/drop?minLat=%f&maxLat=%f&minLong=%f&maxLong=%f";
        String formattedUrl = String.format(baseUrl, minLatitude, maxLatitude, minLongitude, maxLongitude);
        HttpGet request = new HttpGet(formattedUrl);

        String json;
        try {
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            json = EntityUtils.toString(entity, "UTF-8");
        } catch(IOException e) {
            Log.d(LOG_TAG, e.getMessage());
            return;
        }

        mContentResolver.delete(DropContract.DropEntry.CONTENT_URI, null, null);

        try {
            JSONArray dropJsonList = new JSONArray(json);
            Log.d(LOG_TAG, "Syncing " + dropJsonList.length() + " drops.");
            for(int i=0; i<dropJsonList.length(); i++) {
                JSONObject obj = dropJsonList.getJSONObject(i);
                double leafLatitude = obj.getDouble("latitude");
                double leafLongitude = obj.getDouble("longitude");
                String leafCreatedOn = obj.getString("created_on");

                float[] results = new float[1];
                Location.distanceBetween(
                        currentLatitude, currentLongitude,
                        leafLatitude, leafLongitude,
                        results);

                float distance = results[0];
                if(distance <= DropSyncAdapter.DISCOVER_RADIUS_METERS) {
                    ContentValues values = new ContentValues();
                    values.put(DropContract.DropEntry.COLUMN_LATITUDE, obj.getString("latitude"));
                    values.put(DropContract.DropEntry.COLUMN_LONGITUDE, obj.getString("longitude"));
                    values.put(DropContract.DropEntry.COLUMN_DROP_TEXT, obj.getString("text"));
                    values.put(DropContract.DropEntry.COLUMN_CREATED_ON, leafCreatedOn);
                    mContentResolver.insert(DropContract.DropEntry.CONTENT_URI, values);
                }
            }
        } catch(JSONException e) {
            Log.d(LOG_TAG, e.getMessage());
            return;
        }

        mContentResolver.notifyChange(DropContract.DropEntry.CONTENT_URI, null);
    }

    public static void syncImmediately(Context context, Bundle locationBundle) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putAll(locationBundle);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        if (accountManager.getPassword(newAccount) == null) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
        }
        return newAccount;
    }
}
