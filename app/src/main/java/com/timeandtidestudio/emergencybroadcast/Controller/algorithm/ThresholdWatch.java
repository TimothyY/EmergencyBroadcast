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

package com.timeandtidestudio.emergencybroadcast.Controller.algorithm;

import android.hardware.Sensor;

import com.timeandtidestudio.emergencybroadcast.Controller.EventTypes;
import com.timeandtidestudio.emergencybroadcast.Controller.RecordAlgorithmData;
import com.timeandtidestudio.emergencybroadcast.Controller.RecordEventData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorSession;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.LinearAccelerationData;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ThresholdWatch implements Algorithm {

    public static final String FALLINDEX_IMPACT = "imp_thr";

    public static final double DEFAULT_THRESHOLD_FALL = 30; //20

    //Calculate the acceleration.
    private static double fallIndex(List<LinearAccelerationData> sensors, int startList){

        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();
        List<Double> z = new ArrayList<>();
        int startValue = startList;

        for (LinearAccelerationData xyz : sensors){
            x.add((double) xyz.getX());
            y.add((double) xyz.getY());
            z.add((double) xyz.getZ());
        }

        List<List> sensorData = new ArrayList<List>();
        sensorData.add(x);
        sensorData.add(y);
        sensorData.add(z);

        double directionAcceleration = 0;
        double totAcceleration = 0;

        for (int i = 0; i < sensorData.size(); i++){
            for (int j = startValue; j < sensorData.get(i).size(); j++){
                directionAcceleration += Math.pow((Double) sensorData.get(i).get(j) - (Double)sensorData.get(i).get(j - 1), 2);
            }
            totAcceleration += directionAcceleration;
            directionAcceleration = 0;
        }
        return Math.sqrt(totAcceleration);
    }
    //Recognize fall pattern, and decide if there is a fall or not
    public static boolean thresholdAlgorithmWatch(List<LinearAccelerationData> sensors){
        if (sensors.size() == 0) {return true;}

        double accelerationData;
        int startList = 1;

        accelerationData = fallIndex(sensors, startList);

        /** RECORDING - fallIndex */
        if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordEventData(EventTypes.RECORDING_WATCH_FALL_INDEX, accelerationData));


        return accelerationData >= getThresholdFall();
    }

    @Override
    public boolean isFall(long id, SensorAlgorithmPack pack) {
        List<LinearAccelerationData> accDataWatch = new ArrayList<>();

        for (Map.Entry<SensorSession, List<SensorData>> entry : pack.getSensorData().entrySet()) {
            switch (entry.getKey().getSensorDevice()) {
                case WATCH:
                    switch (entry.getKey().getSensorType()){
                        case Sensor.TYPE_LINEAR_ACCELERATION:
                            for (int i = 0; i < entry.getValue().size(); i++) {
                                accDataWatch.add((LinearAccelerationData) entry.getValue().get(i).getSensorData());
                            }
                            break;
                    }
                    break;
            }

        }

        boolean isFall = thresholdAlgorithmWatch(accDataWatch);

        /** RECORDING - isFall */
        if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordAlgorithmData(id, "watch_threshold", isFall));

        return isFall;
    }

    public static double getThresholdFall() {
        return PreferencesHelper.getFloat(FALLINDEX_IMPACT, (float) DEFAULT_THRESHOLD_FALL);
    }
}
