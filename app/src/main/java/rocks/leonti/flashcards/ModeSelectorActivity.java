package rocks.leonti.flashcards;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rocks.leonti.flashcards.dao.WordDao;
import rocks.leonti.flashcards.dao.WordDaoImpl;
import rocks.leonti.flashcards.model.Word;
import rocks.leonti.flashcards.model.WordSet;

public class ModeSelectorActivity extends ActionBarActivity {

    public static String WORD_SET_ID = "setId";

    private long wordSetId;
    private ModeSelectorListAdapter modeSelectorListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Select mode");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            wordSetId = savedInstanceState.getLong(WORD_SET_ID);
        } else {
            Intent intent = getIntent();
            wordSetId = intent.getLongExtra(WORD_SET_ID, -1);
        }

        ListView learnSetList = (ListView) findViewById(R.id.learn_set_list);
        modeSelectorListAdapter = new ModeSelectorListAdapter(this, wordSetId, new Settings(this).getMinViews());
        learnSetList.setAdapter(modeSelectorListAdapter);

        learnSetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    Intent intent = new Intent(ModeSelectorActivity.this, LearnActivity.class);
                    intent.putExtra(LearnActivity.WORD_SET_ID, wordSetId);
                    startActivity(intent);
                } else if (position ==  1) {
                    Intent intent = new Intent(ModeSelectorActivity.this, ReviewActivity.class);
                    intent.putExtra(ReviewActivity.WORD_SET_ID, wordSetId);
                    intent.putExtra(ReviewActivity.REVIEW_STATUS, Word.Review.REVIEW.name());
                    startActivity(intent);
                } else if (position == 2) {
                    Intent intent = new Intent(ModeSelectorActivity.this, ReviewActivity.class);
                    intent.putExtra(ReviewActivity.WORD_SET_ID, wordSetId);
                    intent.putExtra(ReviewActivity.REVIEW_STATUS, Word.Review.DONE.name());
                    startActivity(intent);
                }

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.i("MODE SELECTOR ACTIVITY", "Saving instance state");
        savedInstanceState.putLong(WORD_SET_ID, wordSetId);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestart() {
        Log.i("MODE SELECTOR ACTIVITY", "Restarting");

        modeSelectorListAdapter.refresh();
        modeSelectorListAdapter.notifyDataSetChanged();
        super.onRestart();
    }

    public class ModeSelectorListAdapter extends BaseAdapter {

        private final long wordSetId;
        private final int minViews;

        private class ModeEntry {
            public final String title;
            public final String description;

            private ModeEntry(String title, String description) {
                this.title = title;
                this.description = description;
            }
        }

        private final LayoutInflater layoutInflater;
        private final List<ModeEntry> modeEntries;

        public ModeSelectorListAdapter(Context context, long wordSetId, int minViews) {
            this.wordSetId = wordSetId;
            this.minViews = minViews;
            this.layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            this.modeEntries = new ArrayList<>();
            refresh();
        }

        public void refresh() {
            Log.i("MODE SELECTOR", "Refreshing data");
            modeEntries.clear();
            try (WordDao wordDao = new WordDaoImpl(ModeSelectorActivity.this)) {
                wordDao.open();


                WordSet wordSet = wordDao.getSet(wordSetId);
                String learnModeDescription = String.format("%d out of %d left", wordSet.count - wordDao.getLearnedCount(wordSetId, minViews), wordSet.count);
                modeEntries.add(new ModeEntry("Learn new words", learnModeDescription));

                String reviewModeDescription = String.format("%d words to review", wordDao.getWordsToReviewCount(wordSetId));
                modeEntries.add(new ModeEntry("Review words", reviewModeDescription));

                String doneModeDescription = String.format("%d words marked as \"Done\"", wordDao.getDoneWordsCount(wordSetId));
                modeEntries.add(new ModeEntry("Done words", doneModeDescription));
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object getItem(int position) {
            return modeEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.mode_selector_row, parent, false);
            } else {
                view = convertView;
            }

            TextView modeTitle = (TextView) view.findViewById(R.id.mode_title);
            modeTitle.setText(modeEntries.get(position).title);
            TextView learnSetStats = (TextView) view.findViewById(R.id.mode_description);
            learnSetStats.setText(modeEntries.get(position).description);

            return view;
        }
    }

}
