package com.sprd.validationtools.testinfo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.sprd.validationtools.R;

import android.app.Activity;
import android.widget.TextView;
import android.os.Bundle;
import android.os.SystemProperties;

public class VersionTypeActivity extends Activity{
	
	private TextView mVersionTV, mTypeTV;
	
@Override
protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.version_type);
	init();
}

	private void init() {
		// TODO Auto-generated method stub
		mVersionTV = (TextView)findViewById(R.id.version_tv);
		mTypeTV = (TextView)findViewById(R.id.type_tv);
		
		mVersionTV.setText("Version:" + SystemProperties.get("ro.build.display.id", ""));
		mTypeTV.setText("BuildType:" +SystemProperties.get("ro.system.build.type", ""));
		
	}

}
