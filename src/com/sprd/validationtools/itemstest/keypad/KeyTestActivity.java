package com.sprd.validationtools.itemstest.keypad;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.ValidationToolsUtils;

public class KeyTestActivity extends BaseActivity {
    private static final String TAG = "KeyTestActivity";
    private ImageButton mHomeButton;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    private ImageButton mVolumeUpButton;
    private ImageButton mVolumeDownButton;
    private ImageButton mCameraButton;
    private byte keyPressedFlag = 0;
    private byte keySupportFlag = 0;
    private boolean isHideCamera = false;
    private boolean mHasPhysicalNavigationKey = true;
    private boolean isShowNavigationBar = false;
    private Context mContext;
    public Handler mHandler = new Handler();
    public boolean shouldBack = false;

    private HashMap<Integer, Button> mSupportkeys = new HashMap<>();
    private int mSupportKeyCount = 0;
    private int mPressedKeyCount = 0;
    private static final int COLUMNCOUNT = 4;
    private static final long TEST_TIMEOUT = 60000;

    private Button mAiButton = null;

    /* *Add Auto MMI Test */
    private static final int KEY_EVENT_HOME = 11;
    private static final int KEY_EVENT_BACK = 12;
    private static final int KEY_EVENT_APP_SWITCH = 13;
    private static final int KEY_EVENT_VOLUME_UP = 14;
    private static final int KEY_EVENT_VOLUME_DOWN = 15;
    private Handler mKeyEventHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case KEY_EVENT_HOME:
                    sendKey("input keyevent 3");
                    mKeyEventHandler.sendEmptyMessageDelayed(KEY_EVENT_BACK, 250);
                    break;
                case KEY_EVENT_BACK:
                    sendKey("input keyevent 4");
                    mKeyEventHandler.sendEmptyMessageDelayed(KEY_EVENT_APP_SWITCH, 250);
                    break;
                case KEY_EVENT_APP_SWITCH:
                    sendKey("input keyevent 187");
                    mKeyEventHandler.sendEmptyMessageDelayed(KEY_EVENT_VOLUME_UP, 250);
                    break;
                case KEY_EVENT_VOLUME_UP:
                    sendKey("input keyevent 24");
                    mKeyEventHandler.sendEmptyMessageDelayed(KEY_EVENT_VOLUME_DOWN, 250);
                    break;
                case KEY_EVENT_VOLUME_DOWN:
                    sendKey("input keyevent 25");
                    break;
                default:
                    break;
            }
        }
    };

    private void sendKey(String cmd) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /* @} */

    private void initView(Context context) {
        Resources res = getResources();
        // ScrollView scrollView = new ScrollView(context);
        GridLayout gridLayout = new GridLayout(context);
        gridLayout.setColumnCount(COLUMNCOUNT);
        gridLayout.setFocusable(false);
        // scrollView.addView(gridLayout);

        DisplayMetrics dm = res.getDisplayMetrics();
        int width = dm.widthPixels;

        String[] supportkeyNames = res
                .getStringArray(R.array.keyname_support_list);
        int[] supportkeyCodes = res.getIntArray(R.array.keycode_support_list);
        for (String supportkeyName : supportkeyNames) {
            Log.d(TAG, "initView supportkeyName=" + supportkeyName);
        }
        int i = 0;
        for (int supportkeyCode : supportkeyCodes) {
            Log.d(TAG, "initView supportkeyCode=" + supportkeyCode);
            if (i >= supportkeyNames.length) {
                return;
            }
            Button button = new Button(context);
            button.setWidth(width / COLUMNCOUNT);
            button.setPadding(2, 2, 2, 2);
            button.setText(supportkeyNames[i++]);
            button.setFocusable(false);
            // button.setTextColor(Color.RED);
            gridLayout.addView(button);
            mSupportkeys.put(supportkeyCode, button);
        }
        mSupportKeyCount = supportkeyCodes.length;
        mPressedKeyCount = 0;
        Log.d(TAG, "initView mSupportKeyCount=" + mSupportKeyCount
                + ",mPressedKeyCount=" + mPressedKeyCount);
        setContentView(gridLayout);

       // removeButton();
	   if(mPassButton != null){
	   mPassButton.setVisibility(View.GONE);//
	   }
        //
        mHandler.postDelayed(mTimeOutRunnable, TEST_TIMEOUT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        if (Const.isSupportFeaturePhone()) {
            initView(this);
        } else {
            setContentView(R.layout.key_test);
            setTitle(R.string.key_test);
            mHomeButton = (ImageButton) findViewById(R.id.home_button);
            mMenuButton = (ImageButton) findViewById(R.id.menu_button);
            mBackButton = (ImageButton) findViewById(R.id.back_button);
            mVolumeUpButton = (ImageButton) findViewById(R.id.volume_up_button);
            mVolumeDownButton = (ImageButton) findViewById(R.id.volume_down_button);
            mCameraButton = (ImageButton) findViewById(R.id.camera_button);
            isShowNavigationBar = ValidationToolsUtils.hasNavigationBar(this);
            Log.d(TAG, "isShowNavigationBar=" + isShowNavigationBar);
            mHasPhysicalNavigationKey = checkDeviceHasNavigationBar(mContext) && !isShowNavigationBar;
            Log.d(TAG, "mHasPhysicalNavigationKey=" + mHasPhysicalNavigationKey);
         //   if (Const.isAutoTestMode()) {
                isHideCamera = true;
                showKey(isHideCamera);
       //     } else {
       //         showHasCameraDialog();
       //     }
            if (isShowNavigationBar) {
                mPassButton.setVisibility(View.GONE);
       //         mFailButton.setVisibility(View.GONE);
            }
            mAiButton = (Button) findViewById(R.id.ai_button);
            mAiButton.setVisibility(View.GONE);
        }
        /* *Add Auto MMI Test @{*/
        if (Const.isAutoTestMode()) {
            mHandler.postDelayed(mTimeOutRunnable, 15000);
            if (mHasPhysicalNavigationKey) {
                mKeyEventHandler.sendEmptyMessageDelayed(KEY_EVENT_HOME, 4000);
            } else {
                mKeyEventHandler.sendEmptyMessageDelayed(KEY_EVENT_VOLUME_UP, 4000);
            }
        }
        /* @} */
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isShowNavigationBar) {
            hideNavigationBar();
        }
    }

    public boolean checkDeviceHasNavigationBar(Context context) {
        // boolean hasMenuKey =
        // ViewConfiguration.get(context).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);
        Log.d(TAG, "hasBackKey=:" + hasBackKey);
        if (hasBackKey) {
            return true;
        }
        return false;
    }

    protected void showHasCameraDialog() {
        Dialog cameraKeyDialog = new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(this.getString(R.string.has_camera_title))
                .setCancelable(false)
                .setNegativeButton(R.string.has_camera_key,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // TODO Auto-generated method stub
                                isHideCamera = false;
                                showKey(isHideCamera);
                                dialog.dismiss();
                            }
                        })
                .setPositiveButton(R.string.no_camera_key,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                // TODO Auto-generated method stub
                                isHideCamera = true;
                                showKey(isHideCamera);
                                dialog.dismiss();
                            }
                        }).create();
        cameraKeyDialog.show();
    }

    private void showKey(boolean hideCamera) {
        if (Const.isHomeSupport(this) && mHasPhysicalNavigationKey) {
            mHomeButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 1;
        } else {
            /*
             * SPRD: modify 20140529 Spreadtrum of 305634 MMI test,lack of
             * button which is on the right 0f "Home" @{
             */
            mHomeButton.setVisibility(View.GONE);
            /* @} */
        }
        if (Const.isBackSupport(this) && mHasPhysicalNavigationKey) {
            mBackButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 2;
        } else {
            /*
             * SPRD: modify 20140529 Spreadtrum of 305634 MMI test,lack of
             * button which is on the right 0f "Home" @{
             */
            mBackButton.setVisibility(View.GONE);
            /* @} */
        }
        if (Const.isMenuSupport(this) && mHasPhysicalNavigationKey) {
            mMenuButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 4;
        } else {
            mMenuButton.setVisibility(View.GONE);
        }
        Log.d(TAG, "isCameraSupport:" + Const.isCameraSupport() + " hideCamera:" + hideCamera);
        if (Const.isCameraSupport() && !hideCamera) {
            mCameraButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 8;
        } else if (hideCamera) {
            mCameraButton.setVisibility(View.GONE);
        }
        if (Const.isVolumeUpSupport()) {
            mVolumeUpButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 16;
        }
        if (Const.isVolumeDownSupport()) {
            mVolumeDownButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 32;
        }
        if(ValidationToolsUtils.isSupportAIKey()){
            mAiButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 64;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown keyCode=" + keyCode);
        if (Const.isSupportFeaturePhone()) {
            Button button = mSupportkeys.get(keyCode);
            if (button != null) {
                Log.d(TAG, " onKeyDown is support key!");
                if (!button.isPressed()) {
                    button.setPressed(true);
                    mPressedKeyCount++;
                }
                Log.d(TAG, "onKeyDown mPressedKeyCount=" + mPressedKeyCount
                        + ",mSupportKeyCount=" + mSupportKeyCount);
                if (mPressedKeyCount == mSupportKeyCount) {
                  //  storeRusult(true);
				  mPassButton.setVisibility(View.VISIBLE);
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                          //  finish();
                        }
                    }, 1000);
                }
            }
            return true;
        }
        if (KeyEvent.KEYCODE_HOME == keyCode) {
            mHomeButton.setPressed(true);
            keyPressedFlag |= 1;
        } else if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (!mBackButton.isPressed()) {
                mBackButton.setPressed(true);
                keyPressedFlag |= 2;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else if (KeyEvent.KEYCODE_APP_SWITCH == keyCode || keyCode == 999) {
            mMenuButton.setPressed(true);
            keyPressedFlag |= 4;
        } else if (KeyEvent.KEYCODE_CAMERA == keyCode) {
            mCameraButton.setPressed(true);
            keyPressedFlag |= 8;
        } else if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            mVolumeUpButton.setPressed(true);
            keyPressedFlag |= 16;
        } else if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            mVolumeDownButton.setPressed(true);
            keyPressedFlag |= 32;
        }
        //AI keyCode == 766
        else if (766 == keyCode) {
            mAiButton.setPressed(true);
            keyPressedFlag |= 64;
        }
        Log.d(TAG, "keySupportFlag=" + keySupportFlag + " keyPressedFlag=" + keyPressedFlag);
        if (keySupportFlag == keyPressedFlag) {
            BaseActivity.shouldCanceled = false;
      //      storeRusult(true);
			   mPassButton.setVisibility(View.VISIBLE);
            Toast.makeText(KeyTestActivity.this, R.string.text_pass,
                        Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
         //           finish();
                }
            }, 1000);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mTimeOutRunnable);
        super.onDestroy();
    }

    public Runnable mTimeOutRunnable = new Runnable() {
        public void run() {
            storeRusult(false);
            Toast.makeText(KeyTestActivity.this, R.string.text_fail,
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    };
}
