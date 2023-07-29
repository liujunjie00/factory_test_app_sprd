/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.modules;

import android.content.Context;

import com.sprd.validationtools.itemstest.audio.HeadSetTest;
import com.sprd.validationtools.itemstest.audio.VibratorTest;
import com.sprd.validationtools.itemstest.audio.ReceiverTest;
import com.sprd.validationtools.itemstest.audio.PhoneLoopBackTest;
import com.sprd.validationtools.itemstest.audio.SoundTriggerTestActivity;
import com.sprd.validationtools.itemstest.backlight.BackLightTest;
import com.sprd.validationtools.itemstest.camera.CameraTestActivity;
import com.sprd.validationtools.itemstest.camera.FrontCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.FrontSecondaryCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.MacroLensCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.SecondaryCameraTestActivity;
import com.sprd.validationtools.itemstest.camera.SpwCameraTestActivity;
import com.sprd.validationtools.itemstest.charger.ChargerTest;
import com.sprd.validationtools.itemstest.charger.QuickChargerTest;
import com.sprd.validationtools.itemstest.charger.WirelessChargerTest;
import com.sprd.validationtools.itemstest.fingerprint.FingerprintTestActivity;
import com.sprd.validationtools.itemstest.fm.FMTest;
import com.sprd.validationtools.itemstest.keypad.KeyTestActivity;
import com.sprd.validationtools.itemstest.lcd.ScreenColorTest;
import com.sprd.validationtools.itemstest.led.BlueLightTest;
import com.sprd.validationtools.itemstest.led.GreenLightTest;
import com.sprd.validationtools.itemstest.led.RedLightTest;
import com.sprd.validationtools.itemstest.nfc.NFCTestActivity;
import com.sprd.validationtools.itemstest.otg.OTGTest;
import com.sprd.validationtools.itemstest.sensor.ASensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.CompassTestActivity;
import com.sprd.validationtools.itemstest.sensor.GSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.GsensorTestActivity;
import com.sprd.validationtools.itemstest.sensor.GyroscopeTestActivity;
import com.sprd.validationtools.itemstest.sensor.ProxSensorNoiseTestActivity;
import com.sprd.validationtools.itemstest.sensor.LightSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.PressureSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.MSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.MagneticTestActivity;
import com.sprd.validationtools.itemstest.sensor.PressureTestActivity;
import com.sprd.validationtools.itemstest.sensor.ProxSensorCalibrationActivity;
import com.sprd.validationtools.itemstest.sensor.PsensorTestActivity;
import com.sprd.validationtools.itemstest.sensor.LsensorTestActivity;
import com.sprd.validationtools.itemstest.sensor.SARSensorTestActivity;
import com.sprd.validationtools.itemstest.telephony.PhoneCallTestActivity;
import com.sprd.validationtools.itemstest.tp.MutiTouchTest;
import com.sprd.validationtools.itemstest.tp.SingleTouchPointTest;
import com.sprd.validationtools.itemstest.tp.SingleTouchPointTestEx;

public class FullTestItemList extends TestItemList {
    private static final String TAG = "FullTestItemList";

    /** This array of auto test items. */
    private static final String[] FILTER_CLASS_NAMES = {
            BackLightTest.class.getName(), ScreenColorTest.class.getName(),
            SingleTouchPointTest.class.getName(),
			SingleTouchPointTestEx.class.getName(),
            MutiTouchTest.class.getName(), VibratorTest.class.getName(),
            ReceiverTest.class.getName(),
            PhoneLoopBackTest.class.getName(),
            PhoneCallTestActivity.class.getName(),
            //UNSOC:Bug 1424446 The sensor calibration test should be moved before sensor test
            ASensorCalibrationActivity.class.getName(),
            GSensorCalibrationActivity.class.getName(),
            MSensorCalibrationActivity.class.getName(),
            ProxSensorCalibrationActivity.class.getName(),
            LightSensorCalibrationActivity.class.getName(),
            PressureSensorCalibrationActivity.class.getName(),
            GsensorTestActivity.class.getName(),
            CompassTestActivity.class.getName(),
            PsensorTestActivity.class.getName(),
            LsensorTestActivity.class.getName(),
            ProxSensorNoiseTestActivity.class.getName(),
            MagneticTestActivity.class.getName(),
            GyroscopeTestActivity.class.getName(),
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
            KeyTestActivity.class.getName(), ChargerTest.class.getName(),
            QuickChargerTest.class.getName(),
            WirelessChargerTest.class.getName(),
            HeadSetTest.class.getName(), FMTest.class.getName(),
            SoundTriggerTestActivity.class.getName(),
            RedLightTest.class.getName(), GreenLightTest.class.getName(),
            BlueLightTest.class.getName(),
            OTGTest.class.getName() };

    private static FullTestItemList mTestItemListInstance = null;

    public static TestItemList getInstance(Context context) {
        if (mTestItemListInstance == null) {
            mTestItemListInstance = new FullTestItemList(context);
        }
        return mTestItemListInstance;
    }

    private FullTestItemList(Context context) {
        super(context);
    }

    @Override
    public String[] getfilterClassName() {
        return FILTER_CLASS_NAMES;
    }

}
