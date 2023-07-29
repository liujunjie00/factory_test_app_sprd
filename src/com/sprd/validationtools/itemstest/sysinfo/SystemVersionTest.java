
package com.sprd.validationtools.itemstest.sysinfo;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;
import android.util.Log;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.View;

public class SystemVersionTest extends BaseActivity
{
    private static final String TAG = "SystemVersionTest";
    private static final String PROD_VERSION_FILE = "/proc/version";
    private TextView mAndroidVersionTV;
    private TextView mLinuxVersionTV;
    private TextView mPlatformVersionTV;
    private TextView mPlatformSnTV;
    private TextView mIMEItxt;
    private TextView mMEIDtxt;
    Handler mUiHandler = new UiHandler();
	Handler mTestInfoHandler = null;
	
	private final int GET_IMEI = 1;
    private final int GET_MEID = 2;

    private String mAndroidVersion = null;
    private String mPropVersion = null;
    private String mBuildNumber = null;
    private String mDeviceSn = null;
    private String mPhaseCheck = null;
    private boolean mIsAutoMMI = Const.isAutoTestMode();
    private static final int CHECK_INFO = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_INFO:
                    checkSystemInfoResult();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version);
        setTitle(R.string.version_test);
        showSystemInfo();
        if (mIsAutoMMI) {
            mHandler.sendEmptyMessageDelayed(CHECK_INFO, 2000);
        }
		
		HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        mTestInfoHandler = new TestInfoHandler(ht.getLooper());
        mIMEItxt = (TextView) findViewById(R.id.version_imei);
        mMEIDtxt = (TextView) findViewById(R.id.version_meid);
        mIMEItxt.setVisibility(View.GONE);
		mMEIDtxt.setVisibility(View.GONE);
				
		 mTestInfoHandler.sendEmptyMessage(GET_IMEI);
     //    mTestInfoHandler.sendEmptyMessage(GET_MEID);
    }

    private String getPropVersion() {
        return FileUtils.readFile(PROD_VERSION_FILE);
    }

    private String getSn() {
        PhaseCheckParse parse = PhaseCheckParse.getInstance();
        return parse.getSn();
    }

    private String getPhaseCheck() {
        PhaseCheckParse parse = PhaseCheckParse.getInstance();
        return parse.getPhaseCheck();
    }

    private void showSystemInfo() {
        mAndroidVersionTV = (TextView) findViewById(R.id.android_version);
        mLinuxVersionTV = (TextView) findViewById(R.id.linux_version);
        mPlatformVersionTV = (TextView) findViewById(R.id.platform_version);
        mPlatformSnTV = (TextView) findViewById(R.id.platform_sn);
        mAndroidVersion = Build.VERSION.RELEASE_OR_PREVIEW_DISPLAY;
        mPropVersion = getPropVersion();
        mBuildNumber = SystemPropertiesProxy.get("ro.build.display.id", "unknown");
        mDeviceSn = getSn();
        mPhaseCheck = getPhaseCheck();
        mAndroidVersionTV.setText(getString(R.string.android_version) + "\n" + mAndroidVersion + "\n");
        mLinuxVersionTV.setText(getString(R.string.prop_version) + "\n" + mPropVersion + "\n");
        mPlatformVersionTV.setText(getString(R.string.build_number) + "\n" + mBuildNumber + "\n");
        mPlatformSnTV.setText(getString(R.string.device_sn) + "\n" + Build.getSerial() + "\n");
    }

    private void checkSystemInfoResult() {
        if (mPhaseCheck != null && mAndroidVersion != null && mPropVersion != null && mBuildNumber != null && mDeviceSn != null) {
            storeRusult(true);
        } else {
            storeRusult(false);
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
		if (mTestInfoHandler != null) {
            Log.d(TAG, "HandlerThread has quit");
            mTestInfoHandler.getLooper().quit();
        }
        super.onDestroy();
    }
	
	private class UiHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {

            case GET_IMEI:
                Log.d(TAG, "UiHandler" + (String) msg.obj);
//                mIMEItxt.setText("IMEI:"+ "\n" +(String) msg.obj + "\n");
                break;
            case GET_MEID:
                Log.d(TAG, "UiHandler" + (String) msg.obj);
//                mMEIDtxt.setText("MEID:"+ "\n" +(String) msg.obj + "\n");
                break;

            }
        }
    }

    private class TestInfoHandler extends Handler {
        public TestInfoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String result = null;
            switch (msg.what) {
            case GET_IMEI: {
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                int phoneCnt = tm.getPhoneCount();
                Log.d(TAG, "GET_IMEI===phoneCnt: " + phoneCnt);
                StringBuffer imeiBuffer = new StringBuffer("");

                for (int i = 0; i < phoneCnt; i++) {
                    imeiBuffer.append("imei");
                    imeiBuffer.append(String.valueOf(i + 1));
                    imeiBuffer.append(":");
                    if (getSystemService(TELEPHONY_SERVICE) != null) {
                        imeiBuffer.append(tm.getDeviceId(i));
                    }
                    if (i < phoneCnt - 1) {
                        imeiBuffer.append("\n");
                    }
                }
                mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0,
                        imeiBuffer.toString()));
            }
                break;

            case GET_MEID: {
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                if (tm != null) {
                    int phoneCnt = tm.getPhoneCount();
                    Log.d(TAG, "GET_MEID===phoneCnt: " + phoneCnt);
                    StringBuffer meidBuffer = new StringBuffer("");

                    for (int i = 0; i < phoneCnt; i++) {
                        meidBuffer.append("meid");
                        meidBuffer.append(String.valueOf(i + 1));
                        meidBuffer.append(":");
                        if (tm != null) {
                            meidBuffer.append(tm.getMeid(i));
                        }
                        if (i < phoneCnt - 1) {
                            meidBuffer.append("\n");
                        }
                    }
                    mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what,
                            0, 0, meidBuffer.toString()));
                }
            }
                break;

            }
        }
    }

}