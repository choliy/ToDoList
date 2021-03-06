package com.choliy.igor.todolist.util;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import com.choliy.igor.todolist.R;
import com.choliy.igor.todolist.tool.ProjectConstants;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;

import java.util.ArrayList;
import java.util.List;

public final class MenuUtils {

    public interface OnCloseMenuListener {
        void onCloseMenu();
    }

    public static ContextMenuDialogFragment setupMenu(Context context) {
        MenuObject closeMenu = new MenuObject();
        closeMenu.setResource(R.drawable.ic_close);

        MenuObject aboutMenu = new MenuObject(context.getString(R.string.menu_about));
        aboutMenu.setResource(R.drawable.ic_about);

        MenuObject shareMenu = new MenuObject(context.getString(R.string.menu_share));
        shareMenu.setResource(R.drawable.ic_share);

        MenuObject emailMenu = new MenuObject(context.getString(R.string.menu_email));
        emailMenu.setResource(R.drawable.ic_email);

        MenuObject feedbackMenu = new MenuObject(context.getString(R.string.menu_feedback));
        feedbackMenu.setResource(R.drawable.ic_feedback);

        MenuObject appsMenu = new MenuObject(context.getString(R.string.menu_more_apps));
        appsMenu.setResource(R.drawable.ic_apps);

        List<MenuObject> menuObjects = new ArrayList<>();
        menuObjects.add(closeMenu);
        menuObjects.add(aboutMenu);
        menuObjects.add(shareMenu);
        menuObjects.add(emailMenu);
        menuObjects.add(feedbackMenu);
        menuObjects.add(appsMenu);

        for (MenuObject menuObject : menuObjects) {
            menuObject.setDividerColor(R.color.colorPrimaryDark);
            menuObject.setBgResource(R.color.colorPrimary);
        }

        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize(getActionBarHeight(context));
        menuParams.setMenuObjects(menuObjects);
        menuParams.setClosableOutside(Boolean.FALSE);

        return ContextMenuDialogFragment.newInstance(menuParams);
    }

    public static void onMenuClicked(Context context, int position, OnCloseMenuListener listener) {
        switch (position) {
            case ProjectConstants.MENU_CLOSE:
                listener.onCloseMenu();
                break;
            case ProjectConstants.MENU_ABOUT:
                DialogUtils.infoDialog(context);
                listener.onCloseMenu();
                break;
            case ProjectConstants.MENU_SHARE:
                IntentUtils.shareIntent((AppCompatActivity) context);
                listener.onCloseMenu();
                break;
            case ProjectConstants.MENU_EMAIL:
                IntentUtils.emailIntent(context);
                listener.onCloseMenu();
                break;
            case ProjectConstants.MENU_FEEDBACK:
                IntentUtils.feedbackIntent(context);
                listener.onCloseMenu();
                break;
            case ProjectConstants.MENU_MORE_APPS:
                IntentUtils.appsIntent(context);
                listener.onCloseMenu();
                break;
        }
    }

    private static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, Boolean.TRUE))
            return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        else
            return Math.round(context.getResources().getDimension(R.dimen.priority_size));
    }
}