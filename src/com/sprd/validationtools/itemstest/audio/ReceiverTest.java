/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.audio;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import com.sprd.validationtools.nonpublic.AudioManagerProxy;
import com.sprd.validationtools.nonpublic.AudioSystemProxy;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

public class ReceiverTest extends BaseActivity {
    private final static String TAG = "ReceiverTest";
    private static final String DEFAULT_AUDIO = "Orion.ogg";
    private static final String SPECIFIC_RINGTONE_NAME = "mixtone.wav";
    private static final String SAVE_PATH = "/storage/emulated/0/mmi/";
    private static final boolean PLAY_LOOP = true;
    /* SPRD Bug 771294: The Ringtones motor test need to use the specific ringtone. @{ */
    private static final boolean USE_SPECIFIC_RINGTONE = true;
    /* @} */
    private final int SEARCH_FINISHED = 0;
    private final int PLAY_PASS = 1;
    TextView mContent;
    private int mBackupMode = 0;
    private List<String> mFilePaths;
    private String mFilePath = null;
    private File mFile;
    MediaPlayer mPlayer = null;
    private boolean isSearchFinished = false;
    private AudioManagerProxy mAudioManagerProxy = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAudioManagerProxy = new AudioManagerProxy(getApplicationContext());
        mPlayer = new MediaPlayer();
        mContent = new TextView(this);
        mContent.setGravity(Gravity.CENTER);
        mContent.setTextSize(25);
        setContentView(mContent);
        setTitle(R.string.receiver_test);
        mFilePaths = new ArrayList<String>();
        /* SPRD Bug 771294: The Ringtones motor test need to use the specific ringtone. @{ */
        if (USE_SPECIFIC_RINGTONE) {
            mFilePath = saveMediaFileToSdcard();
            mHandler.sendEmptyMessage(SEARCH_FINISHED);
        } else {
            Thread searchThread = new Thread() {
                public void run() {
                    // Maybe cause StackOverflowError toSearchFiles recursion.
                    File firstAudio = new File("/system/media/audio/ringtones", DEFAULT_AUDIO);
                    if (firstAudio.exists()) {
                        mFilePaths.add(firstAudio.getPath());
                    } else {
                        mFile = new File("/system/media/audio/ringtones");
                        toSearchFiles(mFile);
                    }
                    mHandler.sendEmptyMessage(SEARCH_FINISHED);
                }
            };
            searchThread.start();
        }
        mContent.setText(R.string.start_searching);
        /* @} */
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEARCH_FINISHED:
                    isSearchFinished = true;
                    doPlay();
                    if (Const.isAutoTestMode()) {
                        mHandler.sendEmptyMessageDelayed(PLAY_PASS, 2000);
                    }
                    break;
                case PLAY_PASS:
                    storeRusult(true);
                    finish();
                    break;
            }
        }
    };

    private void doPlay() {
        if (mPlayer == null) {
            return;
        }
        int audioNumber = 0;
        /* SPRD Bug 771294: The Ringtones motor test need to use the specific ringtone. @{ */
        if (!USE_SPECIFIC_RINGTONE) {
            audioNumber = getRandom(mFilePaths.size());
        }
        /* @} */
        /* SPRD Bug 1962058: There was no sound on the receiver for some time. @{ */
        AudioAttributes.Builder audioAttrBuilder = new AudioAttributes.Builder();
        audioAttrBuilder.setLegacyStreamType(AudioSystemProxy.STREAM_VOICE_CALL);
        mPlayer.setAudioAttributes(audioAttrBuilder.build());
        /* @} */
        Log.d(TAG, "doPlay audioNumber= " + audioNumber);
        try {
            /* SPRD Bug 771294: The Ringtones motor test need to use the specific ringtone. @{ */
            mPlayer.reset();
            if (!USE_SPECIFIC_RINGTONE) {
                mPlayer.setDataSource(mFilePaths.get(audioNumber));
            } else {
                mPlayer.setDataSource(mFilePath);
            }
            mPlayer.prepare();
            mPlayer.setOnPreparedListener(mOnPreparedListener);
            /* @} */
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* SPRD Bug 771294: The Ringtones motor test need to use the specific ringtone. @{ */
        if (USE_SPECIFIC_RINGTONE) {
            mContent.setText(getResources().getText(R.string.melody_play_tag) + SPECIFIC_RINGTONE_NAME);
        } else {
            mContent.setText(getResources().getText(R.string.melody_play_tag)
                    + mFilePaths.get(audioNumber));
        }
        /* @} */
    }

    private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mp) {
            // TODO Auto-generated method stub
            if (mp != null) {
                mp.setLooping(PLAY_LOOP);
                mp.start();
            } else {
                Log.w(TAG, "mOnPreparedListener prepare issue!");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        if (isSearchFinished) {
            doPlay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer == null) {
            return;
        }
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }

    public void toSearchFiles(File file) {
        File[] files = file.listFiles();
        if (files == null) {
            return;
        }
        for (File tf : files) {
            if (tf.isDirectory()) {
                toSearchFiles(tf);
            } else {
                try {
                    if (tf.getName().indexOf(".ogg") > -1) {
                        mFilePaths.add(tf.getPath());
                    }
                } catch (IndexOutOfBoundsException e) {
                    Toast.makeText(this, "pathError", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private int getRandom(int max) {
        double random = Math.random();
        int result = (int) Math.floor(random * max);
        return result;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private String saveMediaFileToSdcard() {
        String filePath = SAVE_PATH + SPECIFIC_RINGTONE_NAME;
        saveToRawResourceSDCard(this, R.raw.mixtone, filePath);
        return filePath;
    }
    public void saveToRawResourceSDCard(Context context, int sourceResId, String filePath) {
        InputStream inStream = null;
        FileOutputStream fileOutputStream = null;
        ByteArrayOutputStream outStream = null;
        File dstFile = new File(filePath);
        try {
            if (!dstFile.getParentFile().exists()) {
                dstFile.getParentFile().mkdirs();
            }
            if (!dstFile.exists()) {
                dstFile.createNewFile();
            }
            inStream = context.getResources().openRawResource(sourceResId);
            fileOutputStream = new FileOutputStream(dstFile);
            byte[] buffer = new byte[10];
            outStream = new ByteArrayOutputStream();
            int len = 0;
            while((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] bs = outStream.toByteArray();
            fileOutputStream.write(bs);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
                if (inStream != null) {
                    inStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}