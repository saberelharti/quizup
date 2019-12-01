package net.dm73.quizitup.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataHelper extends SQLiteOpenHelper {


    private static final String DB_NAME = "dataQuiz.db";
    public static final String TABLE_1 = "quiz";
    public static final String TABLE_2 = "category";
    private static final int VERSION = 2;


    public DataHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Creating the table "QUIZ"
        String sql_1 = String.format("create table %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT UNIQUE)", TABLE_1, "id", "quiz_id");
        db.execSQL(sql_1);

        //Creating the table "Category"
        String sql_2 = String.format("create table %s (%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT)", TABLE_2, "id", "name", "image");
        db.execSQL(sql_2);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //alter the schema of data base
        if (oldVersion != newVersion) {
            //Alter the table "Category"
            String sql_2 = String.format("alter table %s add column %s TEXT", TABLE_2, "image");
            db.execSQL(sql_2);
        }
    }

}