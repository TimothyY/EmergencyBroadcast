package com.timeandtidestudio.emergencybroadcast.Fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;
import com.timeandtidestudio.emergencybroadcast.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    EditText etName, etNumber, etAddress;
//    EditText etMessage;
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
        if (getArguments() != null) {
        }
    }

    TelephonyManager tMgr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);
        etName = v.findViewById(R.id.etName);
        etName.setText(PreferencesHelper.getString(PreferencesHelper.USER_NAME));
        tMgr = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE},
                    1);
        }
        etNumber = v.findViewById(R.id.etPhoneNumber);
        etNumber.setText(PreferencesHelper.getString(PreferencesHelper.USER_NUMBER));
        etAddress = v.findViewById(R.id.etAddress);
        etAddress.setText(PreferencesHelper.getString(PreferencesHelper.USER_ADDRESS));
//        etMessage = v.findViewById(R.id.etMessageDraft);
//        etMessage.setText(PreferencesHelper.getString(PreferencesHelper.USER_MESSAGE));
        sbVibrate = v.findViewById(R.id.seekBarVibrate);
        sbVibrate.setOnSeekBarChangeListener(this);
        sbVibrate.post(new Runnable() {
            @Override
            public void run() {
                sbVibrate.setProgress(PreferencesHelper.getInt(PreferencesHelper.SENSOR_SENSITIVITY_INT));
            }
        });
        btnSave = v.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        PreferencesHelper.putString(PreferencesHelper.USER_NAME,etName.getText().toString());
        PreferencesHelper.putString(PreferencesHelper.USER_NUMBER,etNumber.getText().toString());
        PreferencesHelper.putString(PreferencesHelper.USER_ADDRESS,etAddress.getText().toString());
        String generatedMessage = "HELP! I ("+ etName.getText().toString() +"), am facing an accident & currently unable to move. "
                +"MyContact:"+ etNumber.getText().toString()
                +". MyAddress: "+etAddress.getText().toString()
                +".";
        PreferencesHelper.putString(PreferencesHelper.USER_MESSAGE,generatedMessage);
        PreferencesHelper.putInt(PreferencesHelper.SENSOR_SENSITIVITY_INT,sbVibrate.getProgress());
        Toast.makeText(getContext(),getResources().getText(R.string.data_saved),Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {}

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
