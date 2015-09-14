package com.example.drop.drop;

import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
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

    private LatLng currentLocation;
    private ProgressDialog scanningDialog;
    private DropCursorAdapter dropCursorAdapter;
    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "onCreate");
        setContentView(R.layout.activity_drop_map);
        scanningDialog = new ScanningDialog(this);

        initializeDropListView();
        initializeGoogleApiClient();
        initializeDropMap();
        initializeDropLoader();
    }

    private void initializeDropListView() {
        Log.d(LOG_TAG, "Initializing Drop ListView");
        ListView dropListView = (ListView) findViewById(R.id.drop_list);
        dropCursorAdapter = new DropCursorAdapter(this, null, 0);
        dropListView.setAdapter(dropCursorAdapter);
        dropListView.setOnItemClickListener(new DropClickListener());
    }

    private void initializeGoogleApiClient() {
        Log.d(LOG_TAG, "Initializing Google API client");
        googleApiClient = Utility.buildGoogleApiClient(this);
        googleApiClient.registerConnectionCallbacks(this);
        googleApiClient.connect();
    }

    private void initializeDropMap() {
        Log.d(LOG_TAG, "Initializing map");
        SupportMapFragment dropMapFragment = getDropMapFragment();
        dropMapFragment.getMapAsync(this);
    }

    private void initializeDropLoader() {
        Log.d(LOG_TAG, "Initializing Drop loader");
        getLoaderManager().initLoader(0, null, this);
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
                createDropButtonPressed();
                break;
            case R.id.action_scan:
                scanButtonPressed();
                break;
            default:
                logUnknownOptionSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void createDropButtonPressed() {
        startCreateDropActivity();
    }

    private void startCreateDropActivity() {
        Intent intent = new Intent(this, CreateDropActivity.class);
        startActivity(intent);
    }

    private void scanButtonPressed() {
        if(Utility.isLocationServicesEnabled(this)) {
            showScanningDialog();
            beginScan();
        } else {
            showEnableLocationDialog();
        }
    }

    private void beginScan() {
        if(canBeginReceivingLocationUpdates()) {
            beginReceivingLocationUpdates();
        }
    }

    private void showScanningDialog() {
        scanningDialog.show();
    }

    private void hideScanningDialog() {
        scanningDialog.hide();
    }

    private void showEnableLocationDialog() {
        // TODO:  Clean up this method.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.enable_location_message)
                .setTitle(R.string.enable_location_title)
                .setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(settingsIntent);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        return getDropCursorLoader();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dropData) {
        if(currentLocation != null) {
            hideScanningDialog();
            updateUIWithDropData(dropData);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(LOG_TAG, "Loader reset.");
        dropCursorAdapter.swapCursor(null);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Connected to Google Play Services.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "Connection to Google Play Services suspended.");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(LOG_TAG, "Map initialized.");
        this.googleMap = googleMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "Location changed! " + location);
        if (locationIsAccurateEnoughForDropDownload(location)) {
            stopReceivingLocationUpdates();
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            beginDropDownload(currentLocation, DropConstants.DISCOVER_RADIUS_METERS);
        }
    }

    private void logUnknownOptionSelected(MenuItem item) {
        Log.d(LOG_TAG, "Unknown option selected: " + item.getTitle() + "," + item.getItemId());
    }

    private LatLng getPositionFromDropListViewRow(Cursor dropListViewRowCursor) {
        double latitude = dropListViewRowCursor.getDouble(DropConstants.COLUMN_DROP_LATITUDE);
        double longitude = dropListViewRowCursor.getDouble(DropConstants.COLUMN_DROP_LONGITUDE);
        return new LatLng(latitude, longitude);
    }

    private String getCaptionFromDropListViewRow(Cursor dropListViewRowCursor) {
        return dropListViewRowCursor.getString(DropConstants.COLUMN_DROP_CAPTION);
    }

    private MarkerOptions getMapMarkerForDropListViewRow(Cursor dropListViewRowCursor) {
        LatLng dropLocation = getPositionFromDropListViewRow(dropListViewRowCursor);
        String dropCaption = getCaptionFromDropListViewRow(dropListViewRowCursor);
        return new MarkerOptions().position(dropLocation)
                                  .title(dropCaption);
    }

    private void addMarkerToMapForDropListViewRow(Cursor dropListViewRowCursor) {
        MarkerOptions marker = getMapMarkerForDropListViewRow(dropListViewRowCursor);
        if(googleMap != null) {
            googleMap.addMarker(marker);
        }
    }

    private void addMarkersToMapForDropData(Cursor dropData) {
        while (dropData.moveToNext()) {
            addMarkerToMapForDropListViewRow(dropData);
        }
    }

    private void updateMap(Cursor dropData) {
        clearMap();
        moveMapCamera(currentLocation);
        placeDiscoveryCircleOnMap();
        addMarkersToMapForDropData(dropData);
    }

    private void updateUIWithDropData(Cursor dropData) {
        dropCursorAdapter.swapCursor(dropData);
        updateMap(dropData);
    }

    private CursorLoader getDropCursorLoader() {
        return new CursorLoader(
                this, DropContract.DropEntry.CONTENT_URI,
                null, null, null, null);
    }

    private SupportMapFragment getDropMapFragment() {
        return (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private LocationRequest getScanLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(DropConstants.SCAN_LOCATION_UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(DropConstants.SCAN_LOCATION_UPDATE_FASTEST_INTERVAL_MS);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private boolean canBeginReceivingLocationUpdates() {
        return googleApiClient.isConnected() && Utility.isLocationServicesEnabled(this);
    }

    private void beginReceivingLocationUpdates() {
        LocationRequest locationRequest = getScanLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);
    }

    private void stopReceivingLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    private boolean locationIsAccurateEnoughForDropDownload(Location location) {
        return location != null &&
               location.hasAccuracy() &&
               location.getAccuracy() < DropConstants.REQUIRED_ACCURACY_METERS;
    }

    private Bundle createLocationBundle(LatLng location, double radiusInMeters) {
        Bundle bundle = new Bundle();
        bundle.putDouble("latitude", location.latitude);
        bundle.putDouble("longitude", location.longitude);
        bundle.putDouble("radiusInMeters", radiusInMeters);
        return bundle;
    }

    private void beginDropDownload(LatLng location, double radiusInMeters) {
        Bundle bundle = createLocationBundle(location, radiusInMeters);
        DropSyncAdapter.syncImmediately(this, bundle);
    }

    private void clearMap() {
        googleMap.clear();
    }

    private CameraUpdate getCameraUpdateMovement(LatLng location) {
        return CameraUpdateFactory.newLatLngZoom(
                location,
                DropConstants.MAP_ZOOM_LEVEL);
    }

    private void moveMapCamera(LatLng location) {
        CameraUpdate cameraUpdate = getCameraUpdateMovement(location);
        googleMap.moveCamera(cameraUpdate);
    }

    private void placeDiscoveryCircleOnMap() {
        CircleOptions circleData = new CircleOptions()
                .center(currentLocation);
        googleMap.addCircle(circleData);
    }

    private class DropClickListener implements AdapterView.OnItemClickListener {
        private Cursor clickedRow;

        private double getClickedRowLatitude() {
            return clickedRow.getDouble(DropConstants.COLUMN_DROP_LATITUDE);
        }

        private double getClickedRowLongitude() {
            return clickedRow.getDouble(DropConstants.COLUMN_DROP_LONGITUDE);
        }

        private LatLng getClickedRowLocation() {
            double latitude = getClickedRowLatitude();
            double longitude = getClickedRowLongitude();
            return new LatLng(latitude, longitude);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            clickedRow = (Cursor) dropCursorAdapter.getItem(position);
            LatLng location = getClickedRowLocation();
            if (googleMap != null) {
                moveMapCamera(location);
            } else {
                Log.d(LOG_TAG, "Google Map is not ready.");
            }
        }
    }
}
