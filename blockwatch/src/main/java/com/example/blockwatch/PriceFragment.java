package com.example.blockwatch;

import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        String jsonHistoricalPricesResponse=""; // Initialize the JSON response
        if (!data.isNull(8)) {
            jsonHistoricalPricesResponse = data.getString(8); // Get the string representing the JSON of historical prices
        }
        try { // Try parsing the JSON to get the price array
            price_array = TransactionJsonUtils.getHistoricalPricesFromJson(jsonHistoricalPricesResponse);
        } catch(JSONException e){
            e.printStackTrace();
        }
        List<Entry> entries = new ArrayList<>(); // Create a specific object of entries
        int i;
        final String[] dates = new String[366];
        DateFormat df = new SimpleDateFormat("M/d/yy", Locale.US);
        Date dateString;
        int entries_index=0;
        for (i=0; i<price_array.length; i++) {
            // Turn your data into Entry objects
            if(price_array[i][1]!=0) { // Only add if you didn't somehow get a 0 value (sometimes the JSON response is a different length)
                entries.add(new Entry((float) entries_index, (float) price_array[i][1]));
                // The labels that should be drawn on the XAxis
                dateString = new Date((long) price_array[i][0] * 1000);
                dates[entries_index] = String.valueOf(df.format(dateString));
                entries_index++; // Increment the index for storing the entries only if you found a non-zero number
            }
        }
        entries.add(new Entry((float) i,(float) data.getDouble(7)));
        dateString = new Date(System.currentTimeMillis()/1000);
        dates[i] = String.valueOf(df.format(dateString));
        // Price history
        LineChart priceHistoryChart = (LineChart) rootView.findViewById(R.id.price_chart);
        LineDataSet dataSet = new LineDataSet(entries, ""); // Add entries to dataset
        dataSet.setColor(Color.BLACK);
        dataSet.setValueTextColor(Color.BLACK); //
        dataSet.setCircleColor(Color.BLACK);
        dataSet.setCircleRadius(2f);
        LineData lineData = new LineData(dataSet);

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return dates[(int) value+1];
            }
        };
        XAxis xAxis = priceHistoryChart.getXAxis(); // Put dates on the x-axis
        xAxis.setGranularity(15f); // minimum axis-step (interval) is 15 days
        xAxis.setValueFormatter(formatter);
        xAxis.setLabelRotationAngle(45);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);

        YAxis yAxis = priceHistoryChart.getAxisLeft(); // Put dates on the y-axis
        yAxis.setTextSize(16);

        priceHistoryChart.getAxisRight().setEnabled(false);
        priceHistoryChart.setData(lineData);
        priceHistoryChart.setDrawMarkers(false);
        priceHistoryChart.setAutoScaleMinMaxEnabled(true);

        priceHistoryChart.setDrawGridBackground(false);
        priceHistoryChart.setDescription(null);
        priceHistoryChart.getLegend().setEnabled(false);
        priceHistoryChart.setVisibleXRangeMaximum(100);
        //priceHistoryChart.animateX(2500, Linear);
        //priceHistoryChart.moveViewToX(300);
        priceHistoryChart.moveViewToAnimated(300, 1000, YAxis.AxisDependency.LEFT, 2000);
        //priceHistoryChart.centerViewToAnimated(300,1000, YAxis.AxisDependency.LEFT,2000);
        //priceHistoryChart.invalidate(); // Refresh
        priceHistoryChart.setVisibleXRangeMaximum(1000);

        AdView mAdView = (AdView) rootView.findViewById(R.id.adViewDetailPrice);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        mAdView.loadAd(adRequest);

        if (!data.isNull(7)) {
            TextView detailPrice = (TextView) layout.findViewById(R.id.current_price_detail);
            TextView percentagePrice = (TextView) layout.findViewById(R.id.current_price_percentage_change);
            NumberFormat priceFormatter = new DecimalFormat("#0.00");
            priceFormatter.setMinimumFractionDigits(2);
            priceFormatter.setMaximumFractionDigits(2);
            String formattedCurrentPrice = priceFormatter.format(data.getDouble(7)); // Get the current price

            NumberFormat percentFormat = NumberFormat.getPercentInstance();
            percentFormat.setMinimumFractionDigits(2);
            percentFormat.setMaximumFractionDigits(2);
            String formattedPercentageChange = "("+percentFormat.format((data.getDouble(7) - price_array[price_array.length - 1][1])/price_array[price_array.length - 1][1])+")"; // Get the price percentage change from yesterday

            if (data.getDouble(7) < price_array[price_array.length - 1][1]) { // If today's price is currently less than yesterday's closing price
                detailPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.md_red_500)); // Color the price red
                percentagePrice.setTextColor(ContextCompat.getColor(getContext(), R.color.md_red_500)); // Color the price red
                Drawable img = ContextCompat.getDrawable(getContext(), R.mipmap.trending_down);
                img.setBounds(0, 0, 100, 100);
                detailPrice.setCompoundDrawables(null, null, img, null); // Put a trending up button inside        //entries.add(new Entry((float) price_array[i][0], (float) price_array[i][1]));
            } else {
                detailPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.md_light_green_500)); // Otherwise, color it green
                percentagePrice.setTextColor(ContextCompat.getColor(getContext(), R.color.md_light_green_500)); // Color the price red
                Drawable img = ContextCompat.getDrawable(getContext(), R.mipmap.trending_up);
                img.setBounds(0, 0, 100, 100);
                detailPrice.setCompoundDrawables(null,null,img,null); // Put a trending up button inside
            }
            detailPrice.setText(formattedCurrentPrice);
            percentagePrice.setText(formattedPercentageChange);
        }
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
