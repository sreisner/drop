package com.example.drop.drop.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DropDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;

    static final String DATABASE_NAME = "drop.db";

    public DropDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDropTable(db);
    }

    private void createDropTable(SQLiteDatabase db) {
        final String SQL_CREATE_DROP_TABLE = "CREATE TABLE " + DropContract.DropEntry.TABLE_NAME + " (" +
                DropContract.DropEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DropContract.DropEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                DropContract.DropEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
                DropContract.DropEntry.COLUMN_CAPTION + " TEXT NULL, " +
                DropContract.DropEntry.COLUMN_CREATED_ON + " INTEGER NULL);";

        db.execSQL(SQL_CREATE_DROP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DropContract.DropEntry.TABLE_NAME);
        onCreate(db);
    }
}
