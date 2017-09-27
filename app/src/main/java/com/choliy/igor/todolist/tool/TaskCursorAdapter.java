package com.choliy.igor.todolist.tool;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.choliy.igor.todolist.R;
import com.choliy.igor.todolist.data.TaskContract;
import com.choliy.igor.todolist.util.TaskUtils;

public class TaskCursorAdapter extends RecyclerView.Adapter<TaskCursorAdapter.TaskViewHolder> {

    private Cursor mCursor;
    private Context mContext;
    private final OnTaskClickListener mClickListener;

    public TaskCursorAdapter(Context context, OnTaskClickListener clickListener) {
        mContext = context;
        mClickListener = clickListener;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        holder.bindView(position);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) return 0;
        return mCursor.getCount();
    }

    private void infoLayoutVisibility() {
        LinearLayout infoLayout =
                (LinearLayout) ((AppCompatActivity) mContext).findViewById(R.id.infoLayout);

        if (getItemCount() > 0)
            infoLayout.setVisibility(View.INVISIBLE);
        else
            infoLayout.setVisibility(View.VISIBLE);
    }

    /**
     * When data changes and a re-query occurs, this function swaps the old Cursor
     * with a newly updated Cursor (Cursor newCursor) that is passed in.
     */
    public void swapCursor(Cursor newCursor) {

        if (mCursor == newCursor) return;

        mCursor = newCursor;

        // Inform adapter about new data
        switch (TaskUtils.sTaskListAction) {
            case ProjectConstants.TASK_LIST_INSERT:
                notifyItemInserted(TaskUtils.sTaskListPosition);
                break;
            case ProjectConstants.TASK_LIST_REMOVE:
                notifyItemRemoved(TaskUtils.sTaskListPosition);
                break;
            case ProjectConstants.TASK_LIST_REMOVE_ALL:
                notifyDataSetChanged();
                break;
            case ProjectConstants.TASK_LIST_RESTORE_ALL:
                notifyItemRangeInserted(0, getItemCount());
                break;
            default:
                notifyDataSetChanged();
                break;
        }

        // Hide/Show info layout
        infoLayoutVisibility();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnLongClickListener {

        private TextView mTaskDescriptionView;
        private ImageView mReminderTask;
        private View mPriorityView;

        TaskViewHolder(View itemView) {
            super(itemView);
            mTaskDescriptionView = (TextView) itemView.findViewById(R.id.taskDescription);
            mReminderTask = (ImageView) itemView.findViewById(R.id.reminderTask);
            mPriorityView = itemView.findViewById(R.id.priorityView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int indexId = mCursor.getColumnIndex(TaskContract.TaskEntry._ID);
            mCursor.moveToPosition(getAdapterPosition());
            final int taskId = mCursor.getInt(indexId);

            // Set taskId to listener
            mClickListener.onTaskClick(taskId);
        }

        @Override
        public boolean onLongClick(View view) {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(
                    mContext.getString(R.string.app_name),
                    mTaskDescriptionView.getText().toString());
            clipboard.setPrimaryClip(clip);
            TaskUtils.showSnackBar(view, R.string.info_task_copied);
            return true;
        }

        private void bindView(int position) {

            int idIndex = mCursor.getColumnIndex(TaskContract.TaskEntry._ID);
            int descriptionIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_DESCRIPTION);
            int priorityIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_PRIORITY);
            int reminderIndex = mCursor.getColumnIndex(TaskContract.TaskEntry.COLUMN_REMINDER);

            // Get to the right position in the cursor
            mCursor.moveToPosition(position);

            // Get data from the cursor
            final int taskId = mCursor.getInt(idIndex);
            String description = mCursor.getString(descriptionIndex);
            int priority = mCursor.getInt(priorityIndex);
            long reminderTime = mCursor.getLong(reminderIndex);

            // Set values
            itemView.setTag(taskId);
            mTaskDescriptionView.setText(description);
            if (reminderTime > ProjectConstants.REMINDER_TIME_NULL)
                mReminderTask.setVisibility(View.VISIBLE);
            else
                mReminderTask.setVisibility(View.INVISIBLE);

            // Get the appropriate background color based on the priority
            GradientDrawable priorityCircle = (GradientDrawable) mPriorityView.getBackground();
            int priorityColor = getPriorityColor(priority);
            priorityCircle.setColor(priorityColor);
        }

        private int getPriorityColor(int priority) {
            int priorityColor = 0;
            switch (priority) {
                case ProjectConstants.PRIORITY_HIGH:
                    priorityColor = ContextCompat.getColor(mContext, R.color.colorHigh);
                    break;
                case ProjectConstants.PRIORITY_MEDIUM:
                    priorityColor = ContextCompat.getColor(mContext, R.color.colorMedium);
                    break;
                case ProjectConstants.PRIORITY_LOW:
                    priorityColor = ContextCompat.getColor(mContext, R.color.colorLow);
                    break;
                // For avoiding bugs in old versions of the app after updating
                case ProjectConstants.PRIORITY_DEFAULT:
                    priorityColor = ContextCompat.getColor(mContext, R.color.colorLow);
                default:
                    break;
            }
            return priorityColor;
        }
    }

    /**
     * Interface that receive onTaskClick
     */
    public interface OnTaskClickListener {

        void onTaskClick(int taskId);

    }
}