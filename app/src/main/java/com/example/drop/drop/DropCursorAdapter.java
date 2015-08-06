package com.example.drop.drop;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DropCursorAdapter extends CursorAdapter {
    private static final String LOG_TAG = DropCursorAdapter.class.getSimpleName();

    private static final String DATE_FORMAT = "KK:mmaa', MMMM dd', yyyy";

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
        String createdOnString = cursor.getString(DropMapActivity.COL_DROP_CREATED_ON);
        Date createdOn = parseDate(createdOnString);
        String formattedDate = formatDate(createdOn);

        viewHolder.dropTextView.setText(dropText);
        viewHolder.createdOnTextView.setText(formattedDate);
    }

    private Date parseDate(String date) {
        Date createdOn;

        try {
            createdOn = new SimpleDateFormat().parse(date);
        } catch (ParseException e) {
            Log.d(LOG_TAG, "Failed to parse date.", e);
            createdOn = new Date(0);
        }

        return createdOn;
    }

    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }

    public static class ViewHolder {
        public final TextView dropTextView;
        public final TextView createdOnTextView;

        public ViewHolder(View view) {
            dropTextView = (TextView) view.findViewById(R.id.list_item_drop_textview);
            createdOnTextView = (TextView) view.findViewById(R.id.list_item_createdon_textview);
        }
    }
}
