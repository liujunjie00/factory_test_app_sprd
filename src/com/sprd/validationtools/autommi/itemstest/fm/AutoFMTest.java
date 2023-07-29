/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi.itemstest.fm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
//import android.media.UnisocAudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.sprd.validationtools.R;
import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;

public class AutoFMTest extends BaseActivity {
    private static final String TAG = "AutoFMTest";
    private volatile AutoFMTest instance;
    private static String mType;
    private FmManagerSelect mFmManager = null;
    public Context mContext;
    private boolean isOpen = false;
    private float mFreq = 0;
    private String TEST_FREQUENCY = "95.3";
    private String TEST_FREQUENCY_TAG = "Test Frequency=";
    private boolean mIsHeadsetIn;
    private static final int FOR_FM = 8;
    private static final int FORCE_NONE = 0;
//    private static UnisocAudioManager mAudioManagerEx;
    private static final int START_FM = 0;
    private static final int CHECK_RESULT = 2;
    private HandlerThread mFMHandlerThread = null;
    private Handler mFMHandler;
    private TextView mFMFreq;
    private TextView mFMRSSI;

    class FMHandler extends Handler {
        public FMHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_FM:
                    startFM(TEST_FREQUENCY);
                    mFMHandler.sendEmptyMessageDelayed(CHECK_RESULT, 3000);
                    break;
                case CHECK_RESULT:
                    stopFM();
                    if (!isHeadsetExists()) {
                        storeRusult(true);
                    } else {
                        storeRusult(showRssi());
                    }
                    finish();
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fm_auto_test);
        mContext = this.getApplication();
        mFmManager = new FmManagerSelect(mContext);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        mContext.registerReceiver(mEarphonePluginReceiver, filter);
//        mAudioManagerEx = UnisocAudioManager.getInstance(mContext);
        mFMFreq = (TextView)findViewById(R.id.wave_band_textView);
        mFMRSSI = (TextView)findViewById(R.id.band_rssi);
        mFMHandlerThread = new HandlerThread("FMHandlerThread");
        mFMHandlerThread.start();
        mFMHandler = new FMHandler(mFMHandlerThread.getLooper());
        mFMHandler.sendEmptyMessageDelayed(START_FM, 1000);
    }

    private BroadcastReceiver mEarphonePluginReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent earphoneIntent) {
            if (earphoneIntent != null && earphoneIntent.getAction() != null) {
                if (earphoneIntent.getAction().equalsIgnoreCase(
                        Intent.ACTION_HEADSET_PLUG)) {
                    int st = earphoneIntent.getIntExtra("state", 0);
                    int deviceId = earphoneIntent.getIntExtra("microphone", 0);
                    if (st > 0) {
                        mIsHeadsetIn = true;
                    } else {
                        mIsHeadsetIn = false;
                    }
                }
            }
        }
    };

    public void startFM(String param) {
        if (TextUtils.isEmpty(param)) {
            Log.e(TAG, " start() failed -> param is empty!");
            return;
        }
        mFMFreq.setText(TEST_FREQUENCY_TAG + param);
        mFreq = -1;
        try {
            mFreq = Float.parseFloat(param);
            if(!isOpen) {
                startPowerUp();
            } else {
                mFmManager.tuneRadio(mFreq);
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "start param is error : " + param);
            return;
        }
        Log.d(TAG, "FmManager power up and setFreq() : " + mFreq);
    }

    public void stopFM() {
        if (isOpen) {
            powerOffFM();
        }
        mFmManager.setAudioPathEnable(false);
        try {
            mContext.unregisterReceiver(mEarphonePluginReceiver);
            Log.d(TAG, "unregister receiver...");
        } catch (Exception e) {
            Log.d(TAG, "unregisterreceiver Exception:", e);
        }
        if (instance != null) {
            instance = null;
        }
    }

    private void startPowerUp() {
        Log.d(TAG, "startPowerUp");
        boolean value = mFmManager.openDev();
        if (!value) {
            isOpen = false;
            Log.e(TAG, "openDev fail ");
            return;
        }
        isOpen = true;
        mFmManager.setMute(true);
        mFmManager.tuneRadio(mFreq);
//        startRender();
    }

    private boolean isHeadsetExists() {
        int newState = FileUtils.getIntFromFile(Const.HEADSET_STATE_PATH);
        return newState > 0;
    }

//    private synchronized void startRender() {
//        Log.d(TAG, "startRender ");
//        if (!isHeadsetExists()) {
//            mAudioManagerEx.setFmSpeakerOn(true);
//        } else {
//            mAudioManagerEx.setFmSpeakerOn(false);
//        }
//        mFmManager.setAudioPathEnable(true);
//        mFmManager.setMute(false);
//        startScan();
//    }

    public boolean showRssi() {
        Log.d(TAG,"show fm device rssi");
        final int rssi = UniFmRadioManager.getInstance().getRssi();
        mFMRSSI.setText(String.valueOf(rssi));
        Log.d(TAG,"rssi = " + rssi);
        return checkRssi(rssi);
    }

    public boolean checkRssi(int rssi) {
        int result = -rssi;
        if (result > -100 && result < -10) {
            return true;
        }else {
            return false;
        }
    }

    public void startScan() {

    }

    public int getFreq() {
        int iFreq = mFmManager.getFreq();
        return iFreq > 0 ? iFreq : -1;
    }

    public void powerOffFM() {
        Log.d(TAG,"power off fm device");
        mFmManager.setMute(true);
        mFmManager.closeDev();
        isOpen = false;
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        String board = SystemPropertiesProxy.get("ro.product.board");
        Log.d(TAG, "FMTest board name=" + board);
        if (board != null && board.contains("512")) {
            return false;
        }
        return true;
    }

}