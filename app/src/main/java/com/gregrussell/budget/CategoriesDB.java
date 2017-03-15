package com.gregrussell.budget;

import android.provider.BaseColumns;

/**
 * Created by greg on 2/16/2017.
 */

public final class CategoriesDB {




    private CategoriesDB(){}

    /* Inner class that defines the table contents */
    public static class Categories implements BaseColumns {
        public static final String TABLE_NAME = "Categories";
        public static final String COLUMN_NAME_TITLE = "Categories";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
    }


    private static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + Categories.TABLE_NAME + " (" +
            Categories._ID + " INTEGER PRIMARY KEY," +
            Categories.COLUMN_NAME_TITLE + " TEXT)";



}
