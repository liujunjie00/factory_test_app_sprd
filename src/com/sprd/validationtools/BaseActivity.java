package com.sprd.validationtools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

import com.sprd.validationtools.autommi.modules.AutoTestItemList;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.sqlite.EngSqlite;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.sqlite.Sqlite;
import com.sprd.validationtools.sqlite.MMI2EngSqlite;//

import java.util.ArrayList;

public class BaseActivity extends Activity implements OnClickListener {
    private static final String TAG = "BaseActivity";
    private String mTestname = null;
    private String mTestClassName = null;
    private EngSqlite mEngSqlite;
    private MMI2EngSqlite mMMI2EngSqlite;
    public static boolean shouldCanceled = true;

    protected Button mPassButton;
    protected Button mFailButton;
    private static final int TEXT_SIZE = 30;
    protected boolean canPass = true;
    protected WindowManager mWindowManager;
    protected long time;

    private PhaseCheckParse mPhaseCheckParse = null;
    public static final String STATION_MMIT_VALUE = "MMI1";
    public static final String STATION_MMI2_VALUE = "MMI2";//
    public static final String STATION_AGING_VALUE = "AGING";
    public static final boolean SUPPORT_WRITE_STATION = true;
    public static final boolean SUPPORT_WRITE_ITEM_STATION = false;
    public static final int MISCDATA_USERSETION_OFFSET_BASE = 768 * 1024;
    public static final int MISCDATA_USERSETION_OFFSET_AGING = MISCDATA_USERSETION_OFFSET_BASE + 44;
    public static final int MISCDATA_USERSETION_OFFSET_RING_TUNE = MISCDATA_USERSETION_OFFSET_BASE + 74;
	private boolean isMMI2 = false;

    private ArrayList<TestItem> mAutoTestArray = null;
    private int mCurrentID = 0;
    private HandlerThread mAutoTestWriteResultHandlerThread;
    public Handler mAutoTestWriteResultHandler;
    private static final int WRITE_ITEM_RESULT = 0;
    private static final int WRITE_RESULT_FLAG = 1;
    class AutoTestWriteResultHandler extends Handler {
        public AutoTestWriteResultHandler(Looper Looper) {
            super(Looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case WRITE_ITEM_RESULT:
                    FileUtils.writeAutoTestResult(mCurrentID, mTestResults[mCurrentID]);
                    startNextTest();
                    break;
                case WRITE_RESULT_FLAG:
                    FileUtils.writeAutoTestResult(Const.AUTO_TEST_FINAL_RESULT_INDEX, mTestResults[Const.AUTO_TEST_FINAL_RESULT_INDEX]);
                    delayMs(2000);
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    pm.reboot("AutoMMITest");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTestname = this.getIntent().getStringExtra(Const.INTENT_PARA_TEST_NAME);
        mTestClassName = this.getIntent().getStringExtra(Const.INTENT_PARA_TEST_CLASSNAME);
        time = System.currentTimeMillis();

     //   mEngSqlite = EngSqlite.getInstance(this);
	 	////
		 isMMI2 = getIntent().getBooleanExtra("isMMI2", false);
//Log.d(TAG, "tqy+++++isMMI2="+isMMI2);	
//		if(isMMI2){
		 mMMI2EngSqlite = MMI2EngSqlite.getInstance(getApplicationContext());
//		}else{
		 mEngSqlite = EngSqlite.getInstance(getApplicationContext());
//		}////
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWindowManager = getWindowManager();
        createButton(true);
        createButton(false);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        Log.d(TAG, "onCreate SUPPORT_WRITE_STATION=" + SUPPORT_WRITE_STATION);
        if (SUPPORT_WRITE_STATION) {
            mPhaseCheckParse = PhaseCheckParse.getInstance();
        }

    }

    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            decorView.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // for new api versions. View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public int getHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }

    public int getRealHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else {
            display.getMetrics(dm);
        }
        int realHeight = dm.heightPixels;
        return realHeight;
    }

    @Override
    protected void onDestroy() {
        removeButton();
        if (mTestname != null) {
            Log.d("APK_MMI",
                    "*********** " + mTestname + " Time: "
                            + (System.currentTimeMillis() - time) / 1000
                            + "s ***********");
        }
        if (mAutoTestWriteResultHandler != null) {
            mAutoTestWriteResultHandler.removeCallbacksAndMessages(null);
            mAutoTestWriteResultHandlerThread.getLooper().quit();
        }
        super.onDestroy();
    }

    public void createButton(boolean isPassButton) {
        int buttonSize = getResources().getDimensionPixelSize(
                R.dimen.pass_fail_button_size);
        if (isPassButton) {
            mPassButton = new Button(this);
            mPassButton.setText(R.string.text_pass);
            mPassButton.setTextColor(Color.WHITE);
            mPassButton.setTextSize(TEXT_SIZE);
            mPassButton.setBackgroundColor(Color.GREEN);
            mPassButton.setOnClickListener(this);
        } else {
            mFailButton = new Button(this);
            mFailButton.setText(R.string.text_fail);
            mFailButton.setTextColor(Color.WHITE);
            mFailButton.setTextSize(TEXT_SIZE);
            mFailButton.setBackgroundColor(Color.RED);
            mFailButton.setOnClickListener(this);
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                // WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = isPassButton ? Gravity.LEFT | Gravity.BOTTOM
                : Gravity.RIGHT | Gravity.BOTTOM;
        lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = buttonSize;
        lp.height = buttonSize;
        mWindowManager.addView(isPassButton ? mPassButton : mFailButton, lp);
    }

    public void storeRusult(boolean isSuccess) {
        Log.d(TAG, "storeResult classname:" + getClass().getName());
        Log.d(TAG, "storeResult mTestClassName:" + mTestClassName);
        if (TextUtils.isEmpty(mTestClassName)) {
            mTestClassName = getClass().getName();
        }
        if (Const.isAutoTestMode()) {
            mAutoTestArray = AutoTestItemList.getInstance(BaseActivity.this).getAutoTestItemList();
            mCurrentID = getCurrentTestIndex(mTestClassName);
            mAutoTestWriteResultHandlerThread = new HandlerThread("AutoTestWriteRsult");
            mAutoTestWriteResultHandlerThread.start();
            mAutoTestWriteResultHandler = new AutoTestWriteResultHandler(mAutoTestWriteResultHandlerThread.getLooper());
            storeAutoTestResult(isSuccess);
        } else {
			if(isMMI2){
			 mMMI2EngSqlite.updateDB(mTestClassName, isSuccess ? Const.SUCCESS : Const.FAIL);
			}else{
			 mEngSqlite.updateDB(mTestClassName, isSuccess ? Const.SUCCESS : Const.FAIL);
			}////
            
            Log.d(TAG, "storeRusult SUPPORT_WRITE_STATION=" + SUPPORT_WRITE_STATION);
            if (SUPPORT_WRITE_STATION) {
                storePhaseCheck();
            }
        }
    }

    private void storePhaseCheck() {
        String station ;
		if(isMMI2){
			station = STATION_MMI2_VALUE;
		}else{
			station = STATION_MMIT_VALUE;
		}
        if (mPhaseCheckParse == null) {
            return;
        }
		if(isMMI2){
			Log.d(TAG,
                "tqy++storePhaseCheck-mmi2: fail = " + mMMI2EngSqlite.queryFailCount()
                        + ", NotTest = " + mMMI2EngSqlite.queryNotTestCount()
						+" querySystemFailCount="+mMMI2EngSqlite.querySystemFailCount());
			mPhaseCheckParse.writeStationTested(station);
			if (mMMI2EngSqlite.queryFailCount() == 0
					&& mMMI2EngSqlite.queryNotTestCount() == 0) {
				mPhaseCheckParse.writeStationPass(station);
			} else {
				mPhaseCheckParse.writeStationFail(station);
			}
		}else{
		    Log.d(TAG,
                "tqy++storePhaseCheck: fail = " + mEngSqlite.queryFailCount()
                        + ", NotTest = " + mEngSqlite.queryNotTestCount()
						+" querySystemFailCount="+mEngSqlite.querySystemFailCount()
						);
			mPhaseCheckParse.writeStationTested(station);
			if (mEngSqlite.queryFailCount() == 0
					&& mEngSqlite.queryNotTestCount() == 0) {
				mPhaseCheckParse.writeStationPass(station);
			} else {
				mPhaseCheckParse.writeStationFail(station);
			}
		}////
		

    }

    private boolean mTestClikPass = false;

    @Override
    public void finish() {
        removeButton();
        this.setResult(Const.TEST_ITEM_DONE, getIntent());
        super.finish();
    }

    protected void removeButton() {
        if (mPassButton != null) {
            mWindowManager.removeView(mPassButton);
            mPassButton = null;
        }
        if (mFailButton != null) {
            mWindowManager.removeView(mFailButton);
            mFailButton = null;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = BaseActivity.this.getIntent();
        BaseActivity.this.startActivityForResult(intent, 0);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == mPassButton) {
            if (canPass) {
                Log.d("onclick", "pass.." + this);
                mTestClikPass = true;
                storeRusult(true);
                finish();
            } else {
                Toast.makeText(this, R.string.can_not_pass, Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (v == mFailButton) {
            Log.d("onclick", "false.." + this);
            mTestClikPass = false;
            storeRusult(false);
            finish();
        }
    }

    private static int[] mTestResults = new int[Const.AUTO_TEST_NUMBER];
    public void storeAutoTestResult(boolean isSuccess) {
        /*autommi test result:
        0=00 00 not support,not pass;
        257=01 01 support,pass;
        258=01 02 support fail*/
        if (!isSuccess) {
            mTestResults[mCurrentID] = Const.AUTO_TEST_ITEM_FAIL;
          } else {
            mTestResults[mCurrentID] = Const.AUTO_TEST_ITEM_PASS;
        }
        mAutoTestWriteResultHandler.sendEmptyMessage(WRITE_ITEM_RESULT);
        Log.d(TAG, "results[" + mCurrentID + "]=" + mTestResults[mCurrentID]);
    }

    private int getCurrentTestIndex(String currentTestName) {
        int autoTestCurrentID = 0;
        for (int i = 0; i < mAutoTestArray.size(); i++) {
            if (mAutoTestArray.get(i).getTestClassName().equals(currentTestName)) {
                autoTestCurrentID = i;
            }
        }
        return autoTestCurrentID;
    }

    public void delayMs(int millisecond) {
        try {
          Thread.sleep(millisecond);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

    private void startNextTest() {
        mCurrentID++;
        Context context = getApplicationContext();
        Log.d(TAG, "mAutoTestCurrentNumber" + mCurrentID);
        if (mAutoTestArray != null && mCurrentID < mAutoTestArray.size()) {
            boolean isSupport = Const.getSupportInfo(context, mAutoTestArray.get(mCurrentID).getTestClassName());
            if (isSupport) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClassName(this, mAutoTestArray.get(mCurrentID).getTestClassName());
                intent.putExtra(Const.INTENT_PARA_TEST_NAME, mAutoTestArray.get(mCurrentID).getTestName());
                intent.putExtra(Const.INTENT_PARA_TEST_INDEX, mCurrentID);
                startActivity(intent);
            } else {
                mTestClassName = mAutoTestArray.get(mCurrentID).getTestClassName();
                mTestResults[mCurrentID] = Const.AUTO_TEST_NOT_SUPPORT;
                mAutoTestWriteResultHandler.sendEmptyMessage(WRITE_ITEM_RESULT);
            }
        } else if (mAutoTestArray != null && mCurrentID >= mAutoTestArray.size()) {
            if (isAutoTestAllPass()) {
                mTestResults[Const.AUTO_TEST_FINAL_RESULT_INDEX] = Const.AUTO_TEST_FLAG_PASS;
            } else {
                mTestResults[Const.AUTO_TEST_FINAL_RESULT_INDEX] = Const.AUTO_TEST_FLAG_FAIL;
            }
            mAutoTestWriteResultHandler.sendEmptyMessage(WRITE_RESULT_FLAG);
        }
    }

    private boolean isAutoTestAllPass() {
        for (int i = 0; i < mTestResults.length; i++) {
            if (mTestResults[i] == Const.AUTO_TEST_ITEM_FAIL) {
                return false;
            }
        }
        return true;
    }

}
