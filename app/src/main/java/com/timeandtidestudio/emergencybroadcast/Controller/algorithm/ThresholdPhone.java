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
import android.hardware.SensorManager;
import android.util.Log;

import com.timeandtidestudio.emergencybroadcast.Controller.EventTypes;
import com.timeandtidestudio.emergencybroadcast.Controller.RecordAlgorithmData;
import com.timeandtidestudio.emergencybroadcast.Controller.RecordEventData;
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



public class ThresholdPhone implements Algorithm {

    public static final String TOTAL_ACCELEROMETER_THRESHOLD = "tot_acc_thr";
    public static final String VERTICAL_ACCELEROMETER_THRESHOLD = "ver_acc_thr";
    public static final String ACCELEROMETER_COMPARISON_THRESHOLD = "acc_comp_thr";

    public static final double DEFAULT_TOT_ACC_THRESHOLD = 25; //12, 13, 14
    public static final double DEFAULT_VERTICAL_ACC_THRESHOLD = 25; //Little under tot_acc I guess
    public static final double DEFAULT_ACC_COMPARISON_THRESHOLD = 0.5; //tot_acc / vertical_acc


    public boolean isFall(long id, SensorAlgorithmPack pack) {

        //BEGIN Unpacking sensorpack
        List<RotationVectorData> rotData = new ArrayList<>();
        List<MagneticFieldData> geoRotVecData = new ArrayList<>();
        List<LinearAccelerationData> accData = new ArrayList<>();
        for (Map.Entry<SensorSession, List<SensorData>> entry : pack.getSensorData().entrySet()) {
            switch (entry.getKey().getSensorDevice()) {
                case PHONE:
                    switch (entry.getKey().getSensorType()) {
                        case Sensor.TYPE_LINEAR_ACCELERATION:
                            for (int i = 0; i < entry.getValue().size(); i++){
                                accData.add((LinearAccelerationData) entry.getValue().get(i).getSensorData());
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
        //END Unpacking sensorpack

        boolean isFall = ThresholdAlgorithm(accData, rotData, geoRotVecData);

        /** RECORDING - isFall */
        if (PreferencesHelper.isRecording()) EventBus.getDefault().post(new RecordAlgorithmData(id, "phone_threshold", isFall));

        return isFall;
    }

    public static boolean isFall(List<LinearAccelerationData> accData, List<RotationVectorData> rotData, List<MagneticFieldData> geoRotVecData){
        return ThresholdAlgorithm(accData, rotData, geoRotVecData);
    }

    private static boolean ThresholdAlgorithm(List<LinearAccelerationData> accData, List<RotationVectorData> rotData, List<MagneticFieldData> geoRotVecData){
        int numberOfIterations;
        float[] degs = new float[3];
        float[] rotationMatrix = new float[9];
        double tetaY;
        double tetaZ;

        if (accData.size() <= rotData.size() && accData.size() <= geoRotVecData.size()) numberOfIterations = accData.size();
        else if (geoRotVecData.size() <= accData.size() && geoRotVecData.size() <= rotData.size()) numberOfIterations = geoRotVecData.size();
        else numberOfIterations = rotData.size();
        for (int i=0; i < numberOfIterations; i++) {
            SensorManager.getRotationMatrix(rotationMatrix, null, rotData.get(i).getValues(), geoRotVecData.get(i).getValues());
            SensorManager.getOrientation(rotationMatrix, degs);
            tetaY = degs[2];
            tetaZ = degs[0];

            if (calculateThresholdAlgorithm(accData.get(i).getX(), accData.get(i).getY(), accData.get(i).getZ(), tetaY, tetaZ)) {
                return true;
            }
        }
        return false;
    }

    private static boolean calculateThresholdAlgorithm(double x, double y, double z, double tetaY, double tetaZ){
        double totalAcceleration = Math.abs(accelerationTotal(x, y, z));
        double verticalAcceleration = Math.abs(verticalAcceleration(x, y, z, tetaY, tetaZ));

        /** RECORDING - totalAcceleration & verticalAcceleration*/
        if (PreferencesHelper.isRecording()) {
            EventBus.getDefault().post(new RecordEventData(EventTypes.RECORDING_PHONE_TOTAL_ACCELERATION, totalAcceleration));
            EventBus.getDefault().post(new RecordEventData(EventTypes.RECORDING_PHONE_VERTICAL_ACCELERATION, verticalAcceleration));
        }

        if (totalAcceleration >= getTotAccThreshold() && verticalAcceleration >= getVerticalAccThreshold()){

            if(totalAcceleration>=10&&totalAcceleration<20){
                Log.v("logthT10",""+totalAcceleration+"||"+getTotAccThreshold());
            }else if(totalAcceleration>=20&&totalAcceleration<30){
                Log.v("logthT20",""+totalAcceleration+"||"+getTotAccThreshold());
            }else if(totalAcceleration>=30&&totalAcceleration<40){
                Log.v("logthT30",""+totalAcceleration+"||"+getTotAccThreshold());
            }else if(totalAcceleration>=40&&totalAcceleration<50){
                Log.v("logthT40",""+totalAcceleration+"||"+getTotAccThreshold());
            }else if(totalAcceleration>=50&&totalAcceleration<60){
                Log.v("logthT50",""+totalAcceleration+"||"+getTotAccThreshold());
            }

            if (verticalComparedToTotal(verticalAcceleration, totalAcceleration) >= getAccComparisonThreshold()){
                return true;
            }
        }
        return false;
    }

    public static boolean isThresholdFall(double x, double y, double z, double tetaY, double tetaZ, double testtotAccThreshold, double testverticalAccThreshold, double testaccComparisonThreshold)
    {
        double totalAcceleration = Math.abs(accelerationTotal(x, y, z));
        double verticalAcceleration = Math.abs(verticalAcceleration(x, y, z, tetaY, tetaZ));
        if (totalAcceleration >= testtotAccThreshold && verticalAcceleration >= testverticalAccThreshold){
            if (verticalComparedToTotal(verticalAcceleration, totalAcceleration) >= testaccComparisonThreshold) {return true;}
        }
        return false;
    }

    public static boolean isPhoneVertical(double priorAngle, double postAngle, double angleThreshold){
        return postAngle - priorAngle >= angleThreshold;
    }

    private static double verticalComparedToTotal(double vertical, double total){
        return vertical/total;
    }

    //Calculates total acceleration at one point
    private static double accelerationTotal(double x, double y, double z){
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    //calculates vertical acceleration at one point
    private static double verticalAcceleration(double x, double y, double z, double tetaY, double tetaZ){
        return Math.abs(x * Math.sin(tetaZ) + y * Math.sin(tetaY) - z * Math.cos(tetaY) * Math.cos(tetaZ));
    }
    public static double getTotAccThreshold() {
        return PreferencesHelper.getFloat(TOTAL_ACCELEROMETER_THRESHOLD, (float) DEFAULT_TOT_ACC_THRESHOLD);
    }
    public static double getVerticalAccThreshold() {
        return PreferencesHelper.getFloat(VERTICAL_ACCELEROMETER_THRESHOLD, (float) DEFAULT_VERTICAL_ACC_THRESHOLD);
    }
    public static double getAccComparisonThreshold() {
        return PreferencesHelper.getFloat(ACCELEROMETER_COMPARISON_THRESHOLD, (float) DEFAULT_ACC_COMPARISON_THRESHOLD);
    }
}