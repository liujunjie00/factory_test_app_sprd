/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi;

import android.app.NotificationChannel;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.WindowManager;
import android.util.Log;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.autommi.modules.AutoTestItemList;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class AutoMMIService extends Service {
    private static final String TAG = "AutoMMIService";

    private Context mContext = null;
    private PowerManager.WakeLock mWakeLock = null;

    private ArrayList<TestItem> mAutoTestArray = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "WakeLock");
            mWakeLock.acquire();
        }
        initAutoTest();
    }

    private void sendKey(String cmd) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initAutoTest() {
        FileUtils.deleteFile();
        mAutoTestArray = AutoTestItemList.getInstance(this).getAutoTestItemList();
        autoTest();
    }

    private void autoTest() {
        Log.d(TAG, "mAutoTestCurrentNumber" + Const.startTestID);
        if (mAutoTestArray != null && Const.startTestID < mAutoTestArray.size()) {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(this, mAutoTestArray.get(Const.startTestID).getTestClassName());
            intent.putExtra(Const.INTENT_PARA_TEST_NAME, mAutoTestArray.get(Const.startTestID).getTestName());
            intent.putExtra(Const.INTENT_PARA_TEST_INDEX, Const.startTestID);
            mContext.startActivity(intent);
        }
    }

}