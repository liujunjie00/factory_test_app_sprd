/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.fingerprint;

import android.util.Log;

public class NativeFingerprint {

    static {
        //Maybe cause remote UnsatisfiedLinkError.
        try {
            System.loadLibrary("jni_fingerprint");
        } catch (UnsatisfiedLinkError e) {
            Log.d("NativeFingerprint", " #loadLibrary jni_fingerprint failed  ");
            e.printStackTrace();
        }
    }

    static native public int factory_init();

    static native public int factory_exit();

    static native public int spi_test();

    static native public int deadpixel_test();

    static native public int interrupt_test();

    static native public int finger_detect();

}

