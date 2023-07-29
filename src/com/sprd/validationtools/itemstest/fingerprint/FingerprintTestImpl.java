/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.fingerprint;

import android.os.RemoteException;
import android.util.Log;
//import vendor.sprd.hardware.fingerprintmmi.IFingerprintmmi;
import com.sprd.validationtools.itemstest.fingerprint.IFactoryTestImpl;
import com.sprd.validationtools.itemstest.fingerprint.ISprdFingerDetectListener;
import com.sprd.validationtools.nonpublic.ServiceManagerProxy;

public class FingerprintTestImpl implements IFactoryTestImpl {

    private static final String TAG = "MMI-FingerprintTestImpl";
//    private IFingerprintmmi mAidlService = null;
//    private vendor.sprd.hardware.fingerprintmmi.V1_0.IFingerprintmmi mHidlService = null;
    private enum FingerMmiStatus { IDLE, RUNNING, EXIT };
    FingerMmiStatus status = FingerMmiStatus.IDLE;

    private void getFingerprintService() {
//        try {
//            mAidlService = IFingerprintmmi.Stub.asInterface(ServiceManagerProxy.waitForDeclaredService("vendor.sprd.hardware.fingerprintmmi.IFingerprintmmi/default"));
//            if (mAidlService == null) {
//                mHidlService = vendor.sprd.hardware.fingerprintmmi.V1_0.IFingerprintmmi.getService(true);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        Log.d(TAG, "mAidlService =" + mAidlService + "\n" + "mHidlService =" + mHidlService);
    }

    @Override
    public int factory_init() {
        int ret = -1;
        status = FingerMmiStatus.RUNNING;
        synchronized(FingerprintTestImpl.class) {
//            getFingerprintService();
//            try {
//                if (mAidlService != null) {
//                    ret = mAidlService.factory_init();
//                } else if (mHidlService != null) {
//                    ret = mHidlService.factory_init();
//                }
//            } catch (RemoteException e) {
//                Log.e(TAG, "Failed to do factory_init", e);
//            }
        }
        return ret;
    }

    @Override
    public int factory_exit() {
        // TODO Auto-generated method stub
        int ret = -1;
        if (status != FingerMmiStatus.RUNNING){
            status = FingerMmiStatus.IDLE;
            Log.d(TAG, "factory_exit no finger test,return");
            return -1;
        }
        status = FingerMmiStatus.EXIT;
        synchronized(FingerprintTestImpl.class) {
//            try {
//                if (mAidlService != null) {
//                    ret = mAidlService.factory_exit();
//                } else if (mHidlService != null) {
//                    ret = mHidlService.factory_exit();
//                }
//            } catch (RemoteException e) {
//                Log.e(TAG, "Failed to do factory_exit", e);
//            }
        }
        status = FingerMmiStatus.IDLE;
        return ret;
    }

    @Override
    public int spi_test() {
        // TODO Auto-generated method stub
        int ret = -1;
        synchronized(FingerprintTestImpl.class) {
//            try {
//                if (mAidlService != null) {
//                    ret = mAidlService.spi_test();
//                } else if (mHidlService != null) {
//                    ret = mHidlService.spi_test();
//                }
//            } catch (RemoteException e) {
//                Log.e(TAG, "Failed to do spi_test", e);
//            }
        }
        return ret;
    }

    @Override
    public int interrupt_test() {
        // TODO Auto-generated method stub
        int ret = -1;
        synchronized(FingerprintTestImpl.class) {
//            try {
//                if (mAidlService != null) {
//                    ret = mAidlService.interrupt_test();
//                } else if (mHidlService != null) {
//                    ret = mHidlService.interrupt_test();
//                }
//            } catch (RemoteException e) {
//                Log.e(TAG, "Failed to do interrupt_test", e);
//            }
        }
        return ret;
    }

    @Override
    public int deadpixel_test() {
        // TODO Auto-generated method stub
        int ret = -1;
        synchronized(FingerprintTestImpl.class) {
//            try {
//                if (mAidlService != null) {
//                    ret = mAidlService.deadpixel_test();
//                } else if (mHidlService != null) {
//                    ret = mHidlService.deadpixel_test();
//                }
//            } catch (RemoteException e) {
//                Log.e(TAG, "Failed to do deadpixel_test", e);
//            }
        }
        return ret;
    }

    @Override
    public int finger_detect(ISprdFingerDetectListener listener) {
        // TODO Auto-generated method stub
        int ret = -1;
        synchronized(FingerprintTestImpl.class) {
            int times = 100;
//            try {
                if (listener != null) {
                    while (times > 0 && (status != FingerMmiStatus.EXIT)) {
//                        if (mAidlService != null) {
//                            ret = mAidlService.finger_detect();
//                        } else if (mHidlService != null) {
//                            ret = mHidlService.finger_detect();
//                        }
                        if (ret == 0) {
                            break;
                        }
                        times--;
                        Log.d(TAG, "not detect fingerprint, try again... ret = " + ret);
                        try {
                            Thread.sleep(100); //delay 100ms to try again
                        } catch (InterruptedException e) {

                        }
                    }

                    if (ret != 0) {
                        Log.d(TAG, "finger_detect several times but failed");
                    }
                    listener.on_finger_detected(ret);
                }
//            } catch (RemoteException e) {
//                Log.e(TAG, "Failed to do finger_detect()", e);
//            }
        }
        return ret;
    }

}
