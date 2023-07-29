package com.sprd.validationtools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.View;

public class HwInfoActivity extends Activity {
	
	private static final String FILE_PATH_TP = "/sys/class/input/event4/device/name";
	private static final String FILE_PATH_CAMERA = "/sys/devices/virtual/misc/sprd_sensor/camera_sensor_name";
	private static final String FILE_PATH_LCD = "/sys/class/display/panel0/device/display/panel0/name";
	
	private TextView mLcdInfo;
	private TextView mTpInfo;
	private TextView mCameraInfo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hw_info_activity);
		
		mLcdInfo = (TextView) findViewById(R.id.hw_info_lcd);
		mTpInfo = (TextView) findViewById(R.id.hw_info_tp);
		mCameraInfo = (TextView) findViewById(R.id.hw_info_camera);
		
		mLcdInfo.setText("LCD: " + getInfoFromFile(FILE_PATH_LCD));
		mTpInfo.setText("TP:  " + getInfoFromFile(FILE_PATH_TP));
		mCameraInfo.setText("Camera:  " + getCameraInfoFromFile(FILE_PATH_CAMERA));
		//mTpInfo.setVisibility(View.GONE);
	}
	
	private String getInfoFromFile(String path){
		File file = new File(path);
        StringBuffer sBuffer = new StringBuffer();
        try {
    		InputStream fIn = new FileInputStream(file);
    		BufferedReader bReader = new BufferedReader(new InputStreamReader(fIn,Charset.defaultCharset()));
            String str = bReader.readLine();

            while (str != null) {
                sBuffer.append(str + "\n");
                str = bReader.readLine();
            }
            Log.d("hzx", "info = " + sBuffer.toString());
            return sBuffer.toString();
        } catch (IOException e) {
        	Log.d("hzx", "IOException");
            e.printStackTrace();
        }
        return null;
    }
	
	private String getCameraInfoFromFile(String path){
		File file = new File(path);
        StringBuffer sBuffer = new StringBuffer();
        try {
    		InputStream fIn = new FileInputStream(file);
    		BufferedReader bReader = new BufferedReader(new InputStreamReader(fIn,Charset.defaultCharset()));
            String str = bReader.readLine();

            while (str != null) {
            	//str = getCameraIc(str);
            	Log.d("hzx", "str = " + str);
                sBuffer.append(str + "  ");
                str = bReader.readLine();
            }
            Log.d("hzx", "info = " + sBuffer.toString());
            return sBuffer.toString();
        } catch (IOException e) {
        	Log.d("hzx", "IOException");
            e.printStackTrace();
        }
        return null;
    }
	
	private String getCameraIc(String info){
		String[] tmp = info.split("_");
		return tmp[0];
	}
	
}
