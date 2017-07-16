package com.choliy.igor.todolist.util;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.choliy.igor.todolist.tool.ProjectConstants;
import com.choliy.igor.todolist.R;
import com.choliy.igor.todolist.tool.ReminderService;
import com.choliy.igor.todolist.activity.ListActivity;

public final class ReminderUtils {

    public static long sReminderId;
    public static long sReminderTime;
    public static long sReminderTimeTemp;

    private static PendingIntent contentIntent(Context context) {
        Intent intent = new Intent(context, ListActivity.class);

        return PendingIntent.getActivity(
                context,
                ProjectConstants.PENDING_INTENT_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.drawable.ic_notification);
    }

    public static void showReminderNotification(Context context,
                                                String contentText,
                                                int taskId,
                                                int priority) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setLargeIcon(largeIcon(context));
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(contentText);
        builder.setDefaults(Notification.PRIORITY_HIGH);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setDefaults(Notification.DEFAULT_VIBRATE);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setContentIntent(contentIntent(context));
        builder.setAutoCancel(false);

        Uri soundUri = Uri.parse("android.resource://"
                + context.getPackageName() + "/"
                + R.raw.reminder_sound);
        builder.setSound(soundUri);

        switch (priority) {
            case ProjectConstants.PRIORITY_HIGH:
                builder.setColor(ContextCompat.getColor(context, R.color.colorHigh));
                break;
            case ProjectConstants.PRIORITY_MEDIUM:
                builder.setColor(ContextCompat.getColor(context, R.color.colorMedium));
                break;
            case ProjectConstants.PRIORITY_LOW:
                builder.setColor(ContextCompat.getColor(context, R.color.colorLow));
                break;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(taskId, builder.build());
    }

    public static void addAlarm(Context context,
                                long when,
                                String reminderText,
                                int taskId,
                                int priority) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, ReminderService.class);
        intent.putExtra(ProjectConstants.TASK_ID_KEY, taskId);
        intent.putExtra(ProjectConstants.TASK_TEXT_KEY, reminderText);
        intent.putExtra(ProjectConstants.TASK_PRIORITY_KEY, priority);

        PendingIntent pendingIntent = PendingIntent.getService(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo clockInfo = new AlarmManager.AlarmClockInfo(when, pendingIntent);
            alarmManager.setAlarmClock(clockInfo, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, when, pendingIntent);
        }
    }
}