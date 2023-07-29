/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.nonpublic

import android.annotation.SuppressLint

object AudioSystemProxy {

    private const val AS_CLASS_NAME = "android.media.AudioSystem"
    private lateinit var asClass: Class<*>

    init {
        init()
    }

    @SuppressLint("PrivateApi")
    fun init() {
        try {
            asClass = Class.forName(AS_CLASS_NAME)
        } catch (e: ClassNotFoundException) {
            println(e.printStackTrace())
        }
    }

    @JvmField
    val DEVICE_STATE_UNAVAILABLE: Int = asClass.getField("DEVICE_STATE_UNAVAILABLE").get(null) as Int

    @JvmField
    val DEVICE_STATE_AVAILABLE: Int = asClass.getField("DEVICE_STATE_AVAILABLE").get(null) as Int

    @JvmField
    val FORCE_SPEAKER: Int = asClass.getField("FORCE_SPEAKER").get(null) as Int

    @JvmField
    val FORCE_NONE: Int = asClass.getField("FORCE_NONE").get(null) as Int

    @JvmField
    val STREAM_MUSIC: Int = asClass.getField("STREAM_MUSIC").get(null) as Int

    @JvmField
    val STREAM_VOICE_CALL: Int = asClass.getField("STREAM_VOICE_CALL").get(null) as Int

    @JvmField
    val MODE_IN_CALL: Int = asClass.getField("MODE_IN_CALL").get(null) as Int

    @JvmField
    val DEVICE_OUT_EARPIECE: Int = asClass.getField("DEVICE_OUT_EARPIECE").get(null) as Int

    @JvmField
    val DEVICE_IN_BACK_MIC: Int = asClass.getField("DEVICE_IN_BACK_MIC").get(null) as Int

    @JvmStatic
    fun setDeviceConnectionState(device: Int, state: Int, device_address: String, device_name: String): Int {
        return asClass. getMethod("setDeviceConnectionState",
            Int::class.java, Int::class.java, String::class.java, String::class.java)
            .invoke(null, device, state, device_address, device_name) as Int
    }

    @JvmStatic
    fun getDeviceConnectionState(device: Int, device_address: String): Int {
        return asClass. getMethod("getDeviceConnectionState", Int::class.java, String::class.java)
            .invoke(null, device, device_address) as Int
    }

    @JvmStatic
    fun setForceUse(usage: Int, config: Int): Int {
        return asClass. getMethod("setForceUse", Int::class.java, Int::class.java)
            .invoke(null, usage, config) as Int
    }

}