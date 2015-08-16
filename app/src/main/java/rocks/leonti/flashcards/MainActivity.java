package rocks.leonti.flashcards;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import rocks.leonti.flashcards.dao.WordDao;
import rocks.leonti.flashcards.dao.WordDaoImpl;
import rocks.leonti.flashcards.model.WordSet;


public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView leftDrawerList;
    private ArrayAdapter<String> navigationDrawerAdapter;
    private String[] leftSliderData = {"Learn", "Review", "Settings", "About"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leftDrawerList = (ListView) findViewById(R.id.left_drawer);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        navigationDrawerAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, leftSliderData);
        leftDrawerList.setAdapter(navigationDrawerAdapter);
        leftDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        toolbar.setTitle("Eloquence");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);

        initDrawer();

        ListView wordSetList = (ListView) findViewById(R.id.list_word_set);
        final WordSetListAdapter wordSetListAdapter = new WordSetListAdapter(this);
        wordSetList.setAdapter(wordSetListAdapter);

        wordSetList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("MAIN ACTIVITY: ", "Set selected position " + position);

                WordSet wordSet = (WordSet) wordSetListAdapter.getItem(position);
                Intent intent = new Intent(MainActivity.this, ModeSelectorActivity.class);
                intent.putExtra(ModeSelectorActivity.WORD_SET_ID, wordSet.id);
                startActivity(intent);
            }
        });

        Unpacker unpacker = new Unpacker(this, new Handler(), new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Starting to import",
                        Toast.LENGTH_SHORT).show();
            }
        }, new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Import is finished",
                        Toast.LENGTH_SHORT).show();
                wordSetListAdapter.notifyDataSetChanged();
            }
        });
        unpacker.execute();

    }

    private void selectItem(int position) {

        Log.i("DRAWER", "Drawer selected position: " + position);
        if (position == 2) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        // Highlight the selected item, update the title, and close the drawer
        leftDrawerList.setItemChecked(position, true);
        setTitle(leftSliderData[position]);
        drawerLayout.closeDrawer(Gravity.LEFT);
    }


    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public class WordSetListAdapter extends BaseAdapter {

        private final Context context;
        private final LayoutInflater layoutInflater;

        public WordSetListAdapter(Context context) {
            this.context = context;
            this.layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        private List<WordSet> getWordSets() {
            try (WordDao wordDao = new WordDaoImpl(context)) {
                wordDao.open();
                return wordDao.getSets();
            }
        }

        @Override
        public int getCount() {
            return getWordSets().size();
        }

        @Override
        public Object getItem(int position) {
            return getWordSets().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = null;

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.word_set_row, parent, false);
            } else {
                view = convertView;
            }

            TextView wordSetTitle = (TextView) view.findViewById(R.id.word_set_title);
            wordSetTitle.setText(getWordSets().get(position).name);
            TextView wordSetDescription = (TextView) view.findViewById(R.id.word_set_description);
            wordSetDescription.setText(getWordSets().get(position).description);

            return view;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    private void initView() {

    }

    private void initDrawer() {

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //  getMenuInflater().inflate(R.menu.menu_main, menu);
        // return true;
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
