package rocks.leonti.flashcards;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import rocks.leonti.flashcards.dao.WordDao;
import rocks.leonti.flashcards.dao.WordDaoImpl;
import rocks.leonti.flashcards.model.Word;


public class CardsActivity extends ActionBarActivity {

    public static String WORD_SET_ID = "wordSetId";
    public static String WORD_IDS = "wordIds";
    public static String CURRENT_WORD_POSITION = "currentWordPosition";

    private Toolbar toolbar;

    private ViewPager viewPager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private Menu actionMenu;

    private long setId;
    long[] wordIds;
    private int currentWordPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Learn");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = (ViewPager) findViewById(R.id.pager);

        if (savedInstanceState != null) {
            setId = savedInstanceState.getLong(WORD_SET_ID);
            wordIds = savedInstanceState.getLongArray(WORD_IDS);
            currentWordPosition = savedInstanceState.getInt(CURRENT_WORD_POSITION);
        } else {
            setId = getIntent().getLongExtra(WORD_SET_ID, -1);
            wordIds = getIntent().getLongArrayExtra(WORD_IDS);
            currentWordPosition = 0;
            increaseViewCount(wordIds[0]);
        }

        try (WordDao wordDao = new WordDaoImpl(this)) {
            wordDao.open();

            pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), setId, wordIds);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    Log.i("CARDS ACTIVITY", " Page changed " + position);
                    currentWordPosition = position;
                    // FIXME don't use actionMenu as an indicator
                    if (actionMenu != null) {
                        Word word = ((ScreenSlidePagerAdapter) viewPager.getAdapter()).getWord(position);
                        updateReviewIcon(word);
                        increaseViewCount(word.id);
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(WORD_SET_ID, setId);
        savedInstanceState.putLongArray(WORD_IDS, wordIds);
        savedInstanceState.putInt(CURRENT_WORD_POSITION, currentWordPosition);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {

        if (viewPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        }
    }

    private void updateReviewIcon(Word word) {
        if (word.review == Word.Review.DONE) {
            actionMenu.findItem(R.id.action_review).setVisible(false);
        } else {
            actionMenu.findItem(R.id.action_review).setVisible(true);
            actionMenu.findItem(R.id.action_review).setIcon(getReviewIcon(word.review));
        }

        Log.i("CARDS ACTIVITY:", "Done icon for " + word.review);
        actionMenu.findItem(R.id.action_done).setIcon(getDoneIcon(word.review));
    }

    int getReviewIcon(Word.Review review) {
        return review == Word.Review.REVIEW ? R.drawable.ic_action_action_review_marked : R.drawable.ic_action_action_review;
    }

    int getDoneIcon(Word.Review review) {
        return review == Word.Review.DONE ? R.drawable.ic_action_action_done_marked : R.drawable.ic_action_action_done;
    }

    private void increaseViewCount(long wordId) {
        try (WordDao wordDao = new WordDaoImpl(CardsActivity.this)) {
            wordDao.open();
            wordDao.increaseView(wordId);
        }
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final List<Word> words;

        public ScreenSlidePagerAdapter(FragmentManager fm, long setId, long[] wordIds) {
            super(fm);
            try (WordDao wordDao = new WordDaoImpl(CardsActivity.this)) {
                wordDao.open();
                this.words = wordDao.getWords(wordIds);
            }
        }

        @Override
        public Fragment getItem(int position) {
            Log.i("CARDS ACTIVITY", "Geting item: " + position);
            return FlashCard.newInstance(words.get(position).id);
        }

        @Override
        public int getCount() {
            return words.size();
        }

        public Word getWord(int position) {
            try (WordDao wordDao = new WordDaoImpl(CardsActivity.this)) {
                wordDao.open();
                Word cachedWord = words.get(position);
                return wordDao.getWord(cachedWord.id);
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i("CARDS", "On create Options menu");
        getMenuInflater().inflate(R.menu.menu_cards, menu);
        this.actionMenu = menu;
        Word word = ((ScreenSlidePagerAdapter) viewPager.getAdapter()).getWord(currentWordPosition);
        updateReviewIcon(word);
        increaseViewCount(word.id);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id ==  android.R.id.home) {
            Log.i("CARDS ACTIVITY", "On home pressed");
            super.onBackPressed();
        } else if (id == R.id.action_review) {
            Word word = pagerAdapter.getWord(viewPager.getCurrentItem());

            updateReviewIcon(saveReviewStatus(word.id, word.review == Word.Review.REVIEW ? Word.Review.NONE : Word.Review.REVIEW));
            return true;
        } else if (id == R.id.action_done) {
            Word word = pagerAdapter.getWord(viewPager.getCurrentItem());

            updateReviewIcon(saveReviewStatus(word.id, word.review == Word.Review.DONE ? Word.Review.NONE : Word.Review.DONE));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Word saveReviewStatus(long wordId, Word.Review review) {
        try (WordDao wordDao = new WordDaoImpl(this)) {
            wordDao.open();

            wordDao.setReview(wordId, review);
            return wordDao.getWord(wordId);
        }
    }
}
