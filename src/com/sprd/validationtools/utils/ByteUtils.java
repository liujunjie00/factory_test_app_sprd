/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.utils;

public class ByteUtils {

    private static final String TAG = "ByteUtils";

    public static String intToBinary32(int i, int bitNum) {
        String binaryStr = Integer.toBinaryString(i);
        while (binaryStr.length() < bitNum) {
            binaryStr = "0" + binaryStr;
        }
        return binaryStr;
    }
}
