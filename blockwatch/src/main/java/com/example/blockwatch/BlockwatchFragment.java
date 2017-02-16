package com.example.blockwatch;

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
public class BlockwatchFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/*
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;
*/
    PaintView pV;  // Declare paintView to put the watch in
    View rootView; // Declare rootView
    RelativeLayout layout; // Declare layout that will access fragment layout



    public BlockwatchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BlockwatchFragment.
     */
    public static BlockwatchFragment newInstance(String param1, String param2) {
        BlockwatchFragment fragment = new BlockwatchFragment();
/*        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        //setRetainInstance(true);
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
        pV=new PaintView(getActivity()); // Create a new paint view for the watch face
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); // Set width and height
        pV.setBackgroundColor(Color.WHITE); // Set the background white
        pV.setLayoutParams(params); // Apply the layout width and height
        if(android.os.Build.VERSION.SDK_INT>20)
            pV.setElevation(200); // Set elevation if SDK > 20
        int newID = pV.generateViewId(); // Generate a new unique ID
        pV.setId(newID); // Set the ID here
        pV.setSaveEnabled(true); // Make sure it saves its state
        layout.addView(pV); // Add the view to the fragment layout

        // Inflate the layout for this fragment
        return rootView;
    }

/*  public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    *//**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *//*
    public interface OnFragmentInteractionListener {
        //
        void onFragmentInteraction(Uri uri);
    }*/
}
