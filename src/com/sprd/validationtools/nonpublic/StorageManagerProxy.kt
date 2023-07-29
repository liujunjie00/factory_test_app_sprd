/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.nonpublic

import android.content.Context
import android.os.storage.StorageManager
import com.sprd.validationtools.appCtx

object StorageManagerProxy {

    private var storageManagerObject: StorageManager =
        appCtx.getSystemService(Context.STORAGE_SERVICE) as StorageManager


    @JvmStatic
    fun getPrimaryStorageSize(): Long {
        return StorageManager::class.java.getMethod("getPrimaryStorageSize")
            .invoke(storageManagerObject) as Long
    }
}