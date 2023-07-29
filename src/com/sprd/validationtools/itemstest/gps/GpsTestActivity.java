
package com.sprd.validationtools.itemstest.gps;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

public class GpsTestActivity extends BaseActivity {
    private static final String TAG = "GpsTestActivity";
    /**
     * GPS satellite count info name
     */
    public static final String GPS_SATELLITE_COUNT_NAME = "gpsSatelliteCount";

    /**
     * GPS test flag info name
     */
    public static final String GPS_TEST_FLAG_NAME = "gpsTestFlag";

    /**
     * the GPS settings package name
     */
    private static final String GPSSET_PACKAGE_NAME = "com.android.settings";

    /**
     * the GPS settings class name
     */
    private static final String GPSSET_CLASS_NAME = "com.android.settings.Settings$LocationSettingsActivity";

    /**
     * GPS provider name
     */
    private static final String PROVIDER = LocationManager.GPS_PROVIDER;

    /**
     * location update min time
     */
    private static final long UPDATE_MIN_TIME = 1000;

    /**
     * satellite min count for OK
     */
    private static final int SATELLITE_COUNT_MIN = 4;

    /** time count max : 5 minutes */
    // private static final int TIME_COUNT_MAX = 300;
    /**
     * time count length : 1 second
     */
    private static final int TIME_LENGTH = 1000;

    /**
     * location manager object
     */
    private LocationManager manager = null;

    /**
     * location listener object
     */
    private LocationListener locationListener = null;

    /**
     * the text view object for show gps not enabled message
     */
    private TextView txtGpsMsg = null;

    /**
     * the button that show gps settings activity
     */
    private Button btnShow = null;

    /**
     * timer object
     */
    private Timer timer = null;

    /**
     * not time left count
     */
    private int timeCount = 0;

    /**
     * max satellite count that have been searched
     */
    private int mSatelliteCount;

    /**
     * extra gps test result
     **/
    public static final String EXTRA_SET_FACTORY_SET_GPS_RESULT = "EXTRA_SET_FACTORY_SET_GPS_RESULT";

    /**
     * extra auto test result
     **/
    public static final String INTENT_EXTRA_KEY_GPS_SEARCH_STAR_COUNT = "intentExtraGpsSearchStarCount";

    /**
     * the success result of the gps test
     */
    private static final byte RESULT_SUCCESS = 1;

    /**
     * the f result of the gps test
     */
    private static final byte RESULT_FAILURE = 0;

    //UNISOC:Bug1621317 Validationtools targetSdkVersion need 31 on Android S
    private GnssStatus.Callback mGnssStatusListener;
    private GnssStatus mGnssStatus;

    private TextView mSatelliteInfo;
    public Handler mHandler = new Handler();
	private boolean mIsPass = false;////

    private Runnable mR = new Runnable() {
        public void run() {
            Toast.makeText(GpsTestActivity.this, R.string.text_fail,
                    Toast.LENGTH_SHORT).show();
            storeRusult(Const.isAutoTestMode());
            finish();
        }
    };

    @Override
    public void onClick(View v) {
        mHandler.removeCallbacks(mR);
        super.onClick(v);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.gps_test_main);
        setTitle(R.string.gps_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        txtGpsMsg = (TextView) findViewById(R.id.txt_gps_not_enabled);
        mSatelliteInfo = (TextView) findViewById(R.id.txt_gps_satellite_info);
        mSatelliteInfo.setText("\n\n");
        mHandler.postDelayed(mR, 60000);
		
		mPassButton.setVisibility(View.GONE);//
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer();
        showGpsMsg();
        showSatelliteCount();
    }

    @Override
    public void onPause() {
        cancelTimer();
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
            }

            public void onProviderDisabled(String provider) {
                showGpsMsg();
            }

            public void onProviderEnabled(String provider) {
                showGpsMsg();
            }

            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }
        };
        Log.d(TAG, "startTest begin!");
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        setTitle(R.string.gps_title_text);
        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                LocationManager.GPS_PROVIDER, true);
        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    UPDATE_MIN_TIME, 0, locationListener);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.gps_open_err),
                    Toast.LENGTH_SHORT).show();
        }
        mGnssStatusListener = new GnssStatus.Callback() {

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                mGnssStatus = status;
                showSatelliteCount();
            }
        };
        manager.registerGnssStatusCallback(mGnssStatusListener);
    }

    @Override
    public void onStop() {
        Settings.Secure.setLocationProviderEnabled(getContentResolver(),
                LocationManager.GPS_PROVIDER, false);
        if (mGnssStatusListener != null) {
            manager.unregisterGnssStatusCallback(mGnssStatusListener);
        }
        if (locationListener != null) {
            manager.removeUpdates(locationListener);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mR);
        super.onDestroy();
    }

    private boolean isGpsEnabled() {
        if (manager == null) {
            return false;
        }
        return manager.isProviderEnabled(PROVIDER);
    }

    private void showGpsMsg() {
        if (!isGpsEnabled()) {
            // gps not enabled
            txtGpsMsg.setText(getString(R.string.gps_not_enabled_msg));
        } else {
            txtGpsMsg.setText("");
        }
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void showSatelliteCount() {
		if(mIsPass){
			return ;
		}
        boolean flag = false;

        if (mGnssStatus == null) {
            return;
        }
        // get satellite count
        mSatelliteInfo.setText("\n\n");
        final int length = mGnssStatus.getSatelliteCount();
        int svCount = 0;
        int svid;
        float cn0DbHz;
        Log.d(TAG, "length = " + length);
        while (svCount < length) {
            svid = mGnssStatus.getSvid(svCount);
            cn0DbHz = mGnssStatus.getCn0DbHz(svCount);
            svCount++;
            if (cn0DbHz > 35.0)
                flag = true;
            mSatelliteInfo.append("id: ");
            mSatelliteInfo.append(String.valueOf(svid));
            mSatelliteInfo.append("\nsnr: ");
            mSatelliteInfo.append(String.valueOf(cn0DbHz));
            mSatelliteInfo.append("\n\n");
        }
        // satellite count is ok
        if (length >= SATELLITE_COUNT_MIN && flag) {
				mPassButton.setVisibility(View.VISIBLE);////
            Toast.makeText(GpsTestActivity.this, R.string.text_pass,
                    Toast.LENGTH_SHORT).show();
            storeRusult(true);
				mIsPass = true;
            //    finish();
        }
        // save max satellite count that have been searched
        if (length > mSatelliteCount) {
            mSatelliteCount = length;
        }
        // show count
        TextView txtCount = (TextView) findViewById(R.id.txt_gps_satellite_count);
        txtCount.setText(" " + mSatelliteCount);
    }

    private class TimerCountTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                public void run() {
                    // show time count
                    TextView txtTime = (TextView) findViewById(R.id.txt_gps_time_count);
                    txtTime.setText(timeCount + " ");
                }
            });

            timeCount++;
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerCountTask(), 0, TIME_LENGTH);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // This method must be implemented, oherwise the test item will be supported by default
    public static boolean isSupport(Context context) {
        LocationManager mgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null) {
            return false;
        }
        List<String> providers = mgr.getAllProviders();
        if (providers == null) {
            return false;
        }
        return providers.contains(LocationManager.GPS_PROVIDER);
    }

}
