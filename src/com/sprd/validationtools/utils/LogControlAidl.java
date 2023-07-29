/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.utils;

import android.util.Log;
import android.os.RemoteException;
//import vendor.sprd.hardware.log.ILogControl;
import com.sprd.validationtools.nonpublic.ServiceManagerProxy;

public class LogControlAidl {
    private static final String TAG = "MMI-LogControlAidl";
//    private ILogControl mLogControlAidlService;

    public LogControlAidl() {
        try {
//            mLogControlAidlService = ILogControl.Stub.asInterface(ServiceManagerProxy.getService("vendor.sprd.hardware.log.ILogControl/default"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendCmd(String socket, String cmd) {
        try {
//            if (mLogControlAidlService == null) {
//                mLogControlAidlService = ILogControl.Stub.asInterface(ServiceManagerProxy.getService("vendor.sprd.hardware.log.ILogControl/default"));
//            }
//            if (mLogControlAidlService != null) {
//                return mLogControlAidlService.sendCmd(socket, cmd);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
