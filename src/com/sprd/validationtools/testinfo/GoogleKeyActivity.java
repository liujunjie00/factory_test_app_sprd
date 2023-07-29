package com.sprd.validationtools.testinfo;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import com.sprd.validationtools.BaseActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;

public class GoogleKeyActivity extends BaseActivity {
    private final static String TAG = "GoogleKeyActivity";
    
    private TextView  mGoogleKeytxtInfo;  
	private boolean isShowPass = false;//
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        mGoogleKeytxtInfo = new TextView(this);
        mGoogleKeytxtInfo.setGravity(Gravity.CENTER);
        mGoogleKeytxtInfo.setTextSize(25);
        setContentView(mGoogleKeytxtInfo);
//        setTitle(R.string.google_key);//
        mPassButton.setVisibility(View.GONE);//
//        init();

        try {
            String value = android.os.SystemProperties.get("ro.boot.deviceid", "");
            if (TextUtils.isEmpty(value) || "none".equals(value)) {
                mGoogleKeytxtInfo.setText(R.string.google_key_fail);
                mGoogleKeytxtInfo.setTextColor(Color.RED);
            } else if ("success".equals(value)) {
                mGoogleKeytxtInfo.setText(R.string.google_key_ok);
                mGoogleKeytxtInfo.setTextColor(Color.GREEN);
                mPassButton.setVisibility(View.VISIBLE);
            } else {
                mGoogleKeytxtInfo.setText(R.string.google_key_ok);
                mGoogleKeytxtInfo.setTextColor(Color.GREEN);
                mPassButton.setVisibility(View.VISIBLE);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

	//	mPassButton.setVisibility(View.INVISIBLE);
    }

    private void init() {

    	 mGoogleKeyHandler.postDelayed(mGetKeyBoxValueRunnable, 1000L);//
    }



    private static final int GET_KEY_BOX_SUCCESS = 1;
    private static final int GET_KEY_BOX_FAILED = 0;
    private Handler mGoogleKeyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_KEY_BOX_SUCCESS:
                    mGoogleKeytxtInfo.setText(R.string.google_key_ok);
					mGoogleKeytxtInfo.setTextColor(Color.GREEN);
					isShowPass = true;//
					mPassButton.setVisibility(View.VISIBLE);//
                    break;
                case GET_KEY_BOX_FAILED:
                    mGoogleKeytxtInfo.setText(R.string.google_key_fail);
                    mGoogleKeytxtInfo.setTextColor(Color.RED);
                    break;
                default:
                    Log.d(TAG,"**Error**");
            }
			 mFailButton.setVisibility(View.VISIBLE);//
        }
    };

    private Runnable mGetKeyBoxValueRunnable = new Runnable() {
        public void run() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int keyBoxValue = PhaseCheckParse.getInstance().getKeyboxFlag();
                    Log.d("hzx", "**mKeyBoxValue:**" + keyBoxValue);
                    if (keyBoxValue == 1) {
                        //storeRusult(true);
                        mGoogleKeyHandler.sendEmptyMessage(GET_KEY_BOX_SUCCESS);
                    } else {
                        //storeRusult(false);
                        mGoogleKeyHandler.sendEmptyMessage(GET_KEY_BOX_FAILED);
                    }
                }
            }).start();
        }
    };

	
	@Override
    public void onResume() {
        super.onResume();
//        if (!isShowPass) {
//            mPassButton.setVisibility(View.INVISIBLE);
//			mFailButton.setVisibility(View.INVISIBLE);//
//        }
    }

    @Override
    protected void onDestroy() {
//        if (mGoogleKeyHandler != null) {
//            mGoogleKeyHandler.removeCallbacks(mGetKeyBoxValueRunnable);
//        }
        super.onDestroy();
    }


}