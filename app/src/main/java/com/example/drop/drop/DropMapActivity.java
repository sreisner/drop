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
        LocationListener {
    private static final String LOG_TAG = DropMapActivity.class.getSimpleName();

    private static final float DEFAULT_ZOOM_LEVEL = 17;
    private static final float REQUIRED_ACCURACY_METERS = 250;

    private static final int DROP_LOADER_ID = 0;
    private static final int ENABLE_LOCATION_RESULT_CODE = 0;
    private static final int CREATE_DROP_RESULT_CODE = 1;

    public static final int COLUMN_DROP_ID = 0;
    public static final int COLUMN_DROP_LATITUDE = 1;
    public static final int COLUMN_DROP_LONGITUDE = 2;
    public static final int COLUMN_DROP_CAPTION = 3;
    public static final int COLUMN_DROP_CREATED_ON = 4;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private DropCursorAdapter mCursorAdapter;
    private ListView mDropListView;
    private ProgressDialog mScanningDialog;
    private LatLng mCurrentLatLng;
    private boolean mScanInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_map);

        setScanInProgress(false);
        initializeDropList();
        initializeScanningDialog();
        initializeGoogleApiClient();
        initializeMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drop_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_create:
                startCreateDropActivity();
                break;
            case R.id.action_scan:
                scan();
                break;
            default:
                logUnknownOptionSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void startCreateDropActivity() {
        Intent intent = new Intent(this, CreateDropActivity.class);
        startActivityForResult(intent, CREATE_DROP_RESULT_CODE);
    }

    private void logUnknownOptionSelected(MenuItem item) {
        Log.d(LOG_TAG, "Unknown option selected: " + item.getTitle() + "," + item.getItemId());
    }

    private void initializeDropList() {
        Log.d(LOG_TAG, "Initializing drop list.");

        initializeDropListAdapter();
        initializeDropListView();

        getLoaderManager().initLoader(DROP_LOADER_ID, null, this);
    }

    private void initializeDropListAdapter() {
        mCursorAdapter = new DropCursorAdapter(this, null, 0);
    }

    private void initializeDropListView() {
        mDropListView = (ListView) findViewById(R.id.drop_list);
        mDropListView.setAdapter(mCursorAdapter);
        mDropListView.setOnItemClickListener(new DropClickListener());
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
        Log.d(LOG_TAG, "Finished downloading drop data.");

        if (mGoogleMap != null) {
            while (data.moveToNext()) {
                double latitude = data.getDouble(COLUMN_DROP_LATITUDE);
                double longitude = data.getDouble(COLUMN_DROP_LONGITUDE);
                String caption = data.getString(COLUMN_DROP_CAPTION);
                LatLng position = new LatLng(latitude, longitude);

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(position)
                        .title(caption));
            }
        }

        mCursorAdapter.swapCursor(data);
        mScanInProgress = false;
        mScanningDialog.hide();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "Loader reset.");
        if(mCursorAdapter != null) {
            mCursorAdapter.swapCursor(null);
        }
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

    private void setScanInProgress(boolean state) {
        mScanInProgress = state;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "Map initialized.");
        mGoogleMap = googleMap;
    }

    private void scan() {
        if (!isLocationEnabled()) {
            showEnableLocationDialog();
        } else if (canScan()) {
            mScanInProgress = true;
            mScanningDialog.show();
            enableLocationUpdates();
        }
    }

    private void enableLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, locationRequest, this);
        }
    }

    private void disableLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
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
        if (location != null && location.hasAccuracy() &&
                location.getAccuracy() < REQUIRED_ACCURACY_METERS && isInitialized()) {
            mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            updateMapLocation();
            Bundle bundle = new Bundle();
            bundle.putDouble("latitude", location.getLatitude());
            bundle.putDouble("longitude", location.getLongitude());
            DropSyncAdapter.syncImmediately(this, bundle);
            disableLocationUpdates();
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

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ENABLE_LOCATION_RESULT_CODE && resultCode == RESULT_OK) {
            scan();
        } else if(requestCode == CREATE_DROP_RESULT_CODE && resultCode == RESULT_OK) {
            scan();
        }
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private class DropClickListener implements AdapterView.OnItemClickListener {
        private Cursor clickedRow;

        private double getClickedRowLatitude() {
            return clickedRow.getDouble(COLUMN_DROP_LATITUDE);
        }

        private double getClickedRowLongitude() {
            return clickedRow.getDouble(COLUMN_DROP_LONGITUDE);
        }

        private LatLng getClickedRowLocation() {
            double latitude = getClickedRowLatitude();
            double longitude = getClickedRowLongitude();
            return new LatLng(latitude, longitude);
        }

        private void moveCamera(LatLng location) {
            CameraUpdate update = CameraUpdateFactory.newLatLng(location);
            mGoogleMap.animateCamera(update);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clickedRow = (Cursor) mCursorAdapter.getItem(position);
            LatLng location = getClickedRowLocation();
            if (mGoogleMap != null) {
                moveCamera(location);
            } else {
                Log.d(LOG_TAG, "Google Map is not ready.");
            }
        }
    }
}
