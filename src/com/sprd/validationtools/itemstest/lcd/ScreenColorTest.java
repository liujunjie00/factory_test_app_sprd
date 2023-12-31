
package com.sprd.validationtools.itemstest.lcd;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.MotionEvent;//

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.ValidationToolsUtils;

public class ScreenColorTest extends BaseActivity implements OnClickListener {
    private String TAG = "ScreenColorTest";
    TextView mContent;

    protected RelativeLayout mRelativeLayout;
    protected Button passButton;
    protected Button failButton;

    int mIndex = 0, mCount = 0;
    private boolean isShowNavigationBar = false;
    private Handler mUiHandler = new Handler();
    private Runnable mRunnable;
	private boolean mIsLock = false;

    private static final int[] COLOR_ARRAY = new int[] {
            Color.WHITE, Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.BLACK, Color.GRAY
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isShowNavigationBar = ValidationToolsUtils.hasNavigationBar(this);
        Log.d(TAG, "isShowNavigationBar=:" + isShowNavigationBar);
        if (isShowNavigationBar) {
            setContentView(R.layout.lcd_layout);
            mRelativeLayout = (RelativeLayout) findViewById(R.id.lcd_relativelayout);
            passButton = (Button) findViewById(R.id.pass_btn);
            failButton = (Button) findViewById(R.id.fail_btn);
            passButton.setOnClickListener(this);
            failButton.setOnClickListener(this);
            passButton.setVisibility(View.GONE);
            failButton.setVisibility(View.GONE);
        } else {
            mContent = new TextView(this);
            mContent.setGravity(Gravity.CENTER);
            mContent.setTextSize(35);
            setContentView(mContent);
        }
        mRunnable = new Runnable() {
            public void run() {
                if (isShowNavigationBar) {
                    mRelativeLayout.setBackgroundColor(COLOR_ARRAY[mIndex]);
                } else {
                    mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);
                }
                mIndex++;
                mCount++;
				mIsLock = false;
        //        setBackground();
            }
        };
        mPassButton.setVisibility(View.GONE);
        mFailButton.setVisibility(View.GONE);
        setBackground();
    }
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {  
        case MotionEvent.ACTION_DOWN:  
		//if(!mIsLock){
			 setBackground();
		//}
        	 break;  
        case MotionEvent.ACTION_MOVE:
        	break;
		}
		
		//return super.onTouchEvent(event);
		return true;
	}

    @Override
    public void onResume() {
        super.onResume();
        if (isShowNavigationBar) {
            mPassButton.setVisibility(View.GONE);
            mFailButton.setVisibility(View.GONE);
            hideNavigationBar();
        }
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
    }

    private void setBackground() {
		if(mIsLock){
			return;
		}
		mIsLock = true;
        if (mIndex >= COLOR_ARRAY.length) {
            if (isShowNavigationBar) {
                passButton.setVisibility(View.VISIBLE);
                failButton.setVisibility(View.VISIBLE);
                hideNavigationBar();
            } else {
                mPassButton.setVisibility(View.VISIBLE);
                mFailButton.setVisibility(View.VISIBLE);
            }
            if (Const.isAutoTestMode()) {
                storeRusult(true);
                finish();
            }
            return;
        }

 /*       if (isShowNavigationBar) {
            mRelativeLayout.setBackgroundColor(COLOR_ARRAY[mIndex]);
        } else {
            mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);
        }
*/
        mUiHandler.postDelayed(mRunnable, 600);
    }

    @Override
    public void onDestroy() {
        mUiHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
