
package com.sprd.validationtools.itemstest.sensor;

import java.util.Timer;
import java.util.TimerTask;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class GsensorTestActivity extends BaseActivity {
    private static final int DATA_X = 0;

    private static final int DATA_Y = 1;

    private static final int DELAY_TIME = 300;

    /** sensor manager object */
    private SensorManager manager = null;

    /** sensor object */
    private Sensor sensor = null;

    private boolean mPassed = false;

    /** sensor listener object */
    private SensorEventListener listener = null;

    private Timer mTimer;

    private float[] mValues;
//    public Handler mHandler = new Handler();
    private static final int SHOW_MSG = 1;
    private TextView mTextView = null;
    private int mSystemCurrentAccelerometerRotationValue;
    public Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case SHOW_MSG:
                String text = (String) msg.obj;
                if(mTextView != null){
                    mTextView.setText(text);
                }
                break;

            default:
                break;
            }
        };
    };
    private void showMsg(float x, float y, float z) {
        StringBuffer text = new StringBuffer("");
        if (sensor != null){
            text.append("chip id: " + sensor.getName() + "\n");
        }
        text.append(" X = " + x + "\n");
        text.append(" Y = " + y + "\n");
        text.append(" Z = " + z + "\n");
        Message message = Message.obtain();
        message.obj = text.toString();
        message.what = SHOW_MSG;
        mHandler.sendMessage(message);
    }

    private boolean mDxOk = false;
    private boolean mDyOk = false;
    private boolean mDzOk = false;
    private TextView mUpTxt;
    private TextView mDownTxt;
    private TextView mLeftTxt;
    private TextView mRightTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_gravity);
        setTitle(R.string.gravity_sensor_test);
        mTextView = (TextView) findViewById(R.id.txt_msg_gsensor);
        mUpTxt = (TextView) findViewById(R.id.txt_sensor_arrow_up);
        mDownTxt = (TextView) findViewById(R.id.txt_sensor_arrow_down);
        mLeftTxt = (TextView) findViewById(R.id.txt_sensor_arrow_left);
        mRightTxt = (TextView) findViewById(R.id.txt_sensor_arrow_right);
        showMsg(0, 0, 0);
            mPassButton.setVisibility(View.GONE);
        initSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        try {
            mSystemCurrentAccelerometerRotationValue = Settings.System.getInt(getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION);
            Log.d("GsensorTestActivity", "SystemCurrentAccelerometerRotationValue =" + mSystemCurrentAccelerometerRotationValue);
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION,
                0);
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mValues != null) {
                            float x = mValues[DATA_X];
                            float y = mValues[DATA_Y];
                            if (Math.abs(x) < 1) {
                                x = 0;
                            }
                            if (Math.abs(y) < 1) {
                                y = 0;
                            }
                            showArrow(x, y);
                        }
                    }
                });
            }
        }, 0, DELAY_TIME);
    }

    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        manager.unregisterListener(listener);
        Settings.System.putInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, mSystemCurrentAccelerometerRotationValue);
        super.onPause();
    }

    private void initSensor() {
        mPassed = false;
        listener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                mValues = event.values;
                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];
                showMsg(x, y, z);
                double dx = Math.abs(9.8 - Math.abs(x));
                double dy = Math.abs(9.8 - Math.abs(y));
                double dz = Math.abs(9.8 - Math.abs(z));
                double ref = 9.8 * 0.08;
                if (!mDxOk)
                    mDxOk = dx < ref;
                if (!mDyOk)
                    mDyOk = dy < ref;
                if (!mDzOk)
                    mDzOk = dz < ref;
            }
        };

        manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void showArrow(float x, float y) {
        int arrowId = 0;

        if (Math.abs(x) <= Math.abs(y)) {
            if (y < 0) {
                // up is low
                //arrowId = R.drawable.arrow_up;
                arrowId = R.drawable.arrow_left;
                mLeftTxt.setBackgroundResource(arrowId);
            } else if (y > 0) {
                // down is low
                //arrowId = R.drawable.arrow_down;
                arrowId = R.drawable.arrow_right;
                mRightTxt.setBackgroundResource(arrowId);
            } else if (y == 0) {
                // do nothing
            }
        } else {
            if (x < 0) {
                // right is low
                //arrowId = R.drawable.arrow_right;
                arrowId = R.drawable.arrow_up;
                mUpTxt.setBackgroundResource(arrowId);
            } else {
                // left is low
                //arrowId = R.drawable.arrow_left;
                arrowId = R.drawable.arrow_down;
                //mLeftTxt.setBackgroundResource(arrowId);
                mDownTxt.setBackgroundResource(arrowId);
            }
        }
        if (!mPassed && mUpTxt.getBackground() != null && mDownTxt.getBackground() != null
                && mLeftTxt.getBackground() != null && mRightTxt.getBackground() != null) {
            mPassed = true;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(GsensorTestActivity.this, R.string.text_pass,
                            Toast.LENGTH_SHORT).show();
                //    storeRusult(true);
                //    finish();
					if(mPassButton != null){
					mPassButton.setVisibility(View.VISIBLE);
					}
                }
            }, 500);

        }
    }
}
