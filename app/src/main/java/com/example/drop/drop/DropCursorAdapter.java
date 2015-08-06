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
        Date createdOn = Utility.parseDate(createdOnString);
        String formattedDate = Utility.formatDateForDisplay(createdOn);

        viewHolder.dropTextView.setText(dropText);
        viewHolder.createdOnTextView.setText(formattedDate);
    }

    public static class ViewHolder {
        public final TextView dropTextView;
        public final TextView createdOnTextView;

        public ViewHolder(View view) {
            dropTextView = (TextView) view.findViewById(R.id.list_item_drop_textview);
            createdOnTextView = (TextView) view.findViewById(R.id.list_item_created_on_textview);
        }
    }
}
