
package com.sprd.validationtools.itemstest.sensor;

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
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PsensorTestActivity extends BaseActivity {

    private static final String TAG = "PsensorTestActivity";

    // the value of change color
    private static final float VALUE_OF_CHANGE_COLOR = 0.5f;
    // the default value
    private static final float PSENSOR_DEFAULT_VALUE = 1.0f;
    private static final String VALUE_FAR = "Distant";
    private static final String VALUE_CLOSE = "Closer";
    // sensor manager object
    private SensorManager mSensorManager = null;
    // sensor object
    private Sensor mSensor = null;
    // sensor listener object
    private SensorEventListener mListener = null;
    // the status of p-sensor
    private TextView mSensorTextView;
    private Context mContext;
    private boolean mIsCloseDone = false;
    private boolean mIsDistantDone = false;
    private boolean mTestPass = false;
	private boolean mHigh = false;
    private boolean mLow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.sensor_proximity);
        setTitle(R.string.proximity_sensor_test);
        mSensorTextView = (TextView) findViewById(R.id.txt_psensor);
        initSensor();
        mSensorManager.registerListener(mListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
	mPassButton.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPsensorDisplay(VALUE_FAR, PSENSOR_DEFAULT_VALUE, Color.WHITE);
        mSensorTextView.setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDestroy() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(mListener);
        }
        super.onDestroy();
    }

    private void initSensor() {
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                onSensorEvent(event.values[SensorManager.DATA_X]);
 				float x = event.values[SensorManager.DATA_X];
			if(x < 50){
				mLow = true;
			}else if(x > 100){
				mHigh = true;
			}
			
			if(mHigh && mLow){
				try{
				mPassButton.setVisibility(View.VISIBLE);//
				}catch (Exception e){
					
				}
			}
            }
            public void onAccuracyChanged(Sensor s, int accuracy) {
                // ignore
            }
        };
    }

    private void onSensorEvent(float x) {
        if (mTestPass) {
            return;
        }
        if (x <= VALUE_OF_CHANGE_COLOR) {
            setPsensorDisplay(VALUE_CLOSE, x, Color.RED);
            mIsCloseDone = true;
        } else {
            if (mIsCloseDone) {
                setPsensorDisplay(VALUE_FAR, x, Color.WHITE);
                mIsDistantDone = true;
            } else {
                Log.d("", "Must be test near first!");
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

    private void setPsensorDisplay(String dis, float data, int color) {
        mSensorTextView.setText("");
        if (mSensor != null) {
            mSensorTextView.append("Chip id: " + mSensor.getName() + "\n");
        }
        mSensorTextView.append(getString(R.string.psensor_msg_data) + " " + data + "\n");
        mSensorTextView.append(getString(R.string.psensor_msg_value) + " " + dis);
        mSensorTextView.setBackgroundColor(color);
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null) {
            Log.d(TAG, "Not support p sensor!");
            return false;
        }
        return true;
    }
}
