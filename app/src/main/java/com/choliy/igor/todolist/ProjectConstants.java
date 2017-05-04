package com.choliy.igor.todolist;

/**
 * Class for keeping useful constants for To-Do List project
 */
public interface ProjectConstants {

    // Task constants
    String TASK_ID_KEY = "taskIdKey";
    String TASK_TEXT_KEY = "taskTextKey";
    String TASK_REMINDER_KEY = "taskReminderKey";
    String TASK_PRIORITY_KEY = "taskPriorityKey";

    // Priority key constants
    String PRIORITY_KEY = "priorityKey";
    String PRIORITY_CHANGED_KEY = "priorityChangedKey";

    // Date & Time format constants
    String DATE_FORMAT = "d MMMM yyyy";
    String TIME_FORMAT_UK = "HH:mm";
    String TIME_FORMAT_US = "h:mm a";
    String INFO_DATE_FORMAT_UK = DATE_FORMAT + " (" + TIME_FORMAT_UK + ")";
    String INFO_DATE_FORMAT_US = DATE_FORMAT + " (" + TIME_FORMAT_US + ")";

    // Default constants
    String DESCRIPTION_EMPTY = "";
    long REMINDER_TIME_NULL = 0;

    // Unique ID's constants
    int TASK_LOADER_ID = 111;
    int PENDING_INTENT_ID = 333;

    // Task priority constants
    int PRIORITY_HIGH = 1;
    int PRIORITY_MEDIUM = 2;
    int PRIORITY_LOW = 3;
    int PRIORITY_DEFAULT = 4;

    // Activity action constants
    int TASK_ACTION_NULL = 100;
    int TASK_ACTION_ADD = 101;
    int TASK_ACTION_UPDATE = 102;

    // List action constants
    int TASK_LIST_NULL = 0;
    int TASK_LIST_INSERT = -1;
    int TASK_LIST_REMOVE = -2;
    int TASK_LIST_REMOVE_ALL = -3;
    int TASK_LIST_RESTORE_ALL = -4;
}