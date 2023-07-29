/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi.itemstest.fm;

import android.hardware.broadcastradio.V2_0.IBroadcastRadio;
import android.hardware.broadcastradio.V2_0.ITunerSession;
import android.hardware.broadcastradio.V2_0.ProgramSelector;
import android.hardware.broadcastradio.V2_0.VendorKeyValue;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.hardware.radio.RadioManager;

import java.util.ArrayList;

//static_libs: "android.hardware.broadcastradio-V2.0-java"
public class UniFmRadioManager {
    public static final String TAG = "UniFmRadioManager";
    private static UniFmRadioManager sInstance;
    private IBroadcastRadio mIBroadcastRadio;
    private ITunerSession mITunerSession;
    private FmCallback mCallback = new FmCallback();
    private FmTunerCallbackAdapter mFmTunerCallbackAdapter;
    private final Handler mHandler = new Handler(Looper.getMainLooper());;

    private IBroadcastRadio.openSessionCallback mOpenSessionCallback = new IBroadcastRadio.openSessionCallback() {
        public void onValues(int result, ITunerSession session) {
            Log.d(TAG, "openSessionCallback onValues " + result);
            mITunerSession = session;
        }
    };

    public UniFmRadioManager() {
    }

    public static UniFmRadioManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }

        sInstance = new UniFmRadioManager();
        return sInstance;
    }

    /**
     * Open FM device, call before power up
     *
     * @return (true,success; false, failed)
     */
    public boolean openDev() {
        Log.d(TAG, "openDev begin!");
        try {
            mIBroadcastRadio = IBroadcastRadio.getService();
            mFmTunerCallbackAdapter = new FmTunerCallbackAdapter(mCallback, mHandler);
            mIBroadcastRadio.openSession(mFmTunerCallbackAdapter, mOpenSessionCallback);
        } catch (RemoteException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "openDev end!");
        return true;
    }

    /**
     * Close FM device, call after power down
     *
     * @return (true, success; false, failed)
     */
    public boolean closeDev() {
        Log.d(TAG, "closeDev begin!");
        if (mITunerSession == null) {
            return false;
        }

        try {
            mITunerSession.close();
        }  catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        Log.d(TAG, "closeDev end!");
        return true;
    }

    /**
     * tune to frequency
     *
     * @param frequency frequency(50KHZ, 87.55; 100KHZ, 87.5)
     *
     * @return (true, success; false, failed)
     */
    public boolean tune(float frequency) {
        Log.d(TAG, "tuneRadio begin!");
        int result = 0;
        ProgramSelector programSelector = new ProgramSelector();
        programSelector.primaryId.type = 1;
        programSelector.primaryId.value = (long) (frequency * 1000);
        if(mITunerSession != null) {
            try {
                result = mITunerSession.tune(programSelector);
                if (result == RadioManager.STATUS_INVALID_OPERATION) {
                    openDev();
                    result = mITunerSession.tune(programSelector);
                }
            } catch (RemoteException e) {
                return false;
            }
        }
        Log.d(TAG, "Tuning to station: " + frequency + " result: " + result);
        return result == RadioManager.STATUS_OK;
    }

    public int getRssi() {
        return getParm("rssi");
    }

    public int getSnr() {
        return getParm("snr");
    }

    private int getParm(String key) {
        Log.d(TAG, "getParm begin! key " + key);
        int result = 0;
        ArrayList<String> keylist = new ArrayList<>();
        ArrayList<VendorKeyValue> resultMap;
        keylist.add(key);
        try {
            resultMap = mITunerSession.getParameters(keylist);
            result = Integer.parseInt(resultMap.get(0).value);
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }
        Log.d(TAG, "getParm end! result " + result);
        return result;
    }
}