package com.timeandtidestudio.emergencybroadcast;

import android.hardware.Sensor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.timeandtidestudio.emergencybroadcast.Controller.AlarmView;
import com.timeandtidestudio.emergencybroadcast.Controller.algorithm.AlgorithmsToChoose;
import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorDevice;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorLocation;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorSession;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.LinearAccelerationData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.MagneticFieldData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.RotationVectorData;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;

import org.greenrobot.eventbus.EventBus;

public class TestActivity extends AppCompatActivity {

    private static boolean mReceiveAnswer;
    private static int mPreviousAlgorithmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlarmView alarmView = new AlarmView(this, R.layout.alarm_view);
        setContentView(alarmView);

        alarmView.setOnAlarmListener(new AlarmView.OnAlarmListener() {
            @Override
            public void onAlarm() {
                // DO WHAT YOU WANT IN RESPONSE TO AN ALARM
            }
        });

        alarmView.setOnStopListener(new AlarmView.OnStopListener() {
            public void onStop() {
                // STOP WHAT YOU WANT
            }
        });

    }


    /**
     |-------------------------------------------------------------------------|
     |  Test ID                 |  1: Threshold algorithm, fall registered     |
     |-------------------------------------------------------------------------|
     |  Modules involved        |  Controller, Algorithm                       |
     |-------------------------------------------------------------------------|
     |  Case description        |  Sending sensor data from controller that    |
     |                          |  causes a fall detection in the threshold    |
     |                          |  based algorithm.                            |
     |-------------------------------------------------------------------------|
     |  Expected result         |  isFall returning true, and FALL_DETECTED    |
     |                          |  posted to Event bus                         |
     |-------------------------------------------------------------------------|
     */
    public static final String TEST_TAG_1 = "test_1";
    public static final String TEST_SENSOR_1 = "test_sensor_1";
    public void runTestId1() {
        // Store previous algorithm used, to be restored at the end of the test
        mPreviousAlgorithmId = PreferencesHelper.getInt(Constants.PREFS_ALGORITHM, Constants.PREFS_DEFAULT_ALGORITHM);

        // Force the application to use PHONE_THRESHOLD
        PreferencesHelper.putInt(Constants.PREFS_ALGORITHM, AlgorithmsToChoose.ID_PHONE_THRESHOLD);

        // Create a new mock sensor session
        SensorSession sensorSessionAcc = new SensorSession(TEST_SENSOR_1 + ":ACC", Sensor.TYPE_LINEAR_ACCELERATION, SensorDevice.PHONE, SensorLocation.RIGHT_ARM);
        SensorSession sensorSessionRot = new SensorSession(TEST_SENSOR_1 + ":ROT", Sensor.TYPE_ROTATION_VECTOR, SensorDevice.PHONE, SensorLocation.RIGHT_ARM);
        SensorSession sensorSessionMag = new SensorSession(TEST_SENSOR_1 + ":MAG", Sensor.TYPE_MAGNETIC_FIELD, SensorDevice.PHONE, SensorLocation.RIGHT_ARM);

        // Create mock sensor data objects
        LinearAccelerationData linearAccelerationData = new LinearAccelerationData(100, 100, 100);
        RotationVectorData rotationVectorData = new RotationVectorData(new float[] {0.27839798f, 0.16639067f, -0.11554366f, 0.9388601f, 0.0f});
        MagneticFieldData magneticFieldData = new MagneticFieldData(new float[] {0.59999996f, 2.3999999f, -39.18f});

        // Allow to receive data
        mReceiveAnswer = true;

        // Posts the mock session and data at current time
        EventBus.getDefault().post(new SensorData(sensorSessionAcc, linearAccelerationData, System.currentTimeMillis()));
        EventBus.getDefault().post(new SensorData(sensorSessionRot, rotationVectorData, System.currentTimeMillis()));
        EventBus.getDefault().post(new SensorData(sensorSessionMag, magneticFieldData, System.currentTimeMillis()));

        // Write log to console
        Log.i(TEST_TAG_1, "Sent data");
    }

    /**
     |-------------------------------------------------------------------------|
     |  Test ID                 |  3: Pattern recognition algorithm,           |
     |                          |  fall registered                             |
     |-------------------------------------------------------------------------|
     |  Modules involved        |  Controller, Algorithm                       |
     |-------------------------------------------------------------------------|
     |  Case description        |  Sending sensor data from controller that    |
     |                          |  causes a fall detection in the pattern      |
     |                          |  recognition algorithm.                      |
     |-------------------------------------------------------------------------|
     |  Expected result         |  isFall returning true, and FALL_DETECTED    |
     |                          |  posted to Event bus                         |
     |-------------------------------------------------------------------------|
     */

    public static final String TEST_TAG_3 = "test_3";
    public static final String TEST_SENSOR_3 = "test_sensor_3";

    public void runTestId3() {
        // Store previous algorithm used, to be restored at the end of the test
        mPreviousAlgorithmId = PreferencesHelper.getInt(Constants.PREFS_ALGORITHM, Constants.PREFS_DEFAULT_ALGORITHM);

        // Force the application to use PHONE_THRESHOLD
        PreferencesHelper.putInt(Constants.PREFS_ALGORITHM, AlgorithmsToChoose.ID_PHONE_PATTERN_RECOGNITION);

        // Create a new mock sensor session
        SensorSession sensorSessionAcc = new SensorSession(TEST_SENSOR_3 + ":ACC", Sensor.TYPE_LINEAR_ACCELERATION, SensorDevice.PHONE, SensorLocation.RIGHT_ARM);
        SensorSession sensorSessionRot = new SensorSession(TEST_SENSOR_3 + ":ROT", Sensor.TYPE_ROTATION_VECTOR, SensorDevice.PHONE, SensorLocation.RIGHT_ARM);
        SensorSession sensorSessionMag = new SensorSession(TEST_SENSOR_3 + ":MAG", Sensor.TYPE_MAGNETIC_FIELD, SensorDevice.PHONE, SensorLocation.RIGHT_ARM);

        // Create mock sensor data objects
        LinearAccelerationData linearAccelerationData = new LinearAccelerationData(100, 100, 100);
        RotationVectorData rotationVectorData = new RotationVectorData(new float[] {0.27839798f, 0.16639067f, -0.11554366f, 0.9388601f, 0.0f});
        MagneticFieldData magneticFieldData = new MagneticFieldData(new float[] {0.59999996f, 2.3999999f, -39.18f});

        // Allow to receive data
        mReceiveAnswer = true;

        // Posts the mock session and data at current time
        EventBus.getDefault().post(new SensorData(sensorSessionAcc, linearAccelerationData, System.currentTimeMillis()));
        EventBus.getDefault().post(new SensorData(sensorSessionRot, rotationVectorData, System.currentTimeMillis()));
        EventBus.getDefault().post(new SensorData(sensorSessionMag, magneticFieldData, System.currentTimeMillis()));

        // Write log to console
        Log.i(TEST_TAG_3, "Sent data");
    }

}
