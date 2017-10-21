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

package com.timeandtidestudio.emergencybroadcast.Controller.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.timeandtidestudio.emergencybroadcast.Controller.DeviceClient;
import com.timeandtidestudio.emergencybroadcast.Controller.EventTypes;
import com.timeandtidestudio.emergencybroadcast.Controller.common.ClientPaths;
import com.timeandtidestudio.emergencybroadcast.Controller.common.Constants;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.LinearAccelerationData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.MagneticFieldData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.RotationVectorData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.SensorDataObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;
import java.util.HashMap;


public class SensorManager implements SensorEventListener {

    private android.hardware.SensorManager mSensorManager;
    private HashMap<Integer, SensorSession> mSensorGroup = new HashMap<>();
    private DeviceClient mDeviceClient;

    private static SensorManager instance;

    public static synchronized SensorManager getInstance(Context context) {
        if (instance == null) {
            instance = new SensorManager(context.getApplicationContext());
        }

        return instance;
    }

    private SensorManager(Context context) {
        EventBus.getDefault().register(this);

        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mDeviceClient = DeviceClient.getInstance(context);

        addSensorToSystem("phone:magnetic_field", Sensor.TYPE_MAGNETIC_FIELD, SensorDevice.PHONE, SensorLocation.RIGHT_PANT_POCKET);
        addSensorToSystem("phone:linear_acceleration", Sensor.TYPE_LINEAR_ACCELERATION, SensorDevice.PHONE, SensorLocation.RIGHT_PANT_POCKET);
        addSensorToSystem("phone:rotation_vector", Sensor.TYPE_ROTATION_VECTOR, SensorDevice.PHONE, SensorLocation.RIGHT_PANT_POCKET);

    }

    private void addSensorToSystem(String id, int type, SensorDevice device, SensorLocation location) {
        SensorSession sensorSession = new SensorSession(id, type, device, location);
        mSensorGroup.put(type, sensorSession);
    }

    @Subscribe
    public void onEvent(EventTypes eventType) {
        switch (eventType) {
            case ONRESUME:
                mDeviceClient.setMode(ClientPaths.MODE_DEFAULT);
            case RESET_SENSOR_LISTENERS:
                mSensorManager.unregisterListener(this);

                for (int type : mSensorGroup.keySet()) {
                    mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(type), Constants.SENSOR_PULL_FREQ);
                }
                break;
            case ONDESTROY:
                mSensorManager.unregisterListener(this);
                break;
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(final SensorEvent event) {
        SensorDataObject sensorDataObject = null;
        switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                sensorDataObject = new MagneticFieldData(event.values.clone());
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                sensorDataObject = new LinearAccelerationData(event.values.clone());
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                sensorDataObject = new RotationVectorData(event.values.clone());
                break;
        }

        if (sensorDataObject != null)  {
            // sensor event timestamps are time since system boot
            long timestamp = (new Date()).getTime() + (event.timestamp - System.nanoTime()) / 1000000L;
            EventBus.getDefault().post(new SensorData(mSensorGroup.get(event.sensor.getType()), sensorDataObject, timestamp));
        }
    }
}
