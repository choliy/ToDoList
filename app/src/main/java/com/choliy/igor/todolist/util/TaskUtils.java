package com.choliy.igor.todolist.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;

import com.choliy.igor.todolist.ProjectConstants;
import com.choliy.igor.todolist.R;
import com.choliy.igor.todolist.activity.ListActivity;
import com.choliy.igor.todolist.data.TaskContract;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class TaskUtils {

    // Variable for showing info about task action
    public static int sTaskAction;

    // Variables for informing adapted about taskItem changes
    public static int sTaskListAction;
    public static int sTaskListPosition;

    // Variable for updating UI, depends on witch activity is active
    public static boolean sListActivityActive;
    public static boolean sTaskActivityActive;

    private static ContentValues[] getTasksFromCursor(Cursor cursor) {
        ContentValues[] tasks = new ContentValues[cursor.getCount()];
        int task = 0;

        int indexId = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
        int indexDescription = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION);
        int indexPriority = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY);
        int indexTimestamp = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TIMESTAMP);
        int indexReminder = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_REMINDER);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry._ID, cursor.getInt(indexId));
            values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, cursor.getString(indexDescription));
            values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, cursor.getInt(indexPriority));
            values.put(TaskContract.TaskEntry.COLUMN_TIMESTAMP, cursor.getLong(indexTimestamp));
            values.put(TaskContract.TaskEntry.COLUMN_REMINDER, cursor.getLong(indexReminder));

            tasks[task] = values;
            task = ++task;
        }

        return tasks;
    }

    public static void setButtonBackground(Context context, int priority) {
        TextView buttonHigh = (TextView) ((Activity) context).findViewById(R.id.buttonHigh);
        TextView buttonMedium = (TextView) ((Activity) context).findViewById(R.id.buttonMedium);
        TextView buttonLow = (TextView) ((Activity) context).findViewById(R.id.buttonLow);

        GradientDrawable priorityHigh = (GradientDrawable) buttonHigh.getBackground();
        GradientDrawable priorityMedium = (GradientDrawable) buttonMedium.getBackground();
        GradientDrawable priorityLow = (GradientDrawable) buttonLow.getBackground();

        final int colorHigh = ContextCompat.getColor(context, R.color.colorHigh);
        final int colorMedium = ContextCompat.getColor(context, R.color.colorMedium);
        final int colorLow = ContextCompat.getColor(context, R.color.colorLow);
        final int colorDefault = ContextCompat.getColor(context, R.color.colorDefault);

        switch (priority) {
            case ProjectConstants.PRIORITY_HIGH:
                priorityHigh.setColor(colorHigh);
                priorityMedium.setColor(colorDefault);
                priorityLow.setColor(colorDefault);
                break;
            case ProjectConstants.PRIORITY_MEDIUM:
                priorityHigh.setColor(colorDefault);
                priorityMedium.setColor(colorMedium);
                priorityLow.setColor(colorDefault);
                break;
            case ProjectConstants.PRIORITY_LOW:
                priorityHigh.setColor(colorDefault);
                priorityMedium.setColor(colorDefault);
                priorityLow.setColor(colorLow);
                break;
            // For avoiding bugs in old versions of the app after updating
            case ProjectConstants.PRIORITY_DEFAULT:
                priorityHigh.setColor(colorDefault);
                priorityMedium.setColor(colorDefault);
                priorityLow.setColor(colorLow);
                break;
        }
    }

    public static void changeReminderSelector(Context context, boolean isReminderOn) {
        TextView reminderButton = (TextView) ((AppCompatActivity) context).findViewById(R.id.buttonReminder);
        if (isReminderOn) {
            reminderButton.setText(R.string.button_notification_on);
            reminderButton.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.selector_notification_on));
        } else {
            reminderButton.setText(R.string.button_notification_off);
            reminderButton.setBackground(
                    ContextCompat.getDrawable(context, R.drawable.selector_notification_off));
        }
    }

    public static void finishTaskActivity(Context context, int action) {
        Intent intent = new Intent(context, ListActivity.class);
        TaskUtils.sTaskAction = action;
        context.startActivity(intent);
        ((AppCompatActivity) context).finish();
    }

    public static void restoreSingleTask(final Context context,
                                         final Cursor cursor,
                                         final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                         final FloatingActionButton fab) {

        String text = context.getString(R.string.info_task_removed);
        Snackbar snackbar = Snackbar.make(getView(context), text, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.snack_bar_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get data from the Cursor
                int indexId = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
                int indexDescription = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION);
                int indexPriority = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY);
                int indexTimestamp = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_TIMESTAMP);
                int indexReminder = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_REMINDER);

                if (cursor.moveToFirst()) {
                    ContentValues values = new ContentValues();
                    values.put(TaskContract.TaskEntry._ID, cursor.getInt(indexId));
                    values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, cursor.getString(indexDescription));
                    values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, cursor.getInt(indexPriority));
                    values.put(TaskContract.TaskEntry.COLUMN_TIMESTAMP, cursor.getLong(indexTimestamp));
                    values.put(TaskContract.TaskEntry.COLUMN_REMINDER, cursor.getLong(indexReminder));

                    // Add current task to DB
                    context.getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, values);
                }

                // Save "Insert Action"
                TaskUtils.sTaskListAction = ProjectConstants.TASK_LIST_INSERT;

                // Restart loader to re-query for all tasks after insert operation
                ((AppCompatActivity) context).getSupportLoaderManager()
                        .restartLoader(ProjectConstants.TASK_LOADER_ID, null, callbacks);

                // Show FAB for avoiding bugs
                fab.show();

                // Show info SnackBar
                TaskUtils.showSnackBar(view, R.string.info_task_restored);
            }
        });
        snackbar.show();
    }

    public static View getView(Context context) {
        return ((AppCompatActivity) context).findViewById(R.id.activityList);
    }

    public static void showSnackBar(View view, int textId) {
        Snackbar.make(view, textId, Snackbar.LENGTH_SHORT).show();
    }

    static void showSnackBar(View view, String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
    }

    static void showLongSnackBar(View view, int textId) {
        Snackbar.make(view, textId, Snackbar.LENGTH_LONG).show();
    }

    static void restoreAllTasks(final Context context,
                                final Cursor cursor,
                                final LoaderManager.LoaderCallbacks<Cursor> callbacks) {

        String text = context.getString(R.string.info_tasks_removed);
        Snackbar snackbar = Snackbar.make(getView(context), text, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.snack_bar_undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Restore all tasks form cursor
                ContentValues[] tasks = getTasksFromCursor(cursor);

                // Add all tasks to DB
                context.getContentResolver().bulkInsert(TaskContract.TaskEntry.CONTENT_URI, tasks);

                // Save "Restore All Action"
                TaskUtils.sTaskListAction = ProjectConstants.TASK_LIST_RESTORE_ALL;

                // Restart loader to re-query for all tasks after insert operations
                ((AppCompatActivity) context).getSupportLoaderManager()
                        .restartLoader(ProjectConstants.TASK_LOADER_ID, null, callbacks);

                // Show info snackBar
                TaskUtils.showSnackBar(view, R.string.info_tasks_restored);
            }
        });
        snackbar.show();
    }

    static void updateLabel(Context context,
                            TextView textView,
                            boolean isItDate,
                            long milliseconds) {

        SimpleDateFormat sdf;
        if (isItDate) {
            sdf = new SimpleDateFormat(ProjectConstants.DATE_FORMAT, Locale.UK);
        } else {
            if (DateFormat.is24HourFormat(context))
                sdf = new SimpleDateFormat(ProjectConstants.TIME_FORMAT_UK, Locale.UK);
            else
                sdf = new SimpleDateFormat(ProjectConstants.TIME_FORMAT_US, Locale.US);
        }

        textView.setText(sdf.format(milliseconds));
        textView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
    }

    static String formatDate(Context context, long milliseconds) {
        SimpleDateFormat sdf;
        if (DateFormat.is24HourFormat(context))
            sdf = new SimpleDateFormat(ProjectConstants.INFO_DATE_FORMAT_UK, Locale.UK);
        else
            sdf = new SimpleDateFormat(ProjectConstants.INFO_DATE_FORMAT_US, Locale.US);

        return sdf.format(milliseconds);
    }
}