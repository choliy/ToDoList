package com.choliy.igor.todolist.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

public class TaskContentProvider extends ContentProvider {

    private static final String TAG = TaskContentProvider.class.getSimpleName();

    // Define final integer constants for the directory of tasks and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int TASKS = 100;
    public static final int TASK_WITH_ID = 101;

    // Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Member variable for a TaskDbHelper that's initialized in the onCreate() method
    private TaskDbHelper mTaskDbHelper;
    private ContentResolver mContentResolver;

    /**
     * Initialize a new matcher object without any matches,
     * then use .addURI(String authority, String path, int match) to add matches
     */
    private static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // All paths added to the UriMatcher have a corresponding int.
        // For each kind of uri you may want to access, add the corresponding match with addURI.
        // The two calls below add matches for the task directory and a single item by ID.
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS, TASKS);
        uriMatcher.addURI(TaskContract.AUTHORITY, TaskContract.PATH_TASKS + "/#", TASK_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        assert context != null;
        mTaskDbHelper = new TaskDbHelper(context);
        mContentResolver = context.getContentResolver();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {

        Log.i(TAG, "Income query URI: " + uri.toString());

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mTaskDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);

        Cursor cursor;

        switch (match) {
            case TASKS:
                cursor = db.query(
                        TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TASK_WITH_ID:
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(
                        TaskContract.TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }

        // Set a notification URI on the Cursor and return that Cursor
        cursor.setNotificationUri(mContentResolver, uri);

        // Return the desired Cursor
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri,
                      ContentValues values) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case TASKS:
                long id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert row into " + uri.toString());
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }

        // Notify the resolver if the uri has been changed
        mContentResolver.notifyChange(uri, null);

        Log.i(TAG, "Inserted Task URI: " + returnUri.toString());

        // Return constructed Uri
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri,
                          @NonNull ContentValues[] values) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);

        switch (match) {
            case TASKS:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(TaskContract.TaskEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                // Notify the resolver if the Uri has been changed
                if (rowsInserted > 0) {
                    mContentResolver.notifyChange(uri, null);
                }

                Log.i(TAG, "Number of inserted rows: " + rowsInserted);

                // Return the number of inserted rows
                return rowsInserted;

            default:

                // If the URI does match TASKS, return the super implementation
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(@NonNull Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {

        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the task directory
        int match = sUriMatcher.match(uri);

        // Keep track of the number of updated tasks
        int tasksUpdated;

        switch (match) {
            case TASK_WITH_ID:
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                tasksUpdated = db.update(
                        TaskContract.TaskEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }

        // Notify the resolver if data has been updated
        if (tasksUpdated != 0) {
            mContentResolver.notifyChange(uri, null);
            Log.i(TAG, "Count of updated Tasks: " + tasksUpdated);
        } else {
            throw new android.database.SQLException("Failed to update row: " + uri.toString());
        }

        // Return the number of updated tasks
        return tasksUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri,
                      String selection,
                      String[] selectionArgs) {

        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mTaskDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);

        // Keep track of the number of deleted tasks
        int tasksDeleted;

        switch (match) {
            case TASKS:
                tasksDeleted = db.delete(
                        TaskContract.TaskEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case TASK_WITH_ID:
                selection = TaskContract.TaskEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                tasksDeleted = db.delete(
                        TaskContract.TaskEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri.toString());
        }

        // Notify the resolver if data has been deleted
        if (tasksDeleted != 0) {
            mContentResolver.notifyChange(uri, null);
            Log.i(TAG, "Removed Task URI: " + uri.toString());
            Log.i(TAG, "Count of removed Tasks: " + tasksDeleted);
        }

        // Return the number of deleted tasks
        return tasksDeleted;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case TASKS:
                return TaskContract.TaskEntry.CONTENT_DIR_TYPE;
            case TASK_WITH_ID:
                return TaskContract.TaskEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI: " + uri.toString() + ", with match: " + match);
        }
    }
}