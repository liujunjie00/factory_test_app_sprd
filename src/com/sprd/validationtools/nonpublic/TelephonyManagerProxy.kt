/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.nonpublic

import android.content.Context
import android.telephony.ServiceState
import android.telephony.TelephonyManager

open class TelephonyManagerProxy(context: Context) {

    private var appCtx: Context

    init {
        appCtx = context
    }

    @JvmField
    val NETWORK_TYPE_LTE_CA: Int =
        TelephonyManager::class.java.getField("NETWORK_TYPE_LTE_CA").get(null) as Int

    private var telephonyManagerObject: TelephonyManager =
            appCtx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    fun getTelephonyManager(): TelephonyManager {
        return telephonyManagerObject
    }

    fun getService(): TelephonyManager {
       return telephonyManagerObject
    }

    fun getPhoneCount(): Int {
        return TelephonyManager::class.java.getMethod("getPhoneCount")
            .invoke(telephonyManagerObject) as Int
    }

    fun getSimState(simIdx: Int): Int {
        return TelephonyManager::class.java.getMethod("getSimState", Int::class.java)
            .invoke(telephonyManagerObject, simIdx) as Int
    }

    fun getPreferredNetworkType(subId: Int): Int {
        return TelephonyManager::class.java.getMethod("getPreferredNetworkType", Int::class.java)
            .invoke(telephonyManagerObject, subId) as Int
    }

    fun setNetworkRoaming(isRoaming: Boolean) {
        TelephonyManager::class.java.getMethod("setNetworkRoaming", Boolean::class.java)
            .invoke(telephonyManagerObject, isRoaming) as Int
    }

    fun isSimExist(simIndex: Int): Boolean {
        return getSimState(simIndex) == TelephonyManager.SIM_STATE_READY
    }

    fun getNetworkType(subId: Int): Int {
        return TelephonyManager::class.java.getMethod("getNetworkType", Int::class.java)
            .invoke(telephonyManagerObject, subId) as Int
    }

    fun getNetworkTypeName(type: Int): String {
        return TelephonyManager::class.java.getMethod("getNetworkTypeName", Int::class.java)
            .invoke(telephonyManagerObject, type) as String
    }

    fun getVoiceNetworkType(subId: Int): Int {
        return TelephonyManager::class.java.getMethod("getVoiceNetworkType", Int::class.java)
            .invoke(telephonyManagerObject, subId) as Int
    }

    fun getNetworkOperatorName(subId: Int): String {
        return TelephonyManager::class.java.getMethod("getNetworkOperatorName", Int::class.java)
            .invoke(telephonyManagerObject, subId) as String
    }

    fun getNetworkOperatorForPhone(phoneId: Int): String {
        return TelephonyManager::class.java.getMethod("getNetworkOperatorForPhone", Int::class.java)
            .invoke(telephonyManagerObject, phoneId) as String
    }

    fun isNetworkRoaming(subId: Int): Boolean {
        return TelephonyManager::class.java.getMethod("isNetworkRoaming", Int::class.java)
            .invoke(telephonyManagerObject, subId) as Boolean
    }

    fun hasIccCard(slotIndex: Int): Boolean {
        return TelephonyManager::class.java.getMethod("hasIccCard", Int::class.java)
            .invoke(telephonyManagerObject, slotIndex) as Boolean
    }

    fun isMultiSimEnabled(): Boolean {
        return TelephonyManager::class.java.getMethod("isMultiSimEnabled")
            .invoke(telephonyManagerObject) as Boolean
    }

    fun getCurrentPhoneTypeForSlot(slotIndex: Int): Int {
        return TelephonyManager::class.java.getMethod("getCurrentPhoneTypeForSlot", Int::class.java)
            .invoke(telephonyManagerObject, slotIndex) as Int
    }

    fun getSubscriberId(subId: Int): String? {
        return TelephonyManager::class.java.getMethod("getSubscriberId", Int::class.java)
            .invoke(telephonyManagerObject, subId) as String?
    }

    fun getSimOperatorNumericForPhone(phoneId: Int): String {
        return TelephonyManager::class.java.getMethod("getSimOperatorNumericForPhone", Int::class.java)
            .invoke(telephonyManagerObject, phoneId) as String
    }

    fun getSimOperatorNameForPhone(phoneId: Int): String {
        return TelephonyManager::class.java.getMethod("getSimOperatorNameForPhone", Int::class.java)
            .invoke(telephonyManagerObject, phoneId) as String
    }

    fun getCurrentPhoneType(subId: Int): Int {
        return TelephonyManager::class.java.getMethod("getCurrentPhoneType", Int::class.java)
            .invoke(telephonyManagerObject, subId) as Int
    }

    fun getPhoneType(networkMode: Int): Int {
        return TelephonyManager::class.java.getMethod("getPhoneType", Int::class.java)
            .invoke(telephonyManagerObject, networkMode) as Int
    }

    fun  getServiceStateForSubscriber(subId: Int): ServiceState? {
        return TelephonyManager::class.java.getMethod("getServiceStateForSubscriber", Int::class.java)
            .invoke(telephonyManagerObject, subId) as ServiceState?
    }

    fun getCdmaPrlVersion(subId: Int): String? {
        return TelephonyManager::class.java.getMethod("getCdmaPrlVersion", Int::class.java)
            .invoke(telephonyManagerObject, subId) as String?
    }

    fun getEsn(): String? {
        return TelephonyManager::class.java.getMethod("getEsn")
            .invoke(telephonyManagerObject) as String?
    }

    fun getSimCountryIsoForPhone(phoneId: Int): String? {
        return TelephonyManager::class.java.getMethod("getSimCountryIsoForPhone", Int::class.java)
            .invoke(null, phoneId) as String?
    }

    fun getLine1Number(subId: Int): String? {
        return TelephonyManager::class.java.getMethod("getLine1Number", Int::class.java)
            .invoke(telephonyManagerObject, subId) as String?
    }

    fun getSimSerialNumber(subId: Int): String? {
        return TelephonyManager::class.java.getMethod("getSimSerialNumber", Int::class.java)
            .invoke(telephonyManagerObject, subId) as String?
    }
}