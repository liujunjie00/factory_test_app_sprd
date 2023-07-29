/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.charger;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WirelessChargerTest extends BaseActivity {

    private static final String TAG = "WirelessChargerTest";

    private static final String STATUS = "status";
    private static final String PLUGGED = "plugged";
    private static final String VOLTAGE = "voltage";
    private static final String TEST_RESULT_SUCCESS = "success";
    private static final String TEST_RESULT_FAIL = "fail";

    private TextView statusTextView, pluggedTextView, voltageTextView,
            mElectronicTextView, mTestResultTextView, mBatteryElectronicTextView;
    private String mPluggeString = null;
    private float mChargerVoltage;
    private float mBatteryElectronic;

    private static final String ENG_CHARGER_CURRENT = "/sys/class/power_supply/battery/current_now";
    private static final String ENG_CHARGER_VOLTAGE = "/sys/class/power_supply/sc27xx-fgu/constant_charge_voltage";

    private static final String CHARGER_STATUS_USB = "/sys/class/power_supply/usb/online";
    private static final String CHARGER_STATUS_AC = "/sys/class/power_supply/ac/online";
    private static final String WIRELESS_CHARGER_STATUS = "/sys/class/power_supply/wireless/online";
    private static final String CHARGING_PROTOCOL = "/sys/class/power_supply/nu1619_wireless_charger/wireless_type";

    private static final int WORKING = 1;
    private static final int NOT_WORKING = 0;

    private int chargerStateWireless;
    private boolean mIsPlugWirelsee = false;
    private boolean mIsPlugUSB_AC = false;

    AsyncTask<Void, Void, String> task = null;
    private String mInputCurrent = null;
    private int mRetryNum = 0;
    private int mWaitTime = 0;
    private int mElecBeforeCharge = 0;

    private Handler mHandler;

    private Runnable mElectronicUpdate = new Runnable() {
        public void run() {
            task = new AsyncTask<Void, Void, String>(){
                @Override
                protected String doInBackground(Void... params) {
                    String testResult = getInputElectronicNewStep();
                    return testResult;
                }
                protected void onPostExecute(String result) {
                    String testResult = result;
                    chargerStateWireless = Integer.parseInt(readFile(WIRELESS_CHARGER_STATUS).trim());
                    Log.d(TAG, "chargerStateWireless = " + chargerStateWireless);
                    Log.d(TAG, "mElectronicUpdate data,mChargerVoltage=" + mChargerVoltage
                            + ",testResult:" + testResult);

                    if (TEST_RESULT_SUCCESS.equals(testResult)) {
                        if (chargerStateWireless == WORKING) {
                            mTestResultTextView.setText(getString(R.string.charger_test_success));
                            mTestResultTextView.setTextColor(Color.GREEN);
                            storeRusult(true);
                            mHandler.postDelayed(mCompleteTest, 2000);
                        } else {
                            mHandler.postDelayed(mElectronicUpdate, 2000);
                        }
                    } else {
                        if(mIsPlugUSB_AC){
                            mTestResultTextView.setText(getString(R.string.charger_test_fail));
                            mTestResultTextView.setTextColor(Color.RED);
                            storeRusult(false);
                            mHandler.postDelayed(mCompleteTest, 2000);
                        } else if (chargerStateWireless == WORKING) {
                            mRetryNum++;
                            if (mRetryNum <= 15) {
                                mWaitTime = 1000 * mRetryNum;

                                mHandler.postDelayed(mElectronicUpdate, 1000);
                                Log.d(TAG, "retry test num:" + mRetryNum + ",wait time is " + mWaitTime);
                            } else {
                                mTestResultTextView.setText(getString(R.string.charger_test_fail));
                                mTestResultTextView.setTextColor(Color.RED);
                                storeRusult(false);
                                mHandler.postDelayed(mCompleteTest, 2000);
                            }
                        } else {
                            mHandler.postDelayed(mElectronicUpdate, 2000);
                        }
                    }
                };
            };
//            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
        }
    };

    private Runnable mCompleteTest = new Runnable() {
        public void run() {
            mHandler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                }
            }, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(R.string.battery_title_text);
        setContentView(R.layout.battery_wireless_charged_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        mHandler = new Handler();
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        pluggedTextView = (TextView) findViewById(R.id.pluggedTextView);
        voltageTextView = (TextView) findViewById(R.id.voltageTextView);
        mElectronicTextView = (TextView) findViewById(R.id.charger_voltage_tv);
        mTestResultTextView = (TextView) findViewById(R.id.test_resultTextView);
        mBatteryElectronicTextView = (TextView) findViewById(R.id.battery_electronic_tv);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBatteryElectronic = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
        mBatteryElectronicTextView.setText(mBatteryElectronic + " ma");
        mIsPlugUSB_AC = (Integer.parseInt(readFile(CHARGER_STATUS_USB).trim()) == WORKING) ||
                (Integer.parseInt(readFile(CHARGER_STATUS_AC).trim()) == WORKING);
        if(mIsPlugUSB_AC) {
            showTips();
        } else {
            mHandler.post(mUpdataRunnable);
            mHandler.postDelayed(mElectronicUpdate, 1000);
            mHandler.post(mGetCurrentRunnable);
        }
    }

    public Runnable mUpdataRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== checkVolatge! ===");
            initView();
            mHandler.postDelayed(mUpdataRunnable, 1000);
        }
    };

    public Runnable mGetCurrentRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== getCurrent! ===");
            stopCharge();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mElecBeforeCharge = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
            Log.d(TAG, "getInputElectronicNewStep inputCurrent c1=[" + mElecBeforeCharge + "]");
            startCharge();
        }
    };

    private void showTips(){
        AlertDialog.Builder builder = new AlertDialog.Builder(WirelessChargerTest.this)
                .setMessage(R.string.wireless_charger_tip)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mElectronicUpdate);
        mHandler.removeCallbacks(mUpdataRunnable);
        mHandler.removeCallbacks(mGetCurrentRunnable);
    }

    private String getInputElectronicNewStep() {
        Log.d(TAG, "mIsPlugUSB_AC = " + mIsPlugUSB_AC);
        if(mIsPlugUSB_AC) {
            return TEST_RESULT_FAIL;
        }
        String result = "";
        String inputCurrent = "";
        Log.d(TAG, "getInputElectronicNewStep inputCurrent[" + inputCurrent + "]");
        try {
            //step2.Start charge, read charging current
            int c2 = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim())  / 1000;
            Log.d(TAG, "getInputElectronicNewStep inputCurrent c2=[" + c2 + "]");
            int i1 = c2 - mElecBeforeCharge;
            Log.d(TAG, "getInputElectronicNewStep inputCurrent i1=[" + i1 + "]");
            Log.d(TAG, "getInputElectronicNewStep inputCurrent mChargerVoltage=[" + mChargerVoltage + "]");
            if(i1 >= 200) {
                result = TEST_RESULT_SUCCESS;
            } else if(i1 > 100 && i1 < 200 && mChargerVoltage >= 4100) {
                    result = TEST_RESULT_SUCCESS;
            } else {
                result = TEST_RESULT_FAIL;
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void initView() {

        mBatteryElectronic = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
        mBatteryElectronicTextView.setText(mBatteryElectronic + " ma");

        mChargerVoltage = getDateFromNode(ENG_CHARGER_VOLTAGE) / 1000;
        Log.d(TAG, "initView mChargerVoltage = " + mChargerVoltage);

        if (mChargerVoltage >= 100.0) {
            mElectronicTextView.setText(mChargerVoltage + " mv");
        } else {
            mElectronicTextView.setText("n/a");
        }
    }

    private float getDateFromNode(String nodeString) {
        char[] buffer = new char[1024];
        float batteryElectronic = -100;
        FileReader file = null;
        try {
            file = new FileReader(nodeString);
            int len = file.read(buffer, 0, 1024);
            batteryElectronic = Float.valueOf((new String(buffer, 0, len)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }finally{
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                Log.e(TAG, "getDateFromNode fail , nodeString is:" + nodeString);
            }
        }
        return batteryElectronic;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra(STATUS, 0);
                int plugged = intent.getIntExtra(PLUGGED, 0);
                int voltage = intent.getIntExtra(VOLTAGE, 0);
                String statusString = "";
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = getResources().getString(R.string.charger_unknown);
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = getResources().getString(R.string.charger_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = getResources().getString(R.string.charger_discharging);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = getResources().getString(R.string.charger_not_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = getResources().getString(R.string.charger_full);
                        break;
                    default:
                        break;
                }
                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        mIsPlugUSB_AC = true;
                        mPluggeString = getResources().getString(R.string.charger_ac_plugged);
                        mTestResultTextView.setVisibility(View.GONE);
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        mIsPlugUSB_AC = true;
                        mPluggeString = getResources().getString(R.string.charger_usb_plugged);
                        mTestResultTextView.setVisibility(View.GONE);
                        break;
                    default:
                        chargerStateWireless = Integer.parseInt(readFile(WIRELESS_CHARGER_STATUS).trim());
                        mTestResultTextView.setTextColor(Color.WHITE);
                        if(chargerStateWireless == WORKING) {
                            mPluggeString = getResources().getString(R.string.charger_wireless_plugged);
                            mTestResultTextView.setText(getString(R.string.charging_test));
                            mTestResultTextView.setVisibility(View.VISIBLE);
                        } else {
                            mPluggeString = getResources().getString(R.string.charger_no_plugged);
                        }
                        break;
                }
                statusTextView.setText(statusString);
                pluggedTextView.setText(mPluggeString);
                voltageTextView.setText(Integer.toString(voltage) + " mv");
                mBatteryElectronic = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
                mBatteryElectronicTextView.setText(mBatteryElectronic + " ma");
                Log.v(STATUS, statusString);
                Log.v(PLUGGED, mPluggeString);
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        mHandler.removeCallbacks(mCompleteTest);
        if (task != null ) {
            task.cancel(false);
        }
        super.onDestroy();
    }

    private String readFile(String path) {
        char[] buffer = new char[1024];
        String batteryElectronic = "";
        FileReader file = null;
        try {
            file = new FileReader(path);
            int len = file.read(buffer, 0, 1024);
            batteryElectronic = new String(buffer, 0, len);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                Log.w(TAG, "read file close fail");
            }
        }
        return batteryElectronic;
    }

    private void stopCharge() {
        boolean res = PhaseCheckParse.getInstance().writeChargeSwitch(1);
        Log.d(TAG, "stopCharge res="+res);
    }

    private void startCharge() {
        boolean res = PhaseCheckParse.getInstance().writeChargeSwitch(0);
        Log.d(TAG, "startCharge res="+res);
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        File file = new File(Const.WIRELESS_CHARGER_PATH_N6);
        if(!file.exists()){
            return false;
        }
        return true;
    }
}
