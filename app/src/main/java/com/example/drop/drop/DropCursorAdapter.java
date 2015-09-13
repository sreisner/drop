package com.example.drop.drop;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        String caption = cursor.getString(DropConstants.COLUMN_DROP_CAPTION);
        long createdOnUTCSeconds = cursor.getLong(DropConstants.COLUMN_DROP_CREATED_ON);
        String formattedDate = Utility.formatDateForDisplay(createdOnUTCSeconds);

        viewHolder.captionTextView.setText(caption);
        viewHolder.createdOnTextView.setText(formattedDate);
    }

    public static class ViewHolder {
        public final TextView captionTextView;
        public final TextView createdOnTextView;

        public ViewHolder(View view) {
            captionTextView = (TextView) view.findViewById(R.id.list_item_caption_textview);
            createdOnTextView = (TextView) view.findViewById(R.id.list_item_created_on_textview);
        }
    }
}
