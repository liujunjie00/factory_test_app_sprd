
package com.sprd.validationtools.itemstest.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.media.AudioSystem;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.view.View;
import android.graphics.Color;
import android.widget.RadioButton;
import android.provider.Settings;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.utils.IATUtils;
import com.sprd.validationtools.utils.Native;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import android.os.SystemProperties;
import android.media.MediaPlayer;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import com.sprd.validationtools.view.VUMeter;

public class MainMicTest extends BaseActivity {
	
	private final String TAG = "MainMicTest";
    private static String mFileName;
    public boolean isShowPassButton = false;

    public int mAmplitude = 0;
    private MediaPlayer mPlayer = null;
    public VUMeter mVUMeter;
    public int mStatus = 1;
	
	private AudioManager mAudioManager = null;
	
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
	private Context mContext;
   
    public Runnable mShowPassRunnable = new Runnable() {

        public void run() {
            if (mRecorder != null) {
                mAmplitude =  mRecorder.getMaxAmplitude();
            }else{
//Log.d(TAG, "tqy+++++mRecorder == null" );			
			mRecorder = new MediaRecorder();
			mAmplitude =  mRecorder.getMaxAmplitude();
			}
//Log.d(TAG, "tqy+++++mAmplitude = " + mAmplitude + "--------isShowPassButton = " + isShowPassButton);
            if (mAmplitude > 200) {
                isShowPassButton = true;
            }else if(!isShowPassButton && mAmplitude < 1 ){
			try{
				mAmplitude =  mRecorder.getMaxAmplitude();
				if (mAmplitude > 200) {
                isShowPassButton = true;
            	}
				}catch (Exception e) {
				}
			}
            if ( mRecordPlayHandler != null) {
                mRecordPlayHandler.postDelayed(mShowPassRunnable, 0);
            }else {
				mRecordPlayHandler = new Handler();
            	mRecordPlayHandler.postDelayed(mShowPassRunnable, 200);
			}
        }
    };
	
	private Runnable mPlayRunnable;
	private Handler mHandler = new Handler();


    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.mic_test);
        setTitle(R.string.phone_loopback_test);
		mContext = this;
        mRecord_bt = (Button) findViewById(R.id.playRecord);

        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
        mVUMeter.mCurrentAngle = 0.5f;
        mRecord_bt.setOnClickListener(mRecordClick);
        mRecord_bt.setTextColor(Color.RED);
       
        mPassButton.setVisibility(View.GONE);
		
		mAudioManager = (AudioManager) this
                .getSystemService(Context.AUDIO_SERVICE);
        


   	mPlayRunnable = new Runnable() {
            public void run() {
									
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
		if (isShowPassButton /* && BaseActivity.mPassButton.getVisibility() != View.VISIBLE */) {
			try{
				mPassButton.setVisibility(View.VISIBLE);//
				}catch (Exception e){
					
				}
        }
                    return;
                }
            }
            Toast.makeText(mContext, getString(R.string.norecordfile), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "startPlaying:" + e.toString());
        }
            }
        };	
    }
    

    public MainMicTest() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
    }
    public void startPlaying() {
								
		mHandler.postDelayed(mPlayRunnable, 2000);
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

    @Override 
    public void onPause() {
        super.onPause();
        exitRecord();
    }

    @Override 
    public void onDestroy() {
        super.onDestroy();
        exitRecord();
		if(mHandler != null){
		mHandler.removeCallbacks(mPlayRunnable);
		}
    }

/*     @Override 
    public void onClick(View view) {
        super.onClick(view);
        exitRecord();
    } */
}
