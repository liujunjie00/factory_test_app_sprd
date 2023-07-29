/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.background;

import android.content.Context;

import com.sprd.validationtools.modules.TestItem;

public interface BackgroundTest {
	public static int RESULT_INVALID = -1;
	public static int RESULT_FAIL = 0;
	public static int RESULT_PASS = 1;

	public void startTest();

	public void stopTest();

	public int getResult();

	public String getResultStr();

	public int getTestItemIdx();

	public TestItem getTestItem(Context context);
}
