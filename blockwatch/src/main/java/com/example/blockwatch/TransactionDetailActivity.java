package com.example.blockwatch;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Michael on 2/16/2017.
 */
public class TransactionDetailActivity extends AppCompatActivity implements OnMapReadyCallback{
    private Fragment transactionFragment; // Declare the fragment you will include
    private static final String TRANSACTION_FRAGMENT_TAG = "transaction_fragment"; // Create a tag to keep track of created fragments
    private Uri mURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction); // Set the transaction activity
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_transaction); // Get the toolbar ID
        toolbar.setTitle(R.string.transaction_detail);
        setSupportActionBar(toolbar); // Set the toolbar

        mURI = getIntent().getParcelableExtra("URI");
        if (mURI == null) throw new NullPointerException("URI for DetailActivity cannot be null");

        if (getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG) == null) { // If the fragment doesn't exist yet,
            transactionFragment = new TransactionFragment().newInstance(mURI); // Add the transaction fragment here, passing the context as an implementation of the fragment listener
            //transactionFragment.setRetainInstance(true); // Do this so that it retains the text views already created the first time

            getSupportFragmentManager().beginTransaction().add(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG).commit(); // Add the fragment to the transaction
        }
        else {
            transactionFragment = getSupportFragmentManager().findFragmentByTag(TRANSACTION_FRAGMENT_TAG); // Else if it exists
            getSupportFragmentManager().beginTransaction().replace(R.id.transaction_fragment, transactionFragment, TRANSACTION_FRAGMENT_TAG).commit(); // Replace the fragment with the current one
        }
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we
     * just add a marker near Africa.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
}
