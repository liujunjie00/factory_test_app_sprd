package com.sprd.validationtools.itemstest.camera;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import android.content.Context;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.StorageUtil;

public class FlashTestActivity extends BaseActivity {
    private static final String TAG = "FlashTestActivity";

    private Camera mCamera = null;
    private int mCameraId = 0;
	
	private Button mOpenBtn;//
	private Button mCloseBtn;//
	private boolean mIsOpen = false;
	private boolean mIsClose = false;
	
	private CameraManager mCameraManager;
    private Context mContext;
	private boolean mFlashlightEnabled;

    private ComboPreferences mPreferences;
    private boolean mFlag = false;
    private static final int BACK_CAMERA = 0;
    private static final int FRONT_CAMERA = 1;

    private int groupId;
    private boolean isSurportCameraFlash = false;

    private boolean mIsPause = false;
    private static final boolean IS_SUPPORT_FLASHLIGHT = true;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.flash_test);
        setTitle(R.string.flash_test);
		
		mContext = getApplicationContext();
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
		initBtn();//
		
		mPassButton.setVisibility(View.GONE);//
     
    }
	
		private void initBtn(){
		
		mOpenBtn = (Button) findViewById(R.id.open_flash);
		mCloseBtn = (Button) findViewById(R.id.close_flash);
		
		mOpenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				openFlash();

            }
        });
		
		mCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				closeFlash();	
				checkPass();				
            }
        });
	}
	
	private void checkPass(){
		if(mIsOpen && mIsClose){
			mPassButton.setVisibility(View.VISIBLE);
			}	
	}
	

	private void openFlash(){
		setFlashlight(true);
		if(mFlashlightEnabled == false){
			writeFlashDev("0x20");
		}
//			mIsOpen = true;
								
	}
	private void closeFlash(){
		setFlashlight(false);
				mIsClose = true;		
		
	}
	
	    public void setFlashlight(boolean enabled) {
       
        synchronized (this) {
         //   if (mCameraId == null) return;
            if (mFlashlightEnabled != enabled) {
                mFlashlightEnabled = enabled;
                try {
                    mCameraManager.setTorchMode("0", enabled);
		Settings.Secure.putInt(
                    mContext.getContentResolver(), Secure.FLASHLIGHT_ENABLED, enabled ? 1 : 0);
                    mIsOpen = true;
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't set torch mode", e);
                    mFlashlightEnabled = false;
                    mIsOpen = false;
                }
            }
        }

    }
	
    private void writeFlashDev(String cmd) {
        FileUtils.writeFile(Const.CAMERA_FLASH, cmd);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPause = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsPause = true;
        try {
            if (mCamera != null) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy(){
                
        /* SPRD bug 753892 : Flashlight maybe not close */
        try {
			setFlashlight(false);//
            writeFlashDev("0x11");
            writeFlashDev("0x21");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        /* @} */
        super.onDestroy();
    }
}
