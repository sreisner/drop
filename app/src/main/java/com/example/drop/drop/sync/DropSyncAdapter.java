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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String LOG_TAG = DropSyncAdapter.class.getSimpleName();

    private DropAPI dropService;
    private ContentResolver contentResolver;
    private double currentLatitude;
    private double currentLongitude;
    private double discoverRadiusInMeters;

    public DropSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        dropService = Utility.getDropBackendApiService();
        contentResolver = context.getContentResolver();
    }

    private List<Drop> downloadAllDropsFromServer() {
        try {
            return dropService.list().execute().getItems();
        } catch(IOException e) {
            Log.d(LOG_TAG, "Failed to retrieve drops: " + e);
        }
        return new ArrayList<>();
    }

    private float getDistanceBetweenCurrentLocationAndDrop(Drop drop) {
        float[] results = new float[1];
        Location.distanceBetween(
                currentLatitude, currentLongitude,
                drop.getLocation().getLatitude(), drop.getLocation().getLongitude(),
                results);
        return results[0];
    }


    private List<Drop> getDropsWithinDiscoveryRadius(List<Drop> drops) {
        ArrayList<Drop> dropsWithinRadius = new ArrayList<>();
        for(Drop drop : drops) {
            float distance = getDistanceBetweenCurrentLocationAndDrop(drop);
            if(distance < discoverRadiusInMeters) {
                dropsWithinRadius.add(drop);
            }
        }
        return dropsWithinRadius;
    }

    private List<Drop> downloadDropsInDiscoveryRadius() {
        List<Drop> allDrops = downloadAllDropsFromServer();
        return getDropsWithinDiscoveryRadius(allDrops);
    }

    private void deleteLocalDropData() {
        contentResolver.delete(DropContract.DropEntry.CONTENT_URI, null, null);
    }

    private ContentValues getDropContentValues(Drop drop) {
        ContentValues values = new ContentValues();
        values.put(DropContract.DropEntry.COLUMN_LATITUDE, drop.getLocation().getLatitude());
        values.put(DropContract.DropEntry.COLUMN_LONGITUDE, drop.getLocation().getLongitude());
        values.put(DropContract.DropEntry.COLUMN_CAPTION, drop.getCaption());
        values.put(DropContract.DropEntry.COLUMN_CREATED_ON, drop.getCreatedOnUTCSeconds());
        return values;
    }

    private void insertDrop(Drop drop) {
        ContentValues contentValues = getDropContentValues(drop);
        contentResolver.insert(DropContract.DropEntry.CONTENT_URI, contentValues);
    }

    private void insertDropsIntoLocalDatabase(List<Drop> drops) {
        for(Drop drop : drops) {
            insertDrop(drop);
        }
    }

    private void notifyListenersOfDataChange() {
        contentResolver.notifyChange(DropContract.DropEntry.CONTENT_URI, null);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Performing sync.");

        // TODO:  Consider putting these three values into their own class.
        currentLatitude = extras.getDouble("latitude");
        currentLongitude = extras.getDouble("longitude");
        discoverRadiusInMeters = extras.getDouble("radiusInMeters");

        deleteLocalDropData();
        List<Drop> dropsInDiscoveryRadius = downloadDropsInDiscoveryRadius();
        insertDropsIntoLocalDatabase(dropsInDiscoveryRadius);
        notifyListenersOfDataChange();
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
