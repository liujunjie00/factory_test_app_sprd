/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.R;
import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.utils.FileUtils;

public class ProxSensorNoiseTestActivity extends BaseActivity {

    private static final String TAG = "ProxSensorNoiseTestActivity";

    /** the value of change color */
    private static final float VALUE_OF_CHANGE_COLOR = 0.5f;

    private static final float PSENSOR_DEFAULT_VALUE = 1.0f;

    private static final String VALUE_FAR = "Distant";

    private static final String VALUE_CLOSE = "Closer";

    /** sensor manager object */
    private SensorManager pManager = null;

    /** sensor object */
    private Sensor pSensor = null;

    /** sensor listener object */
    private SensorEventListener pListener = null;

    /** the status of p-sensor */
    private TextView psensorTextView;

    private Context mContext;

    private boolean mIsCloseDone = false;
    private boolean mIsDistantDone = false;

    public static final String SensorNoiseFile = "/sys/class/xr-pls/device/ps_noise";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.sensor_noise_proximity);
        //setTitle(R.string.proximity_sensor_test);
        psensorTextView = (TextView) findViewById(R.id.txt_psensor);
        initSensor();
        setPsensorDisplay(VALUE_FAR, PSENSOR_DEFAULT_VALUE, Color.WHITE,"0");
    }

    @Override
    protected void onResume() {
        super.onResume();
        psensorTextView.setBackgroundColor(Color.WHITE);
        pManager.registerListener(pListener, pSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
        startBackgroundThread();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                checkLightSensorNoise(PSENSOR_DEFAULT_VALUE);
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        if (pManager != null) {
            pManager.unregisterListener(pListener);
        }
        stopBackgroundThread();
        super.onPause();
    }

    private void setPsensorDisplay(String dis, float data, int color,String noiseValue) {
        psensorTextView.setText("");
        if (pSensor != null) {
            psensorTextView.append("Chip id: " + pSensor.getName() + "\n");
        }
        psensorTextView.append(getString(R.string.psensor_noise_msg_data) + " "
                + noiseValue + "\n");
        psensorTextView.append(getString(R.string.psensor_msg_value) + " "
                + dis);
        psensorTextView.setBackgroundColor(color);
    }

    private void initSensor() {
        pManager = (SensorManager) this
                .getSystemService(Context.SENSOR_SERVICE);
        pSensor = pManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        pListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
            }
        };
    }

    private static String getValueFromFile(String filename) {
        File file = new File(filename);
        InputStream fIn = null;
        InputStreamReader isr = null;
        try {
            fIn = new FileInputStream(file);
            isr = new InputStreamReader(fIn,Charset.defaultCharset());
            char[] inputBuffer = new char[1024];
            int q = -1;

            q = isr.read(inputBuffer);
            isr.close();
            fIn.close();

            if (q > 0)
                return String.valueOf(inputBuffer, 0, q).trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "0";
        }finally {
            try {
                if (isr != null) {
                    isr.close();
                }
                if(fIn != null){
                    fIn.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        return "0";
    }

    private HandlerThread mBackgroundThread = null;
    private Handler mHandler = null;

    private void checkLightSensorNoise(float x){
        String value = getValueFromFile(SensorNoiseFile);
        Log.d(TAG, "setPsensorDisplay value="+value);
        if (x <= VALUE_OF_CHANGE_COLOR) {
            setPsensorDisplay(VALUE_CLOSE, x, Color.RED, value);
            mIsDistantDone = false;
        } else {
            setPsensorDisplay(VALUE_FAR, x, Color.WHITE, value);
            mIsDistantDone = true;
        }
        Log.d(TAG, "onSensorChanged value="+value +
                ",mIsCloseDone="+mIsCloseDone+",mIsDistantDone="+mIsDistantDone);
        int inValues = 0;
        try {
            inValues = Integer.valueOf(value);
        } catch (NumberFormatException  e) {
            e.printStackTrace();
        }
        Log.d(TAG, "onSensorChanged inValues="+inValues);
        if (mIsDistantDone && inValues > 190) {
            Toast.makeText(mContext, R.string.text_fail, Toast.LENGTH_SHORT).show();
            storeRusult(false);
            finish();
        } else {
            if (Const.isAutoTestMode()) {
                storeRusult(true);
                finish();
            }
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null) {
            Log.d(TAG, "ProxSensorNoiseTestActivity false");
            return false;
        }
        /*SPRD bug 820257:Display test item by file exists.*/
        if(FileUtils.fileIsExists(ProxSensorNoiseTestActivity.SensorNoiseFile)){
            Log.d(TAG, "ProxSensorNoiseTestActivity true");
            return true;
        }
        Log.d(TAG, "ProxSensorNoiseTestActivity false");
        return false;
        /*@}*/
    }
}
