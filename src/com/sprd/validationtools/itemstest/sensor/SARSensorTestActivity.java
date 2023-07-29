/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.sensor;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import com.sprd.validationtools.Const;

import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.content.Context;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

public class SARSensorTestActivity extends BaseActivity {

    private static final String TAG = "SARSensorTestActivity";

    /** the value of change color */
    private static final float VALUE_OF_CHANGE_COLOR = 0.5f;
    private static final float SAR_SENSOR_DEFAULT_VALUE = 1.0f;
    private static final String VALUE_FAR = "Distant";
    private static final String VALUE_CLOSE = "Closer";
    private static final int SAR_SENSOR_TYPE = 65591;

    private TextView mDisplayText;

    private SensorManager mSensorManager = null;
    private Sensor mSensor = null;
    private SensorEventListener mListener = null;

    private TextView mSarSensorTextView;

    private Context mContext;

    private boolean mIsCloseDone = false;
    private boolean mIsDistantDone = false;
    private boolean mTestPass = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.sensor_sar);
        setTitle(R.string.sar_sensor_test);

        mDisplayText = (TextView) findViewById(R.id.title_sensor_sar);
        mSarSensorTextView = (TextView) findViewById(R.id.txt_sar_sensor);
        initSensor();
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        setSARSensorDisplay(VALUE_FAR, SAR_SENSOR_DEFAULT_VALUE, Color.WHITE);
    }

    private void setSARSensorDisplay(String dis, float data, int color) {
        mSarSensorTextView.setText("");
        if (mSensor != null) {
            mSarSensorTextView.append("Chip id: " + mSensor.getName() + "\n");
        }

        mSarSensorTextView.append(getString(R.string.psensor_msg_data) + " " + data + "\n");
        mSarSensorTextView.append(getString(R.string.psensor_msg_value) + " " + dis);
        mSarSensorTextView.setBackgroundColor(color);
    }

    private void initSensor(){
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(SAR_SENSOR_TYPE, true);
        mListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                onSensorEvent(event.values[SensorManager.DATA_X]);
            }
        };
    }

    private void onSensorEvent(float x) {
        if (mTestPass) {
            return;
        }
        if (Const.isAutoTestMode()){
            if ( x > VALUE_OF_CHANGE_COLOR ) {
                mTestPass = true;
                storeRusult(true);
                finish();
            } else {
                mTestPass = true;
                storeRusult(false);
                finish();
            }
        }
        if (x <= VALUE_OF_CHANGE_COLOR) {
            setSARSensorDisplay(VALUE_CLOSE, x, Color.RED);
            mIsCloseDone = true;
        } else {
            if (mIsCloseDone) {
                setSARSensorDisplay(VALUE_FAR, x, Color.WHITE);
                mIsDistantDone = true;
            } else {
                Log.d(TAG, "Must be test near first!");
            }
        }
        Log.d(TAG, "mIsCloseDone="+mIsCloseDone+",mIsDistantDone="+mIsDistantDone);
        if (mIsCloseDone && mIsDistantDone) {
            mTestPass = true;
            Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT).show();
            storeRusult(true);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mListener);
        }
        super.onDestroy();
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
       SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(SAR_SENSOR_TYPE, true) == null) {
            Log.d(TAG, "is not support sar sensor");
            return false;
        }
        Log.d(TAG, "is support sar sensor");
        return true;
    }
}