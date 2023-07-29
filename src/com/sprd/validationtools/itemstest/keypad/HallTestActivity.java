package com.sprd.validationtools.itemstest.keypad;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;

import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.WindowManager;
import android.widget.TextView;
import android.view.Gravity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Intent;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.ValidationToolsUtils;

public class HallTestActivity extends BaseActivity {
    private static final String TAG = "HallTestActivity";
    TextView mContent;
    private String mPowerAction = "sprd.validationtools.action"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // mContext = getApplicationContext();
		 getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContent = new TextView(this);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(25);
        setContentView(mContent);
        setTitle(R.string.hall_test);//


            /* SPRD bug 760913:Test can pass/fail must click button */
           // if (Const.isBoardISharkL210c10()) {
                mPassButton.setVisibility(View.GONE);
           // }
            /* @} */
            
		mContent.setText(R.string.hall_test);
        registerRec();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

	private final BroadcastReceiver mPowerKeyReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {

			final String action = intent.getAction();

			if (Intent.ACTION_SCREEN_OFF.equals(action)||Intent.ACTION_SCREEN_ON.equals(action) || action.equals(mPowerAction)) {
				
			   mContent.setText(R.string.hall_test_pass);
               mPassButton.setVisibility(View.VISIBLE);
			}
		}
	};
	private void registerRec(){
	IntentFilter filter = new IntentFilter();
	filter.addAction(Intent.ACTION_SCREEN_OFF);
	filter.addAction(Intent.ACTION_SCREEN_ON);
	filter.addAction(mPowerAction);
	registerReceiver(mPowerKeyReceiver, filter);
	}
	
	private void unregisterRec(){
		if(mPowerKeyReceiver != null){
			try {
				unregisterReceiver(mPowerKeyReceiver);
			} catch (Exception e) {
				Log.e(TAG, "unregisterReceiver mBatInfoReceiver failure :" + e.getCause());
			}

		}
	}
	

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	// TODO Auto-generated method stub
Log.d(TAG, "tqy++++++dispatchKeyEvent--event.getKeyCode()="+event.getKeyCode());			
    	if (event.getAction() == KeyEvent.ACTION_DOWN) {
    		if (event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
    		   mContent.setText("hall test pass");
               mPassButton.setVisibility(View.VISIBLE);
    		return true;
    		}
    	}
	
    	return super.dispatchKeyEvent(event);
    }

     @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
Log.d(TAG, "tqy+++++onKeyDown keyCode=" + keyCode);

        if (KeyEvent.KEYCODE_POWER == keyCode) {
			 mContent.setText("hall test pass");
             mPassButton.setVisibility(View.VISIBLE);
        }
        return true;
    } 

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
Log.d(TAG, "tqy+++++onKeyUp keyCode=" + keyCode);		
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
		unregisterRec();
    }


}
