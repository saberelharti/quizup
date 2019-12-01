package net.dm73.quizitup.dao;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import static net.dm73.quizitup.dao.DataHelper.TABLE_1;

public class DaoQuiz {

    private DataHelper dataHelper;

    //Constructor
    public DaoQuiz(Context context) {
        this.dataHelper = new DataHelper(context);
    }

    //insert new passed quiz
    public void insertPassedQuiz(String quizId) {

        SQLiteDatabase db = dataHelper.getWritableDatabase();

        if (quizId != null) {
            // Create a new map of value
            ContentValues values = new ContentValues();
            values.put("quiz_id", quizId);

            //Insert the data
            db.insert(TABLE_1, null, values);
        }

        db.close();
    }

    //verify if the quiz in the list
    public boolean isPassed(String quizId) {

        Boolean isQuiz = false;
        SQLiteDatabase db = dataHelper.getReadableDatabase();

        if (quizId != null) {
            String query = "SELECT id FROM " + TABLE_1 + " WHERE quiz_id=" + quizId;

            Cursor cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                isQuiz = true;
            }

            cursor.close();
        }

        db.close();

        return isQuiz;
    }


}
