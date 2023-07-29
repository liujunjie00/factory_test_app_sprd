
package com.sprd.validationtools.itemstest.led;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.R;

import android.os.Handler;
import android.view.View;
public class GreenLightTest extends BaseActivity {
    private static final String TAG = "GreenLightTest";
    private TextView mContent;
    /*Add Auto MMI Test*/
    private static final int SET_BACKGROUND = 0;
    private static final int CHECK_LIGHT = 1;
    private boolean mResult = false;
	private Handler mUiHandler = new Handler();
	private Runnable mRunnable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContent = new TextView(this);
        setBackground();
        setContentView(mContent);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(35);
        mHandler.sendEmptyMessage(SET_BACKGROUND);
		mRunnable = new Runnable() {
            public void run() {
				mPassButton.setVisibility(View.VISIBLE);
            }
        };
		
		initTime();
    }

    /* Add Auto MMI Test*/
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_BACKGROUND:
                    setBackground();
                    if (Const.isAutoTestMode()) {
                        mHandler.sendEmptyMessageDelayed(CHECK_LIGHT, 3000);
                    }
                    break;
                case CHECK_LIGHT:
                    checkLight();
                    break;
            }
        }
    };

    private void checkLight() {
        PhaseCheckParse.getInstance().writeLedlightSwitch(9, 0);
        storeRusult(mResult);
        finish();
    }
    /* @} */

    private void setBackground() {
        mContent.setBackgroundColor(Color.GREEN);
        mContent.setText(getString(R.string.status_indicator_green));
        mResult = PhaseCheckParse.getInstance().writeLedlightSwitch(9, 1);
        Log.d(TAG, "startTestWork GreenLightTest res="+mResult);
    }

    @Override
    protected void onDestroy() {
        PhaseCheckParse.getInstance().writeLedlightSwitch(9, 0);
		if(mUiHandler != null){
		mUiHandler.removeCallbacks(mRunnable);
		}
        super.onDestroy();
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        if (!Const.IS_SUPPORT_LED_TEST && !FileUtils.fileIsExists(Const.LED_GREEN_PATH)) {
            return false;
        }
        return true;
    }
	
	private void initTime(){
		mPassButton.setVisibility(View.GONE);
		
		mUiHandler.postDelayed(mRunnable, 3000);
	}
}
