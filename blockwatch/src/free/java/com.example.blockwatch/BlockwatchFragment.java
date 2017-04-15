package com.example.blockwatch;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import data.BlockContract;
import utilities.TransactionJsonUtils;

import static android.content.Context.WINDOW_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {//@link BlockwatchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlockwatchFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p>
 * 2/2017 Michael Mebane
 * Fragment that shows the main BlockWatch face on the mobile side
 */
public class BlockwatchFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int ID_BLOCKWATCH_LOADER = 444;
    PaintView pV = null;  // Declare paintView to put the watch in
    View rootView; // Declare rootView
    RelativeLayout layout; // Declare layout that will access fragment layout
    private OnFragmentInteractionListener mListener; // Declare the listener to click on the fragment
    double[][] price_array = new double[365][365]; // Declare the array of historical prices
    static final int MSG_UPDATE_TIME = 0;
    static final int INTERACTIVE_UPDATE_RATE_MS = 60 * 1000; // How often to update the watch, in milliseconds
    String currentHash = "";
    boolean isTablet;
    boolean refreshNow = false;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param refreshNow
     * @return A new instance of fragment BlockwatchFragment.
     */
    public BlockwatchFragment newInstance(boolean refreshNow) {
        this.refreshNow = refreshNow;
        return new BlockwatchFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* This connects our Activity into the loader lifecycle. */
        getLoaderManager().initLoader(ID_BLOCKWATCH_LOADER, null, this).forceLoad();
        isTablet = getActivity().getResources().getBoolean(R.bool.isTablet); // Detect whether we are in tablet mode, which will affect the drawing size and coordinates
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    // Handler to update the time once a minute, called from when the Loader gets a new hash from the blockchain
    final Handler mUpdateTimeHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_UPDATE_TIME:
                    pV.setCurrentHash(currentHash); // Change the hash when the minute changes
                    pV.invalidate(); // Redraw the watch
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_blockwatch, container, false);
        layout = (RelativeLayout) rootView.findViewById(R.id.watch_fragment_layout); // Inflate the layout for this fragment

        AdView mAdView = (AdView) layout.findViewById(R.id.adView);
        if (!isTablet) { // In tablet mode, you will load the big ad from the main activity
            // Load the ad here since it doesn't depend on the loader finishing loading
            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .addTestDevice("TEST_DEVICE_ID")
                    .build();
            mAdView.loadAd(adRequest);
        } else {
            // Otherwise, take it out so you don't have two ads
            layout.removeView(mAdView);
        }

        return rootView;
    }

    @Override
    public void onClick(final View v) { //check for what button is pressed
        mListener.onFragmentInteraction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(ID_BLOCKWATCH_LOADER, null, this);
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
            case ID_BLOCKWATCH_LOADER:
                return new CursorLoader(getActivity(),
                        BlockContract.BlockEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

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

        WindowManager wm = (WindowManager) getContext().getSystemService(WINDOW_SERVICE); // Get the window manager
        DisplayMetrics metrics = new DisplayMetrics(); // Declare a metrics object
        wm.getDefaultDisplay().getMetrics(metrics); // Get the metrics of the window
        int rotation = wm.getDefaultDisplay().getRotation(); // Get the orientation of the screen

        // This represents the current transaction hash
        if (!data.isNull(1)) {
            currentHash = data.getString(1);
            if (pV == null) {
                pV = new PaintView(getActivity(), currentHash); // Create a new paint view for the watch face
                pV.setOnClickListener(this); // Set the onClick listener to call back to the activity
                layout.addView(pV); // Add the view to the fragment layout
            }
            if (refreshNow) {
                pV.invalidate(); // If this was the result of swiping down, refresh the watch now
            } else { // Otherwise, only refresh it once the minute changes.
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler
                        .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        Button buttonPrice = (Button) layout.findViewById(R.id.current_price);
        if (!data.isNull(7)) {
                NumberFormat formatter = new DecimalFormat("#0.00");
                formatter.setMinimumFractionDigits(2);
                formatter.setMaximumFractionDigits(2);
                String formattedCurrentPrice = formatter.format(data.getDouble(7)); // Get the current price
                if (!data.isNull(8)) {
                    String jsonHistoricalPricesResponse = data.getString(8); // Get the string representing the JSON of historical prices
                    try { // Try parsing the JSON to get the price array
                        price_array = TransactionJsonUtils.getHistoricalPricesFromJson(jsonHistoricalPricesResponse);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            Drawable img;
            if (data.getDouble(7) < price_array[price_array.length - 1][1]) { // If today's price is currently less than yesterday's closing price
                buttonPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.md_red_500)); // Color the price red
                img = ContextCompat.getDrawable(getContext(), R.mipmap.trending_down);
            } else {
                buttonPrice.setTextColor(ContextCompat.getColor(getContext(), R.color.md_light_green_500)); // Otherwise, color it green
                img = ContextCompat.getDrawable(getContext(), R.mipmap.trending_up);
            }
            img.setBounds(0, 0, 100, 100);

            RelativeLayout.LayoutParams paramsText = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); // Set width and height
            if ((rotation == Surface.ROTATION_90
                    || rotation == Surface.ROTATION_270) && !isTablet) { // If it's in landscape mode and not a tablet,
                buttonPrice.setCompoundDrawablesWithIntrinsicBounds(null, null, null, img); // Put a trending button inside
                paramsText.addRule(RelativeLayout.START_OF, pV.getId());
                paramsText.addRule(RelativeLayout.CENTER_VERTICAL, pV.getId());
            } else {
                buttonPrice.setCompoundDrawables(null, null, img, null); // Put a trending button inside
                paramsText.addRule(RelativeLayout.BELOW, pV.getId());
                paramsText.addRule(RelativeLayout.ALIGN_START, pV.getId());
            }
            buttonPrice.setLayoutParams(paramsText); // Apply the layout width and height
            buttonPrice.setText(formattedCurrentPrice);
            buttonPrice.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), PriceDetailActivity.class);
                    startActivity(intent);
                }
            });
        }

        ImageButton donateButton = (ImageButton) layout.findViewById(R.id.donate);
        final Drawable QRCode;
        QRCode = ContextCompat.getDrawable(getContext(), R.mipmap.donate_qr);

        RelativeLayout.LayoutParams paramsQR = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); // Set width and height
        if ((rotation == Surface.ROTATION_90
                || rotation == Surface.ROTATION_270) && !isTablet) { // If it's in landscape mode and not a tablet,
            paramsQR.addRule(RelativeLayout.END_OF, pV.getId());
            paramsQR.addRule(RelativeLayout.CENTER_VERTICAL, pV.getId());
        }   else{
            paramsQR.addRule(RelativeLayout.BELOW, pV.getId());
            paramsQR.addRule(RelativeLayout.ALIGN_END, pV.getId());
        }
        donateButton.setLayoutParams(paramsQR);
        donateButton.setImageDrawable(QRCode);

        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(getActivity().getLayoutInflater().inflate(R.layout.donate_layout, null))
                        .setNegativeButton(R.string.dont_donate, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.copy_address, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Donation Address", getString(R.string.address_hash));
                                clipboard.setPrimaryClip(clip);
                                Toast toast = Toast.makeText(getActivity(), getString(R.string.address_copied), Toast.LENGTH_LONG);
                                try {
                                    ((TextView) ((LinearLayout) toast.getView()).getChildAt(0))
                                            .setGravity(Gravity.CENTER_HORIZONTAL);
                                } catch (ClassCastException e) {
                                    Log.d("Watch Fragment", "Toast not cast correctly");
                                }
                                toast.show();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
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

    /*
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information. */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction();
    }
}
