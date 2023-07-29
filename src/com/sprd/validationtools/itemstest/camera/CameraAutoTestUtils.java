/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.itemstest.camera;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.sprd.validationtools.utils.StorageUtil;

public class CameraAutoTestUtils {
    public static final String TAG = "CameraAutoTestUtils";

    public static boolean checkPhoto(String fileName) {
        Log.d(TAG, "check photo is black or not");
        String filePath = StorageUtil.getInternalStoragePath() + "/mmi/" + fileName;
        Bitmap bitmap = null;
        int height = 0;
        int width = 0;
        boolean isBlack = true;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            bitmap = BitmapFactory.decodeStream(fis);
            if (bitmap == null) {
                return false;
            }
            height = bitmap.getHeight();
            width = bitmap.getWidth();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int all = height * width;
        int blackCount = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = bitmap.getPixel(x, y);
                int alpha = Color.alpha(color);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                if (red < 50 && green < 50 && blue < 50) {
                    blackCount++;
                } else {
                    break;
                }
            }
        }
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
        Log.d(TAG, "all = " + all + "blackCount = " + blackCount);
        if (blackCount == all) {
            Log.d(TAG, "is black");
            isBlack = false;
        }
        return isBlack;
    }

}