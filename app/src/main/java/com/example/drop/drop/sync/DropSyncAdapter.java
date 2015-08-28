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
import com.example.drop.drop.backend.dropApi.DropAPI;
import com.example.drop.drop.backend.dropApi.model.Drop;
import com.example.drop.drop.data.DropContract;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = DropSyncAdapter.class.getSimpleName();

    public static final double DISCOVER_RADIUS_METERS = 10000;
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

        DropAPI service = Utility.getDropBackendApiService();

        List<Drop> drops = null;
        try {
            drops = service.list().execute().getItems();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failed to retrieve drops: " + e);
        }

        if(drops == null) {
            drops = new ArrayList<>();
        }

        mContentResolver.delete(DropContract.DropEntry.CONTENT_URI, null, null);

        Log.d(LOG_TAG, "Found " + drops.size() + " drops.  Inserting...");
        for(Drop drop : drops) {
            float[] results = new float[1];
            Location.distanceBetween(
                    currentLatitude, currentLongitude,
                    drop.getLocation().getLatitude(), drop.getLocation().getLongitude(),
                    results);

            float distance = results[0];
            if(distance <= DropSyncAdapter.DISCOVER_RADIUS_METERS) {
                ContentValues values = new ContentValues();
                values.put(DropContract.DropEntry.COLUMN_LATITUDE, drop.getLocation().getLatitude());
                values.put(DropContract.DropEntry.COLUMN_LONGITUDE, drop.getLocation().getLongitude());
                values.put(DropContract.DropEntry.COLUMN_CAPTION, drop.getCaption());
                values.put(DropContract.DropEntry.COLUMN_CREATED_ON, drop.getCreatedOnUTCSeconds());
                mContentResolver.insert(DropContract.DropEntry.CONTENT_URI, values);
            }
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
