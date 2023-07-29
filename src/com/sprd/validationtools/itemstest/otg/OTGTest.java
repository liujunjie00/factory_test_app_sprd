
package com.sprd.validationtools.itemstest.otg;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.provider.DocumentsContract;
import android.content.Intent;
import android.net.Uri;
import android.content.UriPermission;
import android.os.ParcelFileDescriptor;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.utils.StorageUtil;
import com.sprd.validationtools.R;
import android.widget.Toast;
import android.os.Message;
import android.view.InputDevice;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.ValidationToolsUtils;

import com.sprd.validationtools.BaseActivity;

import android.view.KeyEvent;//
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;

public class OTGTest extends BaseActivity {
    private static String TAG = "OTGTest";
    private TextView mTextView, mCountDownView;
//    private TextView mTextPlea;
    private StorageManager mStorageManager = null;
    private boolean isUsb = false;
    private String usbMassStoragePath = "/mnt/media_rw/usbdisk";
    private static final String SPRD_OTG_TESTFILE = "otgtest";
    private String otgPath = null;
    public byte mOTGTestFlag[] = new byte[1];
    byte[] result = new byte[1];
    byte[] mounted = new byte[1];
    private static final int UPDATE_TIME = 0;
    private long time = 20;
    private Uri docUri;
    private Uri rootUri;
    private Uri otgUri;
    final static int REQUEST_CODE = 1;
	
	private boolean mTypec_front; 
	private boolean mTypec_back;
    private boolean mOtg_plug;
    private boolean mOtg_unplug;
    private static final int OTG_CHECK = 1001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout OTGLayout = new LinearLayout(this);
        LayoutParams parms = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        OTGLayout.setLayoutParams(parms);
        OTGLayout.setOrientation(1);
        OTGLayout.setGravity(Gravity.CENTER);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mBroadcastReceiver, intentFilter);
        mTextView = new TextView(this);
        mTextView.setTextSize(35);
//        mTextPlea = new TextView(this);
//        mTextPlea.setTextSize(35);
//        mCountDownView = new TextView(this);
//        mCountDownView.setTextSize(15);
//        OTGLayout.addView(mTextPlea);
        OTGLayout.addView(mTextView);
//        OTGLayout.addView(mCountDownView);
        setContentView(OTGLayout);
        setTitle(R.string.otg_test);
//        mTextPlea.setText(getResources().getText(R.string.otg_plea_test));
        mTextView.setText(getResources().getText(R.string.otg_no_devices));
        mHandler.sendEmptyMessageDelayed(OTG_CHECK, 5000);
   //     mCountDownView.setText(time + "");
   //     mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
            mPassButton.setVisibility(View.GONE);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: " + action);
            if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
//                mOtg_plug = true;
//                mTextView.setText(getResources().getText(R.string.otg_plug));
                mTextView.setText(getResources().getText(R.string.otg_no_devices));
                mTextView.setTextColor(Color.WHITE);
                mHandler.removeMessages(OTG_CHECK);
                mHandler.sendEmptyMessageDelayed(OTG_CHECK, 5000);
            } else if (action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)){
//                mOtg_unplug = true;
//                mTextView.setText(getResources().getText(R.string.otg_unplug));
                mTextView.setText(getResources().getText(R.string.otg_no_devices));
                mTextView.setTextColor(Color.WHITE);
                if (mPassButton != null) {
                    mPassButton.setVisibility(View.GONE);
                }
                mHandler.removeMessages(OTG_CHECK);
                mHandler.sendEmptyMessageDelayed(OTG_CHECK, 5000);
            }
//            if (mOtg_plug && mOtg_unplug) {
//                mPassButton.setVisibility(View.VISIBLE);
//            }
        }
    };

    private void checkOTGdevices() {
        /* SPRD Bug 940774:OTG test fail, because permission denied. @{ */
        String otgPath = StorageUtil.getExternalStorageAppPath(getApplicationContext(), Const.OTG_UDISK_PATH);
        if (otgPath != null) {
            mounted[0] = 0;
            Log.i(TAG, "=== OTG mount succeed ===");
            usbMassStoragePath = otgPath;
        } else {
            mounted[0] = 1;
            Log.i(TAG, "=== OTG mount Fail ===");
        }
        /* @} */
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case UPDATE_TIME:
                time--;
                mCountDownView.setText(time + "");
                if(time == 0) {
                    Log.d(TAG, "time out");
                    Toast.makeText(OTGTest.this, R.string.text_fail,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(false);
                    finish();
                } else {
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
                }
                break;
                case OTG_CHECK:
                    init_StoragePath(OTGTest.this);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public Runnable mRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== display OTG test succeed info! ===");
            if (mounted[0] == 0) {
                if (result[0] == 0) {
					if(mTypec_front && !mTypec_back){
						mTextView.setText(getResources().getText(R.string.otg_test_front));
					}else if(mTypec_back && !mTypec_front){
						mTextView.setText(getResources().getText(R.string.otg_test_back));
					}
										if(mTypec_front && mTypec_back ){
						try{
						 mPassButton.setVisibility(View.VISIBLE);
						 mTextView.setText(getResources().getText(R.string.otg_test_success));
						} catch (Exception e) {
						Log.e(TAG, "unregisterReceiver mBatInfoReceiver failure :" + e.getCause());
						}
					}
             //       mTextView.setText(getResources().getText(R.string.otg_test_success));
             //       storeRusult(true);
             //       finish();
                } else {
                    mTextView.setText(getResources().getText(R.string.otg_test_fail));
                    storeRusult(false);
                    finish();
                }
            } else {
                mTextView.setText(getResources().getText(R.string.otg_test_fail));
                storeRusult(false);
                finish();
            }
        }
    };
    public Runnable mCheckRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== checkOTGdevices! ===");
            checkOTGdevices();
            if (mounted[0] != 0) {
                mTextView.setText(getResources().getText(R.string.otg_no_devices));
                mHandler.postDelayed(mCheckRunnable, 1000);
            } else {
           //     if (onScopedDirectoryTest(usbMassStoragePath)){
                    startVtThread();
           //     }
            }
        }
    };
	
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	// TODO Auto-generated method stub
//Log.d(TAG, "tqy++++++dispatchKeyEvent--event.getKeyCode()="+event.getKeyCode());
//Toast.makeText(OTGTest.this, "keycode="+event.getKeyCode(), Toast.LENGTH_SHORT).show();
    	//if (event.getAction() == KeyEvent.ACTION_DOWN) {
    		if (event.getKeyCode() == KeyEvent.KEYCODE_CAMERA) {
    		    mTypec_back = true;
				checkOTG();
				mTextView.setText("back ");
					
    		return true;
    		}else if(event.getKeyCode() == 0){
				mTypec_front = true;
				checkOTG();
				mTextView.setText("front ");
				
			return true;	
			}
    	//}
    	return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*mTextView.setText(getResources().getText(R.string.otg_begin_test));
        mounted[0] = 1;
        result[0] = 1;
        checkOTGdevices();
        if (mounted[0] != 0) {
            mTextView.setText(getResources().getText(R.string.otg_no_devices));
            mHandler.postDelayed(mCheckRunnable, 1000);
        } else {
       //     if (onScopedDirectoryTest(usbMassStoragePath)){
                startVtThread();
       //     }
        }
		mPassButton.setVisibility(View.GONE);//*/
    }
	
	private void checkOTG(){
		checkOTGdevices();
        if (mounted[0] != 0) {
            mTextView.setText(getResources().getText(R.string.otg_no_devices));
            mHandler.postDelayed(mCheckRunnable, 1000);
        } else {
           // if (onScopedDirectoryTest(usbMassStoragePath)){
                startVtThread();
           // }
        }
	}

    @Override
    protected void onPause() {
//        mHandler.removeCallbacks(mCheckRunnable);
//        mHandler.removeMessages(UPDATE_TIME);
        mHandler.removeMessages(OTG_CHECK);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    private void startVtThread() {
        Log.i(TAG, "=== create thread to execute OTG test command! ===");
        /**UNISOC: Bug1360713&1370498 OTG test fail due to storage mechanism changes @{*/
        if (mounted[0] == 0) {
         //   VtThread vtThread = new VtThread();
         //   vtThread.start();
				result[0] = 0;  //
				mHandler.post(mRunnable);//
        }
        /* @} */
    }

    class VtThread extends Thread {
        public void run() {
            FileInputStream in = null;
            FileOutputStream out = null;
            ParcelFileDescriptor pfd = null;
            try {
                if (mounted[0] == 0) {
                    Uri doc = DocumentsContract.buildDocumentUriUsingTree(getCurrentAccessUri(usbMassStoragePath), DocumentsContract.getTreeDocumentId(getCurrentAccessUri(usbMassStoragePath)));
                    docUri = DocumentsContract.createDocument(getContentResolver(), doc, "text/plain", SPRD_OTG_TESTFILE);
                    Log.d(TAG, "docUri= " + docUri);
                    pfd = getContentResolver().openFileDescriptor(docUri, "rw");
                    out = createOutputStream(pfd);
                    mOTGTestFlag[0] = '7';
                    out.write(mOTGTestFlag, 0, 1);
                    out.close();
                    in = createInputStream(pfd);
                    in.read(mOTGTestFlag, 0, 1);
                    in.close();
                    if (mOTGTestFlag[0] == '7') {
                        result[0] = 0;
                    } else {
                        result[0] = 1;
                    }
                }
                mHandler.postDelayed(mRunnable, 2000);
            } catch (Exception e) {
                Log.i(TAG, "=== error: Exception happens when OTG I/O! ===");
                e.printStackTrace();
            } finally {
                try {
                    DocumentsContract.deleteDocument(getContentResolver(), docUri);
                } catch (Exception e) {
                    Log.e(TAG, "deleteDocument exception");
                    e.printStackTrace();
                } finally {
                    closeQuietly(in);
                    closeQuietly(out);
                    closeQuietly(pfd);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        /*if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            otgUri = intent.getData();
            Log.d(TAG,"onActivityResult "+otgUri);
            final int takeFlags = intent.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (otgUri != null) {
                getContentResolver().takePersistableUriPermission(otgUri, takeFlags);
            } else {
                Toast toast = Toast.makeText(this, R.string.external_access_failed, Toast.LENGTH_LONG);
                toast.show();
            }
        }*/
    }
    /*private boolean onScopedDirectoryTest(String storagePath) {
        if (getCurrentAccessUri(storagePath) != null) {
            return true;
        }
        for (StorageVolume volume : getVolumes()) {
            File volumePath = volume.getDirectory();
            Log.d(TAG, "volumePath = " + volumePath);
            if (!volume.isPrimary() && volumePath != null &&
                    StorageUtil.getUsbdiskVolumeState(volumePath).equals(Environment.MEDIA_MOUNTED)) {
                requestScopedDirectoryAccess(volume, null);
            }
        }
        return false;
    }*/

    private void requestScopedDirectoryAccess(StorageVolume volume, String directoryName) {
        final Intent intent = volume.createOpenDocumentTreeIntent();
        if (intent != null) {
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    private List<StorageVolume> getVolumes() {
        final StorageManager sm = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        final List<StorageVolume> volumes = sm.getStorageVolumes();
        return volumes;
    }

    public Uri getCurrentAccessUri(String storagePath) {
        String exactStorageName = getExactStorageName(storagePath);
        List<UriPermission> uriPermissions = getContentResolver().getPersistedUriPermissions();
        Log.i(TAG, "getCurrentAccessUri exactStorageName " + exactStorageName);
        for (UriPermission permission : uriPermissions) {
            Log.i(TAG, "getCurrentAccessUri permission: " + permission.toString());
            if (exactStorageName != null && permission.getUri().toString().contains(exactStorageName)) {
                Log.i(TAG, "getCurrentAccessUri return " + permission.getUri());
                rootUri = permission.getUri();
            }
        }
        Log.i(TAG, "getCurrentAccessUri return rootUri="+rootUri);
        return rootUri;
    }

    private String getExactStorageName(String storagePath) {
        Log.i(TAG, "getExactStorageName, storagePath: " + storagePath);
        String[] pathName;
        if (storagePath!= null) {
            pathName = storagePath.split("/");
            return pathName[pathName.length - 1];
        }
        return null;
    }

    public FileOutputStream createOutputStream(ParcelFileDescriptor pfd) {
        return new FileOutputStream(pfd.getFileDescriptor());
    }

    public FileInputStream createInputStream(ParcelFileDescriptor pfd) {
        return new FileInputStream(pfd.getFileDescriptor());
    }

    private static void closeQuietly(AutoCloseable c) {
        if (c != null) {
            try {
                c.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ex) {
            }
        }
    }
    /**@}*/

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        if (isSupportOTG()) {
            return true;
        }
        if (ValidationToolsUtils.isSupportInPCBAConf("OTG")) {
            return true;
        }
        return true;
    }

    public static boolean isSupportOTG() {
        BufferedReader bReader = null;
        InputStream inputStream = null;
        String otgPath = null;
        if (FileUtils.fileIsExists(Const.OTG_PATH_k414)) {
            otgPath = Const.OTG_PATH_k414;
        } else if (FileUtils.fileIsExists(Const.OTG_PATH_k515)) {
            otgPath = Const.OTG_PATH_k515;
        } else if (FileUtils.fileIsExists(Const.OTG_PATH)) {
            otgPath = Const.OTG_PATH;
        } else {
            return false;
        }
        try {
            inputStream = new FileInputStream(otgPath);
            bReader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()));
            String str = bReader.readLine();
            Log.d(TAG, "OTGTest  str:" + str);
            if (TextUtils.isEmpty(str)) {
                return false;
            }
            if ((!otgPath.equals(Const.OTG_PATH) && str.contains("[dual] source sink"))
                    || (otgPath.equals(Const.OTG_PATH) && str.contains("ufp"))) {
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bReader != null) {
                    bReader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private String flash_dir = Environment.getExternalStorageDirectory().getPath();
    private StorageInfo mTestStorage;

    public void init_StoragePath(Context context) {
        mTestStorage = null;
        mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        flash_dir = Environment.getExternalStorageDirectory().getPath();
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        for (VolumeInfo vol : volumes) {
            if (vol.getType() != VolumeInfo.TYPE_PUBLIC) {
                continue;
            }
            Log.d(TAG, "Volume path:" + vol.getPath());
            DiskInfo disk = vol.getDisk();
            if (null == disk) {
                continue;
            }
//            if (STORAGE_TYPE.PCIE == mStorageType) {
//                if (null != disk.sysPath && disk.sysPath.contains(".pcie/")) {
//                    StorageVolume sv = vol.buildStorageVolume(context,
//                            context.getUserId(), false);
//                    mTestStorage = new StorageInfo(sv.getPath(), disk.sysPath);
//                    break;
//                }
//            } else if (STORAGE_TYPE.SATA == mStorageType) {
//                if (null != disk.sysPath && disk.sysPath.contains(".sata/")) {
//                    StorageVolume sv = vol.buildStorageVolume(context,
//                            context.getUserId(), false);
//                    mTestStorage = new StorageInfo(sv.getPath(), disk.sysPath);
//                    break;
//                }
//            } else if (STORAGE_TYPE.USB_HOST == mStorageType) {
            Log.d(TAG, "flag: " + disk.flags);
            if (disk.isUsb() && null != disk.sysPath
                    && !disk.sysPath.contains(".pcie/")
                    && !disk.sysPath.contains(".sata/")) {
                StorageVolume sv = vol.buildStorageVolume(context,
                        context.getUserId(), false);
                mTestStorage = new StorageInfo(sv.getPath(), disk.sysPath);
                break;
            }
//            } else if (disk.isSd()) {
//
//            }
        }
        if (mTestStorage != null) {
            mTextView.setText(getResources().getText(R.string.otg_test_success));
            mTextView.setTextColor(Color.GREEN);
            if (mPassButton != null) {
                mPassButton.setVisibility(View.VISIBLE);
            }
        } else {
            mTextView.setText(getResources().getText(R.string.otg_test_fail));
            mTextView.setTextColor(Color.RED);
        }
        Log.d(TAG, "mTestStorageInfo: " + mTestStorage);
    }

    class StorageInfo {
        private String path;
        private String sysPath;

        public StorageInfo(String path, String sysPath) {
            this.path = path;
            this.sysPath = sysPath;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSysPath() {
            return sysPath;
        }

        public void setSysPath(String sysPath) {
            this.sysPath = sysPath;
        }

        @Override
        public String toString() {
            return "StorageInfo{" +
                    "path='" + path + '\'' +
                    ", sysPath='" + sysPath + '\'' +
                    '}';
        }
    }

}
