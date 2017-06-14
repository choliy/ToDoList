package com.choliy.igor.todolist.util;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.choliy.igor.todolist.ProjectConstants;
import com.choliy.igor.todolist.R;
import com.choliy.igor.todolist.data.TaskContract;

import java.util.Calendar;

public final class DialogUtils {

    private static boolean sIsReminderAdded;
    public static boolean sIsInfoDialogShown;
    public static boolean sIsReminderDialogShown;

    private static void reminderDialog(final Context context, final long reminderTime) {

        final Calendar calendar = Calendar.getInstance();
        final View view = View.inflate(context, R.layout.dialog_reminder, null);
        final TextView reminderTitle = (TextView) view.findViewById(R.id.reminderTitle);
        final ImageView dateIcon = (ImageView) view.findViewById(R.id.dateIcon);
        final ImageView timeIcon = (ImageView) view.findViewById(R.id.timeIcon);
        final TextView dateText = (TextView) view.findViewById(R.id.dateText);
        final TextView timeText = (TextView) view.findViewById(R.id.timeText);

        // DatePickerDialog
        final DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                dateIcon.setImageResource(R.drawable.selector_date_set);
                ReminderUtils.sReminderTimeTemp = calendar.getTimeInMillis();
                TaskUtils.updateLabel(
                        context,
                        dateText,
                        true,
                        calendar.getTimeInMillis());
            }
        };

        dateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePicker = new DatePickerDialog(
                        context,
                        dateListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePicker.show();
            }
        });

        // TimePickerDialog
        final TimePickerDialog.OnTimeSetListener timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                timeIcon.setImageResource(R.drawable.selector_time_set);
                ReminderUtils.sReminderTimeTemp = calendar.getTimeInMillis();
                TaskUtils.updateLabel(
                        context,
                        timeText,
                        false,
                        calendar.getTimeInMillis());
            }
        };

        timeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePicker = new TimePickerDialog(
                        context,
                        timeListener,
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(context));
                timePicker.show();
            }
        });

        // ReminderDialog in edit mode
        if (reminderTime > ProjectConstants.REMINDER_TIME_NULL) {
            dateIcon.setImageResource(R.drawable.selector_date_set);
            timeIcon.setImageResource(R.drawable.selector_time_set);
            calendar.setTimeInMillis(reminderTime);
            TaskUtils.updateLabel(
                    context,
                    dateText,
                    true,
                    calendar.getTimeInMillis());
            TaskUtils.updateLabel(
                    context,
                    timeText,
                    false,
                    calendar.getTimeInMillis());
        }

        // ActionButton
        String actionButtonName;
        if (reminderTime > ProjectConstants.REMINDER_TIME_NULL && sIsReminderAdded) {
            reminderTitle.setText(context.getString(R.string.dialog_reminder_title_edit));
            actionButtonName = context.getString(R.string.button_update);
        } else {
            reminderTitle.setText(context.getString(R.string.dialog_reminder_title_add));
            actionButtonName = context.getString(R.string.button_add);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(actionButtonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                ReminderUtils.sReminderTime = calendar.getTimeInMillis();
                ReminderUtils.sReminderTimeTemp = ProjectConstants.REMINDER_TIME_NULL;
                TaskUtils.changeReminderSelector(context, true);
                sIsReminderAdded = true;

                String reminderAdded = context.getString(R.string.dialog_reminder_added);
                String reminderUpdated = context.getString(R.string.dialog_reminder_updated);
                String reminderDate = TaskUtils.formatDate(context, calendar.getTimeInMillis());

                String reminderInfoText;
                if (reminderTime > ProjectConstants.REMINDER_TIME_NULL)
                    reminderInfoText = reminderUpdated + reminderDate;
                else
                    reminderInfoText = reminderAdded + reminderDate;

                View view = ((AppCompatActivity) context).findViewById(R.id.activityTask);
                if (ReminderUtils.sReminderTime <= System.currentTimeMillis())
                    TaskUtils.showLongSnackBar(view, R.string.dialog_reminder_better_time);
                else
                    TaskUtils.showLongSnackBar(view, reminderInfoText);
            }
        });

        builder.setNegativeButton(R.string.dialog_info_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                ReminderUtils.sReminderTimeTemp = ProjectConstants.REMINDER_TIME_NULL;
                sIsReminderDialogShown = false;
            }
        });

        sIsReminderDialogShown = true;
        builder.setView(view);
        builder.show();
    }

    public static void infoDialog(final Context context) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View view = View.inflate(context, R.layout.dialog_info, null);
        final TextView developerUrl = (TextView) view.findViewById(R.id.developerUrl);

        int year = Calendar.getInstance().get(Calendar.YEAR);
        String text = context.getString(R.string.dialog_info_developer, String.valueOf(year));
        developerUrl.setText(text);

        developerUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String developerUrl = context.getString(R.string.dialog_linkedin_url);
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(developerUrl));
                if (webIntent.resolveActivity(context.getPackageManager()) != null)
                    context.startActivity(webIntent);
            }
        });

        builder.setPositiveButton(R.string.dialog_info_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sIsInfoDialogShown = false;
            }
        });

        sIsInfoDialogShown = true;
        builder.setView(view);
        builder.show();
    }

    public static Cursor deleteDialog(final Context context,
                                      final LoaderManager.LoaderCallbacks<Cursor> callbacks,
                                      final FloatingActionButton fab) {

        // Save all tasks before deleting for SnackBar UNDO method
        final Cursor cursor = context.getContentResolver().query(
                TaskContract.TaskEntry.CONTENT_URI,
                null,
                null,
                null,
                TaskContract.TaskEntry.SORT_ORDER);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_delete_message);
        builder.setPositiveButton(R.string.dialog_delete_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                // Delete all tasks
                context.getContentResolver().delete(
                        TaskContract.TaskEntry.CONTENT_URI,
                        null,
                        null);

                // Save "Remove All" action
                TaskUtils.sTaskListAction = ProjectConstants.TASK_LIST_REMOVE_ALL;

                // Restart the loader to re-query for all tasks after a deletion
                ((AppCompatActivity) context).getSupportLoaderManager().restartLoader(
                        ProjectConstants.TASK_LOADER_ID,
                        null,
                        callbacks);

                // Close current dialog
                dialog.dismiss();

                // Show FAB for avoiding bugs
                fab.show();

                // Show snackBar with possibility to restore all tasks
                TaskUtils.restoreAllTasks(context, cursor, callbacks);
            }
        });

        builder.setNegativeButton(R.string.dialog_delete_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.show();

        return cursor;
    }

    public static void unsavedChangesDialog(final Context context,
                                            final long reminderTime,
                                            final Uri taskUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.dialog_unsaved_message);
        builder.setPositiveButton(R.string.dialog_unsaved_discard,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (taskUri != null) {
                            ContentValues values = new ContentValues();
                            values.put(TaskContract.TaskEntry.COLUMN_REMINDER, reminderTime);
                            context.getContentResolver().update(taskUri, values, null, null);
                        }
                        TaskUtils.finishTaskActivity(context, ProjectConstants.TASK_ACTION_NULL);
                    }
                });
        builder.setNegativeButton(R.string.dialog_unsaved_keep_editing,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public static void showReminderDialog(Context context) {
        if (ReminderUtils.sReminderTime > ProjectConstants.REMINDER_TIME_NULL
                && ReminderUtils.sReminderTimeTemp == ProjectConstants.REMINDER_TIME_NULL)
            DialogUtils.reminderDialog(context, ReminderUtils.sReminderTime);
        else if (ReminderUtils.sReminderTimeTemp != ReminderUtils.sReminderTime)
            DialogUtils.reminderDialog(context, ReminderUtils.sReminderTimeTemp);
        else
            DialogUtils.reminderDialog(context, ProjectConstants.REMINDER_TIME_NULL);
    }

    public static void isReminderAdded() {
        sIsReminderAdded = ReminderUtils.sReminderTime > ProjectConstants.REMINDER_TIME_NULL;
    }
}