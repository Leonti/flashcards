package rocks.leonti.flashcards;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import rocks.leonti.flashcards.dao.WordDao;
import rocks.leonti.flashcards.dao.WordDaoImpl;
import rocks.leonti.flashcards.model.Word;
import rocks.leonti.flashcards.model.WordSet;


public class LearnActivity extends ActionBarActivity {

    public static String WORD_SET_ID = "setId";

    private LearnSetListAdapter learnSetListAdapter;

    private long wordSetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Learn");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        if (savedInstanceState != null) {
            wordSetId = savedInstanceState.getLong(WORD_SET_ID);
        } else {
            wordSetId = intent.getLongExtra(WORD_SET_ID, -1);
        }

        ListView learnSetList = (ListView) findViewById(R.id.learn_set_list);
        final Settings settings = new Settings(this);
        learnSetListAdapter = new LearnSetListAdapter(this, wordSetId, settings.getWordsPerSet(), settings.getMinViews());
        learnSetList.setAdapter(learnSetListAdapter);

        learnSetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(LearnActivity.this, CardsActivity.class);
                intent.putExtra(CardsActivity.WORD_SET_ID, wordSetId);

                try (WordDao wordDao = new WordDaoImpl(LearnActivity.this)) {
                    wordDao.open();

                    List<Word> words = wordDao.getWords(wordSetId, settings.getWordsPerSet(), position * settings.getWordsPerSet());

                    long[] wordIds = new long[words.size()];
                    for (int i = 0; i < words.size(); i++) {
                        wordIds[i] = words.get(i).id;
                    }

                    intent.putExtra(CardsActivity.WORD_IDS, wordIds);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(WORD_SET_ID, wordSetId);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestart() {
        learnSetListAdapter.notifyDataSetChanged();
        super.onRestart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id ==  android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private class LearnSetListAdapter extends BaseAdapter {

        private final int wordsInSet;
        private final int minViews;
        private final LayoutInflater layoutInflater;
        private final WordSet wordSet;

        public LearnSetListAdapter(Context context, long wordSetId, int wordsInSet, int minViews) {
            this.wordsInSet = wordsInSet;
            this.minViews = minViews;
            this.layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            try (WordDao wordDao = new WordDaoImpl(LearnActivity.this)) {
                wordDao.open();

                wordSet = wordDao.getSet(wordSetId);
            }
        }

        @Override
        public int getCount() {

            if (wordSet.count % wordsInSet == 0) {
                return wordSet.count/wordsInSet;
            }

            if (wordSet.count/wordsInSet < 1) {
                return 1;
            }

            return wordSet.count/wordsInSet + 1;
        }

        @Override
        public Object getItem(int position) {
            return getWordsForPosition(position);
        }

        private List<Word> getWordsForPosition(int position) {
            try (WordDao wordDao = new WordDaoImpl(LearnActivity.this)) {
                wordDao.open();

                return wordDao.getWords(wordSet.id, wordsInSet, position * wordsInSet);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = null;

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.learn_set_row, parent, false);
            } else {
                view = convertView;
            }

            List<Word> words = getWordsForPosition(position);

            String setWords = "";
            int limit = words.size() < 3 ? words.size() : 3;
            for (int i = 0; i < limit; i++) {
                setWords += words.get(i).word;
                setWords += ", ";
            }

            TextView learnSetWords = (TextView) view.findViewById(R.id.learn_set_words);
            learnSetWords.setText(setWords + "...");
            TextView learnSetStats = (TextView) view.findViewById(R.id.learn_set_stats);
            String stats = String.format("%d to review, %d learned", getToReviewCount(words), getLearnedCount(words, minViews));
            int doneCount = getDoneCount(words);
            if (doneCount > 0) {
                stats += String.format(", %d done", doneCount);
            }
            learnSetStats.setText(stats);

            if (isSetDone(words, minViews)) {
                view.setAlpha(0.6f);
            }

            return view;
        }

        private boolean isSetDone(List<Word> words, int minViews) {

            for (Word word : words) {
                if (word.review != Word.Review.DONE
                        && word.views < minViews) {
                    return false;
                }
            }

            return true;
        }

        private int getToReviewCount(List<Word> words) {
            int count = 0;

            for (Word word : words) {
                if (word.review == Word.Review.REVIEW) {
                    count++;
                }
            }
            return count;
        }

        private int getDoneCount(List<Word> words) {
            int count = 0;

            for (Word word : words) {
                if (word.review == Word.Review.DONE) {
                    count++;
                }
            }
            return count;
        }

        private int getLearnedCount(List<Word> words, int minViews) {
            int count = 0;

            for (Word word : words) {
                if (word.views >= minViews) {
                    count++;
                }
            }
            return count;
        }
    }
}
