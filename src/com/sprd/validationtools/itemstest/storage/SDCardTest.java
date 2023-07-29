package com.sprd.validationtools.itemstest.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.StorageUtil;

public class SDCardTest extends BaseActivity {
    private String TAG = "SDCardTest";
    private static final String SPRD_SD_TESTFILE = "sprdtest.txt";
    private static final String PHONE_STORAGE_PATH = "/data/data/com.sprd.validationtools/";
    TextView mContent, mContent2;
    byte[] mounted = new byte[2];
    byte[] result = new byte[2];
	
	private boolean mIsPass = false;////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        LayoutParams parms = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(parms);
        ll.setOrientation(1);
        ll.setGravity(Gravity.CENTER);
        mContent = new TextView(this);
        mContent.setTextSize(35);
        ll.addView(mContent);

        mContent2 = new TextView(this);
        mContent2.setTextSize(35);
        ll.addView(mContent2);
        setContentView(ll);

        setTitle(R.string.sdcard_test);
        mContent.setText(getResources().getText(R.string.sdcard2_test));
        mContent2.setText(getResources().getText(R.string.sdcard1_test));
   //     super.removeButton();
		mPassButton.setVisibility(View.GONE);
        startBackgroundThread();
        mWorkHandler.post(vtThread);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        stopBackgroundThread();
    }

    private byte mSDCardTestFlag[] = new byte[1];
    private Handler mHandler = new Handler();

    private void checkResultText(boolean successExSdcard,
            boolean successInSdcard) {
        Log.d(TAG, "checkResultText successExSdcard=" + successExSdcard
                + ",successInSdcard=" + successInSdcard);
        if (successExSdcard) {
            mContent.setText(getResources().getText(
                    R.string.sdcard2_test_result_success));
        } else {
            mContent.setText(getResources().getText(
                    R.string.sdcard2_test_result_fail));
        }
        if (successInSdcard) {
            mContent2.setText(getResources().getText(
                    R.string.sdcard_test_result_success));
        } else {
            mContent2.setText(getResources().getText(
                    R.string.sdcard_test_result_fail));
        }
    }
    private Runnable mRunnable = new Runnable() {
        public void run() {
            Log.d(TAG, "=== display SDCard test info! === mounted[0] = " + mounted[0]
                    + " result[0] = " + result[0] + " mounted[1] = " + mounted[1]
                            + " result[1] = " + result[1]);
            boolean successExSdcard = (mounted[0] == 0) && (result[0] == 0);
            boolean successInSdcard = (mounted[1] == 0) && (result[1] == 0);
            checkResultText(successExSdcard, successInSdcard);
		if(mIsPass){
			stopBackgroundThread();
			return ;
		}
            if (successExSdcard && successInSdcard) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
				try{
				mPassButton.setVisibility(View.VISIBLE);//
				}catch (Exception e){
					
				}
                        Toast.makeText(SDCardTest.this, R.string.text_pass, Toast.LENGTH_SHORT).show();
                        storeRusult(true);
						mIsPass = true;
                     //   finish();
                    }
                }, 1000);
            }else{
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SDCardTest.this, R.string.text_fail, Toast.LENGTH_SHORT).show();
                        storeRusult(false);
						mIsPass = false;
                        finish();
                    }
                }, 1000);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        checkSDCard();
        if (mounted[0] == 1) {
                mContent.setText(getResources().getText(R.string.no_sdcard2));
        }
        if (mounted[1] == 1) {
                mContent2.setText(getResources().getText(R.string.no_sdcard));
        }
        // create thread to execute SDCard test command
        Log.i("SDCardTest",
                "=== create thread to execute SDCard test command! ===");
    }

    private void checkSDCard() {
        if (!StorageUtil.getExternalStoragePathState(getApplicationContext()).equals(
                Environment.MEDIA_MOUNTED)) {
            mounted[0] = 1;
        } else {
            mounted[0] = 0;
        }
        mounted[1] = 0;
    }

    private Handler mWorkHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("SdcardBackground");
        mBackgroundThread.start();
        mWorkHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkPhonStorageResult(){
        FileInputStream in = null;
        FileOutputStream out = null;
        Log.d(TAG, "checkPhonStorageResult start");
        try {
            if (mounted[1] == 0) {
                File fp = new File(PHONE_STORAGE_PATH, SPRD_SD_TESTFILE);
                if (fp.exists())
                    fp.delete();
                fp.createNewFile();
                out = new FileOutputStream(fp);
                mSDCardTestFlag[0] = 'd';
                out.write(mSDCardTestFlag, 0, 1);
                out.flush();
                in = new FileInputStream(fp);
                mSDCardTestFlag[0] = '0';
                result[1] = 1;
                int count = in.read(mSDCardTestFlag, 0, 1);
                Log.d(TAG, "read count =: " + count);
                if (count > 0){
                    if (mSDCardTestFlag[0] == 'd'){
                        result[1] = 0;
                    }else{
                        result[1] = 1;
                    }
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "=== error: Exception happens when sdcard I/O! ===");
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException io) {
                Log.w(TAG, "close in/out err");
                io.printStackTrace();
            }
        }
    }

    private void checkSdcardRessult(){
        FileInputStream in2 = null;
        FileOutputStream out2 = null;
        Log.d(TAG, "checkSdcardRessult start");
        try {
            if (mounted[0] == 0) {
                String secondPath = StorageUtil.getExternalStorageAppPath(getApplicationContext(), Const.EXT_COMMON_PATH);
                if(!FileUtils.fileIsExists(secondPath)) return;
                Log.d(TAG, "checkSdcardRessult secondPath =: " + secondPath);
                File fp = new File(secondPath, SPRD_SD_TESTFILE);
                if (fp.exists())
                    fp.delete();
                fp.createNewFile();
                out2 = new FileOutputStream(fp);
                mSDCardTestFlag[0] = '6';
                out2.write(mSDCardTestFlag, 0, 1);
                out2.flush();
                in2 = new FileInputStream(fp);
                mSDCardTestFlag[0] = '0';
                result[0] = 1;
                int count = in2.read(mSDCardTestFlag, 0, 1);
                if (count > 0){
                    if (mSDCardTestFlag[0] == '6') {
                        result[0] = 0;
                    } else {
                        result[0] = 1;
                    }
                }
            }
        } catch (IOException e) {
            Log.i(TAG, "=== error: Exception happens when sdcard I/O! ===");
            e.printStackTrace();
        } finally {
            try {
                if (out2 != null) {
                    out2.close();
                }
                if (in2 != null) {
                    in2.close();
                }
            } catch (IOException io) {
                Log.w(TAG, "close in/out err");
                io.printStackTrace();
            }
        }
    }
    Runnable vtThread = new Runnable() {
        public void run() {
		if(mIsPass){
			stopBackgroundThread();
			return ;
		}
            checkPhonStorageResult();
            checkSdcardRessult();
            mHandler.post(mRunnable);
        }
    };

}
