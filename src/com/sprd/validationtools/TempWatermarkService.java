package packages.apps.ValidationTools.src.com.sprd.validationtools;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import com.sprd.validationtools.R;


public class TempWatermarkService extends Service {

    private static final String TAG = "TempWatermarkService";

    private WindowManager.LayoutParams mGooglekeyParam;
    private TextView mTempTv;
    private TextView mGooglekeyTv;
    private boolean mIsAddGooglekeyView = false;
    private WindowManager mWindowManager;
	private final float GOOGLE_KEY_WATER_MARK_ALPHA = 0.8f;

    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()...");
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()...");
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        showOverlay();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()...");
     //   return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy()...");
        super.onDestroy();
        removeGoogleKeyWatermark();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged=>newConfig: " + newConfig);
//        updateGoogleKeyWatermark();
    }
	
	 public final void showOverlay() {
        mTempTv = (TextView)((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.temp_overlay, (ViewGroup) null, false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        lp.gravity = Gravity.CENTER;
        lp.format = mTempTv.getBackground().getOpacity();
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        lp.privateFlags |= WindowManager.LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS;
        mTempTv.setText("The battery temperature is above " + 55 + "\u2103" + "！");
        mWindowManager.addView(mTempTv, lp);
    }

    private void initGoogleKeyWatermark() {
        if (Settings.System.getInt(getContentResolver(), "google_key_state", 0) != 1) {
// Log.d(TAG, "tqy+++++google_key_state=0");
            mGooglekeyTv = (TextView)((LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.googlekey_overlay, (ViewGroup) null, false);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            mGooglekeyParam = layoutParams;
            layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;;
            mGooglekeyParam.width = WindowManager.LayoutParams.WRAP_CONTENT;;
            mGooglekeyParam.height = WindowManager.LayoutParams.WRAP_CONTENT;;
          //  mGooglekeyParam.x = 0;
          //  mGooglekeyParam.y = 0;
            mGooglekeyParam.setTitle("TestVersion");
            mGooglekeyParam.type = WindowManager.LayoutParams.TYPE_SECURE_SYSTEM_OVERLAY;;
            mGooglekeyParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
         //   if (ActivityManager.isHighEndGfx()) {
                mGooglekeyParam.privateFlags |= WindowManager.LayoutParams.SYSTEM_FLAG_SHOW_FOR_ALL_USERS;;
         //   }
            mGooglekeyParam.format = mGooglekeyTv.getBackground().getOpacity();
            mGooglekeyParam.alpha = GOOGLE_KEY_WATER_MARK_ALPHA;
//            setGoogleKeyWatermarkVisible(true);
        }
    }

    private void setGoogleKeyWatermarkVisible(boolean visible) {
        if (visible) {
            mGooglekeyTv.setVisibility(View.VISIBLE);
            if (!mIsAddGooglekeyView) {
                mWindowManager.addView(mGooglekeyTv, mGooglekeyParam);
                mIsAddGooglekeyView = true;
                return;
            }
            mWindowManager.updateViewLayout(mGooglekeyTv, mGooglekeyParam);
            return;
        }
        mGooglekeyTv.setVisibility(View.GONE);
        if (mIsAddGooglekeyView) {
            mWindowManager.updateViewLayout(mGooglekeyTv, mGooglekeyParam);
        }
    }

    private void updateGoogleKeyWatermark() {
        if (mIsAddGooglekeyView) {
            mGooglekeyTv.setText(R.string.no_wr_key);
            mWindowManager.updateViewLayout(mGooglekeyTv, mGooglekeyParam);
        }
    }

    private void removeGoogleKeyWatermark() {
        if (mWindowManager != null && mTempTv != null) {
            mWindowManager.removeView(mTempTv);
        }
    }

}
