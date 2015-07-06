package com.example.drop.drop;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.drop.drop.data.DropContract;


public class CreateDropActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drop);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_drop, menu);
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
        } else if(id == R.id.action_next) {
            Intent intent = new Intent(this, DropMapActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void createDrop(View view) {
        EditText latitudeEditText = (EditText)findViewById(R.id.latitude);
        EditText longitudeEditText = (EditText)findViewById(R.id.longitude);
        EditText dropTextEditText = (EditText)findViewById(R.id.drop_text);

        double latitude = Double.parseDouble(latitudeEditText.getText().toString());
        double longitude = Double.parseDouble(longitudeEditText.getText().toString());
        String dropText = dropTextEditText.getText().toString();

        ContentValues values = new ContentValues();
        values.put(DropContract.DropEntry.COLUMN_LATITUDE, latitude);
        values.put(DropContract.DropEntry.COLUMN_LONGITUDE, longitude);
        values.put(DropContract.DropEntry.COLUMN_DROP_TEXT, dropText);

        Uri uri = DropContract.DropEntry.CONTENT_URI;
        Uri recordUri = getContentResolver().insert(uri, values);
        Toast.makeText(this, "Row created with ID: " + recordUri.getLastPathSegment(), Toast.LENGTH_SHORT).show();
    }
}
