package rocks.leonti.flashcards;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static String WORDS_PER_SET = "wordsPerSet";
    private static String MIN_VIEWS = "minViews";

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

    public void setMinViews(int minViews) {
        SharedPreferences.Editor editor = getSettings().edit();

        editor.putInt(MIN_VIEWS, minViews);
        editor.commit();
    }

    private SharedPreferences getSettings() {
        return context.getSharedPreferences("Settings", 0);
    }

    public int getMinViews() {
        return getSettings().getInt(MIN_VIEWS, 2);
    }
}
