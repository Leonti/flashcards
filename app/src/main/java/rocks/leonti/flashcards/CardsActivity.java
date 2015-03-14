package rocks.leonti.flashcards;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import rocks.leonti.flashcards.dao.WordDao;
import rocks.leonti.flashcards.dao.WordDaoImpl;
import rocks.leonti.flashcards.model.Word;


public class CardsActivity extends ActionBarActivity {

    private Toolbar toolbar;

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cards);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Advanced");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = (ViewPager) findViewById(R.id.pager);

        try (WordDao wordDao = new WordDaoImpl(this)) {
            wordDao.open();

            long setId = wordDao.getSets().get(0).id;

            pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), wordDao.getWords(setId, 20, 0));
            viewPager.setAdapter(pagerAdapter);
        }
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

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        private final List<Word> words;

        public ScreenSlidePagerAdapter(FragmentManager fm, List<Word> words) {
            super(fm);
            this.words = words;
        }

        @Override
        public Fragment getItem(int position) {
            return FlashCard.newInstance(words.get(position).id);
        }

        @Override
        public int getCount() {
            return words.size();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cards, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //if (id == android.R.id.home) {
        //    Intent intent = new Intent(CardsActivity.this, MainActivity.class);
        //   intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //    startActivity(intent);
        //} else
        if (id == R.id.action_settings) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
