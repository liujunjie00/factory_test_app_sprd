package com.sprd.validationtools.itemstest.telephony;

import java.util.ArrayList;
import java.util.List;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.sprd.validationtools.nonpublic.SubscriptionManagerProxy;
import com.sprd.validationtools.nonpublic.TelephonyManagerProxy;

public class SIMCardTestActivity extends BaseActivity {

    private LinearLayout container;
    private static final String TAG = "SIMCardTestActivity";

    private TelephonyManager mTelMgr;
    private SubscriptionManagerProxy mSubscriptionManagerProxy;
    private TelephonyManagerProxy mTelephonyManagerProxy;
    private int mSimReadyCount = 0;
    private int phoneCount;
    public Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sim_card_test);
        setTitle(R.string.sim_card_test_tittle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        container = (LinearLayout) findViewById(R.id.sim_test_result_container);
        setTitle(R.string.sim_card_test_tittle);
        mSubscriptionManagerProxy = new SubscriptionManagerProxy(getApplicationContext());
        mTelephonyManagerProxy = new TelephonyManagerProxy(getApplicationContext());
        mTelMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        phoneCount = mTelMgr.getActiveModemCount();
        Log.d(TAG, "phoneCount=" + phoneCount);
        showDevice();
            mPassButton.setVisibility(View.GONE);
        Log.d(TAG, "mSimReadyCount=" + mSimReadyCount);
        if (phoneCount == mSimReadyCount) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SIMCardTestActivity.this,
                            R.string.text_pass, Toast.LENGTH_SHORT).show();
                    /*@}*/
                    storeRusult(true);
					mPassButton.setVisibility(View.VISIBLE);
              //      finish();
                }
            }, 2000);
        } else {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Const.isAutoTestMode() && getResultList(0) != null) {
                        storeRusult(true);
						mPassButton.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(SIMCardTestActivity.this, R.string.text_fail, Toast.LENGTH_SHORT).show();
                        storeRusult(false);
                    }
                    finish();
                }
            }, 1000);
        }
    //    super.removeButton();
    }
    /* UNISOC:Bug 1506527 java.lang.IndexOutOfBoundsException @{ */
    private List<String> getResultList(int simId) {
        List<String> resultList = new ArrayList<String>();
        mTelMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE + simId);
        if (mTelMgr == null) {
            mTelMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (mTelMgr == null) {
                return null;
            }
        }
        int mSubIds[] = mSubscriptionManagerProxy.getSubId(simId);
        if(mSubIds == null || mSubIds.length <= 0) return null;
        int mSubId = mSubIds[0];
        /* SPRD:436223 Sim test wrong in sigle sim in the phone@{*/
        if (mTelMgr.getSimState(simId) == TelephonyManager.SIM_STATE_READY) {
            resultList.add("fine");
            mSimReadyCount++;
        } else if (mTelMgr.getSimState(simId) == TelephonyManager.SIM_STATE_ABSENT) {
            resultList.add("no SIM card");
        } else {
            resultList.add("locked/unknow");
        }

        if (mTelephonyManagerProxy.getSimCountryIsoForPhone(simId).equals("")) {
            resultList.add("can not get country");
        } else {
            resultList.add(mTelephonyManagerProxy.getSimCountryIsoForPhone(simId));
        }

        if (mTelephonyManagerProxy.getSimOperatorNumericForPhone(simId).equals("")) {
            resultList.add("can not get operator");
        } else {
            resultList.add(mTelephonyManagerProxy.getSimOperatorNumericForPhone(simId));
        }

        if (mTelephonyManagerProxy.getSimOperatorNameForPhone(simId).equals("")) {
            resultList.add("can not get operator name");
        } else {
            resultList.add(mTelephonyManagerProxy.getSimOperatorNameForPhone(simId));
        }

        if (mTelephonyManagerProxy.getSimSerialNumber(mSubId) != null && SubscriptionManager.isValidSubscriptionId(mSubId)) {
            resultList.add(mTelephonyManagerProxy.getSimSerialNumber(mSubId));
        } else {
            resultList.add("can not get serial number");
        }

        if (mTelephonyManagerProxy.getSubscriberId(mSubId) != null && SubscriptionManager.isValidSubscriptionId(mSubId)) {
            resultList.add(mTelephonyManagerProxy.getSubscriberId(mSubId));
        } else {
            resultList.add("can not get subscriber id");
        }

        if (mTelMgr.getDeviceId(simId) != null) {
            resultList.add(mTelMgr.getDeviceId(simId));
        } else {
            resultList.add("can not get device id");
        }

        if (mTelephonyManagerProxy.getLine1Number(mSubId) != null && SubscriptionManager.isValidSubscriptionId(mSubId)) {
            resultList.add(mTelephonyManagerProxy.getLine1Number(mSubId));
        } else {
            resultList.add("can not get phone number");
        }

        if (mTelephonyManagerProxy.getPhoneType(simId) == 0) {
            resultList.add("NONE");
        } else if (mTelephonyManagerProxy.getPhoneType(simId) == 1) {
            resultList.add("GSM");
        } else if (mTelephonyManagerProxy.getPhoneType(simId) == 2) {
            resultList.add("CDMA");
        } else if (mTelephonyManagerProxy.getPhoneType(simId) == 3) {
            resultList.add("SIP");
        } else {
            resultList.add("unknow");
        }
        /* @}*/

        if (mTelMgr.getDataState() == 0) {
            resultList.add("disconnected");
        } else if (mTelMgr.getDataState() == 1) {
            resultList.add("connecting");
        } else if (mTelMgr.getDataState() == 2) {
            resultList.add("connected");
        } else if (mTelMgr.getDataState() == 3) {
            resultList.add("suspended");
        } else {
            resultList.add("unknow");
        }

        if (mTelMgr.getDataActivity() == 0) {
            resultList.add("none");
        } else if (mTelMgr.getDataActivity() == 1) {
            resultList.add("in");
        } else if (mTelMgr.getDataActivity() == 2) {
            resultList.add("out");
        } else if (mTelMgr.getDataActivity() == 3) {
            resultList.add("in/out");
        } else if (mTelMgr.getDataActivity() == 4) {
            resultList.add("dormant");
        } else {
            resultList.add("unknow");
        }

        if (!mTelMgr.getNetworkCountryIso(simId).equals("")) {
            resultList.add(mTelMgr.getNetworkCountryIso(simId));
        } else {
            resultList.add("can not get network country");
        }

        if (!mTelephonyManagerProxy.getNetworkOperatorForPhone(simId).equals("")) {
            resultList.add(mTelephonyManagerProxy.getNetworkOperatorForPhone(simId));
        } else {
            resultList.add("can not get network operator");
        }

        if (mTelephonyManagerProxy.getNetworkType(mSubId) == 1) {
            resultList.add("gprs");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 2) {
            resultList.add("edge");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 3) {
            resultList.add("umts");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 4) {
            resultList.add("hsdpa");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 5) {
            resultList.add("hsupa");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 6) {
            resultList.add("hspa");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 7) {
            resultList.add("cdma");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 8) {
            resultList.add("evdo 0");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 9) {
            resultList.add("evdo a");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 10) {
            resultList.add("evdo b");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 11) {
            resultList.add("1xrtt");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 12) {
            resultList.add("iden");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 13) {
            resultList.add("lte");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 14) {
            resultList.add("ehrpd");
        } else if (mTelephonyManagerProxy.getNetworkType(mSubId) == 15) {
            resultList.add("hspap");
        } else {
            resultList.add("unknow");
        }
        return resultList;
    }
    /* @}*/

    private List<String> getKeyList() {
        List<String> keyList = new ArrayList<String>();
        keyList.add("Sim State:  ");
        keyList.add("Sim Country:  ");
        keyList.add("Sim Operator:  ");
        keyList.add("Sim Operator Name:  ");
        keyList.add("Sim Serial Number:  ");
        keyList.add("Subscriber Id:  ");
        keyList.add("Device Id:  ");
        keyList.add("Line 1 Number:  ");
        keyList.add("Phone Type:  ");
        keyList.add("Data State:  ");
        keyList.add("Data Activity:  ");
        keyList.add("Network Country:  ");
        keyList.add("Network Operator:  ");
        keyList.add("Network Type:  ");
        return keyList;
    }

    private void showDevice() {

        List<String> keyList = getKeyList();
        List<String> resultList0 = null;

        for (int i = 0; i < phoneCount; i++) {
            TextView tv = new TextView(this);
            if (i != 0) {
                tv.append("\n\n");
            }
            tv.append("Sim" + (i + 1) + " "
                    + this.getResources().getString(R.string.sim_test_result)
                    + ":\n");
            resultList0 = getResultList(i);
            for (int j = 0; j < 14; j++) {
                if (resultList0 != null)
                    tv.append(keyList.get(j) + resultList0.get(j) + "\n");
                else
                    tv.append(keyList.get(j) + "\n");
            }
            container.addView(tv);
        }
    }
}
