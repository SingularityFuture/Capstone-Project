package com.example.blockwatch;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.MobileAds;

import data.BlockContract;
import data.BlockExplorerClass;
import sync.BlockwatchSyncAdapter;

public class MainActivity extends AppCompatActivity implements BlockwatchFragment.OnFragmentInteractionListener, View.OnClickListener{

    private Fragment watchFragment; // Declare the fragment you will include
    private Fragment transactionFragment; // Declare the fragment you will include
    private static final String WATCH_FRAGMENT_TAG = "watch_fragment"; // Create a tag to keep track of created fragments
    private static final String TRANSACTION_FRAGMENT_TAG = "transaction_fragment";
    private String hash = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043"; //getString(R.string.dummy_hash); // Start with a dummy hash in case of any network error

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the main activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Get the toolbar ID
        setSupportActionBar(toolbar); // Set the toolbar
        /*
        Temporary to retrieveUnconfirmedTransactions connection - will put in an IntentService laterHi
        */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        if (getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG) == null) { // If the fragment doesn't exist yet,

            BlockwatchSyncAdapter.initializeSyncAdapter(this);

            watchFragment = new BlockwatchFragment().newInstance(hash); // Add the watch fragment here, passing the context as an implementation of the fragment listener
            watchFragment.setRetainInstance(true); // Do this so that it retains the member variable holding the hash
            getSupportFragmentManager().beginTransaction().add(R.id.transaction_fragment,watchFragment,WATCH_FRAGMENT_TAG).commit(); // Add the fragment to the transaction

        }
        else {
           watchFragment = getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG); // Else if it exists
           getSupportFragmentManager().beginTransaction().replace(R.id.transaction_fragment,watchFragment,WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one
        }

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG) == null) {
                transactionFragment = new TransactionFragment().newInstance(BlockContract.BlockEntry.CONTENT_URI); // Add the transaction fragment here, passing the context as an implementation of the fragment listener
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG)
                        .commit();
            }
            else{
                transactionFragment = getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG); // Else if it exists

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Snackbar.make(view, R.string.fab_text, Snackbar.LENGTH_LONG)
                .setAction("action", null).show();
        try {
            hash =  BlockExplorerClass.retrieveUnconfirmedTransactions(); // Get a fresh hash
            /*Intent intentToSyncImmediately = new Intent(this, BlockchainSyncIntentService.class); // Update the ContentProvider with this hash3
            intentToSyncImmediately.putExtra("currentHash", hash); // Update the intent with a new hash
            this.startService(intentToSyncImmediately); // Sync the database with new information from the JSON query
*/
            BlockwatchSyncAdapter.syncImmediately(this);
            watchFragment = new BlockwatchFragment().newInstance(hash); // Create a new watch fragment with a new hash
            getSupportFragmentManager().beginTransaction().replace(R.id.transaction_fragment,watchFragment,WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one with a new hash
        }
        catch (Exception e){
            Log.d("Explorer error: ", e.getMessage());
        }
    }

    //@Override
    public String onFragmentInteraction(String string){
        // Here you should launch a new fragment that shows the details of the clicked transaction
        Intent intent = new Intent(this, TransactionDetailActivity.class);
        intent.putExtra("URI", BlockContract.BlockEntry.CONTENT_URI);
        startActivity(intent);
        return string+string+string;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
    }
}
