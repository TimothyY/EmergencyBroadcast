/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package com.timeandtidestudio.emergencybroadcast;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.timeandtidestudio.emergencybroadcast.Controller.Controller;
import com.timeandtidestudio.emergencybroadcast.Controller.EventTypes;
import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.NextOfKinDialog;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.SoundHelper;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.Utils;
import com.timeandtidestudio.emergencybroadcast.Fragment.AlarmFragment;
import com.timeandtidestudio.emergencybroadcast.Service.AlarmService;
import com.timeandtidestudio.emergencybroadcast.advanced.AdvancedActivity;
import com.timeandtidestudio.emergencybroadcast.wizard.WizardMain;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    public static final String ADVANCED_MENU_AVAILABLE = "advanced_menu_available";
    private static final int ADVANCED_MENU_CLICK_MAX = 7;

    private static final String TAG = "Main Activity";

    private static Toast sToast;

    private int mClickCount = 0;
    private Handler mSevenClickResetHandler = new Handler();
    private Runnable mSevenClickResetRunnable = new Runnable() {
        @Override
        public void run() {
            mClickCount = 0;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Utils.isServiceRunning(this, AlarmService.class)) startDetector();

        EventBus.getDefault().register(this);
        SoundHelper.initializeSoundsHelper(this);
        PreferencesHelper.initializePreferences(this);

        if (PreferencesHelper.getBoolean(Constants.PREFS_FIRST_START, true)) {
            PreferencesHelper.putBoolean(PreferencesHelper.FALL_DETECTION_ENABLED, false);
            startActivity(new Intent(this, WizardMain.class));
        }

        Controller.initializeController(getApplicationContext());

        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        // getSupportActionBar().setIcon(R.drawable.ic_launcher);

        boolean alarm_started = false;
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(AlarmService.ALARM_STARTED)) alarm_started = true;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_placeholder, AlarmFragment.newInstance(alarm_started));
        ft.commit();

        findViewById(R.id.fragment_placeholder).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSevenClickResetHandler.removeCallbacksAndMessages(null);
                mSevenClickResetHandler.postDelayed(mSevenClickResetRunnable, 2000);
                updateAdvancedClickProgress();
            }
        });
    }

    private void updateAdvancedClickProgress() {
        if (PreferencesHelper.getBoolean(ADVANCED_MENU_AVAILABLE, false)) return;

        mClickCount++;
        if (mClickCount >= 3) {
            if (sToast != null) sToast.cancel();
            String message = "";
            if (mClickCount >= ADVANCED_MENU_CLICK_MAX) {
                message = getString(R.string.advanced_became);
                PreferencesHelper.putBoolean(ADVANCED_MENU_AVAILABLE, true);
                EventBus.getDefault().post(EventTypes.ADVANCED_MODE_CHANGED);
                invalidateOptionsMenu();
            } else {
                message = String.format(getString(R.string.advanced_away_from), ADVANCED_MENU_CLICK_MAX - mClickCount);
            }
            sToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            sToast.show();
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

    @Override
    public void onBackPressed() {
    }

    private void startDetector() {
        startService(new Intent(this, AlarmService.class));
    }

    private void openAdvancedActivity() {
        startActivity(new Intent(this, AdvancedActivity.class));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean advanced_menu_available = PreferencesHelper.getBoolean(ADVANCED_MENU_AVAILABLE, false);
        menu.findItem(R.id.action_advanced).setVisible(advanced_menu_available);
        menu.findItem(R.id.action_advanced_remove).setVisible(advanced_menu_available);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_ken:
                new NextOfKinDialog(this);
                return true;
            case R.id.action_help:
                startActivity(new Intent(this, WizardMain.class));
                return true;
            case R.id.action_advanced:
                openAdvancedActivity();
                return true;
            case R.id.action_advanced_remove:
                PreferencesHelper.putBoolean(ADVANCED_MENU_AVAILABLE, false);
                EventBus.getDefault().post(EventTypes.ADVANCED_MODE_CHANGED);
                invalidateOptionsMenu();

                if (sToast != null) sToast.cancel();
                sToast = Toast.makeText(getApplicationContext(), R.string.advanced_removed, Toast.LENGTH_SHORT);
                sToast.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
