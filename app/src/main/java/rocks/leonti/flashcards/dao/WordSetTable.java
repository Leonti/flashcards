package rocks.leonti.flashcards.dao;

import android.content.ContentValues;
import android.database.Cursor;

import rocks.leonti.flashcards.model.WordSet;

public class WordSetTable extends DbTable<WordSet> {

    public static String TABLE = "sets";
    public static String COLUMN_GLOBAL_ID = "global_id";
    public static String COLUMN_NAME = "name";
    public static String COLUMN_DESCRIPTION = "description";
    public static String COLUMN_COUNT = "count";

    @Override
    protected String getTable() {
        return TABLE;
    }

    @Override
    protected String getTableCreate() {
        return  "create table "
                + TABLE + " (_id integer primary key autoincrement, "
                + COLUMN_GLOBAL_ID + " text not null, "
                + COLUMN_NAME + " text not null, "
                + COLUMN_DESCRIPTION + " text not null, " +
                COLUMN_COUNT + " integer);";
    }

    @Override
    public ContentValues toContentValues(WordSet wordSet) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_GLOBAL_ID, wordSet.globalId);
        values.put(COLUMN_NAME, wordSet.name);
        values.put(COLUMN_DESCRIPTION, wordSet.description);
        values.put(COLUMN_COUNT, wordSet.count);

        return values;
    }

    @Override
    public WordSet toEntity(Cursor cursor) {

        Long id = cursor.getLong(cursor.getColumnIndex("_id"));
        String globalId = cursor.getString(cursor.getColumnIndex(COLUMN_GLOBAL_ID));
        String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        String description = cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION));
        Integer count = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));

        return new WordSet(id, globalId, name, description, count);
    }


}
