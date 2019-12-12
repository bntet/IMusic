package com.example.imusic.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecordsSqliteHelper extends SQLiteOpenHelper {
    private String CREATE_RECORDS_TABLE = "create table table_records(id integer primary key autoincrement,name varchar(200))";
    private static String name = "records.db";
    private static int version = 1;
    public RecordsSqliteHelper(Context context){
        super(context,name,null,version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_RECORDS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
