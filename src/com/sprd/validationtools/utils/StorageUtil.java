/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import android.content.Context;
import android.os.Environment;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.os.storage.IStorageManager;
import android.os.UserHandle;
import android.util.Log;
import com.sprd.validationtools.nonpublic.ServiceManagerProxy;

public class StorageUtil {
    public static final String TAG = "StorageUtil";
    private static final String emulatedPathPrefix = "/storage/emulated";
    private static final int EXT_EMULATED_PATH = 0;
    private static final int EXT_COMMON_PATH = 1;
    private static final int OTG_UDISK_PATH = 2;

    /*
     * type:0 --- External storage(SD card) emulated app directory.
     * type:1 --- External storage(SD card) common app directory.
     * type:2 --- USB mass storage(OTG U disk) app directory.
     */
    public static String getExternalStorageAppPath(Context context, int type) {
        String extEmulatedPath = null;
        String extCommonPath = null;
        String extOtgUdiskPath = null;
        String otgUdiskPath = null;

        List<File> allDirPaths = new ArrayList<>();
        Collections.addAll(allDirPaths, context.getExternalFilesDirs(null));

        try {
            File[] otgPaths = getUsbdiskVolumePaths();
            for (File file : otgPaths) {
                if (file != null && Environment.MEDIA_MOUNTED.equals(getUsbdiskVolumeState(file))) {
                    Log.d(TAG, "otg udisk mounted, otg path is " + file.getPath());
                    otgUdiskPath = file.getPath();
                } else {
                    Log.i(TAG, "otg udisk not mounted, otg path is null");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (File file : allDirPaths) {
            if (file != null) {
                String path = file.getAbsolutePath();
                if (path.startsWith("/storage/emulated/0")) {
                    Log.d(TAG, "external storage emulated path is: " + path);
                    extEmulatedPath = path;
                } else {
                    Log.d(TAG, "external storage common path is: " + path);
                    extCommonPath = path;
                }
            }
        }

        if (type == EXT_EMULATED_PATH) {
            return extEmulatedPath;
        } else if (type == EXT_COMMON_PATH) {
            return extCommonPath;
        } else if (type == OTG_UDISK_PATH) {
            return otgUdiskPath;
        }else{
            Log.w(TAG, "type is incorrect!");
            return null;
        }
    }

    public static File[] getUsbdiskVolumePaths() {
        int count;
        List<VolumeInfo> vols = getUsbdiskVolumes();
        count = vols.size();
        final File[] files = new File[count];
        for (int i = 0; i < count; i++) {
            files[i] = vols.get(i).getPath();
        }
        return files;
    }

    public static List<VolumeInfo> getUsbdiskVolumes() {
        List<VolumeInfo> vols;
        final ArrayList<VolumeInfo> res = new ArrayList<>();
        final IStorageManager storageManager = IStorageManager.Stub.asInterface(ServiceManagerProxy.getService("mount"));
        try {
            vols = Arrays.asList(storageManager.getVolumes(0));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
        for (VolumeInfo vol : vols) {
            if (vol.disk != null && vol.disk.isUsb()) {
                res.add(vol);
            }
        }
        return res;
    }

    public static String getUsbdiskVolumeState(File path) {
        List<VolumeInfo> vols = getUsbdiskVolumes();
        for (VolumeInfo vol : vols) {
            if (FileUtils.contains(vol.getPath(), path))
                return VolumeInfo.getEnvironmentForState(vol.getState());
        }
        return Environment.MEDIA_UNKNOWN;
    }


    public static File getExternalStoragePath(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        List<StorageVolume> volumes = storageManager.getStorageVolumes();
        for (StorageVolume volume : volumes) {
            File volumePath = volume.getDirectory();
            Log.i(TAG,"getExternalStoragePath volumePath : " + volumePath);
            if (volumePath != null && volume.isRemovable() &&
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState(volumePath))) {
                Log.i(TAG,"really createAccessIntent for : " + volumePath);
                return volumePath;
            }
        }
        return null;
    }

    public static String getExternalStoragePathState(Context context) {
        File volumePath = getExternalStoragePath(context);
        if (volumePath != null) {
            return Environment.getExternalStorageState(volumePath);
        }
        return Environment.MEDIA_UNKNOWN ;
    }


    public static File getInternalStoragePath() {
        File internalPath = new File(emulatedPathPrefix + "/" + UserHandle.myUserId());
        return internalPath;
    }

}