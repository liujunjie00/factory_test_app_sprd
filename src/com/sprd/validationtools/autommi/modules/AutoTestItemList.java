/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi.modules;

import android.content.Context;

import com.sprd.validationtools.autommi.itemstest.sensor.GsensorAutoTestActivity;
import com.sprd.validationtools.autommi.itemstest.sensor.GyroscopeAutoTestActivity;
import com.sprd.validationtools.autommi.itemstest.sensor.LsensorAutoTestActivity;
import com.sprd.validationtools.autommi.itemstest.sensor.MagneticAutoTestActivity;
import com.sprd.validationtools.autommi.itemstest.sensor.PsensorAutoTestActivity;
import com.sprd.validationtools.autommi.itemstest.fm.AutoFMTest;
import com.sprd.validationtools.itemstest.audio.HeadSetTest;
import com.sprd.validationtools.itemstest.audio.MainMicTest;
import com.sprd.validationtools.itemstest.audio.SpeakerTest;
import com.sprd.validationtools.itemstest.audio.VibratorTest;
import com.sprd.validationtools.itemstest.audio.ReceiverTest;
import com.sprd.validationtools.itemstest.audio.PhoneLoopBackTest;
import com.sprd.validationtools.itemstest.audio.SmartPATest;
import com.sprd.validationtools.itemstest.audio.SoundTriggerTestActivity;
import com.sprd.validationtools.itemstest.backlight.BackLightTest;
import com.sprd.validationtools.itemstest.bt.BluetoothTestActivity;
import com.sprd.validationtools.itemstest.camera.CameraTestActivity;
import com.sprd.validationtools.itemstest.camera.FlashTestActivity;
import com.sprd.validationtools.itemstest.camera.FrontCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.FrontSecondaryCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.MacroLensCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.SecondaryCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.SpwCameraTestActivity;
import com.sprd.validationtools.itemstest.charger.ChargerTest;
import com.sprd.validationtools.itemstest.fingerprint.FingerprintTestActivity;
import com.sprd.validationtools.itemstest.gps.GpsTestActivity;
import com.sprd.validationtools.itemstest.keypad.KeyTestActivity;
import com.sprd.validationtools.itemstest.lcd.ScreenColorTest;
import com.sprd.validationtools.itemstest.led.BlueLightTest;
import com.sprd.validationtools.itemstest.led.GreenLightTest;
import com.sprd.validationtools.itemstest.led.RedLightTest;
import com.sprd.validationtools.itemstest.nfc.NFCTestActivity;
import com.sprd.validationtools.itemstest.otg.OTGTest;
import com.sprd.validationtools.itemstest.rtc.RTCTest;
import com.sprd.validationtools.itemstest.sensor.ASensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.GSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.GsensorTestActivity;
import com.sprd.validationtools.itemstest.sensor.LightSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.PressureSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.ProxSensorNoiseTestActivity;
import com.sprd.validationtools.itemstest.sensor.MSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.PressureTestActivity;
import com.sprd.validationtools.itemstest.sensor.ProxSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.SARSensorTestActivity;
import com.sprd.validationtools.itemstest.storage.SDCardTest;
import com.sprd.validationtools.itemstest.storage.StorageTest;
import com.sprd.validationtools.itemstest.sysinfo.SystemVersionTest;
import com.sprd.validationtools.itemstest.telephony.PhoneCallTestActivity;
import com.sprd.validationtools.itemstest.telephony.SIMCardTestActivity;
import com.sprd.validationtools.itemstest.tp.MutiTouchTest;
import com.sprd.validationtools.itemstest.tp.SingleTouchPointTest;
import com.sprd.validationtools.itemstest.wifi.WifiTestActivity;
import com.sprd.validationtools.modules.TestItemList;

public class AutoTestItemList extends TestItemList {
    private static final String TAG = "AutoTestItemList";

    /** This array of auto test items. */
    private static final String[] FILTER_CLASS_NAMES = new String[]{
            SystemVersionTest.class.getName(),
            /*RTCTest.class.getName(),*/
            BackLightTest.class.getName(),
            ScreenColorTest.class.getName(),
            SingleTouchPointTest.class.getName(),
            MutiTouchTest.class.getName(),
            SpeakerTest.class.getName(),
            MainMicTest.class.getName(),
            FrontCameraTestActivity.class.getName(),
            CameraTestActivity.class.getName(),
            FlashTestActivity.class.getName(),
            KeyTestActivity.class.getName(),
            ChargerTest.class.getName(),
            BluetoothTestActivity.class.getName(),
            WifiTestActivity.class.getName(),
            StorageTest.class.getName(),
            SDCardTest.class.getName(),
            OTGTest.class.getName()

            /* SmartPATest.class.getName(), VibratorTest.class.getName(),*/
            /*ReceiverTest.class.getName(),*/
            /*PhoneLoopBackTest.class.getName(), PhoneCallTestActivity.class.getName(),*/
            /*ASensorCalibrationActivity.class.getName(),
            MSensorCalibrationActivity.class.getName(),
            GSensorCalibrationActivity.class.getName(),
            ProxSensorCalibrationActivity.class.getName(),
            LightSensorCalibrationActivity.class.getName(),
            PressureSensorCalibrationActivity.class.getName(),
            GsensorAutoTestActivity.class.getName(),
            PsensorAutoTestActivity.class.getName(),
            LsensorAutoTestActivity.class.getName(),
            ProxSensorNoiseTestActivity.class.getName(),
            MagneticAutoTestActivity.class.getName(),
            GyroscopeAutoTestActivity.class.getName(),
            PressureTestActivity.class.getName(),
            SARSensorTestActivity.class.getName(),
            NFCTestActivity.class.getName(),
            FrontSecondaryCameraTestActivity.class.getName(),
            SecondaryCameraTestActivity.class.getName(),
            FrontCameraTestActivity.class.getName(),
            CameraTestActivity.class.getName(),
            SpwCameraTestActivity.class.getName(),
            MacroLensCameraTestActivity.class.getName(),
            FingerprintTestActivity.class.getName(),
            KeyTestActivity.class.getName(),
            ChargerTest.class.getName(),
            HeadSetTest.class.getName(),
            AutoFMTest.class.getName(),
            SoundTriggerTestActivity.class.getName(),
            RedLightTest.class.getName(),
            GreenLightTest.class.getName(),
            BlueLightTest.class.getName(),
            BluetoothTestActivity.class.getName(),
            WifiTestActivity.class.getName(),
            GpsTestActivity.class.getName(),
            SIMCardTestActivity.class.getName(),
            SDCardTest.class.getName()*/
        };

    private static AutoTestItemList mTestItemListInstance = null;

    public static TestItemList getInstance(Context context) {
        if (mTestItemListInstance == null) {
            mTestItemListInstance = new AutoTestItemList(context);
        }
        return mTestItemListInstance;
    }

    private AutoTestItemList(Context context) {
        super(context);
    }

    @Override
    public String[] getfilterClassName() {
        return FILTER_CLASS_NAMES;
    }

}