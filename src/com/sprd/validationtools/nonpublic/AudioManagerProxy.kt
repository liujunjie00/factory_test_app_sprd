/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.nonpublic

import android.content.Context
import android.media.AudioManager

open class AudioManagerProxy(context: Context) {

    private var appCtx: Context

    init {
        appCtx = context
    }

    @JvmField
    val EXTRA_VOLUME_STREAM_TYPE: String = getField("EXTRA_VOLUME_STREAM_TYPE") as String

    @JvmField
    val EXTRA_VOLUME_STREAM_VALUE: String = getField("EXTRA_VOLUME_STREAM_VALUE") as String

    @JvmField
    val VOLUME_CHANGED_ACTION: String = getField("VOLUME_CHANGED_ACTION") as String

    private var audioManager: AudioManager =
        appCtx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun getField(fieldName: String): Any {
        return AudioManager::class.java.getField(fieldName).get(null)!!
    }

    fun setParameters(keyValuePairs: String) {
        AudioManager::class.java.getMethod("setParameters", String::class.java).invoke(audioManager, keyValuePairs)
    }

    fun getParameters(keys: String) : String {
        return AudioManager::class.java.getMethod("getParameters", String::class.java)
                .invoke(audioManager, keys) as String
    }

    fun setDeviceConnectionStateForFM(device: Int, state: Int, device_address: String, device_name: String) {
        AudioManager::class.java.getMethod("setDeviceConnectionStateForFM",
                Int::class.java, Int::class.java, String::class.java, String::class.java)
                .invoke(audioManager, device, state, device_address, device_name)
    }

    fun setSpeakerphoneOn(on: Boolean) {
        AudioManager::class.java.getMethod("setSpeakerphoneOn", Boolean::class.java).invoke(audioManager, on)
    }

    fun getStreamMaxVolume(streamType: Int): Int {
        return AudioManager::class.java.getMethod("getStreamMaxVolume", Int::class.java)
            .invoke(audioManager, streamType) as Int
    }

    fun setStreamVolume(streamType: Int, index: Int, flags: Int) {
        AudioManager::class.java.getMethod("setStreamVolume", Int::class.java, Int::class.java, Int::class.java)
            .invoke(audioManager, streamType, index, flags)
    }

    fun getMode(): Int {
        return AudioManager::class.java.getMethod("getMode")
            .invoke(audioManager) as Int
    }

    fun setMode(mode: Int) {
        AudioManager::class.java.getMethod("setMode", Int::class.java)
            .invoke(audioManager, mode)
    }
}