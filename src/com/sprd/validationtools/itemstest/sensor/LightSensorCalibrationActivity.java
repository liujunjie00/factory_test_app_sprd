/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.sensor;
// Add light sensor calibration test items to MMI

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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.util.Log;
import android.os.Message;
import android.widget.Button;

public class LightSensorCalibrationActivity extends BaseActivity {

    private static final String TAG = "LightSensorCalibrationActivity";
    private static final int START_CALIBRATION = 1;
    private static final int CALIBRATION_SUCCESS = 2;
    private static final int CALIBRATION_FAIL = 3;

    private static final String SET_CMD = "0 5 1"; // start calibrating
    private static final String GET_RESULT = "1 5 1";// get result of Calibration
    private static final String SAVE_RESULT = "3 5 1";// save the result

    private static final String CALI_FILE = "/mnt/vendor/productinfo/sensor_calibration_data/light";

    private static final int PASS_DATA_LEN = 30;

    private TextView mDisplayText;
    private Context mContext;
    private Button startCalibration;

    private SensorUtils mSensorUtils = null;

    private Runnable mR = new Runnable() {
        public void run() {
            mDisplayText.setText(mContext.getResources().getString(
                    R.string.light_sensor_calibration_fail));
            if (Const.isAutoTestMode()) {
                storeRusult(false);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_light_calibration);
        mContext = this;
        mDisplayText = (TextView) findViewById(R.id.result_sensor_light);
        startCalibration = (Button) findViewById(R.id.start_test_button);
        mSensorUtils = new SensorUtils(this, Sensor.TYPE_LIGHT);
        mSensorUtils.enableSensor();
        if (Const.isAutoTestMode()) {
            startSensorCalibration();
        }
        startCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDisplayText.setText(mContext.getResources().getString(
                        R.string.light_sensor_calibration_start));
                startSensorCalibration();
                startCalibration.setVisibility(View.GONE);
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

    /**
     ** start calibrating
     **/
    private void sensorCalibration() {
        if (Const.IS_SUPPORT_LIGHT_CALIBTATION_TEST) {
            mHandler.sendMessage(mHandler.obtainMessage(START_CALIBRATION));
        } else {
            FileUtils.writeFile(Const.CALIBRATOR_CMD, SET_CMD);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHandler.sendMessage(mHandler.obtainMessage(START_CALIBRATION));
        }
    }

    private void getResult() {
        if (Const.IS_SUPPORT_LIGHT_CALIBTATION_TEST) {
            int saveResultLen = FileUtils.saveFileData(Const.LIGHT_CALIBRATOR_CMD, CALI_FILE);
            Log.d(TAG, "save result: " + saveResultLen);
            if (saveResultLen == PASS_DATA_LEN) {
                mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_SUCCESS));
            } else {
                mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_FAIL));
            }
        } else {
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
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_CALIBRATION:
                    new Thread(new Runnable() {
                        public void run() {
                            getResult();
                        }
                    }).start();
                    break;
                case CALIBRATION_SUCCESS:
                    Toast.makeText(mContext, R.string.text_pass,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(true);
                    finish();
                    break;
                case CALIBRATION_FAIL:
                    mHandler.post(mR);
                    break;
                default:
            }
        }
    };

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mR);
        if(mSensorUtils != null){
            mSensorUtils.disableSensor();
        }
        super.onDestroy();
    }

    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null
                || !Const.IS_SUPPORT_CALIBTATION_TEST) {
            return false;
        }
        return true;
    }

}
