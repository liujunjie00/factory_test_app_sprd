
package com.sprd.validationtools.itemstest.charger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;

import static android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN;
import static android.os.BatteryManager.BATTERY_PLUGGED_AC;
import static android.os.BatteryManager.BATTERY_PLUGGED_USB;
import static android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS;
import static android.os.BatteryManager.BATTERY_STATUS_CHARGING;
import static android.os.BatteryManager.BATTERY_STATUS_UNKNOWN;
import static android.os.BatteryManager.EXTRA_HEALTH;
import static android.os.BatteryManager.EXTRA_LEVEL;
import static android.os.BatteryManager.EXTRA_PLUGGED;
import static android.os.BatteryManager.EXTRA_PRESENT;
import static android.os.BatteryManager.EXTRA_STATUS;
//import com.android.server.health.HealthServiceWrapper;
//import android.hardware.health.HealthInfo;

public class ChargerTest extends BaseActivity {

    private static final String TAG = "ChargerTest";
    private static final String ENG_BATTERY_CURRENT = "/sys/class/power_supply/battery/current_now";
    private static final String ENG_CHARGER_VOLTAGE = "/sys/class/power_supply/sc27xx-fgu/constant_charge_voltage";
    private static final String ENG_REAL_BATTERY_TEMPERATURE = "sys/class/power_supply/battery/temp";
    private static final String STATUS = "status";
    private static final String PLUGGED = "plugged";
    private static final String VOLTAGE = "voltage";
    private static final String TEST_RESULT_SUCCESS = "success";
    private static final String TEST_RESULT_FAIL = "fail";

    private TextView mStatusTextView, mPluggedTextView, mBatteryCurrentTextView, mBatteryVoltageTextView,
                    mChargerVoltageTextView, mTemperatureTextView, mTestResultTextView;
    private boolean mIsPlugUSB = false;
    private float mChargerVoltage;
    private float mBatteryTemperature;
    // UNISOC: Bug1452480 cancel the task when activity ondestroy
    AsyncTask<Void, Void, String> mAsyncTask = null;
    private int mRetryNum = 0;
    private Handler mHandler;
    private BatteryManager mBatteryManager;

    private Runnable mUpdate = new Runnable() {
        public void run() {
            mAsyncTask = new AsyncTask<Void, Void, String>(){

                @Override
                protected String doInBackground(Void... params) {
                    String testResult = getInputCurrentNewStep();
                    return testResult;
                }
                protected void onPostExecute(String result) {
                    String testResult = result;
                    Log.d(TAG, "mUpdate mChargerVoltage=" + mChargerVoltage
                            + ", mBatteryTemperature=" + mBatteryTemperature
                            + ", testResult:" + testResult);
                    if (TEST_RESULT_SUCCESS.equals(testResult)) {
                        if (mIsPlugUSB) {
                            mTestResultTextView.setText(getString(R.string.charger_test_success));
                            mTestResultTextView.setTextColor(Color.GREEN);
							 try{
							 if(mPassButton != null){
								mPassButton.setVisibility(View.VISIBLE);//
								}
								}catch (Exception e){}
                         //   storeRusult(true);
                         //   mHandler.postDelayed(mCompleteTest, 2000);
                        } else {
                            mHandler.postDelayed(mUpdate, 2000);
                        }
                    } else {
                        if (mIsPlugUSB) {
                            mRetryNum++;
                            if (mRetryNum <= 5) {
                                mHandler.post(mUpdate);
                                Log.d(TAG, "retry test num:" + mRetryNum);
                            } else {
                                mTestResultTextView.setText(getString(R.string.charger_test_fail));
                                mTestResultTextView.setTextColor(Color.RED);
                                storeRusult(false);
                           //     mHandler.postDelayed(mCompleteTest, 2000);
                            }
                        } else {
                            mHandler.postDelayed(mUpdate, 2000);
                        }
                    }
                };
            };
//            mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
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
        setContentView(R.layout.battery_charged_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
//        mHandler = new Handler();
        mStatusTextView = (TextView) findViewById(R.id.statusTextView);
        mPluggedTextView = (TextView) findViewById(R.id.pluggedTextView);
        mBatteryVoltageTextView = (TextView) findViewById(R.id.batteryVoltageTextView);
//        mChargerVoltageTextView = (TextView) findViewById(R.id.chargerVoltageTextView);
        mBatteryCurrentTextView = (TextView) findViewById(R.id.battery_current_tv);
        mTemperatureTextView = (TextView) findViewById(R.id.batterytemperatureTextView);
        mTestResultTextView = (TextView) findViewById(R.id.test_resultTextView);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
            mPassButton.setVisibility(View.GONE);
			mFailButton.setVisibility(View.VISIBLE);//
        mBatteryCurrentTextView.setText("--");
//        Intent intent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
//            int status = intent.getIntExtra(EXTRA_STATUS, BATTERY_STATUS_UNKNOWN);
//            int plugged = intent.getIntExtra(EXTRA_PLUGGED, 0);
//            int level = intent.getIntExtra(EXTRA_LEVEL, 0);
//            int health = intent.getIntExtra(EXTRA_HEALTH, BATTERY_HEALTH_UNKNOWN);
//            int temperature = intent.getIntExtra("temperature", 0);
//            int scale = intent.getIntExtra("scale", 0);
//            boolean present = intent.getBooleanExtra(EXTRA_PRESENT, true);
//            int maxChargingCurrent = intent.getIntExtra("max_charging_current", 0);
//            int maxChargingVoltage = intent.getIntExtra("max_charging_voltage", 0);
//            int voltage = intent.getIntExtra(VOLTAGE, 0);
//            Log.d(TAG, "onReceive: " +
//                    "status = " + status + " , " +
//                    "plugged = " + plugged + " , " +
//                    "level = " + level + " , " +
//                    "health = " + health + " , " +
//                    "temperature = " + temperature + " , " +
//                    "scale = " + scale + " , " +
//                    "present = " + present + " , " +
//                    "maxChargingCurrent = " + maxChargingCurrent + " , " +
//                    "maxChargingVoltage = " + maxChargingVoltage + " , " +
//                    "voltage = " + voltage);
                    mBatteryManager = (BatteryManager)getSystemService(Context.BATTERY_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        int batteryCurrent = Integer.parseInt(readFile(ENG_BATTERY_CURRENT).trim()) / 1000;
//        mBatteryCurrentTextView.setText(batteryCurrent + " ma");
//        mBatteryTemperature = getDataFromNode(ENG_REAL_BATTERY_TEMPERATURE);
//        mTemperatureTextView.setText(mBatteryTemperature / 10 + " \u2103");
//        mHandler.postDelayed(mUpdate, 500);

//        String current_now = readFile("/sys/devices/platform/ff200000.i2c/i2c-0/0-0020/rk817-battery/power_supply/battery/current_now");
//        getCurrentnow();
//        Log.d("cyc", "onResume: " + current_now);
    }

//    private HealthServiceWrapper mHealthService;
//    private android.hardware.health.HealthInfo mHealthInfo;

//    private int getCurrentNow() {
//        int intProperty;
//        if (mBatteryManager != null) {
//            int intProperty = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
//        }
//        return intProperty;
//    }

    @Override
    protected void onPause() {
        super.onPause();
//        mHandler.removeCallbacks(mUpdate);
    }

    private String getInputCurrentNewStep() {
        String result = "";
        String inputCurrent = "";
        try {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    initView();
                }
            });
            //step1.Stop charge, read  current
            stopCharge();
            Thread.sleep(2000);
            int c1 = 0;
            c1 = Integer.parseInt(readFile(ENG_BATTERY_CURRENT).trim()) / 1000;
            Log.d(TAG, "getInputCurrentNewStep inputCurrent c1=[" + c1 + "]");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int batteryCurrent = Integer.parseInt(readFile(ENG_BATTERY_CURRENT).trim()) / 1000;
                    mBatteryCurrentTextView.setText(batteryCurrent + " ma");
                }
            });
            //step2.Start charge, read charging current
            startCharge();
            Thread.sleep(2000);
            int c2 = 0;
            c2 = Integer.parseInt(readFile(ENG_BATTERY_CURRENT).trim()) / 1000;
            Log.d(TAG, "getInputCurrentNewStep inputCurrent c2=[" + c2 + "]");
            int i1 = c2 - c1;
            Log.d(TAG, "getInputCurrentNewStep inputCurrent i1=[" + i1 + "]");
            Log.d(TAG, "getInputCurrentNewStep inputCurrent mChargerVoltage=[" + mChargerVoltage + "]");
            //i1 >= 200mA PASS
            if (i1 >= 200) {
                result = TEST_RESULT_SUCCESS;
            } else if (i1 > 100 && i1 < 200 && mChargerVoltage >= 4100) {
            //i1 > 100mA && i1 < 200mA && mChargerVoltage >= 4100 PASS
                result = TEST_RESULT_SUCCESS;
            } else {
                result = TEST_RESULT_FAIL;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int batteryCurrent = Integer.parseInt(readFile(ENG_BATTERY_CURRENT).trim()) / 1000;
                    mBatteryCurrentTextView.setText(batteryCurrent + " ma");
                }
            });
        } catch (NumberFormatException | InterruptedException e) {
            Log.w(TAG, "getInputCurrentNewStep fail", e);
            e.printStackTrace();
        }
        return result;
    }

    private void initView() {
        mChargerVoltage = getDataFromNode(ENG_CHARGER_VOLTAGE) / 1000;
        Log.d(TAG, "initView mChargerVoltage=" + mChargerVoltage);
        // General power of the test will have an initial value of 40mv.
        // Unfriendly so set a value greater than 100 and must plug usb or
        // ac
        if (mChargerVoltage >= 100.0 && mIsPlugUSB) {
            mChargerVoltageTextView.setText(mChargerVoltage + " mv");
        } else {
            mChargerVoltageTextView.setText("n/a");
        }
        mBatteryTemperature = getDataFromNode(ENG_REAL_BATTERY_TEMPERATURE);
        mTemperatureTextView.setText(mBatteryTemperature / 10 + " \u2103");
    }

    private float getDataFromNode(String nodeString) {
        char[] buffer = new char[1024];
        // Set a special value -100, to distinguish mChargerCurrent greater
        // than -40.
        float data = -100;
        FileReader file = null;
        try {
            file = new FileReader(nodeString);
            int len = file.read(buffer, 0, 1024);
            data = Float.valueOf((new String(buffer, 0, len)));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                Log.e(TAG, "getDataFromNode fail , nodeString is:" + nodeString);
            }
        }
        return data;
    }

    private String readFile(String path) {
        char[] buffer = new char[1024];
        String fileData = "";
        FileReader file = null;
        try {
            file = new FileReader(path);
            int len = file.read(buffer, 0, 1024);
            fileData = new String(buffer, 0, len);
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
        return fileData;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra(STATUS, 0);
                int plugged = intent.getIntExtra(PLUGGED, 0);
                int batteryVoltage = intent.getIntExtra(VOLTAGE, 0);
                int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                int currentNow = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
//                int currentVoltage = intent.getIntExtra(BatteryManager.EXTRA_MAX_CHARGING_CURRENT, 0);
//                int maxVoltage = intent.getIntExtra(BatteryManager.EXTRA_MAX_CHARGING_VOLTAGE, 0);
//                Log.d(TAG, "batteryVoltage: " + batteryVoltage);
//                Log.d(TAG, "temp: " + temp);
//                Log.d(TAG, "currentVoltage: " + currentVoltage);
//                Log.d(TAG, "maxVoltage: " + maxVoltage);
//                Log.d(TAG, "status: " + status);
//                Log.d(TAG, "plugged: " + plugged);
//                int int_BATTERY_PROPERTY_CURRENT_NOW = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
//                Log.d(TAG, "int_BATTERY_PROPERTY_CURRENT_NOW: " + int_BATTERY_PROPERTY_CURRENT_NOW);
//                int int_BATTERY_PROPERTY_CURRENT_AVERAGE = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE);
//                Log.d(TAG, "int_BATTERY_PROPERTY_CURRENT_AVERAGE: " + int_BATTERY_PROPERTY_CURRENT_AVERAGE);
//                int int_BATTERY_PROPERTY_STATUS = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);
//                Log.d(TAG, "int_BATTERY_PROPERTY_STATUS: " + int_BATTERY_PROPERTY_STATUS);
//                int int_BATTERY_PROPERTY_CAPACITY = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
//                Log.d(TAG, "int_BATTERY_PROPERTY_CAPACITY: " + int_BATTERY_PROPERTY_CAPACITY);

                String statusString = "";
                String pluggeString = "";
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
                        mIsPlugUSB = true;
                        pluggeString = getResources().getString(R.string.charger_ac_plugged);
                        mTestResultTextView.setVisibility(View.VISIBLE);
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        mIsPlugUSB = true;
                        pluggeString = getResources().getString(R.string.charger_usb_plugged);
                        mTestResultTextView.setVisibility(View.VISIBLE);
                        break;
                    default:
                        mIsPlugUSB = false;
                        pluggeString = getResources().getString(R.string.charger_no_plugged);
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
                Log.d(TAG, "status:" + statusString + "plugged:" + pluggeString);
                mStatusTextView.setText(statusString);
                mPluggedTextView.setText(pluggeString);
                mBatteryVoltageTextView.setText(batteryVoltage + " mv");
                int curCharge = currentNow / 1000;
                if (mIsPlugUSB && curCharge > 0) {
                    mBatteryCurrentTextView.setText(currentNow / 1000 + " ma");
                } else {
                    mBatteryCurrentTextView.setText("--");
                }
                mTemperatureTextView.setText(temp / 10 + " \u2103");
                if (mIsPlugUSB) {
                    mPassButton.setVisibility(View.VISIBLE);//
                    mTestResultTextView.setText(getString(R.string.charger_test_success));
                    mTestResultTextView.setTextColor(Color.GREEN);
                }
//                int batteryCurrent = Integer.parseInt(readFile(ENG_BATTERY_CURRENT).trim()) / 1000;
//                mBatteryCurrentTextView.setText(batteryCurrent + " ma");
//                mBatteryTemperature = getDataFromNode(ENG_REAL_BATTERY_TEMPERATURE);
//                mTemperatureTextView.setText(mBatteryTemperature / 10 + " \u2103");
            }
        }
    };

    @Override
    public void onDestroy() {
       unregisterReceiver(mBroadcastReceiver);
//        mHandler.removeCallbacks(mCompleteTest);
        // UNISOC: Bug1452480 cancel the task when activity ondestroy
//        if (mAsyncTask != null ) {
//            mAsyncTask.cancel(false);
//        }
        super.onDestroy();
    }

    private void stopCharge() {
        boolean res = PhaseCheckParse.getInstance().writeChargeSwitch(1);
        Log.d(TAG, "stopCharge res=" + res);
    }

    private void startCharge() {
        boolean res = PhaseCheckParse.getInstance().writeChargeSwitch(0);
        Log.d(TAG, "startCharge res=" + res);
    }
}