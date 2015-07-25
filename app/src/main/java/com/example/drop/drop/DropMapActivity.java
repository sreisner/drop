package com.example.drop.drop;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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


public class DropMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = DropMapActivity.class.getName();

    private static final float DEFAULT_ZOOM_LEVEL = 17;

    private static final int DROP_LOADER_ID = 0;

    public static final double DEFAULT_LATITUDE = 41.930853;
    public static final double DEFAULT_LONGITUDE = -87.641325;

    public static final int COL_DROP_ID = 0;
    public static final int COL_DROP_LATITUDE = 1;
    public static final int COL_DROP_LONGITUDE = 2;
    public static final int COL_DROP_TEXT = 3;

    private GoogleApiClient mGoogleApiClient;
    private GoogleMap map;
    private DropCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_map);

        mGoogleApiClient = Utility.buildGoogleApiClient(this);
        mGoogleApiClient.registerConnectionCallbacks(this);
        mGoogleApiClient.connect();

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mCursorAdapter = new DropCursorAdapter(this, null, 0);

        ListView dropListView = (ListView) findViewById(R.id.drop_list);
        dropListView.setAdapter(mCursorAdapter);

        dropListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor data = (Cursor) mCursorAdapter.getItem(position);
                double latitude = data.getDouble(COL_DROP_LATITUDE);
                double longitude = data.getDouble(COL_DROP_LONGITUDE);
                mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            }
        });

        getLoaderManager().initLoader(DROP_LOADER_ID, null, this);

        DropSyncAdapter.syncImmediately(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        } else if(id == R.id.action_scan) {
            DropSyncAdapter.syncImmediately(this);
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

        LatLng lastKnownLocation = getLastKnownLocation();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocation, DEFAULT_ZOOM_LEVEL));

        map.addCircle(new CircleOptions()
                        .center(lastKnownLocation)
                        .radius(DropSyncAdapter.DISCOVER_RADIUS_METERS)
                        .fillColor(R.color.drop_blue)
                        .strokeWidth(0)
        );
    }

    private LatLng getLastKnownLocation() {
        Location lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lastKnownLocation != null) {
            return new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        } else {
            return new LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateMap();
        DropSyncAdapter.syncImmediately(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

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
        updateMap();

        while (data.moveToNext()) {
            double latitude = data.getDouble(COL_DROP_LATITUDE);
            double longitude = data.getDouble(COL_DROP_LONGITUDE);
            String title = data.getString(COL_DROP_TEXT);
            LatLng position = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions()
                            .position(position)
                            .title(title));
        }
        
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
