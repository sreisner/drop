package com.example.drop.drop;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.drop.drop.data.DropContract;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class DropMapActivity extends ActionBarActivity implements OnMapReadyCallback {

    private static final double HOME_LAT = 41.930853;
    private static final double HOME_LONG = -87.641325;
    private static final float DEFAULT_ZOOM_LEVEL = 18;
    // Discover radius in feet.
    private static final double DEFAULT_DISCOVER_RADIUS = 1000;

    public static final int COL_DROP_ID = 0;
    public static final int COL_DROP_LATITUDE = 1;
    public static final int COL_DROP_LONGITUDE = 2;
    public static final int COL_DROP_TEXT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drop_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Cursor cursor = getDropsWithinDiscoverableRadius();
        ListView dropListView = (ListView)findViewById(R.id.drop_list);
        dropListView.setAdapter(new DropCursorAdapter(this, cursor, 0));
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
        LatLng home = new LatLng(HOME_LAT, HOME_LONG);
        // map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, DEFAULT_ZOOM_LEVEL));
    }
}
