package net.dm73.quizitup.dao;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.dm73.quizitup.model.Category;

import java.util.ArrayList;
import java.util.List;

import static net.dm73.quizitup.dao.DataHelper.TABLE_2;

public class DaoCategory {

    private DataHelper dataHelper;

    public DaoCategory(DataHelper dataHelper) {
        this.dataHelper = dataHelper;
    }

    // Replace the categories
    public void replacecategories(List<Category> listCategoris) {

        SQLiteDatabase db = dataHelper.getWritableDatabase();

        for (Category category : listCategoris) {
            // Create a new map of values
            ContentValues values = new ContentValues();
            values.put("id", category.getId());
            values.put("name", category.getName());
            values.put("image", category.getImage());
            // Replace the data
            db.replace(TABLE_2, null, values);
        }

        db.close();

    }

    //get All the categories
    public List<Category> getAll() {

        SQLiteDatabase db = dataHelper.getWritableDatabase();
        String query = "SELECT id, name, image FROM " + TABLE_2;

        Cursor cursor = db.rawQuery(query, null);
        int id = cursor.getColumnIndex("id");
        int name = cursor.getColumnIndex("name");
        int image = cursor.getColumnIndex("image");

        List<Category> categories = new ArrayList<>();
        while (cursor.moveToNext()) {
            categories.add(new Category(cursor.getInt(id), cursor.getString(name), cursor.getString(image)));
        }

        cursor.close();
        db.close();

        return categories;
    }

    //delete all categories
    public void deleteAllCategories() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_2);
        db.close();
    }

    //count Categories
    public int countCategories() {
        SQLiteDatabase db = dataHelper.getWritableDatabase();
        String query = "SELECT id FROM " + TABLE_2;
        Cursor cursor = db.rawQuery(query, null);
        int count = (cursor != null) ? cursor.getCount() : 0;
        db.close();

        return count;
    }
}
