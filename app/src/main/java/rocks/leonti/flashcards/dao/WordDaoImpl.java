package rocks.leonti.flashcards.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;
import java.util.List;

import rocks.leonti.flashcards.model.Word;
import rocks.leonti.flashcards.model.WordSet;


public class WordDaoImpl implements WordDao {

    private final WordsDbHelper dbHelper;
    private final WordSetTable wordSetTable;
    private final WordTable wordTable;

    private SQLiteDatabase database;

    public WordDaoImpl(Context context) {
        dbHelper = new WordsDbHelper(context);
        wordSetTable = new WordSetTable();
        wordTable = new WordTable();
    }

    @Override
    public long createWordSet(WordSet wordSet) {
        return database.insert(wordSetTable.getTable(), null,
                wordSetTable.toContentValues(wordSet));
    }

    @Override
    public long createWord(Word word) {
        return database.insert(wordTable.getTable(), null,
                wordTable.toContentValues(word));
    }

    @Override
    public Word getWord(long id) {
        try (Cursor cursor = database.rawQuery(wordTable.queryById(id), null)) {
            cursor.moveToFirst();
            return wordTable.toEntity(cursor);
        }
    }

    @Override
    public List<WordSet> getSets() {
        List<WordSet> wordSets = new LinkedList<>();

        try(Cursor cursor = database.query(wordSetTable.getTable(), null, null, null, null, null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                wordSets.add(wordSetTable.toEntity(cursor));
                cursor.moveToNext();
            }

            return wordSets;
        }
    }

    @Override
    public List<Word> getWords(long setId, int limit, int offset) {
        List<Word> words = new LinkedList<>();

        try(Cursor cursor = database.rawQuery(wordTable.queryWithOffset(setId, limit, offset), null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {

                words.add(wordTable.toEntity(cursor));
                cursor.moveToNext();
            }

            return words;
        }
    }

    @Override
    public void increaseView(long wordId) {
        database.execSQL(wordTable.increaseViewsStatement(wordId));
    }

    @Override
    public void setReview(long wordId, Word.Review review) {
        database.execSQL(wordTable.setReviewStatement(wordId, review));
    }

    @Override
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    @Override
    public void close() {
        dbHelper.close();
    }
}
