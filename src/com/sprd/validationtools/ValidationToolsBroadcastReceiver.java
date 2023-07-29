package com.sprd.validationtools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;

import android.os.SystemProperties;
import android.os.UserManager;
import android.util.Log;

import com.sprd.validationtools.autommi.AutoMMIService;
import com.sprd.validationtools.PhaseCheckParse;
import android.provider.Settings;
import com.sprd.validationtools.itemstest.ListItemTestActivity;
import com.sprd.validationtools.testinfo.VersionTypeActivity;
import com.sprd.validationtools.testinfo.TestInfoMainActivity2;
import android.text.TextUtils;

public class ValidationToolsBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "ValidationToolsBroadcastReceiver";
    private static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
    private static final String ACTION_LOCKED_BOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";
    private static final String BOOT_MODE = "ro.bootmode";
    private static final String BOOT_MODE_APKMMI = "apkmmi_mode";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive action = " + action);
        if (action == null) return;
        if (action.equals(ACTION_BOOT_COMPLETED)) {
			/*int keyBoxValue = PhaseCheckParse.getInstance().getKeyboxFlag();
			int keyIsHide = Settings.System.getInt(context.getContentResolver(), "google_key_hide", 0);
                Settings.System.putInt(context.getContentResolver(), "google_key_state", keyBoxValue);
     Log.d(TAG, "tqy+++++ getKeyboxFlag= " + keyBoxValue);*/
            try {
                int keyboxHide = Settings.Global.getInt(context.getContentResolver(), "google_keybox_hide", 0);
                String value = android.os.SystemProperties.get("ro.boot.deviceid", "");
                if ((TextUtils.isEmpty(value) || "none".equals(value)) && keyboxHide == 0) {
                    Log.d(TAG, "startService show key window: ");
                    context.startService(new Intent(context, WatermarkService.class));
                } else if ("success".equals(value)) {

                } else {
//                    Log.d(TAG, "key: " + value);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

//            if(keyBoxValue != 1 && keyIsHide != 1){
//                context.startService(new Intent(context, WatermarkService.class));
//				}

            /*String bootmode = SystemPropertiesProxy.get(BOOT_MODE, "unknow");
            Log.d(TAG, "onReceive bootmode = " + bootmode);
            if (bootmode != null && bootmode.equals(BOOT_MODE_APKMMI)) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setClass(context, ValidationToolsMainActivity.class);
                context.startActivity(i);
                return;
            } else if (bootmode != null && bootmode.equals(Const.BOOT_MODE_AUTOMMI)) {
                Intent serviceIntent = new Intent(context, AutoMMIService.class);
                serviceIntent.setAction("com.sprd.validationtools.autommi.auto_mmi_service");
                serviceIntent.setPackage("com.sprd.validationtools.autommi");
                context.startService(serviceIntent);
                return;
            }*/
        }
        /*Uri uri = intent.getData();
        if (uri == null)
            return;
        String host = uri.getHost();

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.d(TAG, "onReceive host=" + host);
        Log.d(TAG, "onReceive getAction=" + intent.getAction());
        if ("83789".equals(host) || "8111".equals(host)) {
            i.setClass(context, ValidationToolsMainActivity.class);
            context.startActivity(i);
        }else if ("9988".equals(host) || "1108".equals(host)) {
            i.setClass(context, HwInfoActivity.class);
            context.startActivity(i);
		}else if ("8112".equals(host)) {
            i.setClass(context, TestInfoMainActivity2.class);
            context.startActivity(i);	
		}else if ("1118".equals(host)) {
            i.setClass(context, VersionTypeActivity.class);
            context.startActivity(i);	
        }else if ("6666".equals(host)) {
			Intent stopIntent = new Intent(context, WatermarkService.class);
			context.stopService(stopIntent);
           Settings.System.putInt(context.getContentResolver(), "google_key_hide", 1);
		   
        }*/
    }
}
