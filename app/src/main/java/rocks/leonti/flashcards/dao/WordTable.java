package rocks.leonti.flashcards.dao;


import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedList;
import java.util.List;

import rocks.leonti.flashcards.model.Word;

public class WordTable extends DbTable<Word> {

    public static String TABLE = "words";
    public static String COLUMN_WORD_SET_ID = "wordSetId";
    public static String COLUMN_WORD = "word";
    public static String COLUMN_TYPES = "types";
    public static String COLUMN_PRONUNCIATION = "pronunciation";
    public static String COLUMN_DEFINITION = "definition";
    public static String COLUMN_USAGE = "usage";
    public static String COLUMN_RELATED_WORDS = "relatedWords";
    public static String COLUMN_INFO = "info";
    public static String COLUMN_VIEWS = "views";
    public static String COLUMN_REVIEW = "review";

    @Override
    protected String getTable() {
        return TABLE;
    }

    @Override
    protected String getTableCreate() {
        return  "create table "
                + TABLE + "(_id integer primary key autoincrement, "
                + COLUMN_WORD_SET_ID + " integer, "
                + COLUMN_WORD + " text not null, "
                + COLUMN_TYPES + " text not null, "
                + COLUMN_PRONUNCIATION + " text not null, "
                + COLUMN_DEFINITION + " text not null, "
                + COLUMN_USAGE + " text not null, "
                + COLUMN_RELATED_WORDS + " text not null, "
                + COLUMN_VIEWS + " integer not null, "
                + COLUMN_REVIEW + " text not null, "
                + COLUMN_INFO + " text not null);";
    }

    @Override
    public ContentValues toContentValues(Word word) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_WORD, word.word);
        values.put(COLUMN_WORD_SET_ID, word.wordSetId);
        values.put(COLUMN_TYPES, toJsonString(toStrings(word.types)));
        values.put(COLUMN_PRONUNCIATION, toJsonString(word.pronunciation));
        values.put(COLUMN_DEFINITION, toJsonString(word.definition));
        values.put(COLUMN_USAGE, toJsonString(word.usage));
        values.put(COLUMN_RELATED_WORDS, toJsonString(word.relatedWords));
        values.put(COLUMN_INFO, word.info);
        values.put(COLUMN_VIEWS, word.views);
        values.put(COLUMN_REVIEW, word.review.name());

        return values;
    }

    @Override
    public Word toEntity(Cursor cursor) {

        Long id = cursor.getLong(cursor.getColumnIndex("_id"));
        Long wordSetId = cursor.getLong(cursor.getColumnIndex(COLUMN_WORD_SET_ID));
        String word = cursor.getString(cursor.getColumnIndex(COLUMN_WORD));
        List<Word.Type> types = toWordTypes(fromJsonString(cursor.getString(cursor.getColumnIndex(COLUMN_TYPES))));
        List<String> pronunciation = fromJsonString(cursor.getString(cursor.getColumnIndex(COLUMN_PRONUNCIATION)));
        List<String> definition = fromJsonString(cursor.getString(cursor.getColumnIndex(COLUMN_DEFINITION)));
        List<String> usage = fromJsonString(cursor.getString(cursor.getColumnIndex(COLUMN_USAGE)));
        List<String> relatedWords = fromJsonString(cursor.getString(cursor.getColumnIndex(COLUMN_RELATED_WORDS)));
        String info = cursor.getString(cursor.getColumnIndex(COLUMN_INFO));
        int views = cursor.getInt(cursor.getColumnIndex(COLUMN_VIEWS));
        Word.Review review = Word.Review.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_REVIEW)));

        return new Word(id, wordSetId, word, types, pronunciation, definition, usage, relatedWords, info, views, review);
    }

    public String queryWithOffset(long wordSetId, int limit, int offset) {
        return "SELECT * FROM " + getTable() + " WHERE " + COLUMN_WORD_SET_ID + "=" + wordSetId + " ORDER BY _id LIMIT " + limit + " OFFSET " + offset;
    }

    public String queryById(long id) {
        return "SELECT * FROM " + getTable() + " WHERE _id=" + id;
    }

    public String increaseViewsStatement(long id) {
        return "UPDATE " + getTable() + " SET " + COLUMN_VIEWS + " = " + COLUMN_VIEWS + " + 1 WHERE _id=" + id;
    }

    public String setReviewStatement(long id, Word.Review review) {
        return "UPDATE " + getTable() + " SET " + COLUMN_REVIEW + " = '" + review.name() + "' WHERE _id=" + id;
    }

    private List<String> toStrings(List<Word.Type> types) {
        List<String> strings = new LinkedList<>();

        for (Word.Type type : types) {
            strings.add(type.name());
        }

        return strings;
    }

    private List<Word.Type> toWordTypes(List<String> types) {
        List<Word.Type> values = new LinkedList<>();

        for (String type : types) {
            values.add(Word.Type.valueOf(type));
        }

        return values;
    }

    private String toJsonString(List<String> items) {
        JSONArray jsonArray = new JSONArray();

        for (String item : items) {
            jsonArray.put(item);
        }

        return jsonArray.toString();
    }

    private List<String> fromJsonString(String jsonString) {
        try {
            List<String> list = new LinkedList<>();
            JSONArray jsonArray = new JSONArray(jsonString);

            for (int i =  0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }

            return list;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
