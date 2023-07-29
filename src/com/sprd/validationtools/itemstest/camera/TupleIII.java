/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.camera;

public class TupleIII<A, B, C> extends Tuple<A, B> {

    public final C third;

    public TupleIII(A first, B second, C third) {
        super(first, second);
        this.third = third;
    }
}
