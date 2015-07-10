package com.example.drop.drop;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DropCursorAdapter extends CursorAdapter {

    public DropCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_drop, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        String dropText = cursor.getString(DropMapActivity.COL_DROP_TEXT);
        double latitude = cursor.getDouble(DropMapActivity.COL_DROP_LATITUDE);
        double longitude = cursor.getDouble(DropMapActivity.COL_DROP_LONGITUDE);

        viewHolder.dropTextView.setText(dropText);
        viewHolder.latitudeTextView.setText("" + latitude);
        viewHolder.longitudeTextView.setText("" + longitude);
    }

    public static class ViewHolder {
        public final TextView dropTextView;
        public final TextView latitudeTextView;
        public final TextView longitudeTextView;

        public ViewHolder(View view) {
            dropTextView = (TextView) view.findViewById(R.id.list_item_drop_textview);
            latitudeTextView = (TextView) view.findViewById(R.id.list_item_latitude_textview);
            longitudeTextView = (TextView) view.findViewById(R.id.list_item_longitude_textview);
        }
    }
}
