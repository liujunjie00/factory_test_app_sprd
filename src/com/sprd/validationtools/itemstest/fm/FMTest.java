/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.fm;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;
import com.sprd.validationtools.R;

public class FMTest extends BaseActivity {
    private static final String TAG = "FMTest";
    private TextView mContent = null;

    private static final String SYSTEM_FMRADIO_PACKAGE = "com.android.fmradio";
    private static final String SYSTEM_FMRADIO_ACTIVITY = "com.android.fmradio.FmMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContent = new TextView(this);
        mContent.setGravity(Gravity.CENTER);
        mContent.setText(getString(R.string.fm_test));
        setContentView(mContent);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        staFmRadioIntent();
    }

    private void staFmRadioIntent(){
        try {
            Intent intent = new Intent();
            intent.setClassName(SYSTEM_FMRADIO_PACKAGE, SYSTEM_FMRADIO_ACTIVITY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
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
