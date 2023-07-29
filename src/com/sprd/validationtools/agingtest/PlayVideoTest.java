package packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest;

import android.app.Activity;
import android.app.IForegroundServiceObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.SurfaceView;
import android.widget.ImageView;
import android.view.SurfaceHolder;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import com.sprd.validationtools.R;
import android.view.WindowManager;
import android.media.AudioManager;
import android.content.Context;
import android.view.Window;
import android.widget.Chronometer;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.widget.RelativeLayout;

public class PlayVideoTest extends Activity {

    private SurfaceView surfaceView;
    private ImageView mStartAndStop;
    private MediaPlayer mPlayer;
    private static final String TAG = "cyc";
    private SharedPreferences mSharedPreferences;
    private Context mContext;
    private boolean isInitFinish = false;
    private AudioManager audioManager;
    private Chronometer mChronometer;
    private RelativeLayout mRlPlayVideo;
    private int mBasicTaskTime;
    private boolean mIsReboot;
    private int mRebootCount;
    private int mHour;
    private int mMin;
    private boolean mIsPlayVideo;
    private boolean mIsMic;
    private boolean mIsFlash;
    private boolean mIsBackCamera;
    private boolean mIsFrontCamera;
    private final static int MSG_PLAY_VIDEO = 101;
    private final static int MSG_MIC = 102;
    private final static int MSG_FLASH = 103;
    private final static int MSG_BACK_CAMERA = 104;
    private final static int MSG_FRONT_CAMERA = 105;
    private final static int MSG_BACK_AND_CAMERA = 106;
    private int mOpenFlash;
    public static final String CAMERA_FLASH = "/sys/devices/virtual/misc/sprd_flash/test";
    public static final String MIC_OUT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.3gp";
    private CameraManager mCameraManager;
    private CameraTextureView mCameraTextureView;
    private CameraProxy mCameraProxy;
    private boolean mIsBackCameraAndFrontCamera;
    private boolean mIsCameraOpenFlash;
    private com.sprd.validationtools.view.VUMeter mVUMeter;
    private MediaRecorder mRecorder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_test);
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        mSharedPreferences = getSharedPreferences("aging", 0);

        mContext = this;
        Intent intent = getIntent();
        mBasicTaskTime = intent.getIntExtra("basicTaskTime", 120);
        mIsPlayVideo = intent.getBooleanExtra("isPlayVideo", false);
        mIsMic = intent.getBooleanExtra("isMic", false);
        mIsFlash = intent.getBooleanExtra("isFlash", false);
        mIsBackCamera = intent.getBooleanExtra("isBackCamera", false);
        mIsFrontCamera = intent.getBooleanExtra("isFrontCamera", false);

        mRebootCount = intent.getIntExtra("rebootCount", 300);
        mIsReboot = intent.getBooleanExtra("isReboot", false);

        Log.d("cyc", "getIntent mBasicTaskTime: " + mBasicTaskTime);
        Log.d("cyc", "getIntent mIsPlayVideo: " + mIsPlayVideo);
        Log.d("cyc", "getIntent mIsMic: " + mIsMic);
        Log.d("cyc", "getIntent mIsFlash: " + mIsFlash);
        Log.d("cyc", "getIntent mIsBackCamera: " + mIsBackCamera);
        Log.d("cyc", "getIntent mIsFrontCamera: " + mIsFrontCamera);

        Log.d("cyc", "getIntent mRebootCount: " + mRebootCount);
        Log.d("cyc", "getIntent mIsReboot: " + mIsReboot);

        mChronometer = findViewById(R.id.play_time);
        mRlPlayVideo = findViewById(R.id.rl_play_video);
        mCameraTextureView = findViewById(R.id.aging_camera_view);
        mVUMeter = findViewById(R.id.vm_mic);

        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    Log.d("cyc", "onChronometerTick Text: " + chronometer.getText());
                    long base = chronometer.getBase();
                    Log.d("cyc", "base: " + base);
                    Log.d("cyc", "elapsedRealtime: " + SystemClock.elapsedRealtime());
                    mHour = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60 / 60);
                    mMin = (int) ((SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000 / 60);
                    Log.d("cyc", "hour: " + mHour);
                    Log.d("cyc", "min: " + mMin);

                    //倒计时大于1小时设置为显示的格式：HH:MM:SS，否则为默认的格式：H:MM:SS
                    if (mHour > 0) chronometer.setFormat("0%s");
                    //倒计时大于9小时设置为显示的格式：HH:MM:SS，否则为格式：0HH:MM:SS
                    if (mHour > 9) chronometer.setFormat("%s");
                    //倒计时小于1小时设置为显示的格式：H:MM:SS，否则为默认格式：MM:SS
                    if (mHour <= 0) chronometer.setFormat("00:%s");

                    saveTestTime(chronometer.getText().toString());

                    if (mMin >= mBasicTaskTime) {
                        if (mIsReboot) {
                        mChronometer.stop();
                        SharedPreferences.Editor edit = mSharedPreferences.edit();
                        edit.putBoolean("isPlayVideoSp", false);
                        edit.putBoolean("isRebootSp", mIsReboot);
                        edit.commit();
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName(mContext, RebootTest.class));
                        intent.putExtra("rebootCount", mRebootCount);
                        intent.putExtra("isReboot", mIsReboot);
                        startActivity(intent);
                        finish();
                        } else {
                            mChronometer.stop();
                            SharedPreferences.Editor edit = mSharedPreferences.edit();
                            edit.putBoolean("isPlayVideoSp", false);
                            edit.commit();
//                            pausePlay();
                            finish();
                        }
                    }
                }
            });
        }

        setMaxStreamVolume();

        if (mIsPlayVideo) {
            mRlPlayVideo.setVisibility(View.VISIBLE);
            initMediaView();
        }

        if (mIsBackCamera && mIsFrontCamera) {
            initCamera();
            if (mCameraProxy != null) {
                mCameraProxy.switchCamera(0);
            }
            mIsBackCameraAndFrontCamera = true;
            mHandler.sendEmptyMessageDelayed(MSG_BACK_CAMERA, 5000);
        } else if (mIsBackCamera){
            initCamera();
            if (mCameraProxy != null) {
                mCameraProxy.switchCamera(0);
            }
            mHandler.sendEmptyMessageDelayed(MSG_BACK_CAMERA, 5000);
        } else if (mIsFrontCamera) {
            initCamera();
            if (mCameraProxy != null) {
                mCameraProxy.switchCamera(1);
            }
            mHandler.sendEmptyMessageDelayed(MSG_FRONT_CAMERA, 5000);
        }

        if (mIsFlash) {
            mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            if (!mIsBackCamera && !mIsFrontCamera) {
                mOpenFlash = 1;
                mHandler.sendEmptyMessage(MSG_FLASH);
            }
        }

        if (mIsMic) {
            mVUMeter.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessage(MSG_MIC);
        }
    }

    private void initCamera() {
        mCameraTextureView.setVisibility(View.VISIBLE);
        mCameraProxy = mCameraTextureView.getCameraProxy();
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLAY_VIDEO:
                    Log.d(TAG, "handleMessage: " + "MSG_PLAY_VIDEO");
                    break;
                case MSG_MIC:
                    try {
                        startRecording();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "handleMessage: " + "MSG_MIC");
                    break;
                case MSG_FLASH:
                    try {
                        Settings.Secure.putInt(mContext.getContentResolver(), Secure.FLASHLIGHT_ENABLED, mOpenFlash);
                        mCameraManager.setTorchMode("0", mOpenFlash == 1 ? true : false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mOpenFlash ^= 1;
                    mHandler.sendEmptyMessageDelayed(MSG_FLASH, 3000);
                    Log.d(TAG, "handleMessage: " + "MSG_FLASH");
                    break;
                case MSG_BACK_CAMERA:
                    Log.d(TAG, "MSG_BACK_CAMERA current switch: " + mCameraProxy.getCameraId());
                    if (mIsFlash && !mIsFrontCamera) {
                        try {
                            mCameraProxy.releaseCamera();
                            Settings.Secure.putInt(mContext.getContentResolver(), Secure.FLASHLIGHT_ENABLED, 1);
                            mCameraManager.setTorchMode("0", true);
                            mIsCameraOpenFlash = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            mIsCameraOpenFlash = false;
                            mCameraProxy.switchCamera(0);
                            mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                        }
                        mHandler.sendEmptyMessageDelayed(MSG_BACK_AND_CAMERA, 1500);
                    } else {
                        mCameraProxy.takePicture(new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                Log.d(TAG, "onPictureTaken MSG_BACK_CAMERA: " + data.length);
                                if (mIsBackCameraAndFrontCamera) {
                                    mCameraProxy.switchCamera(1);
                                    mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                                    mHandler.sendEmptyMessageDelayed(MSG_FRONT_CAMERA, 5000);
                                } else {
                                    mCameraProxy.switchCamera(0);
                                    mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                                    mHandler.sendEmptyMessageDelayed(MSG_BACK_CAMERA, 5000);
                                }
                            }
                        });
                    }
                    break;
                case MSG_FRONT_CAMERA:
                    Log.d(TAG, "MSG_FRONT_CAMERA current switch: " + mCameraProxy.getCameraId());
                    if (mIsFlash) {
                        try {
                            mCameraProxy.releaseCamera();
                            Settings.Secure.putInt(mContext.getContentResolver(), Secure.FLASHLIGHT_ENABLED, 1);
                            mCameraManager.setTorchMode("0", true);
                            mIsCameraOpenFlash = true;
                            mCameraProxy.switchCamera(1);
                            mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                        } catch (Exception e) {
                            e.printStackTrace();
                            mIsCameraOpenFlash = false;
                            mCameraProxy.switchCamera(1);
                            mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                        }
                    }
                        mCameraProxy.takePicture(new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                Log.d(TAG, "onPictureTaken MSG_FRONT_CAMERA: " + data.length);
                                if (mIsFlash && mIsCameraOpenFlash) {
                                    try {
                                        mCameraProxy.releaseCamera();
                                        Settings.Secure.putInt(mContext.getContentResolver(), Secure.FLASHLIGHT_ENABLED, 0);
                                        mCameraManager.setTorchMode("0", false);
                                        mIsCameraOpenFlash = false;
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        mIsCameraOpenFlash = false;
                                    }
                                }
                                if (mIsBackCameraAndFrontCamera) {
                                    mCameraProxy.switchCamera(0);
                                    mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                                    mHandler.sendEmptyMessageDelayed(MSG_BACK_CAMERA, 5000);
                                } else {
                                    mCameraProxy.switchCamera(1);
                                    mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                                    mHandler.sendEmptyMessageDelayed(MSG_FRONT_CAMERA, 5000);
                                }
                            }
                        });
                    break;
                case MSG_BACK_AND_CAMERA:
                    mCameraProxy.switchCamera(0);
                    mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                    mCameraProxy.takePicture(new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera) {
                            Log.d(TAG, "onPictureTaken MSG_BACK_CAMERA: " + data.length);
                            if (mIsFlash && !mIsFrontCamera && mIsCameraOpenFlash) {
                                try {
                                    mCameraProxy.releaseCamera();
                                    Settings.Secure.putInt(mContext.getContentResolver(), Secure.FLASHLIGHT_ENABLED, 0);
                                    mCameraManager.setTorchMode("0", false);
                                    mIsCameraOpenFlash = false;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mIsCameraOpenFlash = false;
                                }
                            }
                            if (mIsBackCameraAndFrontCamera) {
                                mCameraProxy.switchCamera(1);
                                mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                                mHandler.sendEmptyMessageDelayed(MSG_FRONT_CAMERA, 5000);
                            } else {
                                mCameraProxy.switchCamera(0);
                                mCameraProxy.startPreview(mCameraTextureView.getSurfaceTexture());
                                mHandler.sendEmptyMessageDelayed(MSG_BACK_CAMERA, 5000);
                            }
                        }
                    });
                    break;
            }
        }
    };

    private void saveTestTime(String runTime) {
        SharedPreferences.Editor editRun = mSharedPreferences.edit();
        if (mIsPlayVideo) {
            editRun.putString("runPlayVideoTime", runTime);
        }
        if (mIsMic) {
            editRun.putString("runMicTime", runTime);
        }
        if (mIsFlash) {
            editRun.putString("runFlashTime", runTime);
        }
        if (mIsBackCamera) {
            editRun.putString("runBackCameraTime", runTime);
        }
        if (mIsFrontCamera) {
            editRun.putString("runFrontCameraTime", runTime);
        }
        editRun.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mIsPlayVideo) {
            initMediaPalyer();
            initSurfaceviewListener();
            setPlayVideo();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mChronometer.stop();
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putBoolean("isPlayVideoSp", false);
        edit.commit();
        mHandler.removeCallbacksAndMessages(null);
        stopRecording();
        closeFlash();
        if (mCameraProxy != null) {
            mCameraProxy.releaseCamera();
        }
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

    private void initMediaView(){
        surfaceView = (SurfaceView)findViewById(R.id.video_surfaceview);
        mStartAndStop = (ImageView)findViewById(R.id.start_and_stop);
        mStartAndStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.isPlaying()) {
                    pausePlay();
                    mStartAndStop.setImageResource(R.drawable.play);
                } else {
                    startPlay();
                    mStartAndStop.setImageResource(R.drawable.pause);
                }
            }
        });
    }

    private void initMediaPalyer() {
        if(mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
    }

    private void setPlayVideo() {
        try {
            AssetFileDescriptor afd = getResources().getAssets().openFd("test.mp4");
            mPlayer.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());//
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);//缩放模式
            }
            mPlayer.prepareAsync();//异步准备
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { //准备完成回调
                @Override
                public void onPrepared(MediaPlayer mp) {
                    isInitFinish = true;
                    mPlayer.start();
                    mPlayer.setLooping(true);
                    mStartAndStop.setImageResource(R.drawable.pause);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSurfaceviewListener() {

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });

    }

    private void startPlay(){
        if(mPlayer != null) {
            if (!mPlayer.isPlaying()) {
                mPlayer.start();
            }
        }
    }

    private void pausePlay(){
        if(mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
            }
        }
    }

    public void startRecording() throws Exception {
        mRecorder = new MediaRecorder();
        mVUMeter.setRecorder(mRecorder);
        try {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(MIC_OUT_PATH);
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e){
            e.printStackTrace();
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
        mVUMeter.finish();
        clearMicOutFile();
    }

    public void setMaxStreamVolume() {
        audioManager  = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_SHOW_UI);
    }

}
