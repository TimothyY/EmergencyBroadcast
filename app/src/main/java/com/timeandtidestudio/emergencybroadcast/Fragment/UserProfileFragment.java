package com.timeandtidestudio.emergencybroadcast.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.timeandtidestudio.emergencybroadcast.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    EditText etName,etNumber,etAddress,etMessage;
    SeekBar sbVibrate;
    Button btnSave;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {}
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);
        etName = v.findViewById(R.id.etName);
        etNumber = v.findViewById(R.id.etPhoneNumber);
        etAddress = v.findViewById(R.id.etAddress);
        etMessage = v.findViewById(R.id.etMessageDraft);
        sbVibrate = v.findViewById(R.id.seekBarVibrate);
        sbVibrate.setOnSeekBarChangeListener(this);
        btnSave = v.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        Log.v("UsrPrfFrg","Button clicked");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        Log.v("UsrPrfFrg","Seekbar changed: "+i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
