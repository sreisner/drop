package com.example.drop.drop;

import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.drop.drop.data.DropContract;
import com.example.drop.drop.sync.DropSyncAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class DropMapActivity extends ActionBarActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks {

    private static final String LOG_TAG = DropMapActivity.class.getName();

    public static final double DEFAULT_LATITUDE = 41.930853;
    public static final double DEFAULT_LONGITUDE = -87.641325;

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    public static final int COL_DROP_ID = 0;
    public static final int COL_DROP_LATITUDE = 1;
    public static final int COL_DROP_LONGITUDE = 2;
    public static final int COL_DROP_TEXT = 3;

    private Cursor drops;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "DropMapActivity.onCreate");

        setContentView(R.layout.activity_drop_map);

        drops = getDropsWithinDiscoverableRadius();

        mGoogleApiClient = Utility.buildGoogleApiClient(this);
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.connect();

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ListView dropListView = (ListView)findViewById(R.id.drop_list);
        dropListView.setAdapter(new DropCursorAdapter(this, drops, 0));

        dropListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor data = (Cursor) parent.getAdapter().getItem(position);
                double latitude = data.getDouble(COL_DROP_LATITUDE);
                double longitude = data.getDouble(COL_DROP_LONGITUDE);
                mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            }
        });

        DropSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private Cursor getDropsWithinDiscoverableRadius() {
        Uri dropUri = DropContract.DropEntry.CONTENT_URI;
        Cursor cursor = getContentResolver().query(
                dropUri,
                null,
                null,
                null,
                null
        );

        return cursor;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        updateMap();
    }

    private void updateMap() {
        map.clear();

        LatLng lastKnownLocation;

        Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastLocation != null) {
            lastKnownLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        } else {
            lastKnownLocation = new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, DEFAULT_ZOOM_LEVEL));

        map.addCircle(new CircleOptions()
                        .center(lastKnownLocation)
                        .radius(DropSyncAdapter.DISCOVER_RADIUS_METERS)
                        .fillColor(0x110000ff)
                        .strokeWidth(0)
        );

        // map.getUiSettings().setAllGesturesEnabled(false);

        drops.moveToPosition(-1);
        while(drops.moveToNext()) {
            double latitude = drops.getDouble(DropMapActivity.COL_DROP_LATITUDE);
            double longitude = drops.getDouble(DropMapActivity.COL_DROP_LONGITUDE);
            String title = drops.getString(DropMapActivity.COL_DROP_TEXT);
            LatLng position = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions()
                            .position(position)
                            .title(title)
            );
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateMap();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
