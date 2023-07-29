
package com.sprd.validationtools.itemstest.sensor;
/** BUG479359 zhijie.yang 2016/5/5 MMI add the magnetic sensors and the prox sensor calibration**/

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.ValidationToolsUtils;

public class ProxSensorCalibrationActivity extends BaseActivity {

    private static final String TAG = "ProxSensorCalibrationActivity";

    private static final int START_AUTO_TEST = 0;
    private static final int RESULT_AUTO_TEST = 1;
    private static final int SVAE_AUTO_TEST = 2;
    private static final int START_MANUAL_NEAR_TEST = 3;
    private static final int RESULT_MANUAL_NEAR_TEST = 4;
    private static final int SVAE_MANUAL_NEAR_TEST = 5;
    private static final int START_MANUAL_FAR_TEST = 6;
    private static final int RESULT_MANUAL_FAR_TEST = 7;
    private static final int SVAE_MANUAL_FAR_TEST = 8;

    private static final int AUTO_TEST = 0;
    private static final int MANUAL_TEST_NEAR = 1;
    private static final int MANUAL_TEST_FAR = 2;

    private static final String AUTO_SET_CMD = "0 8 1"; // auto calibrating
    private static final String TEST_GET_RESULT = "1 8 1";// get result of Calibration
    private static final String TEST_SAVE_RESULT = "3 8 1";// save the result
    private static final String MANUAL_NEAR_SET_CMD = "0 8 5";// manual near calibrating
    private static final String MANUAL_FAR_SET_CMD = "0 8 6";// manual far calibrating

    private static final String CALI_FILE = "/mnt/vendor/productinfo/sensor_calibration_data/proximity";

    //Result after cali work
    private static final int CALIB_STATUS_OUT_OF_MINRANGE = 253;//-3:1111 1101
    private static final int CALIB_STATUS_OUT_OF_RANGE = 254;//-2:1111 1110
    private static final int CALIB_STATUS_FAIL = 255; //-1:1111 1111
    private static final int CALIB_STATUS_NON = 0;
    private static final int CALIB_STATUS_INPROCESS = 1;
    private static final int CALIB_STATUS_PASS = 2;

    //Result after save data
    private static final int SAVEDATA_STATUS_OUT_OF_MINRANGE = 253;
    private static final int SAVEDATA_STATUS_FAIL = 255;
    private static final int SAVEDATA_STATUS_PASS = 0;

    private static final int TWO_SECONDS = 2 * 1000;// 2 seconds;
    private static final int FOUR_SECONDS = 4 * 1000;// 4 seconds;

    private Button mAutoButton;
    private Button mManualButton;
    private TextView mTestTips;
    private boolean autoResult = false;
    private boolean nearResult = false;
    private boolean farResult = false;
    private Context mContext;

    private boolean autoSaveResult = false;
    private boolean nearSaveResult = false;
    private boolean farSaveResult = false;

    private Handler mHandler = new Handler();
    private ProxHandler mProxHandler;

    private SensorUtils mSensorUtils = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_prox_calibration);
        mContext = this;
        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        mProxHandler = new ProxHandler(ht.getLooper());
        mAutoButton = (Button) findViewById(R.id.prox_auto_button);
        mManualButton = (Button) findViewById(R.id.prox_manual_button);
        mTestTips = (TextView) findViewById(R.id.test_tips);
        if (Const.isAutoTestMode()) {
            mAutoButton.setVisibility(View.GONE);
            mManualButton.setVisibility(View.GONE);
            mTestTips.setVisibility(View.VISIBLE);
            mTestTips.setText(getResources().getString(
                        R.string.prox_auto_test_tips));
            mProxHandler.sendMessage(mProxHandler.obtainMessage(START_AUTO_TEST));
        }
        mAutoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAutoButton.setVisibility(View.GONE);
                mManualButton.setVisibility(View.GONE);
                mTestTips.setVisibility(View.VISIBLE);
                mTestTips.setText(getResources().getString(
                        R.string.prox_auto_test_tips));
                mProxHandler.sendMessage(mProxHandler.obtainMessage(START_AUTO_TEST));
            }
        });
        mManualButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mAutoButton.setVisibility(View.GONE);
                mManualButton.setVisibility(View.GONE);
                mTestTips.setVisibility(View.VISIBLE);
                showTipsDialog(MANUAL_TEST_NEAR);
            }
        });
            mPassButton.setVisibility(View.GONE);
        mSensorUtils = new SensorUtils(this, Sensor.TYPE_PROXIMITY);
        mSensorUtils.enableSensor();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * the function to start calibration autotest: echo "0 8 1" > calibrator_cmd near-manual: echo
     * "0 8 5" > calibrator_cmd far-manual: echo "0 8 6" > calibrator_cmd
     **/
    private void startCalibration(int type) {
        Log.d(TAG, "startCalibration: " + type);
        if (type == AUTO_TEST) {
            FileUtils.writeFile(Const.CALIBRATOR_CMD, AUTO_SET_CMD);
            return;
        } else if (type == MANUAL_TEST_NEAR) {
            FileUtils.writeFile(Const.CALIBRATOR_CMD, MANUAL_NEAR_SET_CMD);
            return;
        } else if (type == MANUAL_TEST_FAR) {
            FileUtils.writeFile(Const.CALIBRATOR_CMD, MANUAL_FAR_SET_CMD);
            return;
        }
    }

    private void showTipsDialog(int type) {
        Log.d(TAG, "showTipsDialog: " + type);
        final int step = type;
        String tips = null;
        if (step == MANUAL_TEST_NEAR) {
            tips = mContext.getString(R.string.prox_manual_tips_near);
        } else {
            tips = mContext.getString(R.string.prox_manual_tips_far);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.prox_calibration_button_manual))
                .setMessage(tips)
                .setNegativeButton(getString(R.string.alertdialog_ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                if (step == MANUAL_TEST_NEAR) {
                                    mProxHandler.sendMessage(mProxHandler
                                            .obtainMessage(START_MANUAL_NEAR_TEST));
                                } else {
                                    mProxHandler.sendMessage(mProxHandler
                                            .obtainMessage(START_MANUAL_FAR_TEST));
                                }
                            }
                        }).create();
        alertDialog.show();
    }

    @Override
    public void onDestroy() {
        if (mProxHandler != null) {
            Log.d(TAG, "HandlerThread has quit");
            mProxHandler.getLooper().quit();
        }
        if(mSensorUtils != null){
            mSensorUtils.disableSensor();
        }
        super.onDestroy();
    }

    private void ThreadSleep(int sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class ProxHandler extends Handler {

        public ProxHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START_AUTO_TEST:
                    startCalibration(AUTO_TEST);
                    mProxHandler.sendMessageDelayed(mProxHandler.obtainMessage(RESULT_AUTO_TEST),
                            FOUR_SECONDS);
                    break;
                case RESULT_AUTO_TEST:
                    autoResult = SensorUtils.getResult(TEST_GET_RESULT);
                    if (autoResult) {
                        mProxHandler.sendMessage(mProxHandler.obtainMessage(SVAE_AUTO_TEST));
                    } else {
                        mHandler.post(new Runnable() {
                            public void run() {
                                mTestTips.setText(getResources().getString(
                                        R.string.prox_auto_test_fail));
                            }
                        });
                        if (Const.isAutoTestMode()) {
                            storeRusult(false);
                            finish();
                        }
                    }
                    break;
                case SVAE_AUTO_TEST:
                    autoSaveResult = SensorUtils.saveResult(TEST_SAVE_RESULT, CALI_FILE);
                    if (autoResult && autoSaveResult) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                mPassButton.setVisibility(View.VISIBLE);
                                Toast.makeText(mContext, R.string.text_pass,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        storeRusult(true);
                        finish();
                    } else {
                        mHandler.post(new Runnable() {
                            public void run() {
                                mTestTips.setText(getResources().getString(
                                        R.string.prox_auto_test_fail));
                            }
                        });
                        if (Const.isAutoTestMode()) {
                            storeRusult(false);
                            finish();
                        }
                    }
                    break;
                case START_MANUAL_NEAR_TEST:
                    mHandler.post(new Runnable() {
                        public void run() {
                            mTestTips.setText(getResources().getString(
                                    R.string.prox_manual_tips_near_testing));
                        }
                    });
                    startCalibration(MANUAL_TEST_NEAR);
                    mProxHandler.sendMessageDelayed(
                            mProxHandler.obtainMessage(RESULT_MANUAL_NEAR_TEST), FOUR_SECONDS);
                    break;
                case RESULT_MANUAL_NEAR_TEST:
                    showTipsDialog(MANUAL_TEST_FAR);
                    break;
                case START_MANUAL_FAR_TEST:
                    mHandler.post(new Runnable() {
                        public void run() {
                            mTestTips.setText(getResources().getString(
                                    R.string.prox_manual_tips_far_testing));
                        }
                    });
                    startCalibration(MANUAL_TEST_FAR);
                    mProxHandler.sendMessageDelayed(
                            mProxHandler.obtainMessage(RESULT_MANUAL_FAR_TEST), FOUR_SECONDS);
                    break;
                case RESULT_MANUAL_FAR_TEST:
                    farResult = SensorUtils.getResult(TEST_GET_RESULT);
                    if (farResult) {
                        mProxHandler.sendMessage(mProxHandler.obtainMessage(SVAE_MANUAL_FAR_TEST));
                    } else {
                        final String str = (farResult ? getResources().getString(R.string.prox_manual_tips_far_pass) :
                                        getResources().getString(R.string.prox_manual_tips_far_fail));
                        mHandler.post(new Runnable() {
                            public void run() {
                                mTestTips.setText(str);
                            }
                        });
                    }
                    break;
                case SVAE_MANUAL_FAR_TEST:
                    farSaveResult = SensorUtils.saveResult(TEST_SAVE_RESULT, CALI_FILE);
                    if (farResult && farSaveResult) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                mPassButton.setVisibility(View.VISIBLE);
                                Toast.makeText(mContext, R.string.text_pass,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        storeRusult(true);
                        finish();
                    } else {
                        //UNISOC: modify for bug1362302, shows incorrect result test
                        final String str = ((farResult && farSaveResult) ? getResources().getString(
                                        R.string.prox_manual_tips_far_pass) : getResources()
                                        .getString(
                                                R.string.prox_manual_tips_far_fail));
                        mHandler.post(new Runnable() {
                            public void run() {
                                mTestTips.setText(str);
                            }
                        });
                    }
                    break;
            }
        }
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) == null
                || !Const.IS_SUPPORT_CALIBTATION_TEST) {
            return false;
        }
        return true;
    }
}
