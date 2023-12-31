/*
 * SPDX-FileCopyrightText: 2016-2023 Unisoc (Shanghai) Technologies Co., Ltd
 * SPDX-License-Identifier: LicenseRef-Unisoc-General-1.0
 */
package com.sprd.validationtools.background;

import java.util.Iterator;

import android.content.Context;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.sprd.validationtools.itemstest.gps.GpsTestActivity;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.modules.UnitTestItemList;

public class BackgroundGpsTest implements BackgroundTest {
    private static final String TAG = "BackgroundGpsTest";
    private LocationListener locationListener = null;
    private LocationManager manager = null;
    private GnssStatus.Callback mGnssStatusListener;
    private GnssStatus mGnssStatus;
    private int testResult = RESULT_INVALID;
    private static final long UPDATE_MIN_TIME = 1000;
    private static final int SATELLITE_COUNT_MIN = 4;
    private Context mContext = null;

    private static final String TEST_CLASS_NAME = GpsTestActivity.class.getName();

    public BackgroundGpsTest(Context context) {
        mContext = context;
    }

    @Override
    public void startTest() {
        locationListener = new LocationListener() {

            public void onLocationChanged(Location location) {
            }

            public void onProviderDisabled(String provider) {

            }

            public void onProviderEnabled(String provider) {
            }

            public void onStatusChanged(String provider, int status,
                    Bundle extras) {
            }
        };

        mGnssStatusListener = new GnssStatus.Callback() {

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;
                boolean flag = false;
                if (mGnssStatus == null) {
                    return;
                }
                final int length = mGnssStatus.getSatelliteCount();
                int svCount = 0;
                float cn0DbHz;
                Log.d(TAG, "length = " + length);
                while (svCount < length) {
                    cn0DbHz = mGnssStatus.getCn0DbHz(svCount);
                    svCount++;
                    if (cn0DbHz > 35.0)
                        flag = true;
                }
                if (length >= SATELLITE_COUNT_MIN && flag) {
                    testResult = RESULT_PASS;
                    stopTest();
                }
            }
        };
        Log.d(TAG, "startTest begin!");
        manager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        Settings.Secure.setLocationProviderEnabled(
                mContext.getContentResolver(), LocationManager.GPS_PROVIDER,
                true);
        try {
            Log.d(TAG, "startTest requestLocationUpdates");
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    UPDATE_MIN_TIME, 0, locationListener);
            manager.registerGnssStatusCallback(mGnssStatusListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopTest() {
        Log.d(TAG, "stopTest!");
        if (mGnssStatusListener != null) {
            manager.unregisterGnssStatusCallback(mGnssStatusListener);
        }
        if (locationListener != null) {
            manager.removeUpdates(locationListener);
        }
    }

    @Override
    public int getResult() {
        return testResult;
    }

    @Override
    public String getResultStr() {
        String btResult = "GPS:";
        if (RESULT_PASS == testResult) {
            btResult += "PASS";
        } else {
            btResult += "FAIL";
        }

        return btResult;
    }

    @Override
    public int getTestItemIdx() {
        return -1;
    }

    @Override
    public TestItem getTestItem(Context context) {
        return UnitTestItemList.getInstance(mContext).getTestItemByClassName(TEST_CLASS_NAME);
    }
}
