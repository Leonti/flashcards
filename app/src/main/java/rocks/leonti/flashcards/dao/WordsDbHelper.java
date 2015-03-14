package rocks.leonti.flashcards.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.LinkedList;
import java.util.List;

public class WordsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "words.db";
    private static final int DATABASE_VERSION = 2;

    private List<DbTable> tables = new LinkedList<>();

    public WordsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        tables.add(new WordSetTable());
        tables.add(new WordTable());
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        for (DbTable table : tables) {
            table.onCreate(database);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        for (DbTable table : tables) {
            table.onUpgrade(database, oldVersion, newVersion);
        }
    }
}
