
package com.sprd.validationtools.itemstest.sensor;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class CompassTestActivity extends BaseActivity {
    private static final float FULL_DEGREES = 360f;

    private Bitmap mBitmap, mBackground, mPointer;

    private Canvas mCanvas;

    private SensorManager mManager;

    private Sensor mOSensor;

    private Sensor mMSensor;

    private SensorEventListener mOListener;

    private SensorEventListener mMListener;
	
	private float mX;
   private boolean onetime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_oritentation);
        setTitle(R.string.oritention_sensor_test);
        initDrawer();
        initSensor();
        showInfo(0, 0, 0);
        draw(0);
		
		mPassButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.registerListener(mOListener, mOSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mManager.registerListener(mMListener, mMSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        mManager.unregisterListener(mOListener);
        mManager.unregisterListener(mMListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mBackground.recycle();
        mPointer.recycle();
        mBitmap.recycle();
        mCanvas = null;
        super.onDestroy();
    }

    private void initSensor() {
        mManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mOSensor = mManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mOListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
				if(onetime && x != 0 ){
				onetime = false;	
				mX = x;
				}
                showOrien(x);
                draw(x);
				if (Math.abs(mX - x) > 20) {
                     try{
						mPassButton.setVisibility(View.VISIBLE);//
						}catch (Exception e){}
                }
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };

        mMSensor = mManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mMListener = new SensorEventListener() {
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                showInfo(x, y, z);
            }

            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }

    private void initDrawer() {
        mBackground = BitmapFactory.decodeResource(getResources(), R.drawable.compass_bg);
        mPointer = BitmapFactory.decodeResource(getResources(), R.drawable.compass_p);
        int width = mPointer.getWidth();
        int height = mPointer.getHeight();
        mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
    }

    private void showInfo(float x, float y, float z) {
        TextView view = (TextView) findViewById(R.id.txt_msg_osensor);
        view.setText("");
        if (mMSensor != null)
            view.append("chip id: " + mMSensor.getName() + "\n");
        view.append(" X = " + x + "\n");
        view.append(" Y = " + y + "\n");
        view.append(" Z = " + z + "\n");
    }

    private void showOrien(float x) {
        TextView view = (TextView) findViewById(R.id.txt_msg_orien);
        view.setText(getString(R.string.osensor_orien) + x);
    }

    private void draw(float value) {
        Matrix matrix = new Matrix();
        Paint paint = new Paint();
        float width = mPointer.getWidth() / (float) 2;
        float height = mPointer.getHeight() / (float) 2;
        matrix.postRotate(FULL_DEGREES - value, width, height);

        mCanvas.drawBitmap(mBackground, new Matrix(), paint);
        mCanvas.drawBitmap(mPointer, matrix, paint);
        View view = findViewById(R.id.orien_image);
        view.setBackgroundDrawable(new BitmapDrawable(mBitmap));
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        return true;
    }
}
