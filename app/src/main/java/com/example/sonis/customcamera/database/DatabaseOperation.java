package com.example.sonis.customcamera.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SONI's on 7/11/2016.
 */
public class DatabaseOperation extends SQLiteOpenHelper {

    String query = "create table "+ DatabaseConstant.TABLE_FLASH + "("+ DatabaseConstant.COLUMN_FLASH +");";
    String insertQuery = "insert into "+DatabaseConstant.TABLE_FLASH+"("+DatabaseConstant.COLUMN_FLASH+") values "+"("+1+")";
    Context context;
    public DatabaseOperation(Context context) {
        super(context, DatabaseConstant.TABLE_FLASH, null, DatabaseConstant.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(query);
        db.execSQL(insertQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void updateFlashState(DatabaseOperation databaseOperation, int state)
    {
        SQLiteDatabase sqLiteDatabase = databaseOperation.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseConstant.COLUMN_FLASH,state);
        sqLiteDatabase.update(DatabaseConstant.TABLE_FLASH,contentValues,null,null);

    }

    public Cursor getFlashState(DatabaseOperation databaseOperation)
    {
        SQLiteDatabase sqLiteDatabase = databaseOperation.getReadableDatabase();
        String query = "select * from "+DatabaseConstant.TABLE_FLASH;
        Cursor cr = sqLiteDatabase.rawQuery(query,null);
        return cr;

    }
}
