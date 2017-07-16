package com.choliy.igor.todolist.tool;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.choliy.igor.todolist.data.TaskContract;

public class TaskLoader extends AsyncTaskLoader<Cursor> {

    private static final String TAG = TaskLoader.class.getSimpleName();
    private Cursor mTaskData;
    private Context mContext;

    public TaskLoader(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * onStartLoading() is called when a loader first starts loading data
     */
    @Override
    protected void onStartLoading() {
        if (mTaskData != null)
            // Delivers any previously loaded data immediately
            deliverResult(mTaskData);
        else
            // Force a new load
            forceLoad();
    }

    /**
     * loadInBackground() performs asynchronous loading of data
     */
    @Override
    public Cursor loadInBackground() {

        // Query and load all tasks data in the background.
        // Sort by priority.
        // Use a try/catch block to catch any errors in loading data.
        try {
            return mContext.getContentResolver().query(
                    TaskContract.TaskEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    TaskContract.TaskEntry.SORT_ORDER);

        } catch (Exception e) {
            Log.e(TAG, "Failed to asynchronously load data.");
            return null;
        }
    }

    /**
     * deliverResult(...) sends the result of the load, a Cursor, to the registered listener
     */
    @Override
    public void deliverResult(Cursor data) {
        mTaskData = data;
        super.deliverResult(data);
    }
}