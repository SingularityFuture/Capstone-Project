package com.example.blockwatch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Michael on 2/16/2017.
 */
public class TransactionFragment extends Fragment{

    View rootView; // Declare rootView
    LinearLayout layout; // Declare layout that will access fragment layout

    public TransactionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment BlockwatchFragment.
     */
    public TransactionFragment newInstance() {
        TransactionFragment fragment = new TransactionFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView =inflater.inflate(R.layout.fragment_transaction, container, false);
        layout = (LinearLayout) rootView.findViewById(R.id.transaction_fragment_layout);
        TextView text = new TextView(getActivity());
        text.setText("test");
        text.setTextColor(Color.BLACK);
        text.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT); // Set width and height
        text.setBackgroundColor(Color.WHITE); // Set the background white
        //text.setLayoutParams(params); // Apply the layout width and height
        text.setWidth(150);
        text.setHeight(150);
        text.setTextSize(40);

        layout.addView(text);

        Toast.makeText(getActivity(),"Inside Transaction",Toast.LENGTH_LONG).show(); // Show the result

        // Inflate the layout for this fragment
        return rootView;
    }

}
