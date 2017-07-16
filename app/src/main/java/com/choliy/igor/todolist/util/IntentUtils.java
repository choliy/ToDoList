package com.choliy.igor.todolist.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ShareCompat;

import com.choliy.igor.todolist.R;

final class IntentUtils {

    static void shareIntent(Activity activity) {
        ShareCompat.IntentBuilder
                .from(activity)
                .setType("text/plain")
                .setChooserTitle(activity.getString(R.string.app_name))
                .setText(activity.getString(R.string.menu_app_url))
                .startChooser();
    }

    static void emailIntent(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + context.getString(R.string.menu_text_email)));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.menu_text_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.menu_text_hello));
        checkIntentBeforeLaunching(context, emailIntent);
    }

    static void feedbackIntent(Context context) {
        String url = context.getString(R.string.menu_app_url);
        Intent feedbackIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        checkIntentBeforeLaunching(context, feedbackIntent);
    }

    static void appsIntent(Context context) {
        String url = context.getString(R.string.menu_apps_url);
        Intent appsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        checkIntentBeforeLaunching(context, appsIntent);
    }

    private static void checkIntentBeforeLaunching(Context context, Intent intent) {
        if (intent.resolveActivity(context.getPackageManager()) != null)
            context.startActivity(intent);
    }
}