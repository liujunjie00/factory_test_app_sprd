package com.sprd.validationtools.itemstest.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.StorageUtil;

import java.io.File;
import android.os.Environment;
import android.os.StatFs;

import android.os.SystemProperties;
import android.text.format.Formatter;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.app.ActivityManager;

public class StorageTest extends BaseActivity {
    private String TAG = "StorageTest";
    private static final String SPRD_SD_TESTFILE = "sprdtest.txt";
    private static final String PHONE_STORAGE_PATH = "/data/data/com.sprd.validationtools/";
	private static final String CONFIG_RAM_SIZE = "ro.deviceinfo.ram";
	private static final String SPRD_RAM_SIZE = "ro.boot.ddrsize";
	private static final int SI_UNITS = 1000;
    private static final int IEC_UNITS = 1024;
    TextView  mRAM, mROM;
	private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.storage_test);
		mContext = this;
        setTitle(R.string.storage_test);
        
		mRAM = (TextView) findViewById(R.id.total_tv);
		mROM = (TextView) findViewById(R.id.available_tv);
		
        mPassButton.setVisibility(View.GONE);
		getTotalMemSizeInfo();
    }
	
		private void getTotalMemSizeInfo(){
		try{	
//		String ramSize = getConfigRam();
//        if (ramSize == null) {
//            ramSize = getRamSizeFromProperty();
//        }
        mRAM.setText(getString(R.string.storage_ram) + getTotalRAMSizeInfo());
		mROM.setText(getString(R.string.storage_rom) + getTotalROMSizeInfo());
		mPassButton.setVisibility(View.VISIBLE);
		}catch(Exception e) {
				e.printStackTrace();
		}
		/* 	try{
			File userDataDir = Environment.getDataDirectory();
			StatFs stat = new StatFs(userDataDir.getPath());
			long blockSize = stat.getBlockSizeLong();
			long totalBlocks = stat.getBlockCountLong();
			long availableBlocks = stat.getAvailableBlocks(); 
			long total = (totalBlocks * blockSize) / (1024 * 1024);
			long availabe = (availableBlocks * blockSize) / (1024 * 1024);
			
			mRAM.setText(getString(R.string.storage_ram) + total + "M");
			mROM.setText(getString(R.string.storage_rom) + availabe + "M");
			
			mPassButton.setVisibility(View.VISIBLE);
			}catch(Exception e) {
				e.printStackTrace();
			} */
		}

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

    }
	
	    public String getConfigRam() {
        String ramConfig = SystemProperties.get(CONFIG_RAM_SIZE, "unconfig");
        if ("unconfig".equals(ramConfig)) {
            Log.d(TAG, "no config ram size.");
            return null;
        } else {
            try {
                long configTotalRam = Long.parseLong(ramConfig);
                Log.d(TAG, "config ram to be: " + configTotalRam);
                return Formatter.formatShortFileSize(mContext, configTotalRam);
            } catch (NumberFormatException e) {
                Log.e(TAG, "config ram format error: " + e.getMessage());
                return null;
            }
        }
    }

    public String getRamSizeFromProperty() {
        String size = SystemProperties.get(SPRD_RAM_SIZE, "unconfig");
        if ("unconfig".equals(size)) {
            Log.d(TAG, "can not get ram size from "+SPRD_RAM_SIZE);
            return null;
        } else {
            Log.d(TAG, "property value is:" + size);
            String regEx="[^0-9]";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(size);
            size = m.replaceAll("").trim();
            long ramSize = Long.parseLong(size);
            return Formatter.formatShortFileSize(mContext, covertUnitsToSI(ramSize));
        }
    }
	
	private String getTotalROMSizeInfo(){
    File userDataDir = Environment.getDataDirectory();
    StatFs stat = new StatFs(userDataDir.getPath());
    long blockSize = stat.getBlockSizeLong();
    long totalBlocks = stat.getBlockCountLong();
    long size = totalBlocks * blockSize;
//Log.d(TAG, "tqy++++++size="+size); 	
    //Toast.makeText(getContext(),"size:"+size,Toast.LENGTH_SHORT).show();
    long GB = 1024 * 1024 * 1024;
    final long[] size_mapping_table = {2*GB, 4*GB, 8*GB, 16*GB, 32*GB, 64*GB, 128*GB,256*GB,512*GB};
    String[] size_mapping_table_str = {"2GB","4GB","8GB","16GB","32GB","64GB","128GB","256GB","512GB"};
    int i;
    for(i = 0 ; i < size_mapping_table.length; i ++)
        if(size <= size_mapping_table[i])
            break;
    if(i == size_mapping_table.length)
        i --;
    return size_mapping_table_str[i];
}

    /**
     * SI_UNITS = 1000bytes; IEC_UNITS = 1024bytes
     * 512MB = 512 * 1000 * 1000
     * 2048MB = 2048/1024 * 1000 * 1000 * 1000
     * 2000MB = 2000 * 1000 * 1000
     */
    private long covertUnitsToSI(long size) {
        if (size > SI_UNITS && size % IEC_UNITS == 0) {
            return size / IEC_UNITS * SI_UNITS * SI_UNITS * SI_UNITS;
        }
        return size * SI_UNITS * SI_UNITS;
    }
	
    @Override
    protected void onResume() {
        super.onResume();
     
    }

    private String getTotalRAMSizeInfo() {
        String totalString = null;
        try {
            long memInfo1 = GetMemInfo1(this);
            Log.d(TAG, "memInfo1: " + memInfo1);
            double mem = (double) memInfo1 / 1024;
            Log.d(TAG, "mem: " + mem);
            String toString = new BigDecimal(mem).setScale(0, BigDecimal.ROUND_HALF_UP).toString() + "GB";
            return toString;
        } catch (Exception e) {
            e.printStackTrace();
            return totalString;
        }
    }

    private long GetMemInfo1(Context mContext) {
        long MEM_UNUSED;
        long MEM_TOTAL;
        ActivityManager am = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        MEM_UNUSED = mi.availMem / (1024*1024);
        MEM_TOTAL = mi.totalMem / (1024*1024);
        String memStr = MEM_TOTAL + " MB";
        return MEM_TOTAL;
    }

    /*public static String getNetFileSizeDescription(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        }
        else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        }
        else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }*/

}
