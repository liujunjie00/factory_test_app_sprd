/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.nonpublic

import android.app.ActivityManager
import android.content.Context
import com.sprd.validationtools.appCtx

fun ActivityManager_forceStopPackage(packageName: String) {
    val am = appCtx.getSystemService(Context.ACTIVITY_SERVICE);
    ActivityManager::class.java.getMethod("forceStopPackage", String::class.java).invoke(am, packageName)
}
