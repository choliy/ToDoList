package com.choliy.igor.todolist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.choliy.igor.todolist.data.TaskContract.TaskEntry;

class TaskDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "toDoList.db";
    private static final int VERSION = 1;

    TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Called when the tasks database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        final String CREATE_TABLE = "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                TaskEntry._ID + " INTEGER PRIMARY KEY, " +
                TaskEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                TaskEntry.COLUMN_PRIORITY + " INTEGER NOT NULL, " +
                TaskEntry.COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                TaskEntry.COLUMN_REMINDER + " INTEGER);";

        db.execSQL(CREATE_TABLE);
    }

    /**
     * This method discards the old table of data and calls onCreate to recreate a new one.
     * This only occurs when the version number for this database (DATABASE_VERSION) is incremented.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        onCreate(db);
    }
}