package com.timeandtidestudio.emergencybroadcast;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.timeandtidestudio.emergencybroadcast.Controller.Controller;
import com.timeandtidestudio.emergencybroadcast.Controller.EventTypes;
import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.SoundHelper;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.Utils;
import com.timeandtidestudio.emergencybroadcast.Fragment.AlarmFragment;
import com.timeandtidestudio.emergencybroadcast.Fragment.ContactListFragment;
import com.timeandtidestudio.emergencybroadcast.Fragment.SentMessagesFragment;
import com.timeandtidestudio.emergencybroadcast.Fragment.UserProfileFragment;
import com.timeandtidestudio.emergencybroadcast.Service.AlarmService;
import com.timeandtidestudio.emergencybroadcast.wizard.WizardMain;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    Context mCtx;
    private static final String TAG = "Main Activity";
    private static final int REQUEST_SMS = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCtx = this;

        if (!Utils.isServiceRunning(this, AlarmService.class)) startDetector();

        EventBus.getDefault().register(this);
        SoundHelper.initializeSoundsHelper(this);
        PreferencesHelper.initializePreferences(this);

        if (PreferencesHelper.getBoolean(Constants.PREFS_FIRST_START, true)) {
            PreferencesHelper.putBoolean(PreferencesHelper.FALL_DETECTION_ENABLED, true);
//            startActivity(new Intent(this, WizardMain.class));
        }

        Controller.initializeController(getApplicationContext());
        init();

        setContentView(R.layout.activity_main_old);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, SentMessagesFragment.newInstance(), "sentMessagesFragmentTag")
                .commit();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_create_white_24dp);
        fab.setTag("message");
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fab.getTag()=="message"){
                    Intent toCreateMessage = new Intent(mCtx,CreateMessageActivity.class);
                    startActivity(toCreateMessage);
                }else if(fab.getTag()=="contact"){
                    Intent toUpdateContact = new Intent(mCtx,UpdateContactActivity.class);
                    startActivity(toUpdateContact);
                }
            }
        });

        //ask permission for sending sms from the beginning
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasSMSPermission = ContextCompat.checkSelfPermission(getApplicationContext(),android.Manifest.permission.SEND_SMS);
            if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(android.Manifest.permission.SEND_SMS)) {
                    showMessageOKCancel("You need to allow access to Send SMS",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[] {android.Manifest.permission.SEND_SMS}, REQUEST_SMS);
                                    }
                                }
                            });
                    return;
                }
                requestPermissions(new String[] {android.Manifest.permission.SEND_SMS}, REQUEST_SMS);
                return;
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.sentMessages:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content, SentMessagesFragment.newInstance(), "sentMessagesFragmentTag")
                            .commit();
                    fab.setImageResource(R.drawable.ic_create_white_24dp);
                    fab.setTag("message");
                    fab.setVisibility(View.VISIBLE);
                    return true;
                case R.id.emergencyContactList:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content, ContactListFragment.newInstance(), "contactListFragmentTag")
                            .commit();
                    fab.setImageResource(R.drawable.ic_person_add_white_24dp);
                    fab.setTag("contact");
                    fab.setVisibility(View.VISIBLE);
                    return true;
                case R.id.userProfile:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content, UserProfileFragment.newInstance(), "userProfileFragmentTag")
                            .commit();
                    fab.setTag("");
                    fab.setVisibility(View.GONE);
                    return true;
            }
            return false;
        }

    };


    private void init() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // getSupportActionBar().setIcon(R.drawable.ic_launcher);

        boolean alarm_started = false;
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(AlarmService.ALARM_STARTED)) alarm_started = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Subscribe
    public void onEvent(EventTypes types) {
        switch (types) {
            case ALARM_STOPPED:
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                break;
        }
    }

    private void startDetector() {
        startService(new Intent(this, AlarmService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(EventTypes.ONRESUME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().post(EventTypes.ONPAUSE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().post(EventTypes.ONSTOP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().post(EventTypes.ONDESTROY);

        startActivity(new Intent(this, this.getClass()));
    }

    @Override
    public void finish() {
        super.finish();
        EventBus.getDefault().post(EventTypes.FINISH);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
}
