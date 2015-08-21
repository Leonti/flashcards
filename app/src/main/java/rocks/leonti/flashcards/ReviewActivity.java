package rocks.leonti.flashcards;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class ReviewActivity extends ActionBarActivity {

    public static String WORD_SET_ID = "setId";
    public static String REVIEW_STATUS = "reviewStatus";

    private ReviewListAdapter reviewListAdapter;

    private long wordSetId;
    private Word.Review reviewStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (savedInstanceState != null) {
            wordSetId = savedInstanceState.getLong(WORD_SET_ID);
            reviewStatus = Word.Review.valueOf(savedInstanceState.getString(REVIEW_STATUS));
        } else {
            Intent intent = getIntent();
            wordSetId = intent.getLongExtra(WORD_SET_ID, -1);
            reviewStatus = Word.Review.valueOf(intent.getStringExtra(REVIEW_STATUS));
        }
        toolbar.setTitle(reviewStatus == Word.Review.REVIEW ? "Review" : "Done");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        ListView reviewList = (ListView) findViewById(R.id.review_list);
        final Settings settings = new Settings(this);
        reviewListAdapter = new ReviewListAdapter(this, wordSetId, settings.getWordsPerSet(), reviewStatus);
        reviewList.setAdapter(reviewListAdapter);

        reviewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ReviewActivity.this, CardsActivity.class);
                intent.putExtra(CardsActivity.WORD_SET_ID, wordSetId);

                try (WordDao wordDao = new WordDaoImpl(ReviewActivity.this)) {
                    wordDao.open();

                    List<Word> words = wordDao.getWords(wordSetId, settings.getWordsPerSet(), position * settings.getWordsPerSet(), reviewStatus);

                    long[] wordIds = new long[words.size()];
                    for (int i = 0; i < words.size(); i++) {
                        wordIds[i] = words.get(i).id;
                    }

                    intent.putExtra(CardsActivity.WORD_IDS, wordIds);
                    startActivity(intent);
                }
            }
        });

        TextView noItemsMessage = (TextView) findViewById(R.id.empty_list_message);

        try (WordDao wordDao = new WordDaoImpl(ReviewActivity.this)) {
            wordDao.open();

            List<Word> words = wordDao.getWords(wordSetId, settings.getWordsPerSet(), 0, reviewStatus);

            if (words.size() == 0) {
                noItemsMessage.setVisibility(View.VISIBLE);
                reviewList.setVisibility(View.GONE);
                noItemsMessage.setText(reviewStatus == Word.Review.REVIEW ? "No words have been marked \"To Review\"" : "No words have been marked as \"Done\"");
            }
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(WORD_SET_ID, wordSetId);
        savedInstanceState.putString(REVIEW_STATUS, reviewStatus.name());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestart() {
        reviewListAdapter.notifyDataSetChanged();
        super.onRestart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Log.i("REVIEW ACTIVITY", "On home pressed");
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private class ReviewListAdapter extends BaseAdapter {

        private final LayoutInflater layoutInflater;
        private final long wordSetId;
        private final int wordsPerSet;
        private final Word.Review reviewStatus;

        public ReviewListAdapter(Context context, long wordSetId, int wordsPerSet, Word.Review reviewStatus) {
            this.wordSetId = wordSetId;
            this.wordsPerSet = wordsPerSet;
            this.reviewStatus = reviewStatus;
            this.layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            try (WordDao wordDao = new WordDaoImpl(ReviewActivity.this)) {
                wordDao.open();

                int wordCount = wordDao.getWordsToReviewCount(wordSetId);

                if (wordCount % wordsPerSet == 0) {
                    return wordCount / wordsPerSet;
                }

                if (wordCount / wordsPerSet < 1) {
                    return 1;
                }

                return wordCount / wordsPerSet + 1;
            }
        }

        @Override
        public Object getItem(int position) {
            return getWordsForPosition(position);
        }

        private List<Word> getWordsForPosition(int position) {
            try (WordDao wordDao = new WordDaoImpl(ReviewActivity.this)) {
                wordDao.open();

                return wordDao.getWords(wordSetId, wordsPerSet, position * wordsPerSet, reviewStatus);
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
                view = layoutInflater.inflate(R.layout.review_list_row, parent, false);
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

            TextView learnSetWords = (TextView) view.findViewById(R.id.review_list_words);
            learnSetWords.setText(setWords + "...");

            return view;
        }
    }
}
