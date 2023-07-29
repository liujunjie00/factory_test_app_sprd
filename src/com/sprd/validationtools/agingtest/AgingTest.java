package packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.sprd.validationtools.R;
import android.content.Context;
import android.widget.Button;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import java.io.File;

public class AgingTest extends Activity {
    private Context mContext;
    private Button mPlayButton;
    private Button mRebootButton;
    private Button mRecoveryButton;
    private CheckBox mCheckBoxPlayVideo;
    private CheckBox mCheckBoxMic;
    private CheckBox mCheckBoxFlash;
    private CheckBox mCheckBoxBackCamera;
    private CheckBox mCheckBoxFrontCamera;
    private CheckBox mCheckBoxReboot;
    private TextView mTvFlash;
    private EditText mEditTextPlayVideo;
    private EditText mEditTextReboot;
    private SharedPreferences mSharedPreferences;
    private boolean mIsPlayVideoSp;
    private boolean mIsRebootSp;
    private int mPlayVideoTime;
    private int mRebootCount;
    private static final String TAG = "cyc";
    private Integer mEdtPlayVideoValue;
    private Integer mEdtRebootValue;
    private TextView mTvVideoRunTime;
    private TextView mTvMicRunTime;
    private TextView mTvFlashRunTime;
    private TextView mTvBackCameraRunTime;
    private TextView mTvFrontCameraRunTime;
    private TextView mTvRunCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aging);
        setTitle("老化测试");
        mContext = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPlayButton = findViewById(R.id.btn_play);
        mRebootButton = findViewById(R.id.btn_reboot);
        mRecoveryButton = findViewById(R.id.btn_recovery);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, PlayVideoTest.class));
            }
        });

        mRebootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, RebootTest.class));
            }
        });

        mRecoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, RecoveryTest.class));
            }
        });

        mCheckBoxPlayVideo = findViewById(R.id.cb_play_video);
        mCheckBoxMic = findViewById(R.id.cb_mic);
        mCheckBoxFlash= findViewById(R.id.cb_flash);
        mCheckBoxBackCamera = findViewById(R.id.cb_back_camera);
        mCheckBoxFrontCamera = findViewById(R.id.cb_front_camera);
        mCheckBoxReboot = findViewById(R.id.cb_reboot);
        mEditTextPlayVideo = findViewById(R.id.edt_play_video);
        mEditTextReboot = findViewById(R.id.edt_reboot);
        mTvVideoRunTime = findViewById(R.id.tv_video_runtime);
        mTvMicRunTime = findViewById(R.id.tv_mic_runtime);
        mTvFlashRunTime = findViewById(R.id.tv_flash_runtime);
        mTvBackCameraRunTime = findViewById(R.id.tv_back_camera_runtime);
        mTvFrontCameraRunTime = findViewById(R.id.tv_front_camera_runtime);
        mTvRunCount = findViewById(R.id.tv_run_count);
        mTvFlash= findViewById(R.id.tv_flash);

        boolean hasSystemFeatureFlash = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        Log.d(TAG, "hasSystemFeatureFlash: " + hasSystemFeatureFlash);
        if (hasSystemFeatureFlash) {
            mCheckBoxFlash.setVisibility(View.VISIBLE);
            mTvFlash.setVisibility(View.VISIBLE);
        }

        mSharedPreferences = getSharedPreferences("aging", 0);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean("isPlayVideoSp", false);
        edit.putBoolean("isRebootSp", false);
        edit.commit();
//        mIsPlayVideoSp = mSharedPreferences.getBoolean("isPlayVideoSp", false);
//        mIsRebootSp = mSharedPreferences.getBoolean("isRebootSp", false);
//        mPlayVideoTime = mSharedPreferences.getInt("playVideoTime", 120);
//        mRebootCount = mSharedPreferences.getInt("rebootCount", 300);
        initCheckBoxListener();

        closeFlash();
        clearMicOutFile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String runPlayVideoTime = mSharedPreferences.getString("runPlayVideoTime", "00:00:00");
        String runMicTime = mSharedPreferences.getString("runMicTime", "00:00:00");
        String runFlashTime = mSharedPreferences.getString("runFlashTime", "00:00:00");
        String runBackCameraTime = mSharedPreferences.getString("runBackCameraTime", "00:00:00");
        String runFrontCameraTime = mSharedPreferences.getString("runFrontCameraTime", "00:00:00");
        int runRebootCount = mSharedPreferences.getInt("runRebootCount", 0);

        mTvVideoRunTime.setText(runPlayVideoTime);
        mTvMicRunTime.setText(runMicTime);
        mTvFlashRunTime.setText(runFlashTime);
        mTvBackCameraRunTime.setText(runBackCameraTime);
        mTvFrontCameraRunTime.setText(runFrontCameraTime);
        mTvRunCount.setText(String.valueOf(runRebootCount));
    }

    private void initCheckBoxListener() {
        mCheckBoxPlayVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mCheckBoxPlayVideo: " + isChecked);
            }
        });

        mCheckBoxMic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mCheckBoxMic: " + isChecked);
            }
        });

        mCheckBoxFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mCheckBoxFlash: " + isChecked);
            }
        });

        mCheckBoxBackCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mCheckBoxBackCamera: " + isChecked);
            }
        });

        mCheckBoxFrontCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mCheckBoxFrontCamera: " + isChecked);
            }
        });

        mCheckBoxReboot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "mCheckBoxReboot: " + isChecked);
            }
        });
    }

    public void startAgingTest(View view) {
        Log.d(TAG, "startAgingTest mCheckBoxPlayVideo: " + mCheckBoxPlayVideo.isChecked());
        Log.d(TAG, "startAgingTest mCheckBoxMic: " + mCheckBoxMic.isChecked());
        Log.d(TAG, "startAgingTest mCheckBoxFlash: " + mCheckBoxFlash.isChecked());
        Log.d(TAG, "startAgingTest mCheckBoxBackCamera: " + mCheckBoxBackCamera.isChecked());
        Log.d(TAG, "startAgingTest mCheckBoxFrontCamera: " + mCheckBoxFrontCamera.isChecked());
        Log.d(TAG, "startAgingTest mCheckBoxReboot: " + mCheckBoxReboot.isChecked());

        if (TextUtils.isEmpty(mEditTextPlayVideo.getText().toString()) || TextUtils.isEmpty(mEditTextReboot.getText().toString())) {
            Toast.makeText(mContext, "请输入正确的值", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mEdtPlayVideoValue = Integer.valueOf(mEditTextPlayVideo.getText().toString());
            mEdtRebootValue = Integer.valueOf(mEditTextReboot.getText().toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Log.d(TAG, "input error");
            Toast.makeText(mContext, "请输入正确的值", Toast.LENGTH_LONG).show();
            return;
        }


        if (mEdtPlayVideoValue <= 0 || mEdtRebootValue <= 0) {
            Toast.makeText(mContext, "请输入正确的值", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "edtPlayVideoValue: " + mEdtPlayVideoValue);
        Log.d(TAG, "edtRebootValue: " + mEdtRebootValue);
        boolean isBasicTask = mCheckBoxPlayVideo.isChecked() || mCheckBoxMic.isChecked() || mCheckBoxFlash.isChecked() || mCheckBoxBackCamera.isChecked() || mCheckBoxFrontCamera.isChecked();
        Log.d(TAG, "isBasicTask: " + isBasicTask);
        if (isBasicTask && mCheckBoxReboot.isChecked()) {
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("isPlayVideoSp", true);
//            edit.putBoolean("isRebootSp", true);
            // TODO: 2023/6/11
            edit.commit();
            Intent intent = new Intent();
            intent.putExtra("basicTaskTime", mEdtPlayVideoValue);
            intent.putExtra("isPlayVideo", mCheckBoxPlayVideo.isChecked());
            intent.putExtra("isMic", mCheckBoxMic.isChecked());
            intent.putExtra("isFlash", mCheckBoxFlash.isChecked());
            intent.putExtra("isBackCamera", mCheckBoxBackCamera.isChecked());
            intent.putExtra("isFrontCamera", mCheckBoxFrontCamera.isChecked());

            intent.putExtra("rebootCount", mEdtRebootValue);
            intent.putExtra("isReboot", true);
            intent.setComponent(new ComponentName(mContext, PlayVideoTest.class));
            startActivity(intent);
        } else if (isBasicTask && !mCheckBoxReboot.isChecked()) {
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("isPlayVideoSp", true);
            edit.commit();
            Intent intent = new Intent();
            intent.putExtra("basicTaskTime", mEdtPlayVideoValue);
            intent.putExtra("isPlayVideo", mCheckBoxPlayVideo.isChecked());
            intent.putExtra("isMic", mCheckBoxMic.isChecked());
            intent.putExtra("isFlash", mCheckBoxFlash.isChecked());
            intent.putExtra("isBackCamera", mCheckBoxBackCamera.isChecked());
            intent.putExtra("isFrontCamera", mCheckBoxFrontCamera.isChecked());
            intent.setComponent(new ComponentName(mContext, PlayVideoTest.class));
            startActivity(intent);
        } else if (!isBasicTask && mCheckBoxReboot.isChecked()) {
            SharedPreferences.Editor edit = mSharedPreferences.edit();
            edit.putBoolean("isRebootSp", true);
            edit.commit();
            Intent intent = new Intent();
            intent.putExtra("rebootCount", mEdtRebootValue);
            intent.putExtra("isReboot", true);
            intent.setComponent(new ComponentName(mContext, RebootTest.class));
            startActivity(intent);
        }
    }

    public void settingsAgingTest(View view) {
        mCheckBoxPlayVideo.setChecked(true);
        mCheckBoxMic.setChecked(true);
        mCheckBoxFlash.setChecked(true);
        mCheckBoxBackCamera.setChecked(true);
        mCheckBoxFrontCamera.setChecked(true);
        mCheckBoxReboot.setChecked(true);
        mEditTextPlayVideo.setText("120");
        mEditTextReboot .setText("300");

        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean("isPlayVideoSp", false);
        edit.putBoolean("isRebootSp", false);
        edit.putString("runPlayVideoTime", "00:00:00");
        edit.putString("runMicTime", "00:00:00");
        edit.putString("runFlashTime", "00:00:00");
        edit.putString("runBackCameraTime", "00:00:00");
        edit.putString("runFrontCameraTime", "00:00:00");
        edit.putInt("runRebootCount", 0);
        edit.commit();

        mTvVideoRunTime.setText("00:00:00");
        mTvMicRunTime.setText("00:00:00");
        mTvFlashRunTime.setText("00:00:00");
        mTvBackCameraRunTime.setText("00:00:00");
        mTvFrontCameraRunTime.setText("00:00:00");
        mTvRunCount.setText(String.valueOf(0));

        closeFlash();
        clearMicOutFile();
    }

    private void closeFlash() {
        try {
            Settings.Secure.putInt(mContext.getContentResolver(), Settings.Secure.FLASHLIGHT_ENABLED, 0);
            CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            cameraManager.setTorchMode("0", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearMicOutFile() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp");
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

