
package com.sprd.validationtools.itemstest.wifi;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.WcndUtils;
import com.sprd.validationtools.utils.WifiTestUtil;

public class WifiTestActivity extends BaseActivity {
    private static final String TAG = "WifiTestActivity";

    private static final int BAND_5_GHZ_START_FREQ_MHZ = 5160;
    private static final int BAND_5_GHZ_END_FREQ_MHZ = 5865;
    private TextView tvWifiAddr = null;
    private TextView tvWifiState = null;
    private TextView tvWifiDeviceList = null;

    private WifiTestUtil wifiTestUtil = null;
    private boolean mFindFlag = false;
    public Handler mHandler = new Handler();
    private WifiManager mWifiManager = null;
    private boolean mIs5GHzBandSupported = false;

    /*SPRD bug 848729:Marlin3 support*/
    private static final int TIMEOUT  = 20000;
    private Runnable mR = new Runnable() {
        public void run() {
            if (mFindFlag) {
                Toast.makeText(WifiTestActivity.this, R.string.text_pass,
                        Toast.LENGTH_SHORT).show();
                storeRusult(true);
            } else {
                Toast.makeText(WifiTestActivity.this, R.string.text_fail,
                        Toast.LENGTH_SHORT).show();
                storeRusult(false);
            }
            mHandler.removeCallbacks(mR);
            wifiTestUtil.stopTest();
//            finish();
        }
    };

    @Override
    public void onClick(View v){
        mHandler.removeCallbacks(mR);
        wifiTestUtil.stopTest();
        super.onClick(v);
    }

    @Override
    protected void onDestroy() {
        WcndUtils.dumpCPLog();
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.wifi_test_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle(R.string.wifi_test);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiTestUtil = new WifiTestUtil(mWifiManager) {

            public void wifiStateChange(int newState) {
                Log.d(TAG, "wifiStateChange newState="+newState);
                switch (newState) {
                    case WifiManager.WIFI_STATE_ENABLED:
                        tvWifiState.setText("Wifi ON,Discovering...");
                        mIs5GHzBandSupported = mWifiManager.is5GHzBandSupported();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        tvWifiState.setText("Wifi OFF");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        tvWifiState.setText("Wifi Closing");
                        break;
                    case WifiManager.WIFI_STATE_ENABLING:
                        tvWifiState.setText("Wifi Opening");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                    default:
                        tvWifiState.setText("Wifi state Unknown");
                        // do nothing
                        break;

                }
            }

            public void wifiDeviceListChange(List<ScanResult> wifiDeviceList) {
                if (wifiDeviceList == null) {
                    return;
                }

                boolean mFind5GFlag = false;
                boolean mFind24GFlag = false;
                int count = 0;
                tvWifiDeviceList.setText("");
                Log.d(TAG, "wifiDeviceListChange mIs5GHzBandSupported="+mIs5GHzBandSupported);
                /* UNISOC: BUG1448763 wifi test fail @{ */
                for (ScanResult result : wifiDeviceList) {
                    tvWifiDeviceList.append("device name: ");
                    tvWifiDeviceList.append(result.SSID);
                    tvWifiDeviceList.append("\nsignal level: ");
                    tvWifiDeviceList.append(String.valueOf(result.level));
                    tvWifiDeviceList.append("\n\n");
                    Log.d(TAG, "wifiDeviceListChange result="+result.toString());
                    if(mIs5GHzBandSupported){
                        int frequency = result.frequency;
                        Log.d(TAG, "wifiDeviceListChange frequency" + frequency);
                        if(is5GHz(frequency)){
                            Log.d(TAG, "is support 5GHz");
                            mFind5GFlag = true;
                        }else{
                            mFind24GFlag = true;
                        }
                        mFindFlag = mFind5GFlag && mFind24GFlag;
						mPassButton.setVisibility(View.VISIBLE);////
                    }else{
                        mFindFlag = true;
                    }
                    /* @} */
                    if (Const.isAutoTestMode()) {
                        count++;
                        if (count >= 2) {
                            mFindFlag = true;
                            mHandler.post(mR);
                        }
                    }
                }
            }
        };

        tvWifiAddr = (TextView) findViewById(R.id.wifi_addr_content);
        tvWifiState = (TextView) findViewById(R.id.wifi_state_content);
        tvWifiDeviceList = (TextView) findViewById(R.id.tv_wifi_device_list);

        tvWifiAddr.setText(wifiTestUtil.getWifiManager().getConnectionInfo().getMacAddress());
		mPassButton.setVisibility(View.GONE);//
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mR, TIMEOUT);
        //mHandler.postDelayed(mR, 10000);
        wifiTestUtil.startTest(this);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mR);
        wifiTestUtil.stopTest();
        super.onPause();
    }
    /* UNISOC: BUG1448763 wifi test fail @{ */
    private boolean is5GHz(int freqMhz) {
        return freqMhz >=  BAND_5_GHZ_START_FREQ_MHZ && freqMhz <= BAND_5_GHZ_END_FREQ_MHZ;
    }
    /* @} */
}