/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.audio;

//import android.hardware.soundtrigger.SoundTrigger;
//import android.hardware.soundtrigger.SoundTrigger.ConfidenceLevel;
//import android.hardware.soundtrigger.SoundTrigger.RecognitionEvent;
//import android.hardware.soundtrigger.SoundTrigger.SoundModelEvent;
//import android.hardware.soundtrigger.SoundTriggerModule;
import android.media.MediaPlayer;
//import android.media.permission.Identity;
import android.os.Bundle;
import android.os.Handler;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import java.util.ArrayList;

public class SoundTriggerTestActivity<mpv> extends BaseActivity {
        //implements SoundTrigger.StatusListener {

    private Button mBtnStart;
    private TextView mTxNote;
    //private SoundTriggerModule stm;
    private static final int MODULE_ID = 1;
    private static final String TAG = "SoundTriggerTest";
    public static final int STATUS_OK = 0;
    private int soundModelHandle[] = {1};
    private byte data[] = {100, 100};
    private byte data2[] = {10, 11, 12, 14};
    //private SoundTrigger.RecognitionConfig config;
    //private SoundTrigger.KeyphraseRecognitionExtra[] recognitionExtra;

    //private MediaPlayer mpv = null;

    /*public void playMusic() {
        mpv = new MediaPlayer();
        mpv = MediaPlayer.create(this, R.raw.soundtriggermp);
        mpv.start();
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.soundtrigger_test);
        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStart.setOnClickListener(this);
        mBtnStart.setClickable(false);

        mTxNote = (TextView) findViewById(R.id.txt_note);
        mTxNote.setText(R.string.loading_soundTrigger);
        /* UNISOC:Bug1610154 Adapte SoundTrigger.attachModuleAsOriginator for A12 @{ */
        /*Identity originatorIdentity = new Identity();
        ArrayList<SoundTrigger.ModuleProperties> modules = new ArrayList<>();
        int result = SoundTrigger.listModules(modules);
        Log.d(TAG, "result = " + result + " modules.size() = " +  modules.size());
        if (result == STATUS_OK && modules.size() > 0) {
            stm = SoundTrigger.attachModuleAsOriginator(MODULE_ID, this, new Handler(), originatorIdentity);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.list_soundTrigger_modules), Toast.LENGTH_SHORT)
                    .show();
            storeRusult(false);
            finish();
            return;
        }
        /* @} */
    }

    /**
     * implements StatusListener when soundtrigger get sound will call back it
     *
     * @param event
     */
   /*@Override
    public void onRecognition(RecognitionEvent event) {
        Log.d(TAG, "the sound had get");
        // pass
        mTxNote.setText(R.string.test_pass);
        if (stm != null) {
            stm.stopRecognition(soundModelHandle[0]);
            stm.startRecognition(soundModelHandle[0], config);
        }
        if (mpv != null) {
            mpv.pause();
            mpv.release();
            mpv = null;
        }
    }*/

    /**
     * implements StatusListener when soundtriggersystem begin work will call back it
     *
     * @param event
     */
    /*@Override
    public void onSoundModelUpdate(SoundModelEvent event) {
        Log.d(TAG, "load SoundTriggle finished,and you can test");
        mBtnStart.setClickable(true);
        mTxNote.setText(R.string.soundtrigger_click_button);
    }

    @Override
    public void onServiceDied() {
    }

    @Override
    public void onServiceStateChange(int status) {
    }*/

    //@Override
    //public void onClick(View v) {
        //if (v == mPassButton) {
            //if (canPass) {
                //Log.d("onclick", "pass.." + this);
                //storeRusult(true);
                //finish();
            //} else {
                //Toast.makeText(this, R.string.can_not_pass, Toast.LENGTH_SHORT).show();
            //}
       // } else if (v == mFailButton) {
            //storeRusult(false);
            //finish();
        //} else if (v == mBtnStart) {
            //mTxNote.setText(R.string.soundtrigger_load);
            //recognitionExtra = new SoundTrigger.KeyphraseRecognitionExtra[1];
            //recognitionExtra[0] = new SoundTrigger.KeyphraseRecognitionExtra(1, 2, 0, new ConfidenceLevel[0]);
            //config = new SoundTrigger.RecognitionConfig(true, true, recognitionExtra, data);

            //Log.d(TAG, "update ketphrase:startRecognition()...");
            //if (stm != null) {
                //stm.stopRecognition(soundModelHandle[0]);
                //stm.startRecognition(soundModelHandle[0], config);
            //}
            //Log.d(TAG, "Recognition finished");
            //mTxNote.setText(R.string.prepare_play_sound);
            //try {
                //Thread.sleep(3000);
            //} catch (Exception e) {

            //}
            //playMusic();
        //}
    //}

    //@Override
    //protected void onDestroy() {
        //if (stm != null) {
            //stm.stopRecognition(soundModelHandle[0]);
            //stm.unloadSoundModel(soundModelHandle[0]);
            //stm.detach();
        //}
        //if (mpv != null) {
            //mpv.pause();
           // mpv.release();
            //mpv = null;
        //}
        //super.onDestroy();
    //}

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        return false;
    }
}