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


public class PatternRecognitionWatch implements Algorithm {

    public static final String FALLINDEX_POST_IMPACT = "post_imp_thr";
    public static final String FALL_DURATION = "fall_dur_thr";

    public static final double DEFAULT_THRESHOLD_STILL = 20; //5
    public static final double DEFAULT_MOVEMENT_THRESHOLD = 170;

    private static final double ATLEAST_READINGS = 10;


    //Calculate the acceleration.
    private static FallIndexValues fallIndex(List<LinearAccelerationData> sensors, int startList){

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
        //double result;

        for (int i = 0; i < sensorData.size(); i++){
            for (int j = startValue; j < sensorData.get(i).size(); j++){
                directionAcceleration += Math.pow((Double)sensorData.get(i).get(j) - (Double)sensorData.get(i).get(j - 1), 2);

                /** RECORDING - directionAcceleration */
                if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordEventData(EventTypes.RECORDING_WATCH_DIRECTION_ACCELERATION, directionAcceleration));

                if (Math.pow((Double)sensorData.get(i).get(j) - (Double)sensorData.get(i).get(j - 1), 2) > getThresholdMovement() && startList < j){
                    startList = j;
                }
            }
            totAcceleration += directionAcceleration;
            directionAcceleration = 0;
        }
        return new FallIndexValues (Math.sqrt(totAcceleration), startList);
    }

    private static double stillPattern(List<LinearAccelerationData> sensors, int startList){
        return fallIndex(sensors, startList).getFallData();
    }


    //Recognize fall pattern, and decide if there is a fall or not
    public static boolean patternRecognition(List<LinearAccelerationData> sensors){
        if (sensors.size() == 0) {return true;}

        FallIndexValues accelerationData;
        double afterFallData;
        int startList = 1;
        accelerationData = fallIndex(sensors, startList);

        /** RECORDING - fallIndex */
        if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordEventData(EventTypes.RECORDING_WATCH_FALL_INDEX, accelerationData.fallData));

        if (accelerationData.getFallData() >= ThresholdWatch.getThresholdFall() && sensors.size()-accelerationData.getStartIndex() > ATLEAST_READINGS){
            afterFallData = stillPattern(sensors, accelerationData.getStartIndex());

            /** RECORDING - stillPattern */
            if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordEventData(EventTypes.RECORDING_WATCH_AFTER_FALL, afterFallData));

            return afterFallData <= getThresholdStill();
        }
        return false;
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


        boolean isFall = patternRecognition(accDataWatch);

        /** RECORDING - isFall */
        if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordAlgorithmData(id, "watch_pattern_recognition", isFall));

        return isFall;
    }

    private static class FallIndexValues{
        private double fallData;
        private int startIndex;
        private final int layingDownCount = 10; //number of readings to skip when checking if the person is laying still, so that it will not check the fall again and therefor say that the person is not laying still

        FallIndexValues(double fallData, int startIndex){
            this.fallData = fallData;
            this.startIndex = startIndex+layingDownCount;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public double getFallData() {
            return fallData;
        }
    }

    public static double getThresholdStill() {
        return PreferencesHelper.getFloat(FALLINDEX_POST_IMPACT, (float) DEFAULT_THRESHOLD_STILL);
    }

    public static double getThresholdMovement() {
        return PreferencesHelper.getFloat(FALL_DURATION, (float) DEFAULT_MOVEMENT_THRESHOLD);
    }
}
