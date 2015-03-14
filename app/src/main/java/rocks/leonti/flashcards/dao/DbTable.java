package rocks.leonti.flashcards.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public abstract class DbTable<T> {

    protected abstract String getTable();

    protected abstract String getTableCreate();

    public abstract ContentValues toContentValues(T entity);

    public abstract T toEntity(Cursor cursor);

    public void onCreate(SQLiteDatabase database) {
        database.execSQL(getTableCreate());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(WordSetTable.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + getTable());
        onCreate(db);
    }

}
