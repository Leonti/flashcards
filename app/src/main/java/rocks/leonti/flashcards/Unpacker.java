package rocks.leonti.flashcards;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import rocks.leonti.flashcards.model.Word;
import rocks.leonti.flashcards.model.WordSet;
import rocks.leonti.flashcards.dao.WordDao;
import rocks.leonti.flashcards.dao.WordDaoImpl;

public class Unpacker extends AsyncTask<Void, Void, Void> {

    private final String IMPORTED = "imported";

    private final Context context;
    private final Handler handler;
    private final Runnable onImportStart;
    private final Runnable onImportFinished;

    public Unpacker(Context context, Handler handler, Runnable onImportStart, Runnable onImportFinished) {
        this.context = context;
        this.handler = handler;
        this.onImportStart = onImportStart;
        this.onImportFinished = onImportFinished;
    }

    @Override
    protected Void doInBackground(Void... nothings) {

        try {
            String[] filePaths = context.getAssets().list("");

            for (String filePath : filePaths) {
                if (filePath.endsWith(".json") && !isImported(filePath)) {
                    handler.post(onImportStart);

                    importWordSet(filePath);

                    handler.post(onImportFinished);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private boolean isImported(String setPath) {
        return context.getSharedPreferences(IMPORTED, 0).getBoolean(setPath, false);
    }

    private void markAsImported(String setPath) {
        SharedPreferences.Editor editor = context.getSharedPreferences(IMPORTED, 0).edit();

        editor.putBoolean(setPath, true);
        editor.commit();
    }

    private void importWordSet(String setPath) {
        try (WordDao wordDao = new WordDaoImpl(context)) {
            wordDao.open();

            WordSet wordSet = getWordSet(setPath);
            long wordSetId = wordDao.createWordSet(wordSet);

            try (
                    InputStream in = context.getAssets().open(setPath);
                    JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            ) {

                reader.beginObject();


                while (reader.hasNext()) {
                    String field = reader.nextName();

                    if (field.equals("words")) {
                        reader.beginArray();

                        while (reader.hasNext()) {
                            wordDao.createWord(parseWord(reader, wordSetId));
                        }

                        reader.endArray();
                    } else {
                        reader.skipValue();
                    }
                }

                reader.endObject();

                markAsImported(setPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Word parseWord(JsonReader reader, long wordSetId) throws IOException {

        String word = null;
        List<Word.Type> types = null;
        List<String> pronunciation = null;
        List<String> definition = null;
        List<String> usage = null;
        List<String> relatedWords = null;
        String info = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String field = reader.nextName();

            if (field.equals("word")) {
                word = reader.nextString();
            } else if (field.equals("types")) {
                types = toWordTypes(readStringsArray(reader));
            } else if (field.equals("pronunciation")) {
                pronunciation = readStringsArray(reader);
            } else if (field.equals("definition")) {
                definition = readStringsArray(reader);
            } else if (field.equals("usage")) {
                usage = readStringsArray(reader);
            } else if (field.equals("relatedWords")) {
                relatedWords = readStringsArray(reader);
            } else if (field.equals("info")) {
                info = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();

        return new Word(null, wordSetId, word, types, pronunciation, definition, usage, relatedWords, info, 0, Word.Review.NONE);
    }

    private List<Word.Type> toWordTypes(List<String> types) {
        List<Word.Type> values = new LinkedList<>();

        for (String type : types) {
            if (!values.contains(Word.Type.valueOf(type))) {
                values.add(Word.Type.valueOf(type));
            }
        }

        return values;
    }

    private WordSet getWordSet(String path) throws IOException {
        try (InputStream in = context.getAssets().open(path)) {
            try (JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"))) {

                String globalId = null;
                String name = null;
                String description = null;
                Integer count = null;

                reader.beginObject();

                while (reader.hasNext()) {
                    String field = reader.nextName();

                    if (field.equals("id")) {
                        globalId = reader.nextString();
                    } else if (field.equals("name")) {
                        name = reader.nextString();
                    } else if (field.equals("description")) {
                        description = reader.nextString();
                    } else if (field.equals("words")) {
                        count = countWords(reader);
                    } else {
                        reader.skipValue();
                    }
                }

                reader.endObject();

                return new WordSet(null, globalId, name, description, count);
            }
        }
    }

    private int countWords(JsonReader reader) throws IOException {

        int count = 0;
        reader.beginArray();
        while (reader.hasNext()) {
            reader.skipValue();
            count++;
        }
        reader.endArray();
        return count;
    }

    private List readStringsArray(JsonReader reader) throws IOException {
        List strings = new LinkedList();

        reader.beginArray();
        while (reader.hasNext()) {
            strings.add(reader.nextString());
        }
        reader.endArray();
        return strings;
    }
}
