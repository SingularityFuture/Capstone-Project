package com.singularityfuture.blockwatch;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.singularityfuture.blockwatch.R;
import com.singularityfuture.blockwatch.PriceFragment;

/**
 * Created by Michael on 4/3/2017.
 */

public class PriceDetailActivity extends AppCompatActivity {

    private static final String PRICE_FRAGMENT_TAG = "price_fragment"; // Create a tag to keep track of created fragments
    private Fragment priceFragment; // Declare the fragment you will include

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price); // Set the price activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_price); // Get the toolbar ID
        toolbar.setTitle(R.string.price_detail);
        setSupportActionBar(toolbar); // Set the toolbar
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (getSupportFragmentManager().findFragmentByTag(PRICE_FRAGMENT_TAG) == null) { // If the fragment doesn't exist yet,
            priceFragment = new PriceFragment().newInstance(); // Add the transaction fragment here, passing the context as an implementation of the fragment listener
            getSupportFragmentManager().beginTransaction().add(R.id.price_fragment, priceFragment, PRICE_FRAGMENT_TAG).commit(); // Add the fragment to the transaction
        } else {
            priceFragment = getSupportFragmentManager().findFragmentByTag(PRICE_FRAGMENT_TAG); // Else if it exists
            getSupportFragmentManager().beginTransaction().replace(R.id.price_fragment, priceFragment, PRICE_FRAGMENT_TAG).commit(); // Replace the fragment with the current one
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}