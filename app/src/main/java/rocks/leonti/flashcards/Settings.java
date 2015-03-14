package rocks.leonti.flashcards;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static String WORDS_PER_SET = "wordsPerSet";

    private final Context context;

    public Settings(Context context) {
        this.context = context;
    }

    public int getWordsPerSet() {
        return getSettings().getInt(WORDS_PER_SET, 20);
    }

    public void setWordsPerSet(int wordsPerSet) {
        SharedPreferences.Editor editor = getSettings().edit();

        editor.putInt(WORDS_PER_SET, wordsPerSet);
        editor.commit();
    }

    private SharedPreferences getSettings() {
        return context.getSharedPreferences("Settings", 0);
    }
}
