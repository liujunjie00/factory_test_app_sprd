/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.sensor;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;
import android.graphics.Color;
import android.util.Log;
import android.os.Message;

public class PressureSensorCalibrationActivity extends BaseActivity {

    private static final String TAG = "PressureSensorCalibrationActivity";
    private static final int SET_CMD_COMPLETE = 1;
    private static final int CALIBRATION_SUCCESS = 2;
    private static final int CALIBRATION_FAIL = 3;

    private static final String SET_CMD = "0 6 1"; // start calibrating
    private static final String GET_RESULT = "1 6 1";// get result of Calibration
    private static final String SAVE_RESULT = "3 6 1";// save the result

    private static final String CALI_FILE = "/mnt/vendor/productinfo/sensor_calibration_data/pressure";

    private TextView mDisplayText, mTipText;
    private EditText mCurrentInputValue;
    private Context mContext;
    private Button mStartCalibration;

    private SensorUtils mSensorUtils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_pressure_calibration);
        mContext = this;
        mDisplayText = (TextView) findViewById(R.id.result_sensor_pressure);
        mTipText = (TextView) findViewById(R.id.sensor_pressure_tips);
        mCurrentInputValue = (EditText) findViewById(R.id.current_pressure);
        mStartCalibration = (Button) findViewById(R.id.start_test_button);
        mPassButton.setVisibility(View.GONE);
        mFailButton.setVisibility(View.GONE);
        mSensorUtils = new SensorUtils(this, Sensor.TYPE_PRESSURE);
        mSensorUtils.enableSensor();
        if (Const.isAutoTestMode()) {
            startSensorCalibration();
        }
        mStartCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayText.setText(mContext.getResources().getString(
                        R.string.pressure_sensor_calibration_start));
                mPassButton.setVisibility(View.VISIBLE);
                mFailButton.setVisibility(View.VISIBLE);
                mCurrentInputValue.setVisibility(View.GONE);
                mTipText.setVisibility(View.GONE);
                startSensorCalibration();
                mStartCalibration.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startSensorCalibration() {
        new SensorCalibrationThread().start();
    }

    class SensorCalibrationThread extends Thread {
        public void run() {
            sensorCalibration();
        };
    };

    private void sensorCalibration() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Start calibration, send calibration command
        String cmd = customSetCMD();
        FileUtils.writeFile(Const.CALIBRATOR_CMD, cmd);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandler.sendMessage(mHandler.obtainMessage(SET_CMD_COMPLETE));

    }

    // Combine the written pressure value with the sensor type
    public String customSetCMD(){
        if (!mCurrentInputValue.getText().toString().equals("")) {
            String string = mCurrentInputValue.getText().toString();
            float fValue = Float.parseFloat(string) * 100;
            long normalizedValue = (long) fValue;
            string =String.valueOf(normalizedValue);
            return SET_CMD +  " " + string;
        }
        return SET_CMD + " 0";
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isShouldHideInput(view, ev)) {
                hideInput(view);
            }
            return super.dispatchTouchEvent(ev);
        }
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    private void hideInput(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public boolean isShouldHideInput(View view, MotionEvent event) {
        if (view != null && (view instanceof EditText)) {
            int[] leftAndTop = {0, 0};
            view.getLocationInWindow(leftAndTop);
            int left = leftAndTop[0];
            int top = leftAndTop[1];
            int bottom = top + view.getHeight();
            int right = left + view.getWidth();
            if (event.getX() > left && event.getX() < right &&
                    event.getY() > top && event.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void getResult() {
        if (SensorUtils.getResult(GET_RESULT)) {
            if (SensorUtils.saveResult(SAVE_RESULT, CALI_FILE)) {
                mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_SUCCESS));
            } else {
                mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_FAIL));
            }

        } else {
            mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_FAIL));
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_CMD_COMPLETE:
                    new Thread(new Runnable() {
                        public void run() {
                            getResult();
                        }
                    }).start();
                    break;
                case CALIBRATION_SUCCESS:
                    Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT).show();
                    storeRusult(true);
                    finish();
                    break;
                case CALIBRATION_FAIL:
                    mDisplayText.setText(mContext.getResources().getString(R.string.pressure_sensor_calibration_fail));
                    storeRusult(false);
                    finish();
                    break;
                default:
            }
        }
    };

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        if (mSensorUtils != null){
            mSensorUtils.disableSensor();
        }
        super.onDestroy();
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) == null
                || !Const.IS_SUPPORT_CALIBTATION_TEST) {
            return false;
        }
        return true;
    }
}
