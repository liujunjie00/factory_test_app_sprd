/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.charger;

import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class QuickChargerTest extends BaseActivity {

    private static final String TAG = "QuickChargerTest";

    private static final String ENG_CHARGER_TEMP = "sys/class/power_supply/battery/temp";

    private static final String STATUS = "status";
    private static final String PLUGGED = "plugged";
    private static final String VOLTAGE = "voltage";
    private static final String TEST_RESULT_SUCCESS = "success";
    private static final String TEST_RESULT_FAIL = "fail";

    private TextView statusTextView, mChargingProtocolTextView, pluggedTextView, mBatteryVoltageTextView,
            mChargerVolatgeTextView, mTestResultTextView, mTemperatureTextView, mBatteryElectronicTextView;
    private String mPluggeString = null;
    private float mChargerVoltage;
    private float mBatteryElectronic;
    private int mPluggedType;
    private PhaseCheckParse mPhaseCheckParse = null;

    private float mBatteryTemperature;
    private TableRow mTemperatureTableRow;

    /** Replace real_time_current, IBat*/
    private static final String ENG_CHARGER_CURRENT = "/sys/class/power_supply/battery/current_now";
    private static final String BATTERY_CURRENT_VOLTAGE = "/sys/class/power_supply/battery/voltage_now";
    /** Replace charger_voltage*/

    private static final String BATTERY_CHARGER_VOLTAGE = "/sys/class/power_supply/sc27xx-fgu/VOLTAGE_AVG";

    private static final String ENG_CHARGER_VOLTAGE = "/sys/class/power_supply/sc27xx-fgu/constant_charge_voltage";

    private static final String ENG_REAL_BATTERY_TEMPERATURE = "sys/class/power_supply/sc27xx-fgu/temp";
    /** Judge whether fast charging is supported or not*/
    // private static final String ENG_REAL_CHARGER_TYPE = "/sys/class/power_supply/sc2730_fast_charger/usb_type";
    /** 3 is 18W charge type: Fast; 5 is 30W charge type: Adaptive; 4 is normal charge type: Standard */
    private static final String ENG_REAL_CHARGER_TYPE = "/sys/class/power_supply/battery/charge_type";

    public static final String QUICK_CHARGER_TYPE_PD_PPS = "/sys/class/power_supply/bq2597x-standalone";

    private static final boolean ENABLE_BATTRY_TEMPERATURE = com.sprd.validationtools.utils.FileUtils.fileIsExists(ENG_REAL_BATTERY_TEMPERATURE);
    AsyncTask<Void, Void, String> task = null;


    private String mChargerType = null;
    private boolean mIsQuickCharger = false;
    private int mRetryNum = 0;
    private int mWaitTime = 3000;
    private int updateTime = 0;
    private int mBatteryPercent = 0;
    private int mElecBeforeCharge = 0;

    private Handler mHandler;

    private Runnable mElectronicUpdate = new Runnable() {
        public void run() {

            task = new AsyncTask<Void, Void, String>(){

                @Override
                protected String doInBackground(Void... params) {
                    String testResult = getInputElectronicNewStep();
                    Log.d(TAG, "testResult = " + testResult);
                    return testResult;
                }
                protected void onPostExecute(String result) {
                    String testResult = result;
                    Log.d(TAG, "mElectronicUpdate data, mChargerVoltage=" + mChargerVoltage
                            + ", mBatteryTemperature=" + mBatteryTemperature
                            + ",testResult:" + testResult);

                    if (TEST_RESULT_SUCCESS.equals(testResult)) {
                        if (1 == mPluggedType) {
                            mTestResultTextView.setText(getChargerTypeTip() + getString(R.string.charger_test_success));
                            mTestResultTextView.setTextColor(Color.GREEN);
                            storeRusult(true);
                            mHandler.postDelayed(mCompleteTest, 1000);
							try{
							 if(mPassButton != null){
								mPassButton.setVisibility(View.VISIBLE);//
								}
								}catch (Exception e){}
                        } else {
                            mHandler.postDelayed(mElectronicUpdate, 1000);
                        }
                    } else {
                        if (1 == mPluggedType) {
                            mRetryNum++;
                            if (mRetryNum <= 30) {
                                mWaitTime = 1000 * mRetryNum;

                                mHandler.postDelayed(mElectronicUpdate, 1000);
                                Log.d(TAG, "retry test num:" + mRetryNum + ",wait time is " + mWaitTime);
                            } else {
                                mTestResultTextView.setText(getChargerTypeTip() + getString(R.string.charger_test_fail));
                                mTestResultTextView.setTextColor(Color.RED);
                                storeRusult(false);
                                mHandler.postDelayed(mCompleteTest, 1000);
                            }
                        } else {
                            mHandler.postDelayed(mElectronicUpdate, 1000);
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
        setContentView(R.layout.battery_quick_charged_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        mHandler = new Handler();
        statusTextView = (TextView) findViewById(R.id.tv_status);
        mChargingProtocolTextView = (TextView) findViewById(R.id.tv_get_protocol);
        pluggedTextView = (TextView) findViewById(R.id.tv_plugged);
        mBatteryVoltageTextView = (TextView) findViewById(R.id.tv_battery_voltage);
        mChargerVolatgeTextView = (TextView) findViewById(R.id.tv_charger_Voltage);
        mTestResultTextView = (TextView) findViewById(R.id.tv_test_result);
        mTemperatureTextView = (TextView) findViewById(R.id.tv_batterytemperature);
        mTemperatureTableRow = (TableRow) findViewById(R.id.TableRow06);

        mBatteryElectronicTextView = (TextView) findViewById(R.id.tv_battery_electronic);
        if (ENABLE_BATTRY_TEMPERATURE) {
            mTemperatureTableRow.setVisibility(View.VISIBLE);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
        mPhaseCheckParse = PhaseCheckParse.getInstance();
		
		mPassButton.setVisibility(View.GONE);//
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ENABLE_BATTRY_TEMPERATURE) {
            mBatteryElectronic = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
            mBatteryElectronicTextView.setText(mBatteryElectronic + " ma");

            mBatteryTemperature = getDateFromNode(ENG_REAL_BATTERY_TEMPERATURE);
            mTemperatureTextView.setText(mBatteryTemperature / 10 + " \u2103");
        }
        mHandler.post(mUpdataRunnable);
        mHandler.postDelayed(mCheckRunnable, 500);
    }

    public Runnable mUpdataRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== checkVolatge! ===");
            initView();
            mHandler.postDelayed(mUpdataRunnable, 1000);
        }
    };

    /**
     * Modified the judgment logic of fast charge test items.
     * If the charge is higher than 90, falI is determined and test after discharge is prompted.
     * Fast charge test when the charge is lower than 90.
     * Replace the charger with a common charger prompt.
     * If the fast charging appliance is used, the fast charging test shall be carried out.
     * If the charger voltage does not rise above 7V after 30 seconds, it is judged as failure.
     * Otherwise, determine whether the difference between the current before and
     * after charging is greater than 1A.
     * **/
    public Runnable mCheckRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== checkVolatge! ===");
            mIsQuickCharger = getChargerType();
            if (mIsQuickCharger) {
                if(mBatteryPercent >= 90){
                    mTestResultTextView.setText(getChargerTypeTip() + getString(R.string.charger_test_fail));
                    mTestResultTextView.setTextColor(Color.RED);
                    Toast.makeText(QuickChargerTest.this, R.string.battery_hight, Toast.LENGTH_SHORT).show();
                    storeRusult(false);
                    mHandler.postDelayed(mCompleteTest, 1000);
                } else {
                    if (updateTime == 0) {
                        stopCharge();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mElecBeforeCharge = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
                        Log.d(TAG, "stop charge mElecBeforeCharge=[" + mElecBeforeCharge + "]");
                        startCharge();
                    } else if (updateTime == 30) {
                        mTestResultTextView.setText(getChargerTypeTip() + getString(R.string.charger_test_fail));
                        mTestResultTextView.setTextColor(Color.RED);
                        storeRusult(false);
                        mHandler.postDelayed(mCompleteTest, 1000);
                    }
                    updateTime++;
                    initView();
                    if(7000 <= mChargerVoltage) {
                        mHandler.postDelayed(mElectronicUpdate, 500);
                    } else {
                        mHandler.postDelayed(mCheckRunnable, 1000);
                    }
                }
            } else {
                updateTime = 0;
                mHandler.postDelayed(mCheckRunnable, 1000);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mElectronicUpdate);
        mHandler.removeCallbacks(mCheckRunnable);
        mHandler.removeCallbacks(mUpdataRunnable);
    }

    private String getInputElectronicNewStep() {
        String result = "";
        String inputCurrent = "";
        Log.d(TAG, "getInputElectronicNewStep inputCurrent[" + inputCurrent + "]");
        try {
            int c2 = 0;
            c2 = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
            Log.d(TAG, "getInputElectronicNewStep inputCurrent c2=[" + c2 + "]");
            int i1 = c2 - mElecBeforeCharge;
            Log.d(TAG, "getInputElectronicNewStep inputCurrent i1=[" + i1 + "]");
            Log.d(TAG, "getInputElectronicNewStep inputCurrent mChargerVoltage=[" + mChargerVoltage + "]");

            if (i1 >= 1000) {
                result = TEST_RESULT_SUCCESS;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void initView() {

        mChargerVoltage = Integer.parseInt(mPhaseCheckParse.getChargeChargingstatus(47).trim()) / 1000;
        mBatteryElectronic = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
        mBatteryElectronicTextView.setText(mBatteryElectronic + " ma");

        mBatteryVoltageTextView.setText((getDateFromNode(BATTERY_CURRENT_VOLTAGE) / 1000) + "mV");
        Log.d(TAG, "initView mChargerVoltage = " + mChargerVoltage);
        if (ENABLE_BATTRY_TEMPERATURE) {
            mBatteryTemperature = getDateFromNode(ENG_REAL_BATTERY_TEMPERATURE);
        } else {
            mBatteryTemperature = getDateFromNode(ENG_CHARGER_TEMP);
        }
        mTemperatureTextView.setText(mBatteryTemperature / 10 + " \u2103");

        // General power of the test will have an initial value of 40mv.
        // Unfriendly so set a value greater than 100 and must plug usb or
        // ac
        if (mChargerVoltage >= 100.0 && (mPluggedType >= 0)) {
            mChargerVolatgeTextView.setText(mChargerVoltage + " mv");
        } else {
            mChargerVolatgeTextView.setText("n/a");
        }
    }

    // Get charging protocol
    public boolean getChargerType() {
        String currentState = readFile(ENG_REAL_CHARGER_TYPE).trim();
        mChargingProtocolTextView.setText(currentState);
        Log.d(TAG, "chargerType = " + currentState);
        if(currentState.equals("Fast") || currentState.equals("Adaptive")) {
            return true;
        }
        return false;
    }

    //UNISOC:Bug1587781 Adapt to two fast charging types
    private String getChargerTypeTip() {
        File file = new File(QUICK_CHARGER_TYPE_PD_PPS);
        boolean isFileExist = file.exists();
        Log.d(TAG, "quick charger type PD_PPS = " + isFileExist);
        if (isFileExist && "PD_PPS".equals(mChargerType)) {
            return "PPS ";
        } else if ("PD".equals(mChargerType)) {
            return "PD ";
        } else {
            return "";
        }
    }

    private float getDateFromNode(String nodeString) {
        char[] buffer = new char[1024];
        // Set a special value -100, to distinguish mChargerElectronic greater
        // than -40.
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
                int current = intent.getExtras().getInt("level");
                int total = intent.getExtras().getInt("scale");
                mBatteryPercent = current * 100 / total;
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
                        mPluggedType = 1;
                        mPluggeString = getResources().getString(R.string.charger_ac_plugged);
                        mTestResultTextView.setVisibility(View.VISIBLE);
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        mPluggedType = 0;
                        mPluggeString = getResources().getString(R.string.charger_usb_plugged);
                        mTestResultTextView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mPluggedType = -1;
                        mPluggeString = getResources().getString(R.string.charger_no_plugged);
                        mTestResultTextView.setText(getString(R.string.charging_test));
                        mTestResultTextView.setTextColor(Color.WHITE);
                        mTestResultTextView.setVisibility(View.GONE);
                        // Prevent unplug the usb cable is still charging
                        // status.
                        if (statusString.equals(getString(R.string.charger_charging))) {
                            statusString = getResources().getString(R.string.charger_discharging);
                            Log.d(TAG, "Correct the error displays charge status.");
                        }
                        break;
                }

                statusTextView.setText(statusString);
                Log.d(TAG, "mIsQuickCharger = " + getChargerType());
                mChargingProtocolTextView.setText(mChargerType);
                pluggedTextView.setText(mPluggeString);
                mBatteryVoltageTextView.setText(Integer.toString(voltage) + " mv");
                if (ENABLE_BATTRY_TEMPERATURE) {
                    mBatteryElectronic = Integer.parseInt(readFile(ENG_CHARGER_CURRENT).trim()) / 1000;
                    mBatteryElectronicTextView.setText(mBatteryElectronic + " ma");

                    mBatteryTemperature = getDateFromNode(ENG_REAL_BATTERY_TEMPERATURE);
                    mTemperatureTextView.setText(mBatteryTemperature / 10 + " \u2103");
                }
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
        String res = FileUtils.readFile(Const.QUICK_CHARGER_PATH).trim();
        if ("1".equals(res)) {
            return true;
        }
        return false;
    }
}
