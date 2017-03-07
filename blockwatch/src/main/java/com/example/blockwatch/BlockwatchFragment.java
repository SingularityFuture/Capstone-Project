package com.example.blockwatch;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {//@link BlockwatchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BlockwatchFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * 2/2017 Michael Mebane
 * Fragment that shows the main BlockWatch face on the mobile side
 */
public class BlockwatchFragment extends Fragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;

    PaintView pV;  // Declare paintView to put the watch in
    View rootView; // Declare rootView
    RelativeLayout layout; // Declare layout that will access fragment layout
    String callBack_result; // Temp variable to make sure callback fragment listener works
    String currentHash; // Store the updated transaction hash here

    public BlockwatchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment BlockwatchFragment.
     */
    public BlockwatchFragment newInstance(String currentHash) {
        BlockwatchFragment fragment = new BlockwatchFragment();
        fragment.currentHash = currentHash; // Put the current hash into this instance's member variable
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView =inflater.inflate(R.layout.fragment_blockwatch, container, false);
        layout = (RelativeLayout) rootView.findViewById(R.id.watch_fragment_layout);


/*        if(getActivity().getResources().getConfiguration().orientation==2){
            mAdView.setRotation(-90);  // Rotate 90 degrees for landscape orientation
            mAdView.setY(200);
            //mAdView.setLeft(-100);
            mAdView.setX(0);
        }*/
        //mAdView.loadAd(adRequest);

        pV=new PaintView(getActivity(),currentHash); // Create a new paint view for the watch face
        RelativeLayout.LayoutParams paramsPaint = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT); // Set width and height
        pV.setBackgroundColor(Color.TRANSPARENT); // Set the background white
        pV.setLayoutParams(paramsPaint); // Apply the layout width and height
        if(android.os.Build.VERSION.SDK_INT>20)
            pV.setElevation(200); // Set elevation if SDK > 20
        int newID = pV.generateViewId(); // Generate a new unique ID
        pV.setId(newID); // Set the ID here
        pV.setSaveEnabled(true); // Make sure it saves its state
        pV.setOnClickListener(this); // Set the onClick listener to call back to the activity
        layout.addView(pV); // Add the view to the fragment layout

        AdView mAdView = (AdView) layout.findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
/*        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_TOP, pV.getId());
        mAdView.setLayoutParams(params);*/
        mAdView.loadAd(adRequest);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onClick(final View v) { //check for what button is pressed
        callBack_result = mListener.onFragmentInteraction("hello");
        //Toast.makeText(getActivity(),callBack_result,Toast.LENGTH_LONG).show(); // Show the result
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
        String onFragmentInteraction(String string);
    }

    // Implement the method for when the configuration changes
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        renewAd();
    }

    /* Renew adView during configuration changes
     */
    public void renewAd(){
        // Remove the ad keeping the attributes
/*        layout = (RelativeLayout) rootView.findViewById(R.id.watch_fragment_layout);
        AdView mAdView = (AdView) layout.findViewById(R.id.adView);
        layout.removeView(mAdView);

        // Re-initialise the ad
        mAdView.destroy();
        mAdView = new AdView(getActivity());
        mAdView.setAdSize(BANNER);
        mAdView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        mAdView.setId(R.id.adView);*/

/*        if(getActivity().getResources().getConfiguration().orientation==2) {
            layout = (RelativeLayout) rootView.findViewById(R.id.watch_fragment_layout);
            AdView mAdView = (AdView) layout.findViewById(R.id.adView);
            mAdView.setRotation(90);
            //RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(150, 150); // Set width and height
            //lp.addRule(RelativeLayout.ALIGN_LEFT);
            //mAdView.setLayoutParams(lp);
        }*/
        //layout.addView(mAdView);

        // Re-fetch add and check successful load
/*        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();

        mAdView.loadAd(adRequest);*/
    }

}
