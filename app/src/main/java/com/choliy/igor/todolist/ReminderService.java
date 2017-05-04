package com.choliy.igor.todolist;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.choliy.igor.todolist.data.TaskContract;
import com.choliy.igor.todolist.util.ReminderUtils;
import com.choliy.igor.todolist.util.TaskUtils;

public class ReminderService extends IntentService {

    public ReminderService() {
        super(ReminderService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String reminderText = intent.getStringExtra(ProjectConstants.TASK_TEXT_KEY);
        int priority = intent.getIntExtra(ProjectConstants.TASK_PRIORITY_KEY, 0);
        int taskId = intent.getIntExtra(ProjectConstants.TASK_ID_KEY, 0);

        Uri taskUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, taskId);
        Cursor cursor = getContentResolver().query(taskUri, null, null, null, null);

        if (cursor == null) return;

        if (cursor.moveToFirst()) {
            int reminderIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_REMINDER);
            long reminderTime = cursor.getLong(reminderIndex);

            if (reminderTime != ProjectConstants.REMINDER_TIME_NULL) {
                ReminderUtils.showReminderNotification(
                        this.getApplicationContext(),
                        reminderText,
                        taskId,
                        priority);

                // Set reminder time to 0 after it'll shown
                ContentValues values = new ContentValues();
                values.put(TaskContract.TaskEntry.COLUMN_REMINDER, ProjectConstants.REMINDER_TIME_NULL);
                getContentResolver().update(taskUri, values, null, null);

                // Update ListActivity after showing reminder notification
                // if it is already visible.
                if (TaskUtils.sListActivityActive)
                    updateIntent(ProjectConstants.REMINDER_TIME_NULL,
                            getString(R.string.receiver_update_list));

                // Update TaskActivity after showing reminder notification
                // if it is already visible.
                if (TaskUtils.sTaskActivityActive)
                    updateIntent(taskId,
                            getString(R.string.receiver_update_task));
            }
        }

        cursor.close();
    }

    private void updateIntent(long reminderId, String action) {
        Intent updateIntent = new Intent();
        updateIntent.setAction(action);
        updateIntent.putExtra(ProjectConstants.TASK_ID_KEY, reminderId);
        sendBroadcast(updateIntent);
    }
}