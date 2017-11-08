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

package com.timeandtidestudio.emergencybroadcast.Service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.ResultReceiver;
import android.telephony.SmsManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.timeandtidestudio.emergencybroadcast.Controller.AlarmEvent;
import com.timeandtidestudio.emergencybroadcast.Controller.Controller;
import com.timeandtidestudio.emergencybroadcast.Controller.EventTypes;
import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.SoundHelper;
import com.timeandtidestudio.emergencybroadcast.Database.ContactsDAO;
import com.timeandtidestudio.emergencybroadcast.Database.MessagesDAO;
import com.timeandtidestudio.emergencybroadcast.MainActivity;
import com.timeandtidestudio.emergencybroadcast.Model.SentMessage;
import com.timeandtidestudio.emergencybroadcast.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by samyboy89 on 03/02/15.
 */
public class AlarmService extends Service implements LocationListener {

    private NotificationManager mNotificationManager;
    private Notification.Builder mNotificationBuilder;
    private PowerManager.WakeLock mWakeLock;

    private TimerState mTimerState = TimerState.PENDING;

    public static final String ALARM_STARTED = "alarm_started";

    private final int WAIT_BEFORE_RESET_PERIOD = 5000;

    public static final int SCREEN_OFF_RECEIVER_DELAY = 500;

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) return;
            Runnable runnable = new Runnable() {
                public void run() {
                    EventBus.getDefault().post(EventTypes.RESET_SENSOR_LISTENERS);
                }
            };
            new Handler().postDelayed(runnable, SCREEN_OFF_RECEIVER_DELAY);
        }
    };

    private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (intent.getAction().compareToIgnoreCase("CANCEL_ACTION") == 0) {
                //stop alarm from waiting to cancelled
                Log.v("NotificationReceiver", "NotificationReceiver called");
                if (sVibrator != null) sVibrator.cancel();
                EventBus.getDefault().post(EventTypes.STOP_ALARM);
            }
        }
    };

    private AsyncTask<Void, Integer, Boolean> mAlarmTask;

    private final int seconds = 60;
    private final int resolution_multiplier = 4;
    private final int max = seconds * resolution_multiplier;
    private final int second = 1000;
    private final int resolution_second = second / resolution_multiplier;
    private final int update_frequency = resolution_multiplier * 4;

    Context mCtx;
    public static Vibrator sVibrator;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    static AddressResultReceiver mAddressResultReceiver;

    @Override
    public void onCreate() {
        mCtx = getApplicationContext();

        EventBus.getDefault().register(this);

        Controller.initializeController(this);
        SoundHelper.initializeSoundsHelper(this);
        PreferencesHelper.initializePreferences(this);

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AlarmService.class.getName());

        sVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        registerReceiver(mScreenStateReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        registerReceiver(mNotificationReceiver, new IntentFilter("CANCEL_ACTION"));

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        EventBus.getDefault().post(EventTypes.ONRESUME);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();
        }

        mAddressResultReceiver = new AddressResultReceiver(new Handler());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(getString(R.string.phone_notification_detecting))
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_announcement_white_24dp);

        startForeground(R.string.app_name, mNotificationBuilder.build());

        mWakeLock.acquire();

        return START_STICKY;    //super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        unregisterReceiver(mScreenStateReceiver);
        unregisterReceiver(mNotificationReceiver);

        mWakeLock.release();
        stopForeground(true);
        mNotificationManager.cancel(R.string.app_name);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    public synchronized void onEvent(EventTypes type) {
        switch (type) {
            case FALL_DETECTED:
                if (!mTimerState.equals(TimerState.PENDING)) return;
                EventBus.getDefault().post(EventTypes.START_ALARM);

                mTimerState = TimerState.RUNNING;

                Intent start_app_intent = new Intent(this, MainActivity.class);
                start_app_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                start_app_intent.putExtra(ALARM_STARTED, true);
                startActivity(start_app_intent);

                PendingIntent start_app_pending_intent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

                mNotificationBuilder.setContentTitle(getString(R.string.phone_notification_waiting));

                Intent cancelIntent = new Intent();
                cancelIntent.setAction("CANCEL_ACTION");
                PendingIntent piCancel = PendingIntent.getBroadcast(this, 12345, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotificationBuilder.addAction(0, "Cancel", piCancel);

                mNotificationBuilder.setContentIntent(start_app_pending_intent);
                mNotificationManager.notify(R.string.app_name, mNotificationBuilder.build());

                mAlarmTask = getAlarmTask();
                mAlarmTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                sVibrator.vibrate(Constants.ALARM_VIBRATION_PATTERN_ON_WATCH, 0);

                break;
            case ALARM_STOPPED:
                //notif detecting
                Log.v("AlarmService","alarm_stopped detected");
            case STOP_ALARM:
                //notif alarm cancelled
                if (mAlarmTask != null) {
                    mAlarmTask.cancel(true);
                }
                Log.v("AlarmService","stop_alarm detected");
                break;

        }
    }

    private AsyncTask<Void, Integer, Boolean> getAlarmTask() {
        return new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                for (int i = 0; i <= max; i++) {
                    if (isCancelled()) {
                        return false;
                    }

                    try {
                        Thread.sleep(resolution_second);
                        publishProgress(i);
                    } catch (InterruptedException e) {
                        Log.d("Alarm", "sleep failure");
                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                int i = values[0];

                if (i % update_frequency == 0) EventBus.getDefault().post(new AlarmEvent(max, i));
                mNotificationBuilder.setProgress(max, i, false);
                mNotificationManager.notify(R.string.app_name, mNotificationBuilder.build());
            }

            @Override
            protected void onPostExecute(Boolean alarm) {
                super.onPostExecute(alarm);
                updateTaskState(alarm);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
                updateTaskState(false);
            }

            @Override
            protected void onCancelled(Boolean alarm) {
                super.onCancelled(alarm);
                updateTaskState(alarm);
            }

            private void updateTaskState(boolean alarm) {

                if (sVibrator != null) sVibrator.cancel();
                sendEmergencyBroadcastMessage();

                mNotificationBuilder = new Notification.Builder(getApplicationContext())
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentTitle(alarm ? getString(R.string.phone_notification_sent) : getString(R.string.phone_notification_cancelled))
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setSmallIcon(R.drawable.ic_vibration_black_24dp);
                mNotificationManager.notify(R.string.app_name, mNotificationBuilder.build());

                mTimerState = alarm ? TimerState.ALARM : TimerState.CANCELLED;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        resetNotification();
                        mAlarmTask = null;
                    }
                }, WAIT_BEFORE_RESET_PERIOD);
            }
        };
    }

    private void resetNotification() {
        if (mTimerState == TimerState.RUNNING) return;

        mTimerState = TimerState.PENDING;

        mNotificationBuilder = new Notification.Builder(getApplicationContext())
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(getString(R.string.phone_notification_detecting))
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_announcement_white_24dp);

        mNotificationManager.notify(R.string.app_name, mNotificationBuilder.build());
    }

    public enum TimerState {
        PENDING, RUNNING, CANCELLED, ALARM,
    }

    public void sendEmergencyBroadcastMessage() {
        getLocation();
    }

    private void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mAddressResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    private class AddressResultReceiver extends ResultReceiver {
        /**
         * Create a new ResultReceive to receive results.  Your
         * {@link #onReceiveResult} method will be called from the thread running
         * <var>handler</var> if given, or from an arbitrary thread if null.
         *
         * @param handler
         */
        @SuppressLint("RestrictedApi")
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);

            // Show a toast message if an address was found.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                Log.v("AddrRsltRcvr","address found: "+mAddressOutput);
            }

            ContactsDAO contactsDAO = new ContactsDAO();
            ArrayList<String> phones =contactsDAO.loadNumbers(mCtx);

            SmsManager sms = SmsManager.getDefault();
            for (String phone:phones) {
                // if message length is too long messages are divided
                List<String> messages = sms.divideMessage(PreferencesHelper.getString(PreferencesHelper.USER_MESSAGE)+"[Last known position: "+mAddressOutput+"]");
                for (String msg : messages) {
                    PendingIntent sentIntent = PendingIntent.getBroadcast(mCtx, 0, new Intent("SMS_SENT"), 0);
                    PendingIntent deliveredIntent = PendingIntent.getBroadcast(mCtx, 0, new Intent("SMS_DELIVERED"), 0);
                    sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);
                }
            }

            MessagesDAO messagesDAO = new MessagesDAO();
            messagesDAO.saveSentMessage(getApplicationContext(),new SentMessage(""+System.currentTimeMillis(),PreferencesHelper.getString(PreferencesHelper.USER_MESSAGE)));
        }
    }

    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;
    protected void getLocation() {
        locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

//        You can still do this if you like, you might get lucky:
//        try{mLastLocation = locationManager.getLastKnownLocation(bestProvider);}catch(SecurityException e){}
//        if (mLastLocation != null) {
//            Log.v("GPS ON", "Latitude:"+mLastLocation.getLatitude());
//            startIntentService();
//        } else{}
        try{
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        }catch(SecurityException e){e.printStackTrace();}


    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        locationManager.removeUpdates(this);//remove location callback:
        Log.v("pew","Latitude"+":"+location.getLatitude());
        startIntentService();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

}
