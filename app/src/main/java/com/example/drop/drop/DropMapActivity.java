package com.example.drop.drop;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.drop.drop.data.DropContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class DropMapActivity extends ActionBarActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = DropMapActivity.class.getName();

    private static final double HOME_LAT = 41.930853;
    private static final double HOME_LONG = -87.641325;
    private static final float DEFAULT_ZOOM_LEVEL = 18;
    // Discover radius in feet.
    private static final double DEFAULT_DISCOVER_RADIUS = 1000;

    public static final int COL_DROP_ID = 0;
    public static final int COL_DROP_LATITUDE = 1;
    public static final int COL_DROP_LONGITUDE = 2;
    public static final int COL_DROP_TEXT = 3;

    private Cursor drops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_map);

        drops = getDropsWithinDiscoverableRadius();

        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ListView dropListView = (ListView)findViewById(R.id.drop_list);
        dropListView.setAdapter(new DropCursorAdapter(this, drops, 0));
        dropListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        dropListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor data = (Cursor) parent.getAdapter().getItem(position);
                double latitude = data.getDouble(COL_DROP_LATITUDE);
                double longitude = data.getDouble(COL_DROP_LONGITUDE);
                mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude)));
            }
        });

        Log.d(LOG_TAG, "DropMapActivity.onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "DropMapActivity.onResume");
    }

    private Cursor getDropsWithinDiscoverableRadius() {
        Uri dropsWithinRadiusUri = DropContract.DropEntry.CONTENT_URI;
        Cursor cursor = getContentResolver().query(
                dropsWithinRadiusUri,
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
        Log.d(LOG_TAG, "DropMapActivity.onMapReady");
        LatLng home = new LatLng(HOME_LAT, HOME_LONG);
        // map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, DEFAULT_ZOOM_LEVEL));

        while(drops.moveToNext()) {
            double latitude = drops.getDouble(DropMapActivity.COL_DROP_LATITUDE);
            double longitude = drops.getDouble(DropMapActivity.COL_DROP_LONGITUDE);
            String title = drops.getString(DropMapActivity.COL_DROP_TEXT);
            LatLng position = new LatLng(latitude, longitude);
            map.addMarker(new MarkerOptions().position(position).title(title));
        }
    }
}
