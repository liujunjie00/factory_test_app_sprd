/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.telephony;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import static com.sprd.validationtools.utils.IATUtils.sendAtCmd;

public class PhoneCallTestActivity extends BaseActivity {
    private static final String TAG = "PhoneCallTestActivity";
    private TextView mContent;
    private static final int CHECK_CALL_STATUS = 0;
    private MyPhoneStateListener mPhoneStateListener;
    private boolean mCallResult = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContent = new TextView(this);
        mContent.setGravity(Gravity.CENTER);
        mContent.setText(getString(R.string.phone_test_title));
        mContent.setTextSize(25);
        setContentView(mContent);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        //showResultDialog(getString(R.string.phone_call_info));
        Intent intent = new Intent("android.intent.action.CALL_PRIVILEGED", Uri.parse("tel:112"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("factory_mode", true);
        startActivity(intent);
        if (Const.isAutoTestMode()) {
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
            mPhoneStateListener = new MyPhoneStateListener();
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            mCallHandler.sendEmptyMessageDelayed(CHECK_CALL_STATUS, 15000);
        }
    }

    private Handler mCallHandler =  new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CHECK_CALL_STATUS:
                    endCall();
                    break;
                default:
                    break;
            }
        }
    };

    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String number) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "idle");
                    mCallResult = true;
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "offhook");
                    break;
            }
        }
    }

    public void endCall() {
        Log.d(TAG, "endCall");
        try {
            sendAtCmd("ATH");
            storeRusult(mCallResult);
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mCallHandler != null) {
            mCallHandler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

}
