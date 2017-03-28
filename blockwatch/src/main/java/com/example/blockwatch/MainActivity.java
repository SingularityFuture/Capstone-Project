package com.example.blockwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.MobileAds;

import data.BlockContract;
import sync.BlockwatchSyncAdapter;

public class MainActivity extends AppCompatActivity implements BlockwatchFragment.OnFragmentInteractionListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the main activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Get the toolbar ID
        setSupportActionBar(toolbar); // Set the toolbar

        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        if (getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG) == null) { // If the fragment doesn't exist yet,

            BlockwatchSyncAdapter.initializeSyncAdapter(this);

            watchFragment = new BlockwatchFragment().newInstance(); // Add the watch fragment here, passing the context as an implementation of the fragment listener
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

        rootView = getLayoutInflater().inflate(R.layout.activity_main, null);
        // Retrieve the SwipeRefreshLayout and ListView instances
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        //mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);

        // Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
        mSwipeRefreshLayout.setColorSchemeColors(
                ContextCompat.getColor(this,R.color.md_red_500), ContextCompat.getColor(this,R.color.md_blue_500),
                ContextCompat.getColor(this,R.color.md_green_500), ContextCompat.getColor(this,R.color.md_yellow_500));

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(false,200,200);

        mSwipeRefreshLayout.setEnabled(true);

/*        mSwipeRefreshLayout.addOnLayoutChangeListener(
                new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                               int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        mSwipeRefreshLayout.removeOnLayoutChangeListener(this);
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });*/

/*        mSwipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
            }
        });*/
    }

    @Override
    public void onClick(View view) {
        Snackbar.make(view, R.string.fab_text, Snackbar.LENGTH_LONG)
                .setAction("action", null).show();
        try {
            BlockwatchSyncAdapter.syncImmediately(this);
            watchFragment = new BlockwatchFragment().newInstance(); // Create a new watch fragment with a new hash
            getSupportFragmentManager().beginTransaction().replace(R.id.blockwatch_fragment, watchFragment, WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one with a new hash
        } catch (Exception e) {
            Log.d(getString(R.string.explorer_error), e.getMessage());
        }
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
        mSwipeRefreshLayout.setRefreshing(true);
        /*mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                BlockwatchSyncAdapter.syncImmediately(getBaseContext());
            }
        });*/
        //mSwipeRefreshLayout.setRefreshing(false);
        BlockwatchSyncAdapter.syncImmediately(getBaseContext());
        watchFragment = new BlockwatchFragment().newInstance(); // Create a new watch fragment with a new hash
        getSupportFragmentManager().beginTransaction().replace(R.id.blockwatch_fragment, watchFragment, WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one with a new hash

        mSwipeRefreshLayout.setRefreshing(false);
/*        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 5000);*/

        //mSwipeRefreshLayout.setEnabled(true);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
    }
}
