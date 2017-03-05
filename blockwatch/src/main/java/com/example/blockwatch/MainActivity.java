package com.example.blockwatch;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import data.BlockContract;
import data.BlockDbHelper;
import data.BlockExplorerClass;
import data.BlockProvider;
import utilities.BlockchainSyncIntentService;

public class MainActivity extends AppCompatActivity implements BlockwatchFragment.OnFragmentInteractionListener, View.OnClickListener{

    private Fragment watchFragment; // Declare the fragment you will include
    private static final String WATCH_FRAGMENT_TAG = "watch_fragment"; // Create a tag to keep track of created fragments
    private String hash = "b5357533bf43d6793aa24d91d6a01055128bff64730627bbb3a512b04d2e9043";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the main activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Get the toolbar ID
        setSupportActionBar(toolbar); // Set the toolbar

        /*
        Temporary to test connection - will put in an IntentService laterHi
        */
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        if (getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG) == null) { // If the fragment doesn't exist yet,
            try {
                hash =  BlockExplorerClass.test(new String[]{"test"}); // Update the watch at the beginning with a fresh hash
                BlockContract currentTransaction = new BlockContract();
            }
            catch (Exception e){
                Log.d("Explorer error: ", e.getMessage());
            }

            //ContentValues transactionDetails = new ContentValues(); // Create transaction details to put in the first creation of the database
            //transactionDetails.put(BlockContract.BlockEntry.COLUMN_HASH,hash); // Put the current hash
            //getContentResolver().insert(BlockContract.BlockEntry.CONTENT_URI,transactionDetails); //

            Intent intentToSyncImmediately = new Intent(this, BlockchainSyncIntentService.class); // Update the ContentProvider with this hash
            this.startService(intentToSyncImmediately);

            watchFragment = new BlockwatchFragment().newInstance(hash); // Add the watch fragment here, passing the context as an implementation of the fragment listener
            getSupportFragmentManager().beginTransaction().add(R.id.transaction_fragment,watchFragment,WATCH_FRAGMENT_TAG).commit(); // Add the fragment to the transaction

        }
        else {
           watchFragment = getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG); // Else if it exists
           getSupportFragmentManager().beginTransaction().replace(R.id.transaction_fragment,watchFragment,WATCH_FRAGMENT_TAG).commit(); // Replace the fragment with the current one
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("action", null).show();
//        WebsocketHelper socketHelper = new WebsocketHelper();
//        WebSocket ws = socketHelper.createSocket(this);
        //ws.sendClose();

        //BlockExplorerClass explorerClass = new BlockExplorerClass();  Class is currently static so no need for an instance right now
        try {
            hash =  BlockExplorerClass.test(new String[]{"test"}); // Get a fresh hash
            Intent intentToSyncImmediately = new Intent(this, BlockchainSyncIntentService.class); // Update the ContentProvider with this hash3

            this.startService(intentToSyncImmediately);
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
        // Launch the ScheduleActivity.
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


/*    @Override
    protected void onPause() {
        super.onPause();
        if (getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG) != null)
            getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG).setRetainInstance(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG) != null)
            getSupportFragmentManager().findFragmentByTag(WATCH_FRAGMENT_TAG).getRetainInstance();
    }*/
}
