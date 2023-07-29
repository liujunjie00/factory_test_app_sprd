
package com.sprd.validationtools.itemstest.sysinfo;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import android.view.View;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.TelephonyManagerSprd;
import com.sprd.validationtools.utils.IATUtils;

public class RFCALITest extends BaseActivity {

    private static final String TAG = "RFCALITest";
    //This is only for 9620
    private static final String ADC_PATH = Const.PRODUCTINFO_DIR + "/adc.bin";

    private String str = "loading...";
    private TextView txtViewlabel01;
    private Handler mUiHandler = new Handler();
    private DataInputStream mInputStream=null;
    private static final int ADCBYTES = 56;
    byte[] buffer = new byte[ADCBYTES];
	private int mFlag = 0;//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rf_cali_test);
        setTitle(R.string.rf_cali_test);
        txtViewlabel01 = (TextView) findViewById(R.id.rfc_id);
        txtViewlabel01.setTextSize(18);
        txtViewlabel01.setText(str);
		
		mPassButton.setVisibility(View.GONE);//
        initial();
    }

    private void initial() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int modemType = TelephonyManagerSprd.getModemType();
                Log.d(TAG,"initial modemType="+modemType);
                if (modemType == TelephonyManagerSprd.MODEM_TYPE_TDSCDMA
                        || TelephonyManagerSprd.getRadioCapbility() == TelephonyManagerSprd.RadioCapbility.TDD_CSFB
                        || TelephonyManagerSprd.getRadioCapbility() == TelephonyManagerSprd.RadioCapbility.CSFB ) {
                    str = "GSM/TD ";
                } else {
                    str = "GSM ";
                }
                Log.d(TAG,"initial str="+str);
                str += IATUtils.sendATCmd("AT+SGMR=0,0,3,0", "atchannel0");
                //Support WCDMA
                if (modemType == TelephonyManagerSprd.MODEM_TYPE_WCDMA
                        || TelephonyManagerSprd.getRadioCapbility() == TelephonyManagerSprd.RadioCapbility.FDD_CSFB
                        || TelephonyManagerSprd.getRadioCapbility() == TelephonyManagerSprd.RadioCapbility.CSFB
                        || TelephonyManagerSprd.getRadioCapbility() == TelephonyManagerSprd.RadioCapbility.LWLW
                        /*SPRd bug 830737:Add for support WCDMA*/
                        || TelephonyManagerSprd.IsSupportWCDMA()) {
                    str += "WCDMA ";
                    str += IATUtils.sendATCmd("AT+SGMR=0,0,3,1,1", "atchannel0");
                } else if(modemType == TelephonyManagerSprd.MODEM_TYPE_LTE) {
                    /*SPRD bug 773421:Supprt WCDMA*/
                    if(TelephonyManagerSprd.getRadioCapbility() == TelephonyManagerSprd.RadioCapbility.WG){
                        str += "WCDMA ";
                        str += IATUtils.sendATCmd("AT+SGMR=0,0,3,1,1", "atchannel0");
                    }
                }
                if(modemType == TelephonyManagerSprd.MODEM_TYPE_LTE || TelephonyManagerSprd.IsSupportLTE()) {
                    //WG not support LTE
                    if(TelephonyManagerSprd.getRadioCapbility() != TelephonyManagerSprd.RadioCapbility.WG){
                        str += "LTE ";
                        //New at cmd for LTE band
                        String temp = IATUtils.sendAtCmd("AT+SGMR=1,0,3,3,1");
                        if(!IATUtils.AT_FAIL.equalsIgnoreCase(temp)){
                            str += temp;
                        }else{
                            str += IATUtils.sendATCmd("AT+SGMR=1,0,3,3", "atchannel0");
                        }
                    }
                }
                if(TelephonyManagerSprd.IsSupportCDMA()) {
                    str += "CDMA2000 ";
                    str += IATUtils.sendATCmd("AT+SGMR=0,0,3,2", "atchannel0");
                }
                if(TelephonyManagerSprd.IsSupportNR()) {
                    str += "NR ";
                    str += IATUtils.sendATCmd("AT+SGMR=1,0,3,4", "atchannel0");
                }
				
				try {
				 BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(str.getBytes(Charset.forName("utf8"))), Charset.forName("utf8"))); 
				 String line; 
								 
					while ( (line = br.readLine()) != null ) { 
					 if((line.trim().contains("Not Pass")) || (line.trim().contains("NOT PASS"))){
//		Log.d(TAG, "tqy+++++not pass str="+line.trim());				 
						continue;
					 }
//		Log.d(TAG, "tqy+++++str="+line.trim());
					  mFlag++;
					 }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                mUiHandler.post(new Runnable() {
                    public void run() {
                        showCaliStr(str);
						if(mFlag > 10){
							mPassButton.setVisibility(View.VISIBLE);//
						}
                    }
                });
            }
        }).start();
    }

    /*UNSOC bug1780225:APK MMI califlag display optimization*/
    private void showCaliStr(String str) {
        String allStr = "";
        String[] strs = str.split("\n");
        for (int i=0;i<strs.length;i++) {
            if (strs[i].toLowerCase().contains("pass")){
                if (strs[i].toLowerCase().contains("not")) {
                    allStr += setStringColor(strs[i], "red");
                } else {
                    allStr += setStringColor(strs[i], "green");
                }
            } else if (strs[i].toLowerCase().contains("error")) {
                allStr += setStringColor(strs[i], "red");
            } else {
                allStr += setStringColor(strs[i], "white");
            }
        }
        txtViewlabel01.setText(Html.fromHtml(allStr));
    }

    private String setStringColor(String str, String color) {
        if (color.equals("red")) {
            return "<font color=\"#ff0000\">" + str + "</font><br/>";
        } else if (color.equals("green")) {
            return "<font color=\"#00ff00\">" + str + "</font><br/>";
        } else if (color.equals("white")) {
            return "<font color=\"#ffffff\">" + str + "</font><br/>";
        } else {
            return "<font color=\"#ffff00\">" + str + "</font><br/>";
        }
    }
    /*@}*/

    public boolean readFile() {
        try {
            File adcFile = new File(ADC_PATH);
            int count = 0;
            if (!adcFile.exists()) {
                Log.d(TAG, "adcFile do not exists");
                return false;
            }
            mInputStream = new DataInputStream(new FileInputStream(adcFile));
            if (mInputStream != null) {
                count = mInputStream.read(buffer, 0, ADCBYTES);
            }
            if (buffer == null || buffer.length <= 0 || count < 0) {
                Log.d(TAG, "buffer == null or buffer.length <= 0");
                return false;
            }
            Log.d(TAG, "count = " + count + " size = " + buffer.length);
            int adcBit = buffer.length - 4;
            int adcResult = buffer[adcBit] | 0xFFFFFFFC;
            Log.d(TAG, "adcBit = " + adcBit + " buffer[" + adcBit + "] = 0x"
                    + Integer.toHexString(buffer[adcBit]) + " adcResult = 0x"
                    + Integer.toHexString(adcResult));
            if (adcResult == 0xFFFFFFFF) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed get outputStream: " + e);
            e.printStackTrace();
        }
        return false;
    }  

    @Override
    protected void onDestroy() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }
}
