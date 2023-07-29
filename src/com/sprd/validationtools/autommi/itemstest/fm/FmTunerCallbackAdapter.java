/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi.itemstest.fm;

import android.hardware.broadcastradio.V2_0.ITunerCallback;
import android.hardware.broadcastradio.V2_0.ProgramInfo;
import android.hardware.broadcastradio.V2_0.ProgramListChunk;
import android.hardware.broadcastradio.V2_0.ProgramSelector;
import android.hardware.broadcastradio.V2_0.VendorKeyValue;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;

import java.util.ArrayList;

/**
 * Implements the ITunerCallback interface by forwarding calls to RadioTuner.Callback.
 */
class FmTunerCallbackAdapter extends ITunerCallback.Stub {
    private static final String TAG = "FmTunerCallbackAdapter";

    private final Handler mHandler;
    private FmCallback mCallback;

    FmTunerCallbackAdapter(FmCallback callback, Handler handler) {
        mCallback = callback;
        if (handler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        } else {
            mHandler = handler;
        }
    }

    @Override
    public void onTuneFailed(int i, ProgramSelector programSelector) throws RemoteException {
        mHandler.post(() -> mCallback.onTuneFailed(i, programSelector));
    }

    @Override
    public void onCurrentProgramInfoChanged(ProgramInfo programInfo) throws RemoteException {
        mHandler.post(() -> mCallback.onCurrentProgramInfoChanged(programInfo));
    }

    @Override
    public void onProgramListUpdated(ProgramListChunk programListChunk) throws RemoteException {
        mHandler.post(() -> mCallback.onProgramListUpdated(programListChunk));
    }

    @Override
    public void onAntennaStateChange(boolean b) throws RemoteException {
        mHandler.post(() -> mCallback.onAntennaStateChange(b));
    }

    @Override
    public void onParametersUpdated(ArrayList<VendorKeyValue> arrayList) throws RemoteException {
        mHandler.post(() -> mCallback.onParametersUpdated(arrayList));
    }
}