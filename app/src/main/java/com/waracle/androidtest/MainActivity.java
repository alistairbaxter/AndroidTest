package com.waracle.androidtest;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

    private static String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
            "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";

    private static String LIST_FRAGMENT_TAG = "ListFragmentTag";

    private CakeListFragment mCakeListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        mCakeListFragment = (CakeListFragment) fm.findFragmentByTag(LIST_FRAGMENT_TAG);

        // create the fragment and data the first time
        if (mCakeListFragment == null) {
            mCakeListFragment = new CakeListFragment(JSON_URL);
            fm.beginTransaction()
                    .add(R.id.container, mCakeListFragment, LIST_FRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {

            // Load data from net.
            mCakeListFragment.clearData();
            mCakeListFragment.refreshData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

