package com.example.blockwatch;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import data.BlockContract;
import utilities.TransactionJsonUtils;

/**
 * Created by Michael on 2/16/2017.
 */
public class PriceFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int ID_DETAIL_LOADER = 444;
    View rootView; // Declare rootView
    RelativeLayout layout; // Declare layout that will access fragment layout
    double[][] price_array = new double[365][365]; // Declare the array of historical prices

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BlockwatchFragment.
     */
    public PriceFragment newInstance() {
        PriceFragment fragment = new PriceFragment();
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
        rootView = inflater.inflate(R.layout.fragment_price, container, false);
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
                        BlockContract.BlockEntry.CONTENT_URI,
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
        layout = (RelativeLayout) rootView.findViewById(R.id.price_fragment_layout);

        if (!data.isNull(7)){
            String jsonHistoricalPricesResponse = data.getString(8); // Get the string representing the JSON of historical prices
            if(!data.isNull(8)){
                try { // Try parsing the JSON to get the price array
                    price_array = TransactionJsonUtils.getHistoricalPricesFromJson(jsonHistoricalPricesResponse);
                } catch(JSONException e){
                    e.printStackTrace();
                }
            }

            List<Entry> entries = new ArrayList<>(); // Create a specific object of entries
            int temp=price_array.length;
            for (int i=0; i<365; i++) {
                // Turn your data into Entry objects
                entries.add(new Entry((float) price_array[i][0], (float) price_array[i][1]));
            }
            entries.add(new Entry((float) System.currentTimeMillis() / 1000f,(float) data.getDouble(7)));
            // Price history
            LineChart priceHistoryChart = (LineChart) rootView.findViewById(R.id.price_chart);
            LineDataSet dataSet = new LineDataSet(entries, "Label"); // add entries to dataset
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK); //
            LineData lineData = new LineData(dataSet);
            priceHistoryChart.setData(lineData);
            priceHistoryChart.invalidate(); // Refresh
        }

        AdView mAdView = (AdView) rootView.findViewById(R.id.adViewDetailPrice);
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

    // Implement the method for when the configuration changes
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
