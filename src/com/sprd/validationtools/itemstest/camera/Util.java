/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */

package com.sprd.validationtools.itemstest.camera;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;
import com.sprd.validationtools.itemstest.camera.Tuple;

public class Util {

    public static final String TAG = "Util";
    public static final String TAKE_IMAGE_PATH = "/storage/emulated/0/mmi/";
    public static final String PROP_RES_BOKEH = "persist.vendor.cam.res.bokeh";
    public static enum RES_NAME{
        RES_0_3M,
        RES_2M,
        RES_1080P,
        RES_5M,
        RES_8M,
        RES_12M,
        RES_13M
    };
    public static final int[][] ALL_SUPPORT_RES_SIZE = {
        {640, 480},//RES_0_3M
        {1600, 1200},//RES_2M
        {1920, 1080},//RES_1080P
        {2592, 1944},//RES_5M
        {3264, 2448},//RES_8M
        {4000, 3000},//RES_12M
        {4160, 3120},//RES_13M
    };
    public static RES_NAME getSupportResName(Context context){
        String type = SystemPropertiesProxy.get(PROP_RES_BOKEH, "RES_5M");
        Log.d(TAG, "getSupportResName type="+type);
        if(type.equals("RES_0_3M")){
            return RES_NAME.RES_0_3M;
        }else if(type.equals("RES_2M")){
            return RES_NAME.RES_2M;
        }else if(type.equals("RES_1080P")){
            return RES_NAME.RES_1080P;
        }else if(type.equals("RES_5M")){
            return RES_NAME.RES_5M;
        }else if(type.equals("RES_8M")){
            return RES_NAME.RES_8M;
        }else if(type.equals("RES_12M")){
            return RES_NAME.RES_12M;
        }else if(type.equals("RES_13M")){
            return RES_NAME.RES_13M;
        }else{
            Log.w(TAG, "getSupportResName wrong type="+type);
            return RES_NAME.RES_8M;
        }
    }
    public static int[] getSupportBokehSize(Context context){
        RES_NAME name = getSupportResName(context);
        Log.d(TAG, "getSupportBokehSize name="+name);
        int index = name.ordinal();
        Log.d(TAG, "getSupportBokehSize index="+index);
        return ALL_SUPPORT_RES_SIZE[index];
    }

    public static Tuple<Integer, Integer> getOptimalSize(
            int sWidth, int sHeight, int width, int height, boolean screen) {
        width = Math.max(width, height);
        height = Math.min(width, height);
        double ratio = (1 / (((double) width) / ((double) height)));
        return getOptimalSize(sWidth, sHeight, ratio, screen);
    }

    public static Tuple<Integer, Integer>
            getOptimalSize(int screenWidth, int screenHeight, double ratio, boolean screen) {
        Tuple<Integer, Integer> result =
                new Tuple<Integer, Integer>(screenWidth, screenHeight);
        int max = -1, min = -1, width = -1, height = -1;
        if (ratio > 1D)
            ratio = (1 / ratio);

        if (screen) {
            max = Math.max(screenWidth, screenHeight);
            min = ((int) (max * ratio));
            if (screenWidth < screenHeight) {
                width = min;
                height = max;
            } else {
                height = min;
                width = max;
            }
        }
        else {
            min = Math.min(screenWidth, screenHeight);
            max = ((int) (min / ratio));
            if (screenWidth > screenHeight) {
                width = max;
                height = min;
            } else {
                width = min;
                height = max;
            }
        }
        if (screenWidth != width || screenHeight != height) {
            result = new Tuple<Integer, Integer>(width, height);
        }
        return result;
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

    }
    public static int getOrientation(int rotation, int sensorOrientation) {
        return (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360;
    }
}
