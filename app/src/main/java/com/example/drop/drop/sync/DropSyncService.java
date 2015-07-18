package com.example.drop.drop.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class DropSyncService extends Service {
    private static DropSyncAdapter syncAdapter = null;
    private static final Object syncAdapterLock = new Object();

    @Override
    public void onCreate() {
        synchronized (syncAdapterLock) {
            if(syncAdapter == null) {
                syncAdapter = new DropSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return syncAdapter.getSyncAdapterBinder();
    }
}
