/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.fingerprint;


public interface IFactoryTestImpl {
    int factory_init();
    int factory_exit();
    int spi_test();
    int interrupt_test();
    int deadpixel_test();
    int finger_detect(ISprdFingerDetectListener listener);
}
