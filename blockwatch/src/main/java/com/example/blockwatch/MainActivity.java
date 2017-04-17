package com.example.blockwatch;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import data.BlockContract;
import sync.BlockwatchSyncAdapter;

import static com.example.blockwatch.R.string.hour_one_color;
import static com.example.blockwatch.R.string.hour_two_color;
import static com.example.blockwatch.R.string.minute_one_color;
import static com.example.blockwatch.R.string.minute_two_color;

public class MainActivity extends AppCompatActivity implements BlockwatchFragment.OnFragmentInteractionListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String WATCH_FRAGMENT_TAG = "watch_fragment"; // Create a tag to keep track of created fragments
    private static final String TRANSACTION_FRAGMENT_TAG = "transaction_fragment";
    boolean isTablet; // Track whether this is a tablet
    private Fragment watchFragment; // Declare the fragment you will include
    private Fragment transactionFragment; // Declare the fragment you will include
    private SwipeRefreshLayout mSwipeRefreshLayout; // Declare the container for the swip refresh action
    View rootView; // Declare rootView
    private static final String LOG_TAG = BlockwatchFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        super.onCreate(null);
        setContentView(R.layout.activity_main); // Set the main activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Get the toolbar ID
        setSupportActionBar(toolbar); // Set the toolbar

        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());
                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);
                //  If the activity has never started before...
                if (isFirstStart) {
                    //  Launch app intro
                    Intent i = new Intent(MainActivity.this, IntroSlidesClass.class);
                    startActivity(i);
                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();
                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);
                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

        if (getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG) == null) { // If the fragment doesn't exist yet,

            BlockwatchSyncAdapter.initializeSyncAdapter(this);

            watchFragment = new BlockwatchFragment().newInstance(false); // Add the watch fragment here, passing the context as an implementation of the fragment listener
            getSupportFragmentManager().beginTransaction().add(R.id.blockwatch_fragment, watchFragment, WATCH_FRAGMENT_TAG).commit(); // Add the fragment to the transaction

        } else {
            watchFragment = getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG); // Else if it exists
            getSupportFragmentManager().beginTransaction().replace(R.id.blockwatch_fragment, watchFragment, WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one
        }

        // The detail container view will be present only in the large-screen layouts
        // (res/layout-sw600dp). If this view is present, then the activity should be
        // in two-pane mode.
        isTablet = getResources().getBoolean(R.bool.isTablet); // Track whether this is a tablet
        if (isTablet) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG) == null) {
                transactionFragment = new TransactionFragment().newInstance(BlockContract.BlockEntry.CONTENT_URI); // Add the transaction fragment here, passing the context as an implementation of the fragment listener
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG)
                        .commit();
            } else {
                transactionFragment = getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG); // Else if it exists

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG)
                        .commit();
            }
        }
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");

        if (isTablet) {
            // Load the ad here since it doesn't depend on the loader finishing loading
            AdView mAdView = (AdView) findViewById(R.id.adViewTabletOnly);
            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("TEST_DEVICE_ID")
                    .build();
            mAdView.loadAd(adRequest);
        }

        // Retrieve the SwipeRefreshLayout and ListView instances
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
        mSwipeRefreshLayout.setColorSchemeColors( // Set the colors to the ones the user has set in preferences
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(hour_one_color), ContextCompat.getColor(this, R.color.md_red_500)),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(hour_two_color), ContextCompat.getColor(this, R.color.md_red_500)),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(minute_one_color), ContextCompat.getColor(this, R.color.md_red_500)),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(minute_two_color), ContextCompat.getColor(this, R.color.md_red_500)));
        mSwipeRefreshLayout.setOnRefreshListener(this); // Set the refresh listener
        mSwipeRefreshLayout.setProgressViewOffset(false, 168, 400); // Set where the progress circle starts and ends up
        mSwipeRefreshLayout.setEnabled(true); // Make sure the swipe screen is enabled
    }

    @Override
    public void onResume() {
        super.onResume(); // Do this so colors are reset after visiting the preference screen
        mSwipeRefreshLayout.setColorSchemeColors( // Set the colors to the ones the us has set in preferences
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(hour_one_color), ContextCompat.getColor(this, R.color.md_red_500)),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(hour_two_color), ContextCompat.getColor(this, R.color.md_red_500)),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(minute_one_color), ContextCompat.getColor(this, R.color.md_red_500)),
                PreferenceManager.getDefaultSharedPreferences(this).getInt(getResources().getString(minute_two_color), ContextCompat.getColor(this, R.color.md_red_500)));

        watchFragment = new BlockwatchFragment().newInstance(true); // Create a new watch fragment with a new hash
        getSupportFragmentManager().beginTransaction().replace(R.id.blockwatch_fragment, watchFragment, WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one with a new hash
    }

    @Override
    public void onFragmentInteraction() {
        // Here you should launch a new fragment that shows the details of the clicked transaction, only in phone mode
        if (!isTablet) {
            Intent intent = new Intent(this, TransactionDetailActivity.class);
            intent.putExtra("URI", BlockContract.BlockEntry.CONTENT_URI);
            startActivity(intent);
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
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, Preferences.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        Log.i(LOG_TAG, "onRefresh called from SwipeRefreshLayout");
        Snackbar snackbar = Snackbar.make(mSwipeRefreshLayout, R.string.fab_text, Snackbar.LENGTH_LONG);
        snackbar.setAction("action", null).show();
        View snackView = snackbar.getView();
        TextView snackText = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text); // Center the snackbar text
        snackText.setGravity(Gravity.CENTER);
        snackText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        try {
            mSwipeRefreshLayout.setRefreshing(true);
            BlockwatchSyncAdapter.syncImmediately(getBaseContext());
            watchFragment = new BlockwatchFragment().newInstance(true); // Create a new watch fragment with a new hash
            getSupportFragmentManager().beginTransaction().replace(R.id.blockwatch_fragment, watchFragment, WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one with a new hash
            mSwipeRefreshLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 3000);
        } catch (Exception e) {
            Log.d(getString(R.string.explorer_error), e.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
    }
}
