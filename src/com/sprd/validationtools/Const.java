/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import android.os.Build;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewConfiguration;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest.Key;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.Native;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Build;

public class Const {
    private static String TAG = "Const";

    public static boolean DEBUG = true;

    //This DIR while be built in (device\sprd\(BOARD)\common\rootdir\root\init.common.rc)
    public static final String PRODUCTINFO_DIR = "/mnt/vendor/productinfo";

    public static final String LED_PATH = "/sys/class/leds/red/brightness";
    public static final String CALIBRATOR_CMD = "/sys/class/sprd_sensorhub/sensor_hub/calibrator_cmd";
    public static final String CALIBRATOR_DATA = "/sys/class/sprd_sensorhub/sensor_hub/calibrator_data";
    public static final String CAMERA_FLASH = "/sys/devices/virtual/misc/sprd_flash/test";
    /*UNISOC bug 776983:Add OTG*/
    public static final String OTG_PATH = "/sys/class/dual_role_usb/sprd_dual_role_usb/supported_modes";
    /*@}*/
    public static final String OTG_PATH_k414 = "/sys/class/typec/port0/port_type";
    //UNISOC bug 1926748:support otg test in mmi for k515
    public static final String OTG_PATH_k515 = "/sys/class/typec/port1/port_type";
    public static final String LED_RED_PATH = "/sys/class/leds/sc27xx:red/brightness";
    public static final String LED_BLUE_PATH = "/sys/class/leds/sc27xx:blue/brightness";
    public static final String LED_GREEN_PATH = "/sys/class/leds/sc27xx:green/brightness";
    public static final String QUICK_CHARGER_PATH = "/sys/class/power_supply/battery/charger.0/support_fast_charge";
    public static final String WIRELESS_CHARGER_PATH_L6 = "/sys/class/power_supply/nu1619_wireless_charger";
    public static final String WIRELESS_CHARGER_PATH_N6 = "/sys/class/power_supply/sy65153_wireless_charger";

    public static final String RESULT_TEST_NAME = "result";
    public static final String INTENT_PARA_TEST_NAME = "testname";
    public static final String INTENT_PARA_TEST_INDEX = "testindex";
    public static final String INTENT_BACKGROUND_TEST_RESULT = "bgtestresult";
    public static final String INTENT_RESULT_TYPE = "resulttype";
    public static final String INTENT_PARA_TEST_CLASSNAME = "test_classname";

    public static final int RESULT_TYPE_FOR_SYSTEMTEST = 0;
    public static final int RESULT_TYPE_NORMAL = 1;

    public static final int EXT_EMULATED_PATH = 0;
    public static final int EXT_COMMON_PATH = 1;
    public static final int OTG_UDISK_PATH = 2;

    public final static int TEST_ITEM_DONE = 0;
    public static final boolean DISABLE_MSENSOR_CALI = true;

    public static final boolean IS_SUPPORT_LED_TEST = FileUtils.fileIsExists(LED_PATH);
    public static final boolean IS_SUPPORT_CALIBTATION_TEST = FileUtils.fileIsExists(CALIBRATOR_CMD);
    public static final String CAMERA_CALI_VERI = "persist.vendor.cam.multicam.cali.veri";

    // add status for test item
    public static final int FAIL = 0;
    public static final int SUCCESS = 1;
    public static final int DEFAULT = 2;

    /** add auto mmi test @{ */
    public static final int AUTO_TEST_ITEM_PASS = 257;
    public static final int AUTO_TEST_ITEM_FAIL = 258;
    public static final int AUTO_TEST_NOT_SUPPORT = 0;
    public static final int AUTO_TEST_FLAG_PASS = 1;
    public static final int AUTO_TEST_FLAG_FAIL = 2;
    public static final int startTestID = 0;
    public static final int AUTO_TEST_NUMBER = 64;
    public static final int AUTO_TEST_FINAL_RESULT_INDEX = AUTO_TEST_NUMBER - 1;
    public static final String AUTO_TEST_PATH_NAME = "/mnt/vendor/BBATtest.txt";
    public static final String BOOT_MODE_AUTOMMI = "apkmmi_auto_mode";
    public static final String HEADSET_STATE_PATH = "/sys/class/switch/h2w/state";

    public static final String LIGHT_CALIBRATOR_CMD = "/sys/class/sprd_sensorhub/sensor_hub/light_sensor_calibrator";
    public static final boolean IS_SUPPORT_LIGHT_CALIBTATION_TEST = FileUtils.fileIsExists(LIGHT_CALIBRATOR_CMD);


    public static boolean isAutoTestMode() {
        return BOOT_MODE_AUTOMMI.equals(SystemPropertiesProxy.get("ro.product.autotest", "unknow"));
        //return
    }
    /** @}*/

    public static boolean isSupportCameraCaliVeri() {
        String calibraionSupport = SystemPropertiesProxy.get(CAMERA_CALI_VERI,"0");
        Log.d(TAG, "isSupportCameraCaliVeri calibraionSupport=" + calibraionSupport);
        return false && calibraionSupport.equals("1");
    }

    public static boolean isCameraSupport() {
        int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
        CameraInfo[] mInfo = new CameraInfo[mNumberOfCameras];
        for (int i = 0; i < mNumberOfCameras; i++) {
            mInfo[i] = new CameraInfo();
            android.hardware.Camera.getCameraInfo(i, mInfo[i]);
            if (mInfo[i].facing == CameraInfo.CAMERA_FACING_BACK) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHomeSupport(Context context) {
        boolean isSupport = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        Log.d(TAG, "hw home is support:" + isSupport);
        return true;
    }

    public static boolean isBackSupport(Context context) {
        boolean isSupport = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        Log.d(TAG, "hw Back is support:" + isSupport);
        return true;
    }

    public static boolean isMenuSupport(Context context) {
        boolean isSupport = ViewConfiguration.get(context)
                .hasPermanentMenuKey();
        Log.d(TAG, "hw menu is support:" + isSupport);
        return true;
    }

    public static boolean isVolumeUpSupport() {
        boolean isSupport = SystemPropertiesProxy.getBoolean(
                "ro.config.hw.vol_up_support", true);
        Log.d(TAG, "hw VolumeUp is support:" + isSupport);
        return isSupport;
    }

    public static boolean isVolumeDownSupport() {
        boolean isSupport = SystemPropertiesProxy.getBoolean(
                "ro.config.hw.vol_down_support", true);
        Log.d(TAG, "hw VolumeDown is support:" + isSupport);
        return isSupport;
    }

    public static boolean isRefMicSupport() {
        return !SystemPropertiesProxy.getBoolean("ro.factory.remove.refmic", false);
    }

    public static boolean isSupportFeaturePhone() {
        return false;
    }

    public static boolean isSupportMeidShow() {
     /*   String board = SystemProperties.get("ro.product.name", "unknown");
        Log.d(TAG, "isSupportMeidShow board=:" + board);
        if (board != null && board.startsWith("ums312_1h10_ctcc")) {
            return true;
        }
        if (board != null && board.startsWith("ums312_2h10_ctcc")) {
            return true;
        }
        if (board != null && board.startsWith("ums312_20c10_ctcc")) {
            return true;
        }*/
        return false;//
    }
	
    public static boolean getSupportInfo(Context context, String testClassName_) {
        Class testClass = null;
        Method testClassGetMethod = null;
        try {
            testClass = Class.forName(testClassName_);
            if (testClass != null) {
                testClassGetMethod = testClass.getMethod("isSupport", Context.class);
            }
        } catch (final ClassNotFoundException e) {
            Log.d(TAG, "not found calss: "+ testClassName_);
            return false;
        } catch (final NoSuchMethodException e) {
            // if not declared "isSupport" method, then support the test item by default
             Log.d(TAG, "support the test item by default: " + testClassName_);
             return true;
        }

        if (testClassGetMethod != null) {
            try {
                return (boolean) testClassGetMethod.invoke(null, context);
            } catch (final IllegalArgumentException e) {
                Log.d(TAG, "invoke isSupport method IllegalArgumentException");
            } catch (final IllegalAccessException e) {
                Log.d(TAG, "invoke isSupport method IllegalAccessException");
            } catch (final InvocationTargetException e) {
                Log.d(TAG, "invoke isSupport method InvocationTargetException");
            }
        }
        return false;
    }
}
