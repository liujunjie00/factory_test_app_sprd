/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.sensor;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LsensorTestActivity extends BaseActivity {

    private static final String TAG = "LsensorTestActivity";
    // the default value
    private static final int LSENSOR_DEFAULT_VALUE = 0;
    private static final float MAXIMUM_BACKLIGHT = 1.0f;
    // Screen backlight when the value of the darkest
    private static final float MINIMUM_BACKLIGHT = 0.1f;
    // the max value of progressBar
    private static final int MAX_VALUE_PROGRESSBAR = 255;
    // Brightness current value
    private static final int BRIGHTNESS_CURRENT_VALUE = 180;
    // Brightness max value
    private static final float BRIGHTNESS_MAX_VALUE = 255.0f;
    // difference between maximum brightness and minimum brightness threshold.
    private static final int BRIGHTNESS_THRESHOLD = 30;
    private int mSensorMin = -1;
    private int mSensorMax = -1;
    // sensor manager object
    private SensorManager mSensorManager = null;
    // sensor object
    private Sensor mSensor = null;
    // sensor listener object
    private SensorEventListener mListener = null;
    // the progressBar object
    private ProgressBar mSensorProgressBar;
    // the textview object
    private TextView mValueIllumination;
    // System backlight value
    private int mCurrentValue;
    // Integer into a floating-point type
    private float mBrightnessValue;
    private Context mContext;
    private boolean mTestPass = false;
    private int mSystemCurrentScreenBrightnessValue;
    private int mSystemCurrentScreenBrightnessMode;
    private int mChangeCount;
    private int mPassCount;
    private boolean isShowPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.sensor_light);
        setTitle(R.string.light_sensor_test);
        TextView textTile = (TextView) findViewById(R.id.test_tile);
        textTile.setText(R.string.tip_for_lsensor);

//        Settings.System.putInt(getContentResolver(),
//                Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
//        Settings.System.putInt(getContentResolver(),
//                Settings.System.SCREEN_BRIGHTNESS, 5);
//        mCurrentValue = mSystemCurrentScreenBrightnessValue;
        Log.d(TAG, "mCurrentValue =" + mCurrentValue);
        mValueIllumination = (TextView) findViewById(R.id.txt_value_lsensor);
        mSensorProgressBar = (ProgressBar) findViewById(R.id.progressbar_lsensor);
        mSensorProgressBar.setMax(MAX_VALUE_PROGRESSBAR);
        getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
                false, mObserver);

        /*mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                onSensorEvent(event.values[SensorManager.DATA_X]);
            }
            @Override
            public void onAccuracyChanged(Sensor s, int accuracy) {
                // ignore
            }
        };
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);*/

        mPassButton.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mSystemCurrentScreenBrightnessValue = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
            mSystemCurrentScreenBrightnessMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
            mCurrentValue = mSystemCurrentScreenBrightnessValue;
            Log.d(TAG, "SystemCurrentScreenBrightnessValue =" + mSystemCurrentScreenBrightnessValue);
            Log.d(TAG, "SystemCurrentScreenBrightnessMode =" + mSystemCurrentScreenBrightnessMode);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        Settings.System.putInt(getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
        mValueIllumination.setText(getString(R.string.lsensor_value) + mCurrentValue);
        mSensorProgressBar.setProgress(mCurrentValue);
    }

    @Override
    protected void onPause() {
        super.onPause();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE, mSystemCurrentScreenBrightnessMode);
    }

    @Override
    protected void onDestroy() {
//        if (mSensorManager != null) {
//            mSensorManager.unregisterListener(mListener);
//        }
//        setBrightness(mBrightnessValue);
//        Log.d(TAG, "onDestory mBrightnessValue =" + mBrightnessValue);
        getContentResolver().unregisterContentObserver(mObserver);
        super.onDestroy();
    }

    private ContentObserver mObserver = new ContentObserver(null) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
                int anInt = 0;
                try {
                    anInt = Settings.System.getInt(getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS);
                    if (mCurrentValue != anInt) {
                        mChangeCount ++;
                    }
                    mCurrentValue = anInt;
                    onSensorEvent(mCurrentValue);
                    Log.d(TAG, "onChange ChangeCount: " + mChangeCount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onChange SCREEN_BRIGHTNESS: " + anInt);
        }
    };

    private void onSensorEvent(int x) {
        if (mTestPass) {
            return;
        }
        int processInt = x;
        if (mSensorMin == -1 || mSensorMin > x) {
            mSensorMin = processInt;
        }
        if (mSensorMax == -1 || mSensorMax < x) {
            mSensorMax = processInt;
        }
        showStatus(x);
        if (mChangeCount >= 3) {
            Log.d(TAG, "pass");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isShowPass) {
                        Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT).show();
                        isShowPass = true;
                    }
                    if (mPassButton != null && mPassButton.getVisibility() == View.GONE) {
                        mPassButton.setVisibility(View.VISIBLE);
                    }
                    /*if (mPassCount >= 3) {
                        storeRusult(true);
                        finish();
                    }
                    mPassCount ++;*/
                }
            });
        }
//        if ((mSensorMax - mSensorMin) > BRIGHTNESS_THRESHOLD) {
//            mTestPass = true;
//            Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT).show();
//            storeRusult(true);
//            finish();
//        }
    }

    private void showStatus(int x) {
        mValueIllumination.setText(getString(R.string.lsensor_value) + x);
        int valueProgress = x;
        mSensorProgressBar.setProgress(valueProgress);
        /*float currentBrightnessValue = x / BRIGHTNESS_MAX_VALUE;
        Log.d(TAG, "x = " + x + " currentBrightnessValue = " + currentBrightnessValue);
        if (currentBrightnessValue > MAXIMUM_BACKLIGHT) {
            setBrightness(MAXIMUM_BACKLIGHT);
        } else if (currentBrightnessValue < MINIMUM_BACKLIGHT) {
            setBrightness(MINIMUM_BACKLIGHT);
        } else {
            setBrightness(currentBrightnessValue);
        }*/
    }

    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
        getWindow().setAttributes(lp);
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null) {
            Log.d(TAG, "Not support light sensor!");
            return false;
        }
        return true;
    }
}
