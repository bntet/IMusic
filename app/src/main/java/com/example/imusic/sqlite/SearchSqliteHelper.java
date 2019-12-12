package com.example.imusic.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SearchSqliteHelper extends SQLiteOpenHelper {
    private String CREATE_SEARCH_TABLE = "create table table_search(id integer primary key autoincrement,position int(200),musicName varchar(200),musicArtist varchar(200))";
    private static String name = "search.db";
    private static int version = 1;
    public SearchSqliteHelper(Context context) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_SEARCH_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
