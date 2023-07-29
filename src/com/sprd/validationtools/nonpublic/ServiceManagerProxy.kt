/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.nonpublic

import android.os.IBinder
import android.annotation.SuppressLint

object ServiceManagerProxy {

    private const val SERVICE_MANAGER_CLASS_NAME = "android.os.ServiceManager"

    private lateinit var serviceManagerPropClass: Class<*>

    init {
        init()
    }

    @SuppressLint("PrivateApi")
    fun init() {
        serviceManagerPropClass = Class.forName(SERVICE_MANAGER_CLASS_NAME)
    }

    @JvmStatic
    fun waitForDeclaredService(name: String): IBinder {
        return serviceManagerPropClass.getMethod("waitForDeclaredService", String::class.java)
        .invoke(null, name) as IBinder
    }

    @JvmStatic
    fun getService(name: String): IBinder {
        return serviceManagerPropClass.getMethod("getService", String::class.java)
        .invoke(null, name) as IBinder
    }
}