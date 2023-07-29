
package com.sprd.validationtools.itemstest.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import android.app.Activity;
import android.graphics.Color;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.nonpublic.AudioManagerProxy;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.IATUtils;
import com.sprd.validationtools.utils.Native;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import android.media.AudioManager;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Message;
import android.util.Log;
import android.graphics.Color;
import android.widget.RadioButton;
import android.provider.Settings;
import android.media.MediaPlayer;
import android.os.Environment;
import java.io.IOException;
import android.widget.LinearLayout;
import com.sprd.validationtools.view.VUMeter;

public class HeadSetTest extends BaseActivity {
    private static final String TAG = "HeadSetTest";
    private static final String HEADSET_UEVENT_MATCH = "DEVPATH=/devices/virtual/switch/h2w";
    private static final String HEADSET_NAME_PATH = "/sys/class/switch/h2w/name";
    public Handler mHandler = new Handler();
    private boolean isRollbackStarted = false;

    private static final int STEP_NONE = 0;
    private static final int STEP_INSERT_HEADSET = 1;
    private static final int STEP_PRESS_HEADSET_KEY = 2;
    private static final int STEP_LOOPBACK = 3;

    private TextView mInsertHeadsetNotice = null;
    private TextView mPressHeadsetKey = null;
    private TextView mLoopbackTest = null;
    private Button mEarKey = null;

    private static final int mPassColor = Color.GREEN;
    private static final int mNormalColor = Color.RED;

    private boolean mIsThirdPartHeadset = false;
    private AudioManagerProxy mAudioManagerProxy = null;
    private boolean mAudioWhaleHalFlag = false;
///////////////////	
	private static String mFileName;
    public boolean isShowPassButton = false;

    public float mAmplitude = 0.0f;
    private MediaPlayer mPlayer = null;
    public VUMeter mVUMeter;
    public int mStatus = 1;
	private View mLayout;
//	private AudioManager mAudioManager = null;
	
    View.OnClickListener mRecordClick = new View.OnClickListener() {
 
        public void onClick(View view) {
            if (mStatus == 1) {
                mRecord_bt.setText(getString(R.string.play_record));
                stopPlaying();
                try {
                    startRecording();
                } catch (Exception e) {
                    Log.e(TAG, "onClick startRecording: error");
                }
                mRecord_bt.setTextColor(Color.GREEN);
                mStatus = 2;
            } else if (mStatus == 2) {
                mStatus = 1;
                stopRecording();
                mVUMeter.finish();
                startPlaying();
                mRecord_bt.setTextColor(Color.RED);
                mRecord_bt.setText(getString(R.string.start_record));
            }
        }
    };
   
    public Handler mRecordPlayHandler;
    public Button mRecord_bt;
    public MediaRecorder mRecorder = null;
    public Runnable mShowPassRunnable = new Runnable() {

        public void run() {
            if (mRecorder != null) {
                mAmplitude = (float) mRecorder.getMaxAmplitude();
            }
//Log.d(TAG, "tqy+++++mAmplitude = " + mAmplitude + "--------isShowPassButton = " + isShowPassButton);
            if (mAmplitude > 200.0f) {
                isShowPassButton = true;
            }
            if (mRecordPlayHandler != null) {
                mRecordPlayHandler.postDelayed(mShowPassRunnable, 300);
            }
        }
    };
	
	 public HeadSetTest() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }
///////////////////
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				
        setContentView(R.layout.headset_test);
        mAudioWhaleHalFlag = ValidationToolsUtils.isSupportAGDSP(getApplicationContext());
        Log.d(TAG, "onCreate mAudioWhaleHalFlag=" + mAudioWhaleHalFlag);
        mAudioManagerProxy = new AudioManagerProxy(getApplicationContext());
        mInsertHeadsetNotice = (TextView) findViewById(R.id.tx_insert_headset);
        mPressHeadsetKey = (TextView) findViewById(R.id.tx_press_earkey);
        mLoopbackTest = (TextView) findViewById(R.id.tx_start_phoneloop);
        mEarKey = (Button) findViewById(R.id.btn_earkey);
        mEarKey.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "onClick isRollbackStarted="+isRollbackStarted);
                if(!isRollbackStarted){
                    step(STEP_PRESS_HEADSET_KEY);
          //          startMmiAudio();
					mLayout.setVisibility(View.VISIBLE);//
                    isRollbackStarted = true;
                }
            }
        });

        if (isHeadsetExists()) {
          //  step(STEP_INSERT_HEADSET);
		  step(STEP_NONE);//
            if (Const.isAutoTestMode()) {
                step(STEP_PRESS_HEADSET_KEY);
                startMmiAudio();
                isRollbackStarted = true;
            }
        } else {
            step(STEP_NONE);
            if (Const.isAutoTestMode()) {
                storeRusult(false);
                finish();
            }
        }
		mLayout = (LinearLayout)findViewById(R.id.Record_layout);//
		mRecord_bt = (Button) findViewById(R.id.playRecord);
        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
        mVUMeter.mCurrentAngle = 0.0f;
        mRecord_bt.setOnClickListener(mRecordClick);
        mRecord_bt.setTextColor(Color.RED);
        mPassButton.setVisibility(View.GONE);
		mLayout.setVisibility(View.INVISIBLE);//
    }

    private void step(int step) {
        switch (step) {
            case STEP_NONE:
                mInsertHeadsetNotice.setTextColor(mNormalColor);
                mPressHeadsetKey.setTextColor(mNormalColor);
                mLoopbackTest.setTextColor(mNormalColor);
                mEarKey.setEnabled(false);
                break;
            case STEP_INSERT_HEADSET:
                mInsertHeadsetNotice.setTextColor(mPassColor);
                mPressHeadsetKey.setTextColor(mNormalColor);
                mLoopbackTest.setTextColor(mNormalColor);
                mEarKey.setEnabled(true);
                break;
            case STEP_PRESS_HEADSET_KEY:
                mInsertHeadsetNotice.setTextColor(mPassColor);
                mPressHeadsetKey.setTextColor(mPassColor);
                mLoopbackTest.setTextColor(mNormalColor);
                mEarKey.setEnabled(false);
                break;
            case STEP_LOOPBACK:
                mInsertHeadsetNotice.setTextColor(mPassColor);
                mPressHeadsetKey.setTextColor(mPassColor);
                mLoopbackTest.setTextColor(mPassColor);
                mEarKey.setEnabled(false);
				try{
				mPassButton.setVisibility(View.VISIBLE);//
				}catch (Exception e){
				}
                break;
        }
    }

    private boolean mIsPausing = false;
    @Override
    protected void onResume() {
        super.onResume();
        mIsPausing = false;
        Log.d(TAG, "onResume isRollbackStarted=" + isRollbackStarted);
        if (isRollbackStarted) {
            step(STEP_PRESS_HEADSET_KEY);
        //    startMmiAudio();
		mLayout.setVisibility(View.VISIBLE);//
            isRollbackStarted = false;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(earphonePluginReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPausing = true;
    //    rollbackMmiAudio();
        isRollbackStarted = false;
        unregisterReceiver(earphonePluginReceiver);
		exitRecord();//
    }
	
	@Override 
    public void onDestroy() {
        super.onDestroy();
        exitRecord();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            Log.d(TAG, "onKeyDown isRollbackStarted=" + isRollbackStarted);
            Log.d(TAG, "onKeyDown isRollbackStarted=" + isDestroyed() + ",isFinishing()=" + isFinishing() + mIsPausing);
            if (isDestroyed() || isFinishing() || mIsPausing) {
                return super.onKeyDown(keyCode, event);
            }
            if (!isRollbackStarted) {
                step(STEP_PRESS_HEADSET_KEY);
                //    startMmiAudio();
                mLayout.setVisibility(View.VISIBLE);//
                isRollbackStarted = true;
                mEarKey.setPressed(true);
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (!isRollbackStarted) {
                step(STEP_PRESS_HEADSET_KEY);
                //          startMmiAudio();
                mLayout.setVisibility(View.VISIBLE);//
                isRollbackStarted = true;
                if (mEarKey.isPressed()) {
                    mEarKey.setPressed(false);
                }
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            mEarKey.setPressed(false);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean isHeadsetExists() {
        int newState = FileUtils.getIntFromFile(Const.HEADSET_STATE_PATH);
        //UNISOC:Bug1547894 Headset status judgment condition is wrong
        return newState > 0;
    }

    private BroadcastReceiver earphonePluginReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent earphoneIntent) {
            if (earphoneIntent != null && earphoneIntent.getAction() != null) {
                Log.d(TAG, "earphonePluginReceiver action : " + earphoneIntent.getAction());
                if (earphoneIntent.getAction().equalsIgnoreCase(Intent.ACTION_HEADSET_PLUG)) {
                    int st = 0;
                    st = earphoneIntent.getIntExtra("state", 0);
                    int deviceId = earphoneIntent.getIntExtra("microphone", 0);
                    Log.d(TAG, "microphone = " + deviceId);
                    mIsThirdPartHeadset = (deviceId == 0);
                    if (st > 0) {
                        step(STEP_INSERT_HEADSET);
                    } else if (st == 0) {
                        step(STEP_NONE);
                    //    rollbackMmiAudio();
                        isRollbackStarted = false;
                    }
                }
            }
        }
    };

	//////////////
	    public void startPlaying() {
		
				if(mIsThirdPartHeadset){
                      // mAudioManager.setParameter("test_out_stream_route", "0x8");
					   mAudioManagerProxy.setParameters("test_out_stream_route=0x8");
                    }else{
                      // mAudioManager.setParameter("test_out_stream_route", "0x4");
					   mAudioManagerProxy.setParameters("test_out_stream_route=0x4");
                    }
                    if(mIsThirdPartHeadset){
                     //  mAudioManager.setParameter("test_in_stream_route", "0x80000004");
					   mAudioManagerProxy.setParameters("test_in_stream_route=0x80000004");
                    }else{
                     //  mAudioManager.setParameter("test_in_stream_route", "0x80000010");
					mAudioManagerProxy.setParameters("test_in_stream_route=0x80000010");		
                    }
								
        mPlayer = new MediaPlayer();
        try {
            File file = new File(mFileName);
//Log.d(TAG, "file length=" + file.length());
            if (file.exists()) {
                if (file.length() != 0) {
                    mPlayer.setDataSource(mFileName);
                    mPlayer.prepare();
                    mPlayer.start();
                    mPlayer.setLooping(true);
                    return;
                }
            }
            Toast.makeText(this, getString(R.string.norecordfile), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "startPlaying:" + e.toString());
        }
    }

   
    public void stopPlaying() {
        MediaPlayer mediaPlayer = mPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mPlayer = null;
        }
    }

   
    public void startRecording() throws Exception {
        MediaRecorder mediaRecorder = new MediaRecorder();
        mRecorder = mediaRecorder;
        mVUMeter.setRecorder(mediaRecorder);
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mFileName);
            mRecorder.prepare();
            mRecorder.start();
            mRecordPlayHandler = new Handler();
            mRecordPlayHandler.postDelayed(mShowPassRunnable, 300);
        } finally {
            Log.e(TAG, "startRecording: error");
        }
    }

   
    public void stopRecording() {
        MediaRecorder mediaRecorder = mRecorder;
        if (mediaRecorder != null) {
            try {
                mediaRecorder.setOnErrorListener(null);
                mRecorder.setOnInfoListener(null);
                mRecorder.setPreviewDisplay(null);
                mRecorder.stop();
            } catch (IllegalStateException e) {
                Log.i("Exception", Log.getStackTraceString(e));
            } catch (RuntimeException e2) {
                Log.i("Exception", Log.getStackTraceString(e2));
            } catch (Exception e3) {
                Log.i("Exception", Log.getStackTraceString(e3));
            }
            mRecorder.release();
            mRecorder = null;
        }
        if (isShowPassButton /* && BaseActivity.mPassButton.getVisibility() != View.VISIBLE */) {
			/* try{
				mPassButton.setVisibility(View.VISIBLE);//
				}catch (Exception e){
				} */
		                 mHandler.post(new Runnable() {
                            public void run() {
                                step(STEP_LOOPBACK);
                            }
                        });		
        }
   
        if (mRecordPlayHandler != null) {
            mRecordPlayHandler.removeCallbacks(mShowPassRunnable);
            mRecordPlayHandler = null;
        }
    }

    private void exitRecord() {
        mVUMeter.finish();
        stopRecording();
        
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
      
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
        File file = new File(mFileName);
        if (file.exists()) {
            file.delete();
        }
    }
	//////////////
	
    private void startMmiAudio() {
        Log.d(TAG, "startMmiAudio");
        if (!mAudioWhaleHalFlag) {
            new Thread() {
                public void run() {
                    String result;
                    Log.d(TAG, "mIsThirdPartHeadset = " + mIsThirdPartHeadset);
                    if (mIsThirdPartHeadset) {
                        result = IATUtils.sendATCmd("AT+SPVLOOP=1,4,8,2,3,0", "atchannel0");
                    } else {
                        result = IATUtils.sendATCmd("AT+SPVLOOP=1,2,8,2,3,0", "atchannel0");
                    }
                    if (!result.contains(IATUtils.AT_OK)) {
                        mHandler.post(new Runnable() {
                            public void run() {
                                step(STEP_PRESS_HEADSET_KEY);
                                Toast.makeText(HeadSetTest.this, "PhoneLoopBack Init Fail!", Toast.LENGTH_SHORT).show();
                                if (Const.isAutoTestMode()) {
                                    storeRusult(false);
                                    finish();
                                }
                            }
                        });
                    } else {
                        mHandler.post(new Runnable() {
                            public void run() {
                                step(STEP_LOOPBACK);
                                if (Const.isAutoTestMode()) {
                                    storeRusult(true);
                                    finish();
                                }
                            }
                        });
                    }
                }
            }.start();
        } else {
            /**
             * whale2 need to send test_out_stream_route=0x8,test_in_stream_route=0x80000010,
             * dsploop_type=1,dsp_loop=1
             **/
            new Thread() {
                public void run() {
                    if (mIsThirdPartHeadset) {
                        mAudioManagerProxy.setParameters("test_out_stream_route=0x8");
                        mAudioManagerProxy.setParameters("test_in_stream_route=0x80000004");
                    } else {
                        mAudioManagerProxy.setParameters("test_out_stream_route=0x4");
                        mAudioManagerProxy.setParameters("test_in_stream_route=0x80000010");
                    }
                    mAudioManagerProxy.setParameters("dsp_delay=2000");
                    mAudioManagerProxy.setParameters("dsploop_type=1");
                    mAudioManagerProxy.setParameters("dsp_loop=1");
                    mHandler.post(new Runnable() {
                        public void run() {
                            step(STEP_LOOPBACK);
                        }
                    });
                }
            }.start();
        }
    }

    private void rollbackMmiAudio() {
        Log.d(TAG, "rollbackMmiAudio");
        if (!mAudioWhaleHalFlag) {
            new Thread() {
                public void run() {
                    String result = IATUtils.sendATCmd("AT+SPVLOOP=0,2,8,2,3,0", "atchannel0");
                    Log.d(TAG, result);
                }
            }.start();
        } else {
            /** whale2 close the function need to send "dsp_loop=0" **/
            new Thread() {
                public void run() {
                    mAudioManagerProxy.setParameters("dsp_loop=0");
                }
            }.start();
        }

    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        if (Const.isAutoTestMode()) {
            int newState = FileUtils.getIntFromFile(Const.HEADSET_STATE_PATH);
            Log.d(TAG, "HeadSet=" + newState);
            if (newState <= 0) {
                return false;
            }
        }
        return true;
    }

}
