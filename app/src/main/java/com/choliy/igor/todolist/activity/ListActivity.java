package com.choliy.igor.todolist.activity;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.choliy.igor.todolist.tool.ProjectConstants;
import com.choliy.igor.todolist.R;
import com.choliy.igor.todolist.tool.TaskCursorAdapter;
import com.choliy.igor.todolist.tool.TaskLoader;
import com.choliy.igor.todolist.data.TaskContract;
import com.choliy.igor.todolist.util.DialogUtils;
import com.choliy.igor.todolist.util.MenuUtils;
import com.choliy.igor.todolist.util.ReminderUtils;
import com.choliy.igor.todolist.util.TaskUtils;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

public class ListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TaskCursorAdapter.OnTaskClickListener,
        OnMenuItemClickListener,
        MenuUtils.OnCloseMenuListener {

    private boolean mMenuShown;
    private Cursor mCursor;
    private FloatingActionButton mFab;
    private TaskCursorAdapter mAdapter;
    private ContextMenuDialogFragment mContextMenu;
    private BroadcastReceiver mUpdateListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            restartLoader();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        setupUi(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mMenuShown) showMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (TaskUtils.sTaskAction) {
            case ProjectConstants.TASK_ACTION_ADD:
                TaskUtils.showSnackBar(TaskUtils.getView(this), R.string.info_task_added);
                break;
            case ProjectConstants.TASK_ACTION_UPDATE:
                TaskUtils.showSnackBar(TaskUtils.getView(this), R.string.info_task_updated);
                break;
        }

        TaskUtils.sListActivityActive = true;
        TaskUtils.sTaskAction = ProjectConstants.TASK_ACTION_NULL;
        ReminderUtils.sReminderTime = ProjectConstants.REMINDER_TIME_NULL;
        ReminderUtils.sReminderTimeTemp = ProjectConstants.REMINDER_TIME_NULL;

        restartLoader();
        registerReceiver(mUpdateListReceiver,
                new IntentFilter(getString(R.string.receiver_update_list)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        TaskUtils.sListActivityActive = false;
        TaskUtils.sTaskListAction = ProjectConstants.TASK_LIST_NULL;
        if (mMenuShown) mContextMenu.dismiss();
        if (mUpdateListReceiver != null) unregisterReceiver(mUpdateListReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showMenu();
                break;
            case R.id.menu_delete:
                deleteAllTasks();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ProjectConstants.MENU_KEY, mMenuShown);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (mMenuShown) mMenuShown = Boolean.FALSE;
        else super.onBackPressed();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {
        return new TaskLoader(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onTaskClick(int taskId) {
        Intent intent = new Intent(ListActivity.this, TaskActivity.class);
        Uri taskUri = ContentUris.withAppendedId(TaskContract.TaskEntry.CONTENT_URI, taskId);
        intent.setData(taskUri);
        startActivity(intent);
        finish();
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        MenuUtils.onMenuClicked(this, position, this);
    }

    @Override
    public void onCloseMenu() {
        mMenuShown = Boolean.FALSE;
    }

    private void setupUi(Bundle bundle) {
        if (bundle != null) mMenuShown = bundle.getBoolean(ProjectConstants.MENU_KEY);
        mContextMenu = MenuUtils.setupMenu(this);
        mContextMenu.setItemClickListener(this);

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addTaskIntent = new Intent(ListActivity.this, TaskActivity.class);
                startActivity(addTaskIntent);
                finish();
            }
        });

        mAdapter = new TaskCursorAdapter(this, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) mFab.hide();
                if (dy < 0) mFab.show();
            }
        });

        // Add a touch helper to the RecyclerView,
        // to recognize when a user swipes to delete an item.
        ItemTouchHelper touchHelper = new ItemTouchHelper(new OnSwipeCallback());
        touchHelper.attachToRecyclerView(recyclerView);

        // Ensure a loader is initialized and active. If the loader doesn't already exist,
        // one is created, otherwise the last created loader is re-used.
        getSupportLoaderManager().initLoader(ProjectConstants.TASK_LOADER_ID, null, this);

        // Restore Info dialog after rotating if he is shown already
        if (DialogUtils.sIsInfoDialogShown) DialogUtils.infoDialog(this);
    }

    private void deleteAllTasks() {
        if (mAdapter.getItemCount() == ProjectConstants.TASK_LIST_NULL) {
            try {
                TaskUtils.showSnackBar(TaskUtils.getView(this), R.string.info_list_already_empty);
            } catch (NullPointerException ex) {
                Log.e(ListActivity.class.getSimpleName(), ex.getMessage());
            }
        } else {
            mCursor = DialogUtils.deleteDialog(this, this, mFab);
        }
    }

    private void showMenu() {
        mMenuShown = Boolean.TRUE;
        mContextMenu.show(getSupportFragmentManager(), ListActivity.class.getSimpleName());
    }

    private void restartLoader() {
        getSupportLoaderManager().restartLoader(ProjectConstants.TASK_LOADER_ID, null, this);
    }

    private class OnSwipeCallback extends ItemTouchHelper.SimpleCallback {

        OnSwipeCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView,
                              RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            // Use getTag (from the adapter) to get the id of the swiped item
            int taskId = (int) viewHolder.itemView.getTag();

            // Build appropriate Uri with String row id appended
            String stringId = Integer.toString(taskId);
            Uri taskUri = TaskContract.TaskEntry.CONTENT_URI;
            taskUri = taskUri.buildUpon().appendPath(stringId).build();

            // Save data to Cursor before removing. For snackBar Undo method.
            mCursor = getContentResolver().query(
                    taskUri,
                    null,
                    null,
                    null,
                    TaskContract.TaskEntry.SORT_ORDER);

            // Delete a single row of data
            getContentResolver().delete(taskUri, null, null);

            // Save "Remove Action" & task position
            TaskUtils.sTaskListAction = ProjectConstants.TASK_LIST_REMOVE;
            TaskUtils.sTaskListPosition = viewHolder.getAdapterPosition();

            // Restart loader to re-query for all tasks after deletion
            restartLoader();

            // Show FAB for avoiding bugs
            mFab.show();

            // Show snackBar with possibility to restore task
            TaskUtils.restoreSingleTask(ListActivity.this, mCursor, ListActivity.this, mFab);
        }
    }
}