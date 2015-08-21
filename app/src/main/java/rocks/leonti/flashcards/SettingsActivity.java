package rocks.leonti.flashcards;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import rocks.leonti.flashcards.R;

public class SettingsActivity extends ActionBarActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Settings");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        final Settings settings = new Settings(this);
        Spinner wordsPerSetSpinner = (Spinner) findViewById(R.id.words_per_set);

        for (int i = 0; i < wordsPerSetSpinner.getAdapter().getCount(); i++) {

            String savedValue = String.valueOf(settings.getWordsPerSet());
            if (wordsPerSetSpinner.getAdapter().getItem(i).toString().equals(savedValue)) {
                wordsPerSetSpinner.setSelection(i);
            }
        }

        wordsPerSetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setWordsPerSet(Integer.valueOf(parent.getItemAtPosition(position).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner minViewsSpinner = (Spinner) findViewById(R.id.min_views);

        for (int i = 0; i < minViewsSpinner.getAdapter().getCount(); i++) {

            String savedValue = String.valueOf(settings.getMinViews());
            if (minViewsSpinner.getAdapter().getItem(i).toString().equals(savedValue)) {
                minViewsSpinner.setSelection(i);
            }
        }

        minViewsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                settings.setMinViews(Integer.valueOf(parent.getItemAtPosition(position).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id ==  android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
