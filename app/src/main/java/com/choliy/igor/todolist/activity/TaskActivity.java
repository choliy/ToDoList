package com.choliy.igor.todolist.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.choliy.igor.todolist.ProjectConstants;
import com.choliy.igor.todolist.R;
import com.choliy.igor.todolist.data.TaskContract;
import com.choliy.igor.todolist.util.DialogUtils;
import com.choliy.igor.todolist.util.ReminderUtils;
import com.choliy.igor.todolist.util.TaskUtils;

public class TaskActivity extends AppCompatActivity implements View.OnClickListener {

    private Uri mTaskUri;
    private TextView mButtonAction;
    private EditText mEditTextDescription;
    private BroadcastReceiver mUpdateTaskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long reminderId = intent.getLongExtra(ProjectConstants.TASK_ID_KEY, 0);
            if (reminderId == ReminderUtils.sReminderId) {
                dismissReminder(findViewById(R.id.activityTask));
                mSavedReminderTime = ReminderUtils.sReminderTime;
            }
        }
    };

    // Variables for handle changes for unsaved changes dialog
    private long mSavedReminderTime;
    private int mPriority = ProjectConstants.PRIORITY_LOW;
    private int mPriorityChanged = ProjectConstants.PRIORITY_LOW;
    private String mSavedDescription = ProjectConstants.DESCRIPTION_EMPTY;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        setupUi(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TaskUtils.sTaskActivityActive = true;
        registerReceiver(mUpdateTaskReceiver,
                new IntentFilter(getString(R.string.receiver_update_task)));

        if (ReminderUtils.sReminderTime > ProjectConstants.REMINDER_TIME_NULL)
            TaskUtils.changeReminderSelector(this, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TaskUtils.sTaskActivityActive = false;
        unregisterReceiver(mUpdateTaskReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_done:
                addOrUpdateTask();
                return true;
            case android.R.id.home:
                return checkActionAndFinish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        checkActionAndFinish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(ProjectConstants.PRIORITY_KEY, mPriority);
        outState.putInt(ProjectConstants.PRIORITY_CHANGED_KEY, mPriorityChanged);
        outState.putLong(ProjectConstants.TASK_REMINDER_KEY, mSavedReminderTime);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonReminder:
                DialogUtils.showReminderDialog(this);
                break;
            case R.id.buttonHigh:
                mPriority = ProjectConstants.PRIORITY_HIGH;
                TaskUtils.setButtonBackground(this, mPriority);
                break;
            case R.id.buttonMedium:
                mPriority = ProjectConstants.PRIORITY_MEDIUM;
                TaskUtils.setButtonBackground(this, mPriority);
                break;
            case R.id.buttonLow:
                mPriority = ProjectConstants.PRIORITY_LOW;
                TaskUtils.setButtonBackground(this, mPriority);
                break;
            case R.id.buttonAction:
                addOrUpdateTask();
                break;
        }
    }

    private void setupUi(Bundle savedInstanceState) {
        setViewsAndListeners();
        TaskUtils.setButtonBackground(this, mPriority);

        Intent intent = getIntent();
        mTaskUri = intent.getData();

        if (mTaskUri == null) {
            setTitle(R.string.activity_name_add_task);
            mButtonAction.setText(R.string.button_add);
            DialogUtils.isReminderAdded();
        } else {
            setTitle(R.string.activity_name_edit_task);
            mButtonAction.setText(R.string.button_update);
            loadTask();
            DialogUtils.isReminderAdded();
        }

        if (savedInstanceState != null) {
            mPriority = savedInstanceState.getInt(ProjectConstants.PRIORITY_KEY);
            mPriorityChanged = savedInstanceState.getInt(ProjectConstants.PRIORITY_CHANGED_KEY);
            mSavedReminderTime = savedInstanceState.getLong(ProjectConstants.TASK_REMINDER_KEY);
            TaskUtils.setButtonBackground(this, mPriority);
        }

        // Restore Reminder dialog after rotating if he is shown already
        if (DialogUtils.sIsReminderDialogShown) DialogUtils.showReminderDialog(this);
    }

    private void loadTask() {
        Cursor cursor = getContentResolver().query(
                mTaskUri,
                null,
                null,
                null,
                TaskContract.TaskEntry.SORT_ORDER);

        assert cursor != null;
        int idIndex = cursor.getColumnIndex(TaskContract.TaskEntry._ID);
        int descriptionIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION);
        int priorityIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY);
        int reminderIndex = cursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_REMINDER);

        // Get data from the cursor
        if (cursor.moveToFirst()) {
            ReminderUtils.sReminderId = cursor.getLong(idIndex);
            String description = cursor.getString(descriptionIndex);
            mPriority = cursor.getInt(priorityIndex);

            // For avoiding bugs in old versions of the app after updating
            if (mPriority == ProjectConstants.PRIORITY_DEFAULT)
                mPriority = ProjectConstants.PRIORITY_LOW;

            if (ReminderUtils.sReminderTime > ProjectConstants.REMINDER_TIME_NULL)
                mSavedReminderTime = ReminderUtils.sReminderTime;
            else
                ReminderUtils.sReminderTime = cursor.getLong(reminderIndex);

            // Bind data
            mEditTextDescription.setText(description);
            TaskUtils.setButtonBackground(this, mPriority);

            // Place EditText cursor at the end of text
            int index = mEditTextDescription.getText().length();
            mEditTextDescription.setSelection(index);

            // Save data to variables for unsaved changes dialog
            mSavedDescription = description;
            mPriorityChanged = mPriority;
            mSavedReminderTime = ReminderUtils.sReminderTime;
        }
        cursor.close();
    }

    private Uri addTask() {
        if (getValues() == null) return null;
        Uri taskUri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, getValues());
        TaskUtils.finishTaskActivity(this, ProjectConstants.TASK_ACTION_ADD);
        return taskUri;
    }

    private void updateTask() {
        if (getValues() == null) return;
        getContentResolver().update(mTaskUri, getValues(), null, null);
        TaskUtils.finishTaskActivity(this, ProjectConstants.TASK_ACTION_UPDATE);
    }

    private ContentValues getValues() {
        String description = mEditTextDescription.getText().toString();
        if (description.length() == 0) {
            TaskUtils.showSnackBar(findViewById(R.id.activityTask), R.string.info_empty_title);
            return null;
        } else {
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, description);
            values.put(TaskContract.TaskEntry.COLUMN_PRIORITY, mPriority);
            values.put(TaskContract.TaskEntry.COLUMN_TIMESTAMP, System.currentTimeMillis());
            values.put(TaskContract.TaskEntry.COLUMN_REMINDER, ReminderUtils.sReminderTime);
            return values;
        }
    }

    private boolean dismissReminder(View view) {
        ReminderUtils.sReminderTime = ProjectConstants.REMINDER_TIME_NULL;
        if (mTaskUri != null) {
            ContentValues values = new ContentValues();
            values.put(TaskContract.TaskEntry.COLUMN_REMINDER, ReminderUtils.sReminderTime);
            getContentResolver().update(mTaskUri, values, null, null);
        }
        TaskUtils.changeReminderSelector(this, false);
        TaskUtils.showSnackBar(view, R.string.dialog_reminder_dismiss);
        return true;
    }

    private void addOrUpdateTask() {
        Uri uri;
        if (mTaskUri == null) {
            uri = addTask();
            addOrUpdateReminder(uri);
        } else {
            updateTask();
            uri = mTaskUri;
            addOrUpdateReminder(uri);
        }
    }

    private void addOrUpdateReminder(Uri uri) {
        boolean timeNotNull = ReminderUtils.sReminderTime > ProjectConstants.REMINDER_TIME_NULL;
        boolean descriptionNull = mEditTextDescription.getText().toString()
                .equals(ProjectConstants.DESCRIPTION_EMPTY);

        if (timeNotNull && !descriptionNull) {
            String reminderText = mEditTextDescription.getText().toString();
            int taskId = (int) ContentUris.parseId(uri);
            ReminderUtils.addAlarm(
                    this.getApplicationContext(),
                    ReminderUtils.sReminderTime,
                    reminderText,
                    taskId,
                    mPriority);
        }
    }

    private boolean checkActionAndFinish() {
        boolean textChanged = mSavedDescription.equals(mEditTextDescription.getText().toString());
        boolean reminderChanged = mSavedReminderTime == ReminderUtils.sReminderTime;
        if (mPriorityChanged != mPriority || !textChanged || !reminderChanged)
            DialogUtils.unsavedChangesDialog(this, mSavedReminderTime, mTaskUri);
        else TaskUtils.finishTaskActivity(this, ProjectConstants.TASK_ACTION_NULL);

        return true;
    }

    private void setViewsAndListeners() {
        mEditTextDescription = (EditText) findViewById(R.id.editTextDescription);
        mButtonAction = (TextView) findViewById(R.id.buttonAction);
        TextView buttonHigh = (TextView) findViewById(R.id.buttonHigh);
        TextView buttonMedium = (TextView) findViewById(R.id.buttonMedium);
        TextView buttonLow = (TextView) findViewById(R.id.buttonLow);
        TextView buttonReminder = (TextView) findViewById(R.id.buttonReminder);

        mButtonAction.setOnClickListener(this);
        buttonHigh.setOnClickListener(this);
        buttonMedium.setOnClickListener(this);
        buttonLow.setOnClickListener(this);
        buttonReminder.setOnClickListener(this);
        buttonReminder.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return dismissReminder(view);
            }
        });
    }
}