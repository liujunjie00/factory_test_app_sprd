/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

public class SpwCameraTestActivity extends BaseActivity implements
        TextureView.SurfaceTextureListener {
    private static final String TAG = "SpwCameraTestActivity";
    private Handler mHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    private CaptureRequest.Builder mPreviewBuilder;
    private TextureView mPreviewView;
    private CameraDevice mCameraDevice;

    private String mCameraID;

    private CameraCaptureSession mSession;

    private ImageReader mImageReader;

    private Button mTakePhotoBtn;
    private static final String TAKE_IMAGE_NAME = "spw_backphoto.jpg";

    protected boolean mNeedThumb = false;
    protected ImageReader mThumbnailReader;
    private CameraManager mCameraManager;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the
     * camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "onImageAvailable");
            mHandler.post(new ImageSaver(reader.acquireNextImage(), new
             File(Util.TAKE_IMAGE_PATH + TAKE_IMAGE_NAME)));
        }
    };
    private Runnable mTimeOut = new Runnable() {
        public void run() {
            SpwCameraTestActivity.this.storeRusult(false);
            SpwCameraTestActivity.this.finish();
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
                    storeRusult(CameraAutoTestUtils.checkPhoto(TAKE_IMAGE_NAME));
                    finish();
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.back_camera_result);
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        mPreviewView = (TextureView) findViewById(R.id.surfaceView);
        mPreviewView.setSurfaceTextureListener(this);
        TextView mLightMsg = (TextView) findViewById(R.id.light_msg);
        mLightMsg.setText(R.string.secondary_msg_text);
        mLightMsg.setVisibility(View.GONE);
        /* UNISOC: Bug1559859 Get camera ID dynamically @{ */
        mCameraID = getCameraID();
        Log.d(TAG, "onCreate mCameraID=" + mCameraID);
        if(mCameraID == null) {
            Toast.makeText(SpwCameraTestActivity.this, "SpwCamera Open Fialed!",
                    Toast.LENGTH_SHORT).show();
            storeRusult(false);
            finish();
        }
        /* @} */
        mTakePhotoBtn = (Button) findViewById(R.id.start_take_picture);
        mTakePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureStillPicture();
            }
        });
        mTakePhotoBtn.setVisibility(View.GONE);
        startBackgroundThread();
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mHandler = new Handler(mBackgroundThread.getLooper());
        mHandler.postDelayed(mTimeOut, 120000);
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
            mHandler = null;
        } catch (NullPointerException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In
        // that case, we can open a camera and start preview from here (otherwise, we wait until the
        // surface is ready in the SurfaceTextureListener).
        if (mPreviewView.isAvailable()) {
            Log.d(TAG, "onResume isAvailable reopen camera");
            openCamera(mCameraID);
        } else {
            mPreviewView.setSurfaceTextureListener(this);
        }
        super.onResume();
    }

    private void captureStillPicture() {
        try {
            final CaptureRequest.Builder captureBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                        CaptureRequest request, TotalCaptureResult result) {
                    session.close();
                    session = null;
                }
            };
            Log.d(TAG, "capture  in camera");
            mSession.capture(captureBuilder.build(), CaptureCallback, mHandler);
        } catch (NullPointerException | CameraAccessException e) {
            Log.d(TAG, "capture a picture1 fail" + e.toString());
        }
    }

    private void openCamera(String cameraId) {
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                Toast.makeText(SpwCameraTestActivity.this, "no CloseLock",
                        Toast.LENGTH_LONG).show();
                return;
            }
            mImageReader = ImageReader.newInstance(2592, 1944, ImageFormat.JPEG, 1);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                    mHandler);
            Log.d(TAG, "openCamera openCamera cameraId:"
                    + cameraId);
            mCameraManager.openCamera(cameraId, mCameraDeviceStateCallback,
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
        Log.d(TAG, "onSurfaceTextureAvailable");
        openCamera(mCameraID);
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
                mSession.setRepeatingRequest(mPreviewBuilder.build(),
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
        closeCamera();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTimeOut);
        }
        if (mTakePhotoHandler != null) {
            mTakePhotoHandler.removeCallbacksAndMessages(null);
        }
        stopBackgroundThread();
        super.onDestroy();
    }
    /* UNISOC: Bug1559859 Get camera ID dynamically @{ */
    private String getCameraID() {
        try {
            String[] cameraIdList = mCameraManager.getCameraIdList();
            if(cameraIdList.length >= 4) {
                return cameraIdList[3];
            }
        } catch (CameraAccessException e) {
            Log.d(TAG, "CameraAccessException" + e.toString());
        }
        return null;
    }
    /* @} */
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
        Log.d(TAG, "SpwCameraTestActivity numberOfCameras=" + numberOfCameras);
        if (numberOfCameras <= 3) {
            return false;
        } else {
            return true;
        }
    }
}
