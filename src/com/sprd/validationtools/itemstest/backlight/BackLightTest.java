package com.sprd.validationtools.itemstest.backlight;

import java.util.Timer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.ValidationToolsUtils;

public class BackLightTest extends BaseActivity implements OnClickListener {
    private static final String TAG = "BackLightTest";
    PowerManager mPowerManager = null;
    TextView mContent;
    private static final int[] COLOR_ARRAY = new int[] { Color.WHITE,
            Color.BLACK };
    private boolean isShowNavigationBar = false;
    private int mIndex = 0, mCount = 5;//
    Timer mTimer;
    private static final int TIMES = 5;
    private Handler mUiHandler = new Handler();;
    private Runnable mRunnable;

    protected RelativeLayout mRelativeLayout;
    protected Button passButton;
    protected Button failButton;
    private Button mAddButton;
    private Button mReduceButton;
	private boolean mIsAdd = false; 
	private boolean mIsReduce = false;

    /*SPRD bug 839657:Change screen light*/
    private static final int MAX_BRIGHTNESS = 255;
    private static final boolean TEST_SCREEN_LIGHT = true;
    private void setScreenLight(Activity context, int brightness) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        context.getWindow().setAttributes(lp);
    }
    private void startScreenLight(){
        try {
            if (isShowNavigationBar) {
                mRelativeLayout.setBackgroundColor(Color.WHITE);
            } else {
                mContent.setBackgroundColor(Color.WHITE);
            }
            setScreenLight(BackLightTest.this, MAX_BRIGHTNESS >> mCount);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isShowNavigationBar = ValidationToolsUtils.hasNavigationBar(this);
        if (isShowNavigationBar) {
            setContentView(R.layout.background_layout);
            mRelativeLayout = (RelativeLayout) findViewById(R.id.background_relativelayout);
            passButton = (Button) findViewById(R.id.pass_btn);
            failButton = (Button) findViewById(R.id.fail_btn);
            mAddButton = (Button) findViewById(R.id.add_light_btn);
            mReduceButton = (Button) findViewById(R.id.reduce_light_btn);
            passButton.setOnClickListener(this);
            failButton.setOnClickListener(this);
            mAddButton.setOnClickListener(this);
            mReduceButton.setOnClickListener(this);
        } else {
            mContent = new TextView(this);
            setContentView(mContent);
        }
        setTitle(R.string.backlight_test);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mRunnable = new Runnable() {
            public void run() {
                if(TEST_SCREEN_LIGHT){
                    startScreenLight();
               //     mCount++;
                }else{
                    if (isShowNavigationBar) {
                        mRelativeLayout.setBackgroundColor(COLOR_ARRAY[mIndex]);
                    } else {
                        mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);
                    }
                    mIndex = 1 - mIndex;
                //    mCount++;
                }
                if (isShowNavigationBar) {
                    mPassButton.setVisibility(View.GONE);
					mFailButton.setVisibility(View.GONE);
                    failButton.setVisibility(View.VISIBLE);//
                }
                setBackground();
            }
        };
        setBackground();
    }

    @Override
    public void onClick(View v) {
        if (isShowNavigationBar) {
            if (v == passButton) {
                storeRusult(true);
                finish();
            } else if (v == failButton) {
                storeRusult(false);
                finish();
            }
        } else {
            if (v == mPassButton) {
                storeRusult(true);
                finish();
            } else if (v == mFailButton) {
                storeRusult(false);
                finish();
            }
        }
		if (v == mAddButton) {
			 
			  if(mCount > 1){
			   mCount -= 2;
			   mIsAdd = true;
			   setBackground();

			 }
            } else if (v == mReduceButton) {
              if(mCount < 5){
               mCount += 2;
			   mIsReduce = true;
			   setBackground();
			   
			   }
			   
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShowNavigationBar) {
            mPassButton.setVisibility(View.GONE);
            mFailButton.setVisibility(View.GONE);
			
            failButton.setVisibility(View.VISIBLE);//
            hideNavigationBar();
        }
    }

    private void setBackground() {
        if (mCount > TIMES) {
            if(TEST_SCREEN_LIGHT){
                setScreenLight(BackLightTest.this, MAX_BRIGHTNESS / 2);
            }
            if (isShowNavigationBar) {
             //   passButton.setVisibility(View.VISIBLE);
             //   failButton.setVisibility(View.VISIBLE);
                if (Const.isAutoTestMode()) {
                    storeRusult(true);
                    finish();
                }
            } else {
                if (Const.isAutoTestMode()) {
                    storeRusult(true);
                    finish();
                }
            }
            return;
        }
		startScreenLight();
		
		if (mIsAdd && mIsReduce) {
                passButton.setVisibility(View.VISIBLE);
            }		
 //       mUiHandler.postDelayed(mRunnable, 1000);
    }

    @Override
    public void onDestroy() {
        mUiHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
