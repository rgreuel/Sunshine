package com.example.android.sunshine.app;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private static final int DETAIL_LOADER_ID = 0;
        private String mForecastStr;
        ShareActionProvider mShareActionProvider;

        private static final String[] DETAIL_COLUMNS = {
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED
        };

        // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
        // must change.
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        static final int COL_WEATHER_CONDITION_ID = 5;
        static final int COL_HUMIDITY = 6;
        static final int COL_PRESSURE = 7;
        static final int COL_WIND_SPEED = 8;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            // Inflate the menu; this adds items to the action bar if it is present
            inflater.inflate(R.menu.detailfragment, menu);

            // Retrieve the share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            // Get the provider and hold onto it to set/change the share intent
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            // Attach an intent to this ShareActionProvider. You can update this at any time,
            // like when the user selects a new piece of data they might like to share
            if (mForecastStr != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null?");
            }
        }

        private Intent createShareForecastIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    mForecastStr + FORECAST_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Intent intent = getActivity().getIntent();
            if (intent == null) {
                return null;
            }

            switch (id) {
                case DETAIL_LOADER_ID:
                    return new CursorLoader(
                            getActivity(), // parent activity context
                            intent.getData(), // table to query
                            DETAIL_COLUMNS, // projection to return
                            null, // selection clause
                            null, // selection arguments
                            null // sort order
                    );

                default:
                    // invalid ID passed in
                    return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (!data.moveToFirst())
                return;

            mForecastStr = "" +
                    Utility.formatDate(data.getLong(COL_WEATHER_DATE)) +
                    "\nHigh: " + Utility.formatTemperature(getActivity(),
                    data.getDouble(COL_WEATHER_MAX_TEMP), Utility.isMetric(getActivity())) +
                    "\nLow: " + Utility.formatTemperature(getActivity(),
                    data.getDouble(COL_WEATHER_MIN_TEMP), Utility.isMetric(getActivity())) +
                    "\n" + data.getString(COL_WEATHER_DESC) +
                    "\nHumidity: " + data.getDouble(COL_HUMIDITY) +
                    "\nPressure: " + data.getDouble(COL_PRESSURE) +
                    "\nWind: " + data.getDouble(COL_WIND_SPEED) + " km/h";

            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
            detailTextView.setText(mForecastStr);

            // if onCreateOptionsMenu has already happened, we need to update the share intent
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}
