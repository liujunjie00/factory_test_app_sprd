/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.nfc;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.sprd.validationtools.R;
import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;

public class NFCTestActivity extends BaseActivity {
    private static final String TAG = "NFCTestActivity";
    TextView mContent;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mPendingIntent;
    public static final int ACTION_TIME_OUT = 0;
    private static final int TIME_OUT = 20000;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case ACTION_TIME_OUT:
                Log.d(TAG, "time out");
                storeRusult(false);
                finish();
                break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.nfc_test);
        NfcManager nfcManager = (NfcManager) getSystemService("nfc");
        mNfcAdapter = nfcManager.getDefaultAdapter();
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d(TAG, "action: "+intent.getAction());
        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if(detectedTag != null) {
            Log.d(TAG, "tag:" + detectedTag.toString());
            mContent.setText(R.string.nfc_tag);
            Toast.makeText(this, R.string.text_pass, Toast.LENGTH_SHORT).show();
            storeRusult(true);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.sendEmptyMessageDelayed(ACTION_TIME_OUT, TIME_OUT);
        mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        mContent = (TextView)findViewById(R.id.nfc_scan_bt);
        mContent.setText(R.string.nfc_scan);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(ACTION_TIME_OUT);
        mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        boolean result = false;
        NfcManager manager = (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null) {
            result = true;
        }
        return result;
    }

}
