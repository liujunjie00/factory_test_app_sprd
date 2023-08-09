package com.sprd.validationtools.itemstest.camera;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.itemstest.AutoListItemTestActivity;
import com.sprd.validationtools.utils.FileUtils;
import android.view.Window;
import packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest.CameraProxy;
import packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest.CameraTextureView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest.ImageUtil;

public class CameraTestActivity extends BaseActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = "CameraTestActivity";

    private CaptureRequest.Builder mPreviewBuilder;
    private TextureView mPreviewView;
    private CameraDevice mCameraDevice;
    private CaptureRequest mPreviewRequest;

    private String mCameraID = "0";
    private int mCameraSensorOrientation = 0;

    private CameraCaptureSession mSession;

    private ImageReader mImageReader;
    private static final String TAKE_IMAGE_NAME = "backphoto.jpg";

    protected boolean mNeedThumb = false;
    protected ImageReader mThumbnailReader;
    private int mCameraPictures;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the
     * camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable");
//            mHandler.post(new ImageSaver(reader.acquireNextImage(), new File(Util.TAKE_IMAGE_PATH + TAKE_IMAGE_NAME)));
//            Toast.makeText(CameraTestActivity.this, Util.TAKE_IMAGE_PATH + TAKE_IMAGE_NAME, Toast.LENGTH_SHORT).show();
			  if(mPassButton != null){
                mPassButton.setVisibility(View.VISIBLE);
				}
        }
    };

    private TextView mLightMsg = null;
    private static int mDisplayRotation;

    private boolean isSurportCameraFlash = false;
    private static final int CAMERA_FLASH_LIGHT = 0;
    private static final int CAMERA_SECOND_FLASH_LIGHT = 1;
    private static final int CAMERA_FLASH_LIGHT_CLOSE = 2;
    private static final int CAMERA_SECOND_FLASH_LIGHT_CLOSE = 3;
    private static final int CAMERA_TIME_OUT = 4;
    private boolean mIsPause = false;
    private static final boolean IS_SUPPORT_FLASHLIGHT = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CAMERA_FLASH_LIGHT:
                openFlashLight(CAMERA_FLASH_LIGHT);
                break;
            case CAMERA_SECOND_FLASH_LIGHT:
                openFlashLight(CAMERA_SECOND_FLASH_LIGHT);
                break;
            case CAMERA_FLASH_LIGHT_CLOSE:
                closeFlashLight(CAMERA_FLASH_LIGHT);
                break;
            case CAMERA_SECOND_FLASH_LIGHT_CLOSE:
                closeFlashLight(CAMERA_SECOND_FLASH_LIGHT);
                break;
            case CAMERA_TIME_OUT:
                CameraTestActivity.this.storeRusult(false);
                CameraTestActivity.this.finish();
                break;
            }
            super.handleMessage(msg);
        }
    };
    private static final int TAKE_PHOTO = 0;
    private static final int CHECK_PHOTO = 1;
    private Handler mTakePhotoHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TAKE_PHOTO:
                    mTakePhotoBtn.performClick();
                    mTakePhotoHandler.sendEmptyMessageDelayed(CHECK_PHOTO, 5000);
                    break;
                case CHECK_PHOTO:
  //                  storeRusult(CameraAutoTestUtils.checkPhoto(TAKE_IMAGE_NAME));
  //                  finish();
                default:
                    break;
            }
        }
    };

    private Runnable mTimeOut = new Runnable() {
        public void run() {
        }
    };

    private Button mTakePhotoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getResources().getText(R.string.back_camera_title_text));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.back_camera_result);
        setTitle(R.string.camera_test_title);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        mPreviewView = (TextureView) findViewById(R.id.surfaceView);
//        mLightMsg = (TextView)findViewById(R.id.light_msg);
//        mDisplayRotation = getWindowManager().getDefaultDisplay().getRotation();
//        Log.d(TAG, "onCreate mCameraID=" + mCameraID);
        /*BEGIN 2016/04/13 zhijie.yang BUG535005 mmi add take photes of camera test */
//        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
//        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                captureStillPicture();
//            }
//        });;
//        Log.d(TAG, "oncreate IS_SUPPORT_FLASHLIGHT="+IS_SUPPORT_FLASHLIGHT);
//        if (IS_SUPPORT_FLASHLIGHT) {
       //     startDoubleFlashLightTest();
//        }
        /* @} */
//        startBackgroundThread();
		mPassButton.setVisibility(View.INVISIBLE);//
//        View view = findViewById(R.id.fl);
//        view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCameraPictures = getCameraPictures();
//                mPassButton.setVisibility(View.INVISIBLE);
//                Log.d(TAG, "mCameraPictures: " + mCameraPictures);
//                Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//                startActivityForResult(intent, 1);
//            }
//        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                initCameraView();
            }
        },2000);
    }
    private CameraTextureView mCameraView;
    private CameraProxy mCameraProxy;
    private View mTakePicture;
    private View mOpenCameraFail;

    private void initCameraView() {
        mCameraView = findViewById(R.id.camera_view);
        mTakePicture = findViewById(R.id.btn_take_picture);
        mOpenCameraFail = findViewById(R.id.tv_openfailed);

        mCameraProxy = mCameraView.getCameraProxy();
        if (mCameraProxy != null) {
            mOpenCameraFail.setVisibility(mCameraProxy.getCameraNum() <= 0 ? View.VISIBLE : View.GONE);
        } else {
            mOpenCameraFail.setVisibility(View.VISIBLE);
        }

        mTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraProxy.takePicture(mPictureCallback);
            }
        });
    }

    private final Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken: " + data.length);
            if (data.length > 0) {
                mPassButton.setVisibility(View.VISIBLE);
            }
//            mCameraProxy.startPreview(mCameraView.getSurfaceTexture()); // 拍照结束后继续预览
            /*long time = System.currentTimeMillis();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            int rotation = mCameraProxy.getLatestRotation();
            time = System.currentTimeMillis();
            ImageUtil.getImageUtil(getApplicationContext());
            Bitmap rotateBitmap = ImageUtil.rotateBitmap(bitmap, rotation, mCameraProxy.isFrontCamera(), true);
            time = System.currentTimeMillis();
            ImageUtil.saveBitmap(rotateBitmap);*/// TODO: 2023/6/3
        }
    };

    private int getCameraPictures() {
        File file = new File("/sdcard/Pictures");
        File[] files = file.listFiles();
        return files.length;
    }

    private void deleteCameraPictures() {
        File file = new File("/sdcard/Pictures");
        File[] files = file.listFiles();
        try {
            for (File file1 : files) {
                file1.delete();
            }
            mCameraPictures = files.length;
            Log.d(TAG, "deletCameraPictures files: " + files.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: " + requestCode + "," + resultCode);
//        if (requestCode == 1) {
//            int cameraPictures = getCameraPictures();
//            Log.d(TAG, "cameraPictures: " + cameraPictures);
//            if (cameraPictures > mCameraPictures) {
//                mPassButton.setVisibility(View.VISIBLE);
//            }
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Handler mCameraHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mCameraHandler = new Handler(mBackgroundThread.getLooper());
        mCameraHandler.postDelayed(mTimeOut, 120000);
        if (Const.isAutoTestMode()) {
            mTakePhotoHandler.sendEmptyMessageDelayed(TAKE_PHOTO, 5000);
        }
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mCameraHandler = null;
        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* SPRD Bug 744113:adjust the test plan of the flashlight. @{ */
    private void openFlashLightByCameraId(int cameraId) {
        if (mIsPause) return;
        Log.d(TAG, "openFlashLightByCameraId cameraId="+cameraId);
        if (cameraId == CameraCharacteristics.LENS_FACING_BACK) {
            writeFlashDev("0x20");
        } else {
            writeFlashDev("0x10");
        }
    }
    /* @} */

    private void startDoubleFlashLightTest() {
        mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT, 300);
    }

    private void openFlashLight(int type) {
        if (mIsPause) return;
        if (type == CAMERA_FLASH_LIGHT) {
            writeFlashDev("0x10");
            mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT_CLOSE, 1000);
        } else {
            writeFlashDev("0x20");
            mHandler.sendEmptyMessageDelayed(CAMERA_SECOND_FLASH_LIGHT_CLOSE, 1000);
        }
    }

    private void closeFlashLight(int type) {
        if (type == CAMERA_FLASH_LIGHT) {
            writeFlashDev("0x11");
            mHandler.sendEmptyMessageDelayed(CAMERA_SECOND_FLASH_LIGHT, 1000);
        } else {
            writeFlashDev("0x21");
            mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT, 1000);
        }
    }

    private void writeFlashDev(String cmd) {
        FileUtils.writeFile(Const.CAMERA_FLASH, cmd);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mPreviewView.isAvailable()) {
//            Log.d(TAG, "onResume isAvailable reopen camera");
//            openCamera(mCameraID);
//        } else {
//            mPreviewView.setSurfaceTextureListener(this);
//        }
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void captureStillPicture() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, com.sprd.validationtools.itemstest.camera.Util.getOrientation(rotation, mCameraSensorOrientation));
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                        CaptureRequest request, TotalCaptureResult result) {
                    try {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        mSession.setRepeatingRequest(mPreviewRequest, null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            };
            Log.d(TAG, "capture  in camera");
            mSession.stopRepeating();
            mSession.capture(captureBuilder.build(), CaptureCallback, mHandler);
        } catch (NullPointerException | CameraAccessException e) {
            Log.d(TAG, "capture a picture1 fail" + e.toString());
        }
    }

    private void openCamera(String cameraId) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Toast.makeText(CameraTestActivity.this, "no CloseLock",
                        Toast.LENGTH_LONG).show();
                return;
            }
            mImageReader = ImageReader.newInstance(1600, 1200, ImageFormat.JPEG, 1);
            mCameraSensorOrientation = cameraManager.getCameraCharacteristics(mCameraID).get(CameraCharacteristics.SENSOR_ORIENTATION);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                    mHandler);
            Log.d(TAG, "openCamera openCamera cameraId:"
                    + cameraId);
            cameraManager.openCamera(cameraId, mCameraDeviceStateCallback,
                    mHandler);
            Log.d(TAG, "openCamera openCamera end");
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException" + e.toString());
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException" + e.toString());
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
            int height) {
//        Log.d(TAG, "onSurfaceTextureAvailable");
//        openCamera(mCameraID);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
            int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            Log.i(TAG, "CameraDevice.StateCallback onOpened in");
            mCameraDevice = camera;
            startPreview(camera);
            Log.i(TAG, "CameraDevice.StateCallback onOpened out");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onDisconnected");
            if (mThumbnailReader != null) {
                Log.i(TAG, "mThumbnailReader.close");
                mThumbnailReader.close();
                mThumbnailReader = null;
            }
            mNeedThumb = false;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            Log.i(TAG, "CameraDevice.StateCallback1 stay onError");
        }
    };

    private void startPreview(CameraDevice camera) {
        Log.i(TAG, "start preview ");
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        texture.setDefaultBufferSize(960, 720);
        Surface surface = new Surface(texture);
        try {
            mPreviewBuilder = camera
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.i(TAG, e.toString());
        }

        mPreviewBuilder.addTarget(surface);

        if (mThumbnailReader != null) {
            mThumbnailReader.close();
        }
        try {
            if (mNeedThumb) {
                mThumbnailReader = ImageReader.newInstance(320, 240,
                        ImageFormat.YUV_420_888, 1);
                camera.createCaptureSession(Arrays.asList(surface,
                        mImageReader.getSurface(),
                        mThumbnailReader.getSurface()),
                        mCameraCaptureSessionStateCallback, mHandler);
            } else {
                camera.createCaptureSession(
                        Arrays.asList(surface, mImageReader.getSurface()),
                        mCameraCaptureSessionStateCallback, mHandler);
            }
        } catch (CameraAccessException ex) {
            Log.e(TAG, "Failed to create camera capture session", ex);
        }
    }

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                mSession = session;
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewRequest = mPreviewBuilder.build();
                mSession.setRepeatingRequest(mPreviewRequest,
                        mSessionCaptureCallback, mHandler);

            } catch (CameraAccessException e) {
                Log.i(TAG, e.toString());
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }

        @Override
        public void onActive(CameraCaptureSession session) {

        }
    };
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                CaptureRequest request, TotalCaptureResult result) {
            mSession = session;
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session,
                CaptureRequest request, CaptureResult partialResult) {
            mSession = session;
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
//        closeCamera();
    }

    @Override
    public void onDestroy() {
//        if (mHandler != null) {
//            mHandler.removeCallbacks(mTimeOut);
//        }
//        if (IS_SUPPORT_FLASHLIGHT) {
//            mHandler.removeMessages(CAMERA_FLASH_LIGHT);
//            mHandler.removeMessages(CAMERA_SECOND_FLASH_LIGHT);
//            mHandler.removeMessages(CAMERA_FLASH_LIGHT_CLOSE);
//            mHandler.removeMessages(CAMERA_SECOND_FLASH_LIGHT_CLOSE);
//        }
//        /* SPRD bug 753892 : Flashlight maybe not close */
//        try {
//            writeFlashDev("0x11");
//            writeFlashDev("0x21");
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//        /* @} */
//        if (mTakePhotoHandler != null) {
//            mTakePhotoHandler.removeCallbacksAndMessages(null);
//        }
//        stopBackgroundThread();
//        deleteCameraPictures();
        super.onDestroy();
    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mSession) {
                mSession.close();
                mSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            Log.d(TAG, "InterruptedException" + e.toString());
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public static String bytesToChar(byte[] bytes, int offset) {
        char a = (char) (bytes[offset] & 0xFF);
        char b = (char) (bytes[offset + 1] & 0xFF);
        char c = (char) (bytes[offset + 2] & 0xFF);
        char d = (char) (bytes[offset + 3] & 0xFF);
        String s = new String(new char[] { a, b, c, d });
        return s;
    }

    private static class ImageSaver implements Runnable {

        private final Image mImage;

        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                if (!mFile.getParentFile().exists()) {
                    mFile.getParentFile().mkdirs();
                }
                if (!mFile.exists()) {
                    mFile.createNewFile();
                }
                output = new FileOutputStream(mFile);
                output.write(bytes);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        int numberOfCameras = android.hardware.Camera.getNumberOfCameras();
        Camera.CameraInfo[] info = new Camera.CameraInfo[numberOfCameras];
        for (int i = 0; i < numberOfCameras; i++) {
            info[i] = new Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(i, info[i]);
            if (info[i].facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                return true;
            }
        }
        return false;
    }
}
