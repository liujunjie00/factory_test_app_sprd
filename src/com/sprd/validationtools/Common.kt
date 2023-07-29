/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools

import android.annotation.SuppressLint
import android.content.Context

/**
 *  It's OK here to keep the static context because we only keep application context
 */
@SuppressLint("StaticFieldLeak")
lateinit var appCtx: Context