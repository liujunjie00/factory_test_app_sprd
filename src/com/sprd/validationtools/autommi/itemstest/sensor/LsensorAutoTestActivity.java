/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi.itemstest.sensor;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LsensorAutoTestActivity extends BaseActivity{
    /** the status of l-sensor */
    private TextView mTextView = null;

    /** sensor manager object */
    private SensorManager mSensorManager = null;

    /** sensor object */
    private Sensor mSensor = null;

    /** sensor listener object */
    private SensorEventListener mListener = null;

    private boolean mSensorTestResult = true;
    private float mSensorData = 0;

    private static final String TAG = "LsensorAutoTestActivity";

    private static final int STOP_TEST = 0;
    private Handler mLHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case STOP_TEST:
                    if (mSensorData <= 0) {
                        mSensorTestResult = false;
                    }
                    autoCheckResult();
                    break;
                default:
                    break;
            }
        }
    };

    private void autoCheckResult() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mListener);
        }
        storeRusult(mSensorTestResult);
        finish();
    }

    private void diplaySensorData(float x) {
        mTextView.setText("\nX: " + x + "\n");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_auto_test);
        setTitle(R.string.proximity_sensor_test);
        mTextView = (TextView) findViewById(R.id.txt_msg_sensor);
        diplaySensorData(0);
        initSensor();
        mLHandler.sendEmptyMessageDelayed(STOP_TEST, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mListener);
        }
        mLHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    private void initSensor() {
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager == null) {
            storeRusult(false);
            finish();
        }
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                mSensorData = event.values[SensorManager.DATA_X];
                diplaySensorData(mSensorData);
            }
        };
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null) {
            return false;
        }
        return true;
    }

}