package com.example.blockwatch;

import android.content.SyncAdapterType;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.google.android.gms.internal.zzt.TAG;

/**
 * Created by Michael on 2/16/2017.
 */
public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnMapReadyCallback {

    /*
     * This ID will be used to identify the Loader responsible for loading the weather details
     * for a particular day. In some cases, one Activity can deal with many Loaders. However, in
     * the loader for best practice. Please note that 353 was chosen arbitrarily. You can use
     * our case, there is only one. We will still use this ID to initialize the loader and create
     * whatever number you like, so long as it is unique and consistent.
    */
    private static final int ID_DETAIL_LOADER = 353;
    View rootView; // Declare rootView
    RelativeLayout layout; // Declare layout that will access fragment layout
    MapView mMapView;
    LatLng currentLocation; // Declare the location of the current transaction based on the Relayed By IP address
    private Uri mURI; // Declare URI for loader query
    private SyncAdapterType mAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
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
        rootView = inflater.inflate(R.layout.fragment_transaction, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Creates and returns a CursorLoader that loads the data for our URI and stores it in a Cursor.
     *
     * @param loaderId   The loader ID for which we need to create a loader
     * @param loaderArgs Any arguments supplied by the caller
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
        layout = (RelativeLayout) rootView.findViewById(R.id.transaction_fragment_layout);

        if (!data.isNull(1)) {
            // Add hash value
            TextView textHash = (TextView) rootView.findViewById(R.id.transactionHash);
            textHash.setText(data.getString(1));
            textHash.setSingleLine(false); // Make it multiline
            textHash.setTextColor(Color.RED);
            textHash.setTextSize(19);
            textHash.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        RelativeLayout.LayoutParams relativeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        relativeLayoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.transactionHash);
        relativeLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.transactionHash);
        rootView.findViewById(R.id.transactionHashLabel).setLayoutParams(relativeLayoutParams);

        if (!data.isNull(3)) {
            // Relayed By IP text
            TextView textRelayedBy = (TextView) rootView.findViewById(R.id.transactionRelayedBy);
            textRelayedBy.setText(data.getString(3));
            textRelayedBy.setTextColor(Color.RED);
            textRelayedBy.setSingleLine(false); // Make it multiline
            textRelayedBy.setTextSize(19);
            textRelayedBy.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            RelativeLayout.LayoutParams relativeLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            relativeLayoutParams2.addRule(RelativeLayout.ALIGN_BASELINE, R.id.transactionRelayedByLabel);
            relativeLayoutParams2.addRule(RelativeLayout.ALIGN_START, R.id.transactionHash);
            relativeLayoutParams2.addRule(RelativeLayout.ALIGN_END, R.id.transactionHash);
            relativeLayoutParams2.addRule(RelativeLayout.END_OF, R.id.transactionRelayedByLabel);
            textRelayedBy.setLayoutParams(relativeLayoutParams2);
        }

        AdView mAdView = (AdView) rootView.findViewById(R.id.adViewDetail);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        mAdView.loadAd(adRequest);

        // For dropping a marker at a point on the Map
        currentLocation = new LatLng(data.getDouble(5), data.getDouble(6));
        mMapView.getMapAsync(this);
    }

    // Override this method for when the map is ready
    @Override
    public void onMapReady(GoogleMap map) {
        // Add a marker to current location
        Marker marker = map.addMarker(new MarkerOptions().position(currentLocation).title(getString(R.string.relayed_by_ip_location)).snippet(getString(R.string.location_of_ip)));
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getActivity(), R.raw.black_white_map_style));

/*            boolean success = map.setMapStyle(new MapStyleOptions(getResources()
                    .getString(R.string.black_white_map_style)));*/
            if (!success) {
                Log.e(TAG, getString(R.string.style_parsing_failed));
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, getString(R.string.cant_find_style), e);
        }
        map.animateCamera(CameraUpdateFactory.newLatLng(currentLocation));
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

    // Implement the method for when the configuration changes
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}