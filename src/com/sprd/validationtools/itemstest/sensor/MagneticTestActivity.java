package com.sprd.validationtools.itemstest.sensor;

import java.util.Timer;
import java.util.TimerTask;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import com.sprd.validationtools.itemstest.sensor.Mi;//modify by tqy 20220110


public class MagneticTestActivity extends BaseActivity {
    private static final String TAG = "MagneticTestActivity";
    private static final int M_COMPAR_VALUE = 20;

    private float[] data_last = { 0, 0, 0 };

    private float[] data_current = { 0, 0, 0 };

    private SensorManager mSManager = null;

    private Sensor mSensor = null;

    private SensorEventListener mListener = null;

    private Context mContext;

    private TextView mWarnText;

    private TextView mDisplayText;

    private boolean onetime = true;

    private boolean xPass = false;
    private boolean yPass = false;
    private boolean zPass = false;

    private boolean mSuccessFlag = false;
    private boolean mFailFlag = false;

    /*SPRD bug 742118:Add test fail while hasn't sensor*/
    private static final long TEST_FAIL_TIMEOUT = 5000l;
    private Handler mHandler = new Handler();
    private Runnable mFailRunnable = new Runnable() {
        public void run() {
            if(xPass || yPass || zPass){
                Log.d(TAG, "mFailRunnable already success xPass="+xPass+",yPass="+yPass+",zPass="+zPass);
                return;
            }
            Log.d(TAG, "mFailRunnable test fail finish!");
            Toast.makeText(mContext, R.string.text_fail,
                    Toast.LENGTH_SHORT).show();
            storeRusult(false);
            finish();
        }
    };
    /*@}*/
    private Mi mMiui;//modify by tqy 20220110
    private  float mVal;//modify by tqy 20220110

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.sensor_magnetic);
        mDisplayText = (TextView) findViewById(R.id.magnetic_xyz);
		mMiui = findViewById(R.id.miui);////modify by tqy 20220110
        diplayXYZ(0, 0, 0);
        initSensor();
        /*SPRD bug 742118:Add test fail while hasn't sensor*/
        mHandler.postDelayed(mFailRunnable, TEST_FAIL_TIMEOUT);
        /*@}*/
            mPassButton.setVisibility(View.GONE);
        mSuccessFlag = false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        /*SPRD bug 742118:Add test fail while hasn't sensor*/
        mHandler.removeCallbacks(mFailRunnable);
        /*@}*/
    }

    private void diplayXYZ(float x, float y, float z) {
        mDisplayText.setText("\nX: " + x + "\nY: " + y + "\nZ: " + z);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSManager.registerListener(mListener, mSensor,
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        if (mSManager != null) {
            mSManager.unregisterListener(mListener);
        }
        super.onPause();
    }

    private void initSensor() {
        mSManager = (SensorManager) this
                .getSystemService(Context.SENSOR_SERVICE);
    //    mSensor = mSManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensor = mSManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);////modify by tqy 20220110
        mListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                Log.d(TAG, "mmi test values: " + event.values[0] + ","
                        + event.values[1] + "," + event.values[2]);
                /*SPRD bug 742118:Add test fail while hasn't sensor*/
                mHandler.removeCallbacks(mFailRunnable);
                /*@}*/

				mVal = event.values[0] ; ////modify by tqy 20220110
                mMiui.setVal(mVal);  ////modify by tqy 20220110
                diplayXYZ(event.values[0], event.values[1], event.values[2]);
                data_current[0] = event.values[0];
                data_current[1] = event.values[1];
                data_current[2] = event.values[2];

                if (onetime && (data_current[0] != 0) && (data_current[1] != 0)
                        && (data_current[2] != 0)) {
                    onetime = false;
                    data_last[0] = data_current[0];
                    data_last[1] = data_current[1];
                    data_last[2] = data_current[2];
                }
                if (Math.abs(data_current[0] - data_last[0]) > M_COMPAR_VALUE) {
                    xPass = true;
                }
                if (Math.abs(data_current[1] - data_last[1]) > M_COMPAR_VALUE) {
                    yPass = true;
                }
                if (Math.abs(data_current[1] - data_last[1]) > M_COMPAR_VALUE) {
                    zPass = true;
                }

                if (xPass && yPass && zPass) {
                    if(!mSuccessFlag){
                        mSuccessFlag = true;
						mPassButton.setVisibility(View.VISIBLE);//
                 /*       Toast.makeText(mContext, R.string.text_pass,
                                Toast.LENGTH_SHORT).show();
                        storeRusult(true);
                        finish(); */
                    }
                }
            }
        };
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null) {
            return false;
        }
        return true;
    }
}