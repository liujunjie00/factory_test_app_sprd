/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.testinfo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.TelephonyManagerSprd;
import com.sprd.validationtools.ValidationToolsMainActivity;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.modules.UnitTestItemList;
import com.sprd.validationtools.sqlite.EngSqlite;
import com.sprd.validationtools.utils.BtTestUtil;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import com.sprd.validationtools.utils.WifiTestUtil;
import com.sprd.validationtools.utils.ZXingUtils;
import com.sprd.validationtools.R;

public class TestInfoMainActivity extends Activity {
    private final static String TAG = "TestInfoMainActivity";
    private TextView mSNtxt, mSNtxtInfo;
    private TextView mIMEItxt, mIMEItxtInfo;
    private TextView mBLTtxt, mBLTtxtInfo;
    private TextView mWIFItxt, mWIFItxtInfo;
    private TextView mCHECKtxt, mCHECKtxtInfo;
    private TextView mTesttxt, mTesttxtInfo;
    private TextView mUid;
    private TextView mMEIDtxt, mMEIDtextInfo;
    private static final boolean SUPPORT_QRCODE = true;
    private ImageView mQRCodeImageView = null;

    private TextView mGoogleKeytxt, mGoogleKeytxtInfo;  //hzx add for read keybox status
    private final int GET_SN = 0;
    private final int GET_IMEI = 1;
    private final int GET_WIFI = 3;
    private final int GET_PCHECK = 4;
    private final int GET_TESTR = 5;
    private final int GET_MEID = 6;

    private boolean mIsTested = false;

    Handler mTestInfoHandler = null;
    Handler mUiHandler = new UiHandler();

    class UiHandler extends Handler {

        public void handleMessage(Message msg) {
            switch (msg.what) {
            case GET_SN:
                Log.d(TAG, "UiHandler" + (String) msg.obj);
                mSNtxtInfo.setText((String) msg.obj);
                break;

            case GET_IMEI:
                Log.d(TAG, "UiHandler" + (String) msg.obj);
                mIMEItxtInfo.setText((String) msg.obj);
                break;
            case GET_MEID:
                Log.d(TAG, "UiHandler" + (String) msg.obj);
                mMEIDtextInfo.setText((String) msg.obj);
                break;

            case GET_PCHECK:
                Log.d(TAG, "UiHandler" + (String) msg.obj);
                mCHECKtxtInfo.setText((String) msg.obj);
                break;

            case GET_TESTR:
                Log.d(TAG, "UiHandler" + (String) msg.obj);
                mTesttxtInfo.setText((String) msg.obj);
                break;
            }
        }
    }

    class TestInfoHandler extends Handler {
        public TestInfoHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String result = null;
            switch (msg.what) {
            case GET_SN: {
                PhaseCheckParse parse = PhaseCheckParse.getInstance();
                /*
                 * SPRD:435125 The serial number shows invalid in
                 * ValidationTools @{
                 */
                mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0,
                        parse.getSn()));
                /* @} */
            }
                break;

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

            case GET_PCHECK: {
                PhaseCheckParse parse = PhaseCheckParse.getInstance();
                mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0,
                        parse.getPhaseCheck()));
            }
                break;

            case GET_TESTR: {
                EngSqlite engSqlite = EngSqlite
                        .getInstance(TestInfoMainActivity.this);
                int failCount = engSqlite.queryFailCount();
                StringBuilder buffer = new StringBuilder("");
                if (mIsTested) {
                    if (failCount > 0) {
                        result = "";
                        ArrayList<TestItem> supportList = UnitTestItemList
                                .getInstance(TestInfoMainActivity.this)
                                .getTestItemList();
                        int index = 0;
                        for (int i = 0; i < supportList.size(); i++) {
                            if (Const.FAIL == engSqlite
                                    .getTestListItemStatus(supportList.get(i)
                                            .getTestClassName())) {
                                index = i + 1;
                                buffer.append(index + "  "
                                        + supportList.get(i).getTestName()
                                        + "  Failed" + "\n");
                            }
                        }
                        result = buffer.toString();
                    } else {
                        result = "All Pass";
                    }
                } else {
                    result = "Not Test";
                }
                mUiHandler.sendMessage(mUiHandler.obtainMessage(msg.what, 0, 0,
                        result));
            }
                break;
            }
        }
    }

    private BtTestUtil mBtTestUtil = null;
    private WifiTestUtil mWifiTestUtil = null;
    private WifiManager mWifiManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_info);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        mTestInfoHandler = new TestInfoHandler(ht.getLooper());
        init();
        Log.d(TAG, "onCreate SUPPORT_QRCODE=" + SUPPORT_QRCODE);
        if (SUPPORT_QRCODE) {
            AsyncTask<Void, Void, Bitmap> mQRCodeTask = new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... params) {
                    // TODO Auto-generated method stub
                    TestInfoQRCode mTestInfoQRCode = new TestInfoQRCode();
                    String qrtext = mTestInfoQRCode
                            .getTestInfoQRCode(TestInfoMainActivity.this);
                    int width = TestInfoMainActivity.this.getResources()
                            .getDimensionPixelSize(R.dimen.qrcode_bitmap_width);
                    int height = TestInfoMainActivity.this
                            .getResources()
                            .getDimensionPixelSize(R.dimen.qrcode_bitmap_height);
                    Log.d(TAG, "onCreate width=" + width + ",height=" + height);
                    Bitmap bm = ZXingUtils.createQRImage(qrtext, width, height);
                    return bm;
                }

                @Override
                protected void onPostExecute(Bitmap result) {
                    // TODO Auto-generated method stub
                    super.onPostExecute(result);
                    if (mQRCodeImageView != null) {
                        mQRCodeImageView.setImageBitmap(result);
                    }
                }
            };
            mQRCodeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        mWifiManager = (WifiManager) getSystemService(this.WIFI_SERVICE);
        mBtTestUtil = new BtTestUtil() {

            public void btStateChange(int newState) {
                switch (newState) {
                case BluetoothAdapter.STATE_ON:
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                            .getDefaultAdapter();
                    String localAddress = bluetoothAdapter.getAddress();
                    mBLTtxtInfo.setText(localAddress);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    break;
                case BluetoothAdapter.STATE_OFF:
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    break;
                default:
                    break;
                }
            }

            public void btDeviceListAdd(BluetoothDevice device) {
            }

            public void btDiscoveryFinished() {
            }
        };
        mWifiTestUtil = new WifiTestUtil(mWifiManager) {

            public void wifiStateChange(int newState) {
                Log.d(TAG, "wifiStateChange newState=" + newState);
                switch (newState) {
                case WifiManager.WIFI_STATE_ENABLED:
                    String macAddress = getMacAddress(TestInfoMainActivity.this);
                    if (TextUtils.isEmpty(macAddress)) {
                        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                        macAddress = wifiInfo == null ? "Wlan off!" : wifiInfo
                                .getMacAddress();
                    }
                    Log.d(TAG, "macAddress=" + macAddress);
                    if (mWIFItxtInfo != null) {
                        mWIFItxtInfo.setText(macAddress);
                    }
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    break;
                case WifiManager.WIFI_STATE_DISABLING:
                    break;
                case WifiManager.WIFI_STATE_ENABLING:
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                default:
                    // do nothing
                    break;

                }
            }

            public void wifiDeviceListChange(List<ScanResult> wifiDeviceList) {
            }
        };
        if (mBtTestUtil != null) {
            mBtTestUtil.startTest(this);
        }
        if (mWifiTestUtil != null) {
            mWifiTestUtil.startTest(this);
        }
    }

    private void init() {
        mSNtxt = (TextView) findViewById(R.id.testinfo_sn);
        mSNtxtInfo = (TextView) findViewById(R.id.test_info_sn_dec);
        mIMEItxt = (TextView) findViewById(R.id.testinfo_imei);
        mIMEItxtInfo = (TextView) findViewById(R.id.test_info_imei_dec);
        mBLTtxt = (TextView) findViewById(R.id.testinfo_blt);
        mBLTtxtInfo = (TextView) findViewById(R.id.test_info_blt_dec);
        mWIFItxt = (TextView) findViewById(R.id.testinfo_wifi);
        mWIFItxtInfo = (TextView) findViewById(R.id.test_info_wifi_dec);

		 //hzx add for read keybox status
        mGoogleKeytxt = (TextView) findViewById(R.id.testinfo_googlekey);
        mGoogleKeytxtInfo = (TextView) findViewById(R.id.test_info_googlekey_dec);
		 //hzx add for read keybox status end

        mCHECKtxt = (TextView) findViewById(R.id.testinfo_phase);
        mCHECKtxtInfo = (TextView) findViewById(R.id.test_info_phase_dec);
        mTesttxt = (TextView) findViewById(R.id.testinfo_test);
        mTesttxtInfo = (TextView) findViewById(R.id.test_info_test_dec);
        mUid = (TextView) findViewById(R.id.uid);
        mMEIDtxt = (TextView) findViewById(R.id.testinfo_meid);
        mMEIDtextInfo = (TextView) findViewById(R.id.test_info_meid_dec);
        if (TelephonyManagerSprd.IsSupportCDMA()) {
            mMEIDtxt.setVisibility(View.VISIBLE);
            mMEIDtextInfo.setVisibility(View.VISIBLE);
        }
        Log.d(TAG, "init");
        mQRCodeImageView = (ImageView) findViewById(R.id.testinfo_qrcode);
        Intent intent = getIntent();
        if (intent != null) {
            mIsTested = intent.getBooleanExtra(
                    ValidationToolsMainActivity.IS_SYSTEM_TESTED, false);
        }
        if (!ValidationToolsUtils.ENABLE_UID) {
            mUid.setVisibility(View.GONE);
            TextView mUidTitle = (TextView) findViewById(R.id.uid_title);
            mUidTitle.setVisibility(View.GONE);
        }
        setTxtInfo();
    }

    private void setTxtInfo() {
        mTestInfoHandler.sendEmptyMessage(GET_SN);
        mTestInfoHandler.sendEmptyMessage(GET_IMEI);
        if (TelephonyManagerSprd.IsSupportCDMA()) {
            mTestInfoHandler.sendEmptyMessage(GET_MEID);
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String localAddress = bluetoothAdapter.getAddress();
        mBLTtxtInfo.setText(localAddress);

        AsyncTask<Void, Void, String> mWifiTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                WifiManager wifiManager = (WifiManager) getSystemService(TestInfoMainActivity.this.WIFI_SERVICE);
                WifiTestUtil wifiTestUtil = new WifiTestUtil(wifiManager);
                String macAddress = wifiTestUtil
                        .getMacAddress(TestInfoMainActivity.this);
                if (TextUtils.isEmpty(macAddress)) {
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    macAddress = wifiInfo == null ? "Wlan off!" : wifiInfo
                            .getMacAddress();
                    Log.d(TAG, "getMacAddress=" + macAddress);
                }
                return macAddress;
            }

            protected void onPostExecute(String macAddress) {
                Log.d(TAG, "macAddress=" + macAddress);
                if (mWIFItxtInfo != null) {
                    mWIFItxtInfo.setText(macAddress);
                }
            };
        };
//        mWifiTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);

        mTestInfoHandler.sendEmptyMessage(GET_PCHECK);
        mTestInfoHandler.sendEmptyMessage(GET_TESTR);
        if(ValidationToolsUtils.ENABLE_UID){
            String uid = ValidationToolsUtils.getUid();
            mUid.setText(uid);
        }
        mGoogleKeyHandler.postDelayed(mGetKeyBoxValueRunnable, 1000L);//hzx add for read keybox status
    }
	//hzx add for read keybox status
    private static final int GET_KEY_BOX_SUCCESS = 1;
    private static final int GET_KEY_BOX_FAILED = 0;
    private Handler mGoogleKeyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_KEY_BOX_SUCCESS:
                    mGoogleKeytxtInfo.setText(R.string.google_key_ok);
                    break;
                case GET_KEY_BOX_FAILED:
                    mGoogleKeytxtInfo.setText(R.string.google_key_fail);
                    
                    break;
                default:
                    Log.d(TAG,"**Error**");
            }

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
 	//hzx add for read keybox status end
    @Override
    protected void onDestroy() {
        if (mTestInfoHandler != null) {
            Log.d(TAG, "HandlerThread has quit");
            mTestInfoHandler.getLooper().quit();
        }
        if (mBtTestUtil != null) {
            mBtTestUtil.stopTest();
        }
        if (mWifiTestUtil != null) {
            mWifiTestUtil.stopTest();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
