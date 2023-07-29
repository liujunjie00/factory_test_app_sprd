/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi.itemstest.fm;

import android.util.Log;
import android.media.AudioManager;
//import android.media.UnisocAudioManager;
import com.sprd.validationtools.nonpublic.AudioManagerProxy;
import android.content.Context;

public class FmManagerSelect {
    private final Context mContext;
    private static final String TAG = "FmManagerSelect";
//    private static UnisocAudioManager mAudioManagerEx;
    private AudioManagerProxy mAudioManagerProxy;
    private static final int DEVICE_STATE_UNAVAILABLE = 0;
    private static final int DEVICE_STATE_AVAILABLE = 1;
    public static final int DEVICE_OUT_FM_HEADSET = 0x100000;

    public FmManagerSelect(Context context) {
        mContext = context;
        mAudioManagerProxy = new AudioManagerProxy(mContext);
//        mAudioManagerEx = UnisocAudioManager.getInstance(mContext);
    }

    public boolean openDev() {
        return UniFmRadioManager.getInstance().openDev();//FmNative.openDev();
    }

    public boolean closeDev() {
        return UniFmRadioManager.getInstance().closeDev();//FmNative.closeDev();
    }

    public boolean tuneRadio(float frequency) {
        return UniFmRadioManager.getInstance().tune(frequency);//FmNative.tune(frequency);
    }

    public void setMute(boolean mute) {
        int volumeMusic = mAudioManagerProxy.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mAudioManagerProxy.setStreamVolume(AudioManager.STREAM_MUSIC, volumeMusic, 0);
    }

    public boolean setAudioPathEnable(boolean enable) {
//        if (enable) {
//            mAudioManagerEx.setDeviceConnectionStateForFM(DEVICE_OUT_FM_HEADSET,
//                    DEVICE_STATE_AVAILABLE, "", "");
//        } else {
//            mAudioManagerEx.setDeviceConnectionStateForFM(DEVICE_OUT_FM_HEADSET,
//                    DEVICE_STATE_UNAVAILABLE, "", "");
//        }
        return true;
    }

    public int getFreq() {
          return -1;
    }

}