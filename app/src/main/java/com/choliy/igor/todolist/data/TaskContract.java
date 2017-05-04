package com.choliy.igor.todolist.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class TaskContract {

    /*
     Instruction for Contract class:

     Add ContentProvider constants to the Contract.
     Clients need to know how to access the task data, and it's your job to provide
     these content URI's for the path to that data:
        1) Content authority,
        2) Base content URI,
        3) Path(s) to the tasks directory
        4) Content URI for data in the TaskEntry class
     */

    // The authority, which is how your code knows which Content Provider to access
    static final String AUTHORITY = "com.choliy.igor.todolist";

    // The base content URI = "content://" + <authority>
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract.
    // This is the path for the "tasks" directory.
    static final String PATH_TASKS = "tasks";

    /**
     * TaskEntry is an inner class that defines the contents of the task table
     */
    public static final class TaskEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_TASKS);

        // The MIME type of the CONTENT_URI for a single task
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + ".provider." + PATH_TASKS;

        // The MIME type of the CONTENT_URI for a list of tasks
        static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + ".provider." + PATH_TASKS;

        // Task table and columns names
        static final String TABLE_NAME = PATH_TASKS;

        // Since TaskEntry implements the interface "BaseColumns",
        // it has an automatically produced "_ID" column in addition to the two below
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PRIORITY = "priority";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_REMINDER = "reminder";

        // Constant for sortOrder by two parameters
        public static final String SORT_ORDER = COLUMN_PRIORITY + " ASC, " + COLUMN_TIMESTAMP + " DESC";
    }
}