package com.waracle.androidtest;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


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
            mCakeListFragment = new CakeListFragment();
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

    /**
     * Fragment is responsible for loading in some JSON and
     * then displaying a list of cakes with images.
     */
    public static class CakeListFragment extends ListFragment {

        private static final String TAG = CakeListFragment.class.getSimpleName();

        private ListView mListView;
        private MyAdapter mAdapter;

        public CakeListFragment() { /**/ }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mListView = (ListView) rootView.findViewById(android.R.id.list);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            if (savedInstanceState == null) {
                // Create and set the list adapter.
                mAdapter = new MyAdapter();

                // Load data from net.
                refreshData();

            } else {
                mListView.setAdapter(mAdapter);
            }

            // retain this fragment
            setRetainInstance(true);
        }

        public void refreshData()
        {
            // Kick off a loaded task to get data from the internet
            // and update the visible list when it's done
            DataLoaderTask loadListData = new DataLoaderTask();
            loadListData.execute();
        }

        public void clearData()
        {
            // Clear the visible list
            mAdapter.setItems(new JSONArray());
            mListView.setAdapter(mAdapter);
        }

        // Asynchronous loading for the JSON file
        public class DataLoaderTask extends AsyncTask<Integer,Integer,String> {
            JSONArray mArray;

            @Override
            protected String doInBackground(Integer... params) {
                try {
                    mArray = loadData();
                } catch (IOException | JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                return "Task Completed.";
            }

            @Override
            protected void onPostExecute(String result) {

                mAdapter.setItems(mArray);
                mListView.setAdapter(mAdapter);
            }

        // Fetch JSON data from the web, and parse it
        private JSONArray loadData() throws IOException, JSONException {
            URL url = new URL(JSON_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader br=new BufferedReader(new InputStreamReader(url.openStream()));
                String jsonText = new String();
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line+"\n");
                }
                br.close();

                jsonText = sb.toString();


                // Read string as JSON.
                return new JSONArray(jsonText);
            } finally {
                urlConnection.disconnect();
            }
        }
    }

        /**
         * Returns the charset specified in the Content-Type of this header,
         * or the HTTP default (ISO-8859-1) if none can be found.
         */
        public static String parseCharset(String contentType) {
            if (contentType != null) {
                String[] params = contentType.split(",");
                for (int i = 1; i < params.length; i++) {
                    String[] pair = params[i].trim().split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("charset")) {
                            return pair[1];
                        }
                    }
                }
            }
            return "UTF-8";
        }

        private class MyAdapter extends BaseAdapter {

            // Can you think of a better way to represent these items???
            private JSONArray mItems;
            private ImageLoader mImageLoader;

            public MyAdapter() {
                this(new JSONArray());
            }

            public MyAdapter(JSONArray items) {
                mItems = items;
                mImageLoader = new ImageLoader();
            }

            @Override
            public int getCount() {
                return mItems.length();
            }

            @Override
            public Object getItem(int position) {
                try {
                    return mItems.getJSONObject(position);
                } catch (JSONException e) {
                    Log.e("", e.getMessage());
                }
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View root = inflater.inflate(R.layout.list_item_layout, parent, false);
                if (root != null) {
                    TextView title = (TextView) root.findViewById(R.id.title);
                    TextView desc = (TextView) root.findViewById(R.id.desc);
                    ImageView image = (ImageView) root.findViewById(R.id.image);
                    try {
                        JSONObject object = (JSONObject) getItem(position);
                        title.setText(object.getString("title"));
                        desc.setText(object.getString("desc"));
                        mImageLoader.load(object.getString("image"), image);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }

                return root;
            }

            public void setItems(JSONArray items) {
                mItems = items;
            }
        }
    }
}

