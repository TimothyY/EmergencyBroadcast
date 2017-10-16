package com.timeandtidestudio.emergencybroadcast;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.timeandtidestudio.emergencybroadcast.Controller.Controller;
import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.SoundHelper;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.Utils;
import com.timeandtidestudio.emergencybroadcast.Fragment.ContactListFragment;
import com.timeandtidestudio.emergencybroadcast.Fragment.SentMessagesFragment;
import com.timeandtidestudio.emergencybroadcast.Fragment.UserProfileFragment;
import com.timeandtidestudio.emergencybroadcast.Service.AlarmService;
import com.timeandtidestudio.emergencybroadcast.wizard.WizardMain;

import org.greenrobot.eventbus.EventBus;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab;
    Context mCtx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent niat = new Intent(this,TestActivity.class);
        startActivity(niat);

        mCtx = this;

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

        if (!Utils.isServiceRunning(this, AlarmService.class)) startDetector();

        EventBus.getDefault().register(this);
        SoundHelper.initializeSoundsHelper(this);
        PreferencesHelper.initializePreferences(this);

        if (PreferencesHelper.getBoolean(Constants.PREFS_FIRST_START, true)) {
            PreferencesHelper.putBoolean(PreferencesHelper.FALL_DETECTION_ENABLED, false);
            startActivity(new Intent(this, WizardMain.class));
        }

        Controller.initializeController(getApplicationContext());
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

    private void startDetector() {
        startService(new Intent(this, AlarmService.class));
    }


}
