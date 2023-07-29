/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.view.View;
import android.widget.Toast;
import android.widget.RadioButton;
import android.provider.Settings;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.utils.IATUtils;
import com.sprd.validationtools.utils.Native;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.nonpublic.AudioManagerProxy;
import com.sprd.validationtools.nonpublic.AudioSystemProxy;
import com.sprd.validationtools.R;

public class PhoneLoopBackTest extends BaseActivity {
    private static final String TAG = "PhoneLoopBackTest";
    public byte mPLBTestFlag[] = new byte[1];
    public Handler mUihandler = new Handler();
    private RadioButton mRadioSpeaker = null;
    private RadioButton mRadioReceiver = null;
    private RadioButton mRadio3Receiver = null;
    private static final int LOOPBACK_NONE = 0;
    private static final int LOOPBACK_SPEAKER = 1;
    private static final int LOOPBACK_RECEIVER = 2;
    private static final int LOOPBACK_MIC3_RECEIVER = 3;
    private static final int LOOPBACK_PASS = 4;
    private int mCurLoopback = LOOPBACK_NONE;
    private AudioManagerProxy mAudioManagerProxy = null;

    private Object mLock = new Object();

    private boolean mAudioWhaleHalFlag = false;
    private boolean mPendingPaused = false;
    private boolean mRadioOpenSuccess = false;
    private boolean mIsRefMicSupport = Const.isRefMicSupport();

    private boolean mSavedSoundEffect = false;
    private boolean mSavedLockSound = false;
    private boolean mAux3DeviceSupport = false;

    private Handler mPhoneLoopBackHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOOPBACK_RECEIVER:
                    switchLoopback(LOOPBACK_RECEIVER);
                    if (mAux3DeviceSupport) {
                        mPhoneLoopBackHandler.sendEmptyMessageDelayed(LOOPBACK_MIC3_RECEIVER, 3000);
                    } else {
                        mPhoneLoopBackHandler.sendEmptyMessageDelayed(LOOPBACK_PASS, 3000);
                    }
                    break;
                case LOOPBACK_MIC3_RECEIVER:
                    switchLoopback(LOOPBACK_MIC3_RECEIVER);
                    mPhoneLoopBackHandler.sendEmptyMessageDelayed(LOOPBACK_PASS, 3000);
                    break;
                case LOOPBACK_PASS:
                    rollbackMmiAudio(mCurLoopback);
                    storeRusult(true);
                    finish();
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.micphone_test);
        setTitle(R.string.phone_loopback_test);
        mAudioManagerProxy = new AudioManagerProxy(getApplicationContext());
        mRadioSpeaker = (RadioButton) findViewById(R.id.radio_speaker);
        mRadioReceiver = (RadioButton) findViewById(R.id.radio_earpiece);
        mRadio3Receiver = (RadioButton) findViewById(R.id.radio3_earpiece);
        mRadioSpeaker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchLoopback(LOOPBACK_SPEAKER);
            }
        });
        mRadioReceiver.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchLoopback(LOOPBACK_RECEIVER);
            }
        });
        /* Bug 1687037 Add a third MIC*/
        mRadio3Receiver.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switchLoopback(LOOPBACK_MIC3_RECEIVER);
            }
        });
        if (AudioSystemProxy.DEVICE_STATE_AVAILABLE != AudioSystemProxy
                .getDeviceConnectionState(AudioSystemProxy.DEVICE_OUT_EARPIECE, "")) {
            mRadioReceiver.setVisibility(View.GONE);
        }
        /*SPRD bug 850400:Aux device check.*/
        boolean mAuxDeviceSupport = true;
        if (AudioSystemProxy.DEVICE_STATE_AVAILABLE != AudioSystemProxy
                .getDeviceConnectionState(AudioSystemProxy.DEVICE_IN_BACK_MIC, "")) {
            mAuxDeviceSupport = false;
        }
        Log.d(TAG, "onCreate mAuxDeviceSupport=" + mAuxDeviceSupport);
        /* Bug 1687037 Add a third MIC*/
        boolean mAux3DeviceSupport = false;
        String result = mAudioManagerProxy.getParameters("SubMic2");
        Log.d(TAG, "getParameters result=" + result);
        if (result != null &&result.contains("SubMic2=1")) {
            mAux3DeviceSupport = true;
        }
        Log.d(TAG, "onCreate mAux3DeviceSupport=" + mAux3DeviceSupport);
        mAudioWhaleHalFlag = ValidationToolsUtils.isSupportAGDSP(getApplicationContext());
        Log.d(TAG, "onCreate mAudioWhaleHalFlag=" + mAudioWhaleHalFlag);
        mRadioReceiver.setVisibility(mIsRefMicSupport && mAuxDeviceSupport ? View.VISIBLE : View.GONE);
        mRadio3Receiver.setVisibility(mIsRefMicSupport && mAux3DeviceSupport ? View.VISIBLE : View.GONE);
        /*@}*/
        mSavedSoundEffect = ValidationToolsUtils.setSoundEffect(this, false);
        mSavedLockSound = ValidationToolsUtils.setLockSound(this, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPendingPaused = false;
        startMmiAudio(mCurLoopback);
    }

    @Override
    protected void onPause() {
        if(mRadioOpenSuccess) {
            rollbackMmiAudio(mCurLoopback);
        }
        mPendingPaused = true;
        super.onPause();
    }

    private void switchLoopback(int loopbackType) {
        Log.i("PhoneLoopBackTest",
                "=== create thread to execute PhoneLoopBackTest switch command! ===");
        mRadioReceiver.setEnabled(false);
        mRadio3Receiver.setEnabled(false);
        mRadioSpeaker.setEnabled(false);
        if (!mAudioWhaleHalFlag) {
            if (loopbackType == LOOPBACK_RECEIVER) {
                new Thread() {
                    public void run() {
                        String result = IATUtils.sendAtCmd("AT+SSAM=0");
                        setInMac(LOOPBACK_RECEIVER);
                        if (result.contains(IATUtils.AT_OK)) {
                            mUihandler.post(new Runnable() {
                                public void run() {
                                    mRadioSpeaker.setEnabled(true);
                                    mCurLoopback = LOOPBACK_RECEIVER;
                                    mRadioReceiver.setChecked(true);
                                    mRadioOpenSuccess = true;
                                    if(mPendingPaused) {
                                        rollbackMmiAudio(mCurLoopback);
                                    } else {
                                        Toast.makeText(
                                                PhoneLoopBackTest.this,
                                                R.string.receiver_loopback_success,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            mUihandler.post(new Runnable() {
                                public void run() {
                                    if(!mPendingPaused) {
                                        Toast.makeText(
                                                PhoneLoopBackTest.this,
                                                R.string.receiver_loopback_fail,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }.start();
            } else {
                new Thread() {
                    public void run() {
                        String result = IATUtils.sendAtCmd("AT+SSAM=1");
                        setInMac(LOOPBACK_SPEAKER);
                        if (result.contains(IATUtils.AT_OK)) {
                            mUihandler.post(new Runnable() {
                                public void run() {
                                    mRadioReceiver.setEnabled(true);
                                    mCurLoopback = LOOPBACK_SPEAKER;
                                    mRadioSpeaker.setChecked(true);
                                    mRadioOpenSuccess = true;
                                    if(mPendingPaused) {
                                        rollbackMmiAudio(mCurLoopback);
                                    } else {
                                        Toast.makeText(
                                                PhoneLoopBackTest.this,
                                                R.string.speaker_loopback_success,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            mUihandler.post(new Runnable() {
                                public void run() {
                                    if(!mPendingPaused) {
                                        Toast.makeText(PhoneLoopBackTest.this,
                                                R.string.speaker_loopback_fail,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                }.start();
            }
        } else {
            if (loopbackType == LOOPBACK_RECEIVER) {
                new Thread() {
                    public void run() {
                        Log.d(TAG, "audio whale loopback, LOOPBACK_RECEIVER");
                        mAudioManagerProxy.setParameters("dsp_loop=0");
                        mAudioManagerProxy.setParameters("test_out_stream_route=0x2");
                        mAudioManagerProxy.setParameters("test_in_stream_route=0x80000080");
                        mAudioManagerProxy.setParameters("dsploop_type=1");
                        mAudioManagerProxy.setParameters("dsp_loop=1");
                        mRadioOpenSuccess = true;
                        mUihandler.post(new Runnable() {
                            public void run() {
                                mRadioSpeaker.setEnabled(true);
                                mRadio3Receiver.setEnabled(true);
                                mCurLoopback = LOOPBACK_RECEIVER;
                                mRadioReceiver.setChecked(true);
                                if(!mPendingPaused) {
                                    Toast.makeText(
                                            PhoneLoopBackTest.this,
                                            R.string.receiver_loopback_success,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();

            } else if (loopbackType == LOOPBACK_MIC3_RECEIVER) {
                new Thread() {
                    public void run() {
                        Log.d(TAG, "audio whale loopback, LOOPBACK_MIC3_RECEIVER");
                        mAudioManagerProxy.setParameters("dsp_loop=0");
                        mAudioManagerProxy.setParameters("test_out_stream_route=0x2");
                        mAudioManagerProxy.setParameters("test_in_stream_route=0x90000000");
                        mAudioManagerProxy.setParameters("dsploop_type=1");
                        mAudioManagerProxy.setParameters("dsp_loop=1");
                        mRadioOpenSuccess = true;
                        mUihandler.post(new Runnable() {
                            public void run() {
                                mRadioSpeaker.setEnabled(true);
                                mRadioReceiver.setEnabled(true);
                                mCurLoopback = LOOPBACK_MIC3_RECEIVER;
                                mRadio3Receiver.setChecked(true);
                                if(!mPendingPaused) {
                                    Toast.makeText(
                                            PhoneLoopBackTest.this,
                                            R.string.receiver3_loopback_success,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();

            } else {
                new Thread() {
                    public void run() {
                        Log.d(TAG, "audio whale loopback, LOOPBACK_SPEAKER");
                        mAudioManagerProxy.setParameters("dsp_loop=0");
                        mAudioManagerProxy.setParameters("test_out_stream_route=0x2");
                        mAudioManagerProxy.setParameters("test_in_stream_route=0x80000004");
                        mAudioManagerProxy.setParameters("dsploop_type=1");
                        mAudioManagerProxy.setParameters("dsp_loop=1");
                        mRadioOpenSuccess = true;
                        mUihandler.post(new Runnable() {
                            public void run() {
                                mRadioReceiver.setEnabled(true);
                                mRadio3Receiver.setEnabled(true);
                                mCurLoopback = LOOPBACK_SPEAKER;
                                mRadioSpeaker.setChecked(true);
                                if(!mPendingPaused) {
                                    Toast.makeText(PhoneLoopBackTest.this,
                                            R.string.speaker_loopback_success,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();

            }
        }

    }

    private boolean setInMac(int loopbackType) {
        String result = null;
        if (loopbackType == LOOPBACK_SPEAKER) {
            result = IATUtils.sendAtCmd("AT+SPVLOOP=4,,,,,,2,1");
        } else {
            result = IATUtils.sendAtCmd("AT+SPVLOOP=4,,,,,,2,2");
        }
        if (result != null && result.length() >= 0 && IATUtils.AT_OK.contains(result)) {
            return true;
        }
        return false;
    }

    private void startMmiAudio(int loopbackType) {
        Log.i("PhoneLoopBackTest",
                "=== create thread to execute PhoneLoopBackTest start command! ===");
        mRadioReceiver.setEnabled(false);
        mRadio3Receiver.setEnabled(false);
        mRadioSpeaker.setEnabled(false);
        if (!mAudioWhaleHalFlag) {
            new Thread() {
                public void run() {
                    try {
                        sleep(1500);
                    } catch (Exception e) {

                    }
                    String result = IATUtils.sendAtCmd("AT+SPVLOOP=1,1,8,2,3,0");
                    setInMac(LOOPBACK_SPEAKER);
                    if (result.contains(IATUtils.AT_OK)) {
                        mUihandler.post(new Runnable() {
                            public void run() {
                                mRadioReceiver.setEnabled(true);
                                mCurLoopback = LOOPBACK_SPEAKER;
                                mRadioSpeaker.setChecked(true);
                                mRadioOpenSuccess = true;
                                if(mPendingPaused) {
                                    rollbackMmiAudio(mCurLoopback);
                                } else {
                                    Toast.makeText(PhoneLoopBackTest.this,
                                            R.string.speaker_loopback_open,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        mUihandler.post(new Runnable() {
                            public void run() {
                                if(!mPendingPaused) {
                                    Toast.makeText(PhoneLoopBackTest.this,
                                            R.string.speaker_loopback_open_fail,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }.start();
            if (Const.isAutoTestMode()) {
                mPhoneLoopBackHandler.sendEmptyMessageDelayed(LOOPBACK_RECEIVER, 3000);
            }
        } else {
            /*
             * main mic need to send test_out_stream_route=0x2,test_in_stream_route=0x80000004,
             * dsploop_type=1,dsp_loop=1 auxiliarymic need to send
             * test_out_stream_route=0x2,test_in_stream_route=0x80000080 dsploop_type=1,dsp_loop=1
             * close need to send dsp_loop=0
             */
            new Thread() {
                public void run() {
                    mAudioManagerProxy.setParameters("dsp_loop=0");
                    mAudioManagerProxy.setParameters("test_out_stream_route=0x2");
                    mAudioManagerProxy.setParameters("test_in_stream_route=0x80000004");
                    mAudioManagerProxy.setParameters("dsploop_type=1");
                    mAudioManagerProxy.setParameters("dsp_loop=1");
                    mRadioOpenSuccess = true;
                    mUihandler.post(new Runnable() {
                        public void run() {
                            mRadioReceiver.setEnabled(true);
                            mRadio3Receiver.setEnabled(true);
                            mCurLoopback = LOOPBACK_SPEAKER;
                            mRadioSpeaker.setChecked(true);
                            if(!mPendingPaused) {
                                Toast.makeText(PhoneLoopBackTest.this,
                                        R.string.speaker_loopback_open,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }.start();
            if (Const.isAutoTestMode()) {
                mPhoneLoopBackHandler.sendEmptyMessageDelayed(LOOPBACK_RECEIVER, 3000);
            }

        }

    }

    private void rollbackMmiAudio(int loopbackType) {
        Log.i("PhoneLoopBackTest",
                "=== create thread to execute PhoneLoopBackTest stop command! ===");
        if (!mAudioWhaleHalFlag) {
            if (loopbackType == LOOPBACK_RECEIVER) {
                new Thread() {
                    public void run() {
                        String result = IATUtils.sendAtCmd("AT+SPVLOOP=0,0,8,2,3,0");
                        Log.d(TAG, result);
                    }
                }.start();
            } else {
                new Thread() {
                    public void run() {
                        String result = IATUtils.sendAtCmd("AT+SPVLOOP=0,1,8,2,3,0");
                        Log.d(TAG, result);
                    }
                }.start();
            }
        } else {
            /** close the function need to send "dsp_loop=0" **/
            new Thread() {
                public void run() {
                    mAudioManagerProxy.setParameters("dsp_loop=0");
                }
            }.start();
        }
    }

    @Override
    public void onDestroy() {
        ValidationToolsUtils.setSoundEffect(this, mSavedSoundEffect);
        ValidationToolsUtils.setLockSound(this, mSavedLockSound);
        super.onDestroy();
    }
}
