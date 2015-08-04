package com.example.drop.drop;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.drop.drop.data.DropContract;
import com.example.drop.drop.sync.DropSyncAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class DropMapActivity extends AppCompatActivity
                             implements GoogleApiClient.ConnectionCallbacks,
                                        LoaderManager.LoaderCallbacks<Cursor>,
                                        OnMapReadyCallback,
                                        LocationListener
{
    private static final String LOG_TAG = DropMapActivity.class.getSimpleName();

    private static final float DEFAULT_ZOOM_LEVEL = 17;
    private static final int DROP_LOADER_ID = 0;
    private static final int ENABLE_LOCATION_RESULT_CODE = 0;

    public static final int COL_DROP_ID = 0;
    public static final int COL_DROP_LATITUDE = 1;
    public static final int COL_DROP_LONGITUDE = 2;
    public static final int COL_DROP_TEXT = 3;

    private Menu mMenu;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private DropCursorAdapter mCursorAdapter;
    private ProgressDialog mScanningDialog;
    private LatLng mCurrentLatLng;
    private boolean mScanInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_map);

        mScanInProgress = false;
        mCurrentLatLng = null;

        initializeDropList();
        initializeScanningDialog();
        initializeGoogleApiClient();
        initializeMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_drop_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_create) {
            Intent intent = new Intent(this, CreateDropActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_scan) {
            scan();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeDropList() {
        Log.d(LOG_TAG, "Initializing drop list.");
        mCursorAdapter = new DropCursorAdapter(this, null, 0);

        ListView dropListView = (ListView) findViewById(R.id.drop_list);
        dropListView.setAdapter(mCursorAdapter);

        dropListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mGoogleMap != null) {
                    Cursor data = (Cursor) mCursorAdapter.getItem(position);

                    double latitude = data.getDouble(COL_DROP_LATITUDE);
                    double longitude = data.getDouble(COL_DROP_LONGITUDE);
                    LatLng location = new LatLng(latitude, longitude);

                    CameraUpdate update = CameraUpdateFactory.newLatLng(location);
                    mGoogleMap.animateCamera(update);
                } else {
                    Log.d(LOG_TAG, "Google Map is not ready.");
                }
            }
        });

        getLoaderManager().initLoader(DROP_LOADER_ID, null, this);
    }

    private void initializeScanningDialog() {
        mScanningDialog = new ProgressDialog(this);
        mScanningDialog.setMessage(getString(R.string.scanning));
        mScanningDialog.setCancelable(false);
    }

    private void initializeGoogleApiClient() {
        Log.d(LOG_TAG, "Initializing Google API client.");
        mGoogleApiClient = Utility.buildGoogleApiClient(this);
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.connect();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == DROP_LOADER_ID) {
            return new CursorLoader(
                    this, DropContract.DropEntry.CONTENT_URI,
                    null, null, null, null);
        } else {
            throw new UnsupportedOperationException("id not recognized");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "Finished downloading leaf data.");
        updateMapLocation();

        if(mGoogleMap != null) {
            while (data.moveToNext()) {
                double leafLatitude = data.getDouble(COL_DROP_LATITUDE);
                double leafLongitude = data.getDouble(COL_DROP_LONGITUDE);
                String title = data.getString(COL_DROP_TEXT);
                LatLng position = new LatLng(leafLatitude, leafLongitude);

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(title));
            }
        }

        mCursorAdapter.swapCursor(data);
        disableScan();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "Loader reset.");
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Connected to Google Play Services.");
        scan();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "Connection to Google Play Services suspended.");
    }

    private void initializeMap() {
        Log.d(LOG_TAG, "Initializing map.");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "Map initialized.");
        mGoogleMap = googleMap;
    }

    private void scan() {
        if (!isLocationEnabled()) {
            showEnableLocationDialog();
        } else if(canScan()) {
            enableScan();
        }
    }

    private void setScanButtonEnabled(boolean enabled) {
        if (mMenu != null) {
            MenuItem scanButton = mMenu.findItem(R.id.action_scan);
            if (scanButton != null) {
                scanButton.setEnabled(enabled);
            }
        }
    }

    private void enableScan() {
        mScanInProgress = true;
        setScanButtonEnabled(false);
        mScanningDialog.show();

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
    }

    private void disableScan() {
        if(mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        mScanInProgress = false;
        mScanningDialog.hide();
        setScanButtonEnabled(true);
    }

    private boolean canScan() {
        return !mScanInProgress && isInitialized() && isLocationEnabled();
    }

    private boolean isInitialized() {
        return mGoogleMap != null && mGoogleApiClient.isConnected();
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location changed! " + location);
        if(location != null && isInitialized()) {
            Log.d(LOG_TAG, "Valid location found.");
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", location.getLatitude());
            bundle.putDouble("longitude", location.getLongitude());
            DropSyncAdapter.syncImmediately(this, bundle);
        }
    }

    private void updateMapLocation() {
        if (mCurrentLatLng == null || mGoogleMap == null) {
            return;
        }

        Log.d(LOG_TAG, "Moving camera to user's current location.");

        mGoogleMap.clear();
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentLatLng, DEFAULT_ZOOM_LEVEL));
        mGoogleMap.addCircle(new CircleOptions()
                .center(mCurrentLatLng)
                .radius(DropSyncAdapter.DISCOVER_RADIUS_METERS)
                .fillColor(getResources().getColor(R.color.discover_circle_color))
                .strokeWidth(0));
    }

    private void showEnableLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_location_message)
                .setTitle(R.string.enable_location_title)
                .setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(settingsIntent, DropMapActivity.ENABLE_LOCATION_RESULT_CODE);
                    }
                });

        AlertDialog dialog =  builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ENABLE_LOCATION_RESULT_CODE && resultCode == RESULT_OK) {
            scan();
        }
    }


    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
               locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
