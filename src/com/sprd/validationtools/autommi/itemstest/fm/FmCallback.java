/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.autommi.itemstest.fm;

import android.hardware.broadcastradio.V2_0.ProgramInfo;
import android.hardware.broadcastradio.V2_0.ProgramListChunk;
import android.hardware.broadcastradio.V2_0.ProgramSelector;
import android.hardware.broadcastradio.V2_0.VendorKeyValue;
import java.util.ArrayList;

public class FmCallback {
    public void onTuneFailed(int i, ProgramSelector programSelector) {}

    public void onCurrentProgramInfoChanged(ProgramInfo programInfo) {}

    public void onProgramListUpdated(ProgramListChunk programListChunk) {}

    public void onAntennaStateChange(boolean b) {}

    public void onParametersUpdated(ArrayList<VendorKeyValue> arrayList) {}
}