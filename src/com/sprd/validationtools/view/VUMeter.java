package com.sprd.validationtools.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.View;
import com.sprd.validationtools.R;
import android.util.Log;
 
public class VUMeter extends View {
	private static final String TAG = "VUMeter";
    private static final float PIVOT_RADIUS = 3.5f;
    private static final float PIVOT_Y_OFFSET = 10f;
    private static final float SHADOW_OFFSET = 2.0f;
    private static final float DROPOFF_STEP = 0.18f;
    private static final float SURGE_STEP = 0.35f;
    private static final long  ANIMATION_INTERVAL = 70;
    private static final int COLOR_NUMBER = 60;
    private static final float MIN_ANGLE = (float) Math.PI / 8;
    private static final float MAX_ANGLE = (float) Math.PI * 7 / 8;
    private static final float BASE_NUMBER = 32768;
    
    private Paint mPaint, mShadow;
    public float mCurrentAngle;
    
    private MediaRecorder mRecorder;
 
    public VUMeter(Context context) {
        super(context);
        init(context);
    }
 
    public VUMeter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
 
    void init(Context context) {
        Drawable background = context.getResources().getDrawable(R.drawable.record_bg);
        setBackgroundDrawable(background);
        
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadow.setColor(Color.argb(COLOR_NUMBER, 0, 0, 0));
        
        mRecorder = null;
        
        mCurrentAngle = 0;
    }
 
    public void setRecorder(MediaRecorder recorder) {
    	mRecorder = recorder;
    	invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
 
        final float minAngle = MIN_ANGLE;
        final float maxAngle = MAX_ANGLE;
                
        float angle = minAngle;
        if (mRecorder != null) 
        //	angle += (float)(maxAngle - minAngle)*mRecorder.getMaxAmplitude()/BASE_NUMBER;
        	angle += (float)((maxAngle *mRecorder.getMaxAmplitude())*6)/BASE_NUMBER;

//Log.d(TAG, "tqy+++++onDraw angle== "+angle /* +"  maxAngle="+maxAngle+" minAngle="+minAngle */);	
			
        if (angle > mCurrentAngle){
            mCurrentAngle = angle;
        }else{
            mCurrentAngle = Math.max(angle, mCurrentAngle - DROPOFF_STEP);
		}
        mCurrentAngle = Math.min(maxAngle, mCurrentAngle);
//Log.d(TAG, "tqy+++++onDraw mCurrentAngle== "+mCurrentAngle );
 
        float w = getWidth();
        float h = getHeight();
        float pivotX = w/2;
        float pivotY = h - PIVOT_RADIUS - PIVOT_Y_OFFSET;
        float l = h*4/5;
        float sin = (float) Math.sin(mCurrentAngle);
        float cos = (float) Math.cos(mCurrentAngle);
        float x0 = pivotX - l*cos;
        float y0 = pivotY - l*sin;
        canvas.drawLine(x0 + SHADOW_OFFSET, y0 + SHADOW_OFFSET, pivotX + SHADOW_OFFSET, pivotY + SHADOW_OFFSET, mShadow);
        canvas.drawCircle(pivotX + SHADOW_OFFSET, pivotY + SHADOW_OFFSET, PIVOT_RADIUS, mShadow);
        canvas.drawLine(x0, y0, pivotX, pivotY, mPaint);
        canvas.drawCircle(pivotX, pivotY, PIVOT_RADIUS, mPaint);
        
        if (mRecorder != null)
        	postInvalidateDelayed(ANIMATION_INTERVAL);
    }
    
    public void finish() {
        if (mRecorder != null) {
            mRecorder = null;
            mCurrentAngle = 0.0f;
        }
    }

}
