package com.sprd.validationtools.itemstest.audio;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.MotionEvent;//

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

public class VibratorTest extends BaseActivity {
    private final static String TAG = "VibratorTest";
    private static final long V_TIME = 100000;
    /*SPRD bug 755106:Repeat vibrate support*/
    private boolean mVibratePattern = false;
    private static final long[] PATTERN = new long[]{1000, 10000, 1000, 10000};
    /*@}*/
    TextView mContent;
    private Vibrator mVibrator = null;

    private final int PLAY_PASS = 0;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PLAY_PASS:
                    storeRusult(true);
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContent = new TextView(this);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(25);
        setContentView(mContent);
        setTitle(R.string.vibrator_test);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mContent.setText(R.string.vibrator_test);
		mPassButton.setVisibility(View.GONE);//
    }

    @Override
    protected void onResume() {
        super.onResume();
		if (Const.isAutoTestMode()) {
            doVibrator();
        }
	 
    }

    private void doVibrator() {
        /*SPRD bug 755106:Repeat vibrate support*/
        Log.d(TAG, "mVibratePattern=" + mVibratePattern);
        if (mVibratePattern) {
            mVibrator.vibrate(PATTERN, 0);
        } else {
            mVibrator.vibrate(V_TIME);
        }
		mPassButton.setVisibility(View.VISIBLE);//
        /*@}*/
        if (Const.isAutoTestMode()) {
            mHandler.sendEmptyMessageDelayed(PLAY_PASS, 2000);
        }

    }
	
	private void stop(){
		if (mVibrator != null) {
            mVibrator.cancel();
        }
	}

    @Override
    protected void onPause() {
        super.onPause();
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
				doVibrator();
        	 break;  
		 case MotionEvent.ACTION_UP:  
				stop();
        	 break;  	 
        case MotionEvent.ACTION_MOVE:
        	break;
		}
		
		//return super.onTouchEvent(event);
		return true;
	}
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
