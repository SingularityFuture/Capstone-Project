package com.example.blockwatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by Michael on 2/16/2017.
 */
public class TransactionDetailActivity extends AppCompatActivity{
    private Fragment transactionFragment; // Declare the fragment you will include
    private static final String TRANSACTION_FRAGMENT_TAG = "transaction_fragment"; // Create a tag to keep track of created fragments

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction); // Set the transaction activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Get the toolbar ID
        setSupportActionBar(toolbar); // Set the toolbar

        if (getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG) == null) { // If the fragment doesn't exist yet,
            transactionFragment = new TransactionFragment().newInstance(); // Add the transaction fragment here, passing the context as an implementation of the fragment listener
            getSupportFragmentManager().beginTransaction().add(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG).commit(); // Add the fragment to the transaction
        }
        else {
            transactionFragment = getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG); // Else if it exists
            getSupportFragmentManager().beginTransaction().replace(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG).commit(); // Replace the fragment with the current one
        }
    }
}
