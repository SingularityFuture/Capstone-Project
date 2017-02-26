package com.example.blockwatch;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


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
/*        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }*/
    }

/*    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView =inflater.inflate(R.layout.fragment_blockwatch, container, false);
        layout = (RelativeLayout) rootView.findViewById(R.id.watch_fragment_layout);
        pV=new PaintView(getActivity(),currentHash); // Create a new paint view for the watch face
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); // Set width and height
        pV.setBackgroundColor(Color.WHITE); // Set the background white
        pV.setLayoutParams(params); // Apply the layout width and height
        if(android.os.Build.VERSION.SDK_INT>20)
            pV.setElevation(200); // Set elevation if SDK > 20
        int newID = pV.generateViewId(); // Generate a new unique ID
        pV.setId(newID); // Set the ID here
        pV.setSaveEnabled(true); // Make sure it saves its state
        pV.setOnClickListener(this); // Set the onClick listener to call back to the activity
        layout.addView(pV); // Add the view to the fragment layout

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onClick(final View v) { //check for what button is pressed
        callBack_result = mListener.onFragmentInteraction("hello");
        //Toast.makeText(getActivity(),callBack_result,Toast.LENGTH_LONG).show(); // Show the result
    }

/*  public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    } */

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

    /*@Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    } */

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
}
