package com.example.blockwatch;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by Michael on 2/16/2017.
 */
public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    View rootView; // Declare rootView
    LinearLayout layout; // Declare layout that will access fragment layout
    private String currentHash = ""; // Set default version of the transaction
    private Uri mURI; // Declare URI for loader query
    /*
     * This ID will be used to identify the Loader responsible for loading the weather details
     * for a particular day. In some cases, one Activity can deal with many Loaders. However, in
     * the loader for best practice. Please note that 353 was chosen arbitrarily. You can use
     * our case, there is only one. We will stilwhatel use this ID to initialize the loader and create
     * whatever number you like, so long as it is unique and consistent.
    */
    private static final int ID_DETAIL_LOADER = 353;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment BlockwatchFragment.
     */
    public TransactionFragment newInstance(Uri mURI) {
        TransactionFragment fragment = new TransactionFragment();
        fragment.mURI = mURI;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* This connects our Activity into the loader lifecycle. */
        getLoaderManager().initLoader(ID_DETAIL_LOADER, null, this).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView =inflater.inflate(R.layout.fragment_transaction, container, false);

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Creates and returns a CursorLoader that loads the data for our URI and stores it in a Cursor.
     *
     * @param loaderId The loader ID for which we need to create a loader
     * @param loaderArgs Any arguments supplied by the caller
     *
     * @return A new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {

        switch (loaderId) {

            case ID_DETAIL_LOADER:

                Loader<Cursor> tempCursor = new CursorLoader(getActivity(),
                        mURI,
                        null,
                        null,
                        null,
                        null);

                return tempCursor;

            default:
                throw new RuntimeException("Loader Not Implemented: " + loaderId);
        }
    }

    /**
     * Runs on the main thread when a load is complete. If initLoader is called (we call it from
     * onCreate in TransactionDetailActivity) and the LoaderManager already has completed a previous load
     * for this Loader, onLoadFinished will be called immediately. Within onLoadFinished, we bind
     * the data to our views so the user can see the details of the weather on the date they
     * selected from the forecast.
     *
     * @param loader The cursor loader that finished.
     * @param data   The cursor that is being returned.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        boolean cursorHasValidData = false;

        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }
        layout = (LinearLayout) rootView.findViewById(R.id.transaction_fragment_layout);

        if(!data.isNull(1)) {
            // Add hash value
            TextView textHash = new TextView(getActivity());
            textHash.setText("TX Hash: " + data.getString(1));
            textHash.setSingleLine(false); // Make it multiline
            textHash.setTextColor(Color.RED);
            textHash.setGravity(Gravity.TOP);
            textHash.setBackgroundColor(Color.WHITE); // Set the background white
            textHash.setWidth(150);
            textHash.setTextSize(20);
            layout.addView(textHash);
        }

        if(!data.isNull(3)) {
            // Relayed By IP text
            TextView textRelayedBy = new TextView(getActivity());
            textRelayedBy.setText("Relayed By: " + data.getString(3));
            textRelayedBy.setTextColor(Color.GREEN);
            textRelayedBy.setBackgroundColor(Color.WHITE); // Set the background white
            textRelayedBy.setSingleLine(false); // Make it multiline
            textRelayedBy.setWidth(150);
            textRelayedBy.setPadding(0,50,0,0);
            textRelayedBy.setTextSize(20);
            layout.addView(textRelayedBy);
        }
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        mAdView.loadAd(adRequest);
    }

    /**
     * Called when a previously created loader is being reset, thus making its data unavailable.
     * The application should at this point remove any references it has to the Loader's data.
     * Since we don't store any of this cursor's data, there are no references we need to remove.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
