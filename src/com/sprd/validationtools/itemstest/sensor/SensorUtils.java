/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.sensor;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.utils.FileUtils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorUtils {

    private static final String TAG = "SensorUtils";
    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private Context mContext = null;
    private int mSensorType = -1;
    private SensorEventListener mSensorEventListener = null;

    private static final String TEST_OK = "2";
    private static final int mSensorCaliSaveResultPass = 30;

    public SensorUtils(Context context, int sensorType) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mContext = context;
        mSensorType = sensorType;
        mSensor = mSensorManager.getDefaultSensor(mSensorType);
        mSensorEventListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
            }
        };
    }

    public boolean enableSensor() {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        }
        Log.d(TAG, "enableSensor mSensorType=" + mSensorType);
        if (mSensorManager != null) {
            boolean ret = mSensorManager.registerListener(mSensorEventListener,
                    mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            return ret;
        }
        return false;
    }

    public void disableSensor() {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        }
        Log.d(TAG, "enableSensor mSensorType=" + mSensorType);
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorEventListener);
        }
    }

    /**
     * start calibrating echo "1 [SENSOR_ID] 1" > calibrator_cmd cat calibrator_data, if the get
     * value is 2 ,the test is ok ,or test is fial
     **/
    public static boolean getResult(String getResultCmd) {
        FileUtils.writeFile(Const.CALIBRATOR_CMD, getResultCmd);
        String getResult = FileUtils.readFile(Const.CALIBRATOR_DATA);
        Log.d(TAG, "getResult the result of boolen Result: " + getResult);
        if (getResult != null && TEST_OK.equals(getResult.trim())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * save the result echo "3 [SENSOR_ID] 1" > calibrator_cmd cat calibrator_data to save test
     * result
     **/
    public static boolean saveResult(String saveResultCmd, String saveResultPath) {
        Log.d(TAG, "saveResult...");
        FileUtils.writeFile(Const.CALIBRATOR_CMD, saveResultCmd);
        int readResult = FileUtils.readFileData(Const.CALIBRATOR_DATA);
        if (readResult == mSensorCaliSaveResultPass) {
            int saveResultLen = FileUtils.saveFileData(Const.CALIBRATOR_DATA, saveResultPath);
            Log.d(TAG, "save result: " + saveResultLen);
            if (saveResultLen == mSensorCaliSaveResultPass) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
        /* Modify the data reading mode of the calibrator_data node to read
        the data in the Calibrator_data node for judgment and to store the data*/
    }
}
