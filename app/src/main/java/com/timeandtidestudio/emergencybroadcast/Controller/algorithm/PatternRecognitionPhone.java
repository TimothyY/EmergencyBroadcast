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
import android.util.Log;

import com.timeandtidestudio.emergencybroadcast.Controller.RecordAlgorithmData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.SensorSession;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.LinearAccelerationData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.MagneticFieldData;
import com.timeandtidestudio.emergencybroadcast.Controller.sensor.data.RotationVectorData;
import com.timeandtidestudio.emergencybroadcast.Controller.utils.PreferencesHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PatternRecognitionPhone implements Algorithm {
    public static final String IMPACT_THRESHOLD = "impact_thr";
    public static final String PRE_IMPACT_THRESHOLD = "pre_impact_thr";
    public static final String POST_IMPACT_THRESHOLD = "post_impact_thr";

    public static final double DEFAULT_IMPACT_THRESHOLD = 1.5; //fra topp til bunn
    public static final double DEFAULT_PREIMPACT_THRESHOLD = 1.5; //fra bunn til topp
    public static final double DEFAULT_POST_IMPACT_THRESHOLD = 6; //average maa vaere under denne verdien.
    /* Test values == TRUE
    x = [0, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0]
    y = [0, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0]
    z = [0, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0]
    Test values == FALSE
    x = [5, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0]
    y = [5, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0]
    z = [5, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 1, 2, 0, 0]
    Test values == FALSE
    x = [0, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 2, 0, 0]
    y = [0, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 2, 0, 0]
    z = [0, 6, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 2, 0, 0]
    Test values == FALSE
    x = [0, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5]
    y = [0, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5]
    z = [0, 6, 5, 5, 2, 1, 1, 1, 1, 1, 1, 5, 5, 5, 5]
    Main pattern recognition method*/

    public boolean isFall(long id, SensorAlgorithmPack pack){
        //BEGIN unpacking sensorpack
        List<LinearAccelerationData> accelerometerData = new ArrayList<>();
        List<RotationVectorData> rotData = new ArrayList<>();
        List<MagneticFieldData> geoRotVecData = new ArrayList<>();
        for (Map.Entry<SensorSession, List<SensorData>> entry : pack.getSensorData().entrySet()) {
            switch (entry.getKey().getSensorDevice()) {
                case PHONE:
                    switch (entry.getKey().getSensorType()) {
                        case Sensor.TYPE_LINEAR_ACCELERATION:
                            for (int i = 0; i < entry.getValue().size(); i++){
                                accelerometerData.add((LinearAccelerationData) entry.getValue().get(i).getSensorData());
                            }
                            break;
                        case Sensor.TYPE_ROTATION_VECTOR:
                            for (int i = 0; i < entry.getValue().size(); i++) {
                                rotData.add((RotationVectorData) entry.getValue().get(i).getSensorData());
                            }
                            break;
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            for (int i = 0; i < entry.getValue().size(); i++) {
                                geoRotVecData.add((MagneticFieldData) entry.getValue().get(i).getSensorData());
                            }
                            break;
                    }
                    break;
            }
        }
        //END unpacking sensorpack

        //Hvis threshold algoritme sier det ikke er fall, kalles ikke patternRecognition

        boolean isThresholdFall = ThresholdPhone.isFall(accelerometerData, rotData, geoRotVecData);

        /** RECORDING - isFall */
        if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordAlgorithmData(id, "phone_threshold", isThresholdFall));

        if (!isThresholdFall) {
            return false;
        }

        double maxAcceleration = 0;
        double currentAcceleration;
        int index = 0;
        int iterations = 7;
        //iterating over the data and finds the point with the highest acceleration

        for (int i = 0; i < accelerometerData.size(); i++){
            currentAcceleration = accelerationTotal(accelerometerData.get(i).getX(), accelerometerData.get(i).getY(), accelerometerData.get(i).getY());
            if (currentAcceleration > maxAcceleration){
                index = i;
                maxAcceleration = currentAcceleration;
            }
        }

        boolean isFall = preImpactPattern(accelerometerData, index, iterations,maxAcceleration) && impactPattern(accelerometerData, index, iterations,maxAcceleration) && postImpactPattern(accelerometerData, index+iterations);

        /** RECORDING - isFall */
        if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordAlgorithmData(id, "phone_pattern_recognition", isFall));

        return isFall;
    }
    /*
    impact pattern recognition:
    if there is a massive deaccelreation close after the max acceleration impact indicates that it is a fall.
    Test values == TRUE:
    x = [3, 4, 2, 6, 4, 4, 2]
    y = [3, 4, 2, 6, 4, 3, 1]
    z = [3, 4, 2, 6, 4, 4, 2]
    index = 3
    iterations = 5
    maxAcceleration = 10.39
    TESTimpactThreshold = 3
    Test values == FALSE:
    x = [6, 5, 5, 4, 4, 3, 0]
    y = [6, 5, 5, 4, 3, 3, 0]
    z = [6, 5, 5, 4, 3, 3, 0]
    index = 0
    iterations = 5
    maxAcceleration = 10.39
    TESTimpactThreshold = 3
    */
    private static boolean impactPattern(List<LinearAccelerationData> accelerometerData, int index, int iterations, double maxAcceleration){
        double currentAcceleration;
        //iterating from toppoint to see if there is a big deacceleration after it.
        for (int i = index+1; i < index+iterations; i++){
            currentAcceleration = accelerationTotal(accelerometerData.get(i).getX(), accelerometerData.get(i).getY(), accelerometerData.get(i).getZ());
            if (currentAcceleration*getImpactThreshold() <= maxAcceleration){
                return true;
            }
        }
        return false;
    }
    //FOR TESTING PURPOSES
    public static boolean impactPattern(List<LinearAccelerationData> accelerometerData, int index, int iterations, double maxAcceleration, double TESTimpactThreshold){
        double currentAcceleration;
        //iterating from toppoint to see if there is a big deacceleration after it.
        for (int i = index+1; i <= index+iterations; i++){
            currentAcceleration = accelerationTotal(accelerometerData.get(i).getX(), accelerometerData.get(i).getY(), accelerometerData.get(i).getZ());
            if (currentAcceleration*TESTimpactThreshold <= maxAcceleration){
                return true;
            }
            return false;
        }
        return false;
    }
    /*
    pattern for pre impact:
    increase in acceleration, to the max
    hvis det ved et punkt før var mye lavere akslerasjon så indikerer det et fall i preimpact stadiet.
    Test values == TRUE:
    x = [1, 3, 4, 6]
    y = [1, 3, 4, 6]
    z = [1, 3, 4, 6]
    index = 3
    iterations = 5
    maxAcceleration = 10,39
    TESTpreimpactThreshold = 3
    Test values == FALSE
    x = [1, 3, 4, 4, 4, 4, 6]
    y = [1, 3, 4, 4, 4, 4, 6]
    z = [1, 3, 4, 4, 4, 4, 6]
    index = 6
    iterations = 5
    maxAcceleration = 10,39
    TESTpreimpactThreshold = 3
    */
    private static boolean preImpactPattern(List<LinearAccelerationData> accelerometerData, int index, int iterations, double maxAcceleration){
        double currentAcceleration;
        int endLoop = index-iterations;
        if (endLoop < 0){endLoop = 0;}
        for (int i = index-1; i >= endLoop; i--){
            currentAcceleration = accelerationTotal(accelerometerData.get(i).getX(), accelerometerData.get(i).getY(), accelerometerData.get(i).getZ());
            if (currentAcceleration*getPreImpactThreshold() < maxAcceleration){
                return true;
            }
        }
        return false;
    }
    //for testing purposes
    public static boolean preImpactPattern(List<LinearAccelerationData> accelerometerData, int index, int iterations, double maxAcceleration, double TESTpreimpactThreshold){
        double currentAcceleration;
        for (int i = index-1; i >= index-iterations; i--){
            currentAcceleration = accelerationTotal(accelerometerData.get(i).getX(), accelerometerData.get(i).getY(), accelerometerData.get(i).getZ());
            if (currentAcceleration*TESTpreimpactThreshold < maxAcceleration){return true;}
        }
        return false;
    }
    /*
    Pattern for post impact:
    Test values == TRUE
    x = [0, 0, 5, 6]
    y = [0, 0, 5, 6]
    z = [0, 0, 5, 6]
    index = 0
    TESTPostImpactThreshold = 5
    Test values == FALSE
    x = [0, 0, 5, 6]
    y = [0, 0, 5, 6]
    z = [0, 0, 5, 6]
    index = 2
    TESTPostImpactThreshold = 5
    */
    private static boolean postImpactPattern(List<LinearAccelerationData> accelerometerData, int index){
        double sumOfAccelerations = 0;
        double listSize = accelerometerData.size();
        if (listSize-index > 20){
            for (int i = index; i < listSize; i++){
                sumOfAccelerations += accelerationTotal(accelerometerData.get(i).getX(), accelerometerData.get(i).getY(), accelerometerData.get(i).getZ());
            }
            if(sumOfAccelerations/(accelerometerData.size()-index) < getPostImpactThreshold()){
                return true;
            }
        }
        return false;
    }

    //For testing
    public static boolean postImpactPattern(List<LinearAccelerationData> accelerometerData, int index, double TESTPostImpactThreshold){
        double sumOfAccelerations = 0;
        for (int i = index; i < accelerometerData.size(); i++){
            sumOfAccelerations += accelerationTotal(accelerometerData.get(i).getX(), accelerometerData.get(i).getY(), accelerometerData.get(i).getZ());
        }
        if(sumOfAccelerations/(accelerometerData.size()-index) < TESTPostImpactThreshold){
            return true;
        }
        return false;
    }

    private static double accelerationTotal(double x, double y, double z){
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }


    public static double getImpactThreshold() {
        return PreferencesHelper.getFloat(IMPACT_THRESHOLD, (float) DEFAULT_IMPACT_THRESHOLD);
    }
    public static double getPreImpactThreshold() {
        return PreferencesHelper.getFloat(PRE_IMPACT_THRESHOLD, (float) DEFAULT_PREIMPACT_THRESHOLD);
    }
    public static double getPostImpactThreshold() {
        return PreferencesHelper.getFloat(POST_IMPACT_THRESHOLD, (float) DEFAULT_POST_IMPACT_THRESHOLD);
    }
}
