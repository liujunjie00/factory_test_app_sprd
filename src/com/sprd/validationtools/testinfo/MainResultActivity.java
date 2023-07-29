package com.sprd.validationtools.testinfo;


import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.sprd.validationtools.itemstest.TestResultActivity;
import com.sprd.validationtools.itemstest.Test2ResultActivity;
import com.sprd.validationtools.R;

import com.sprd.validationtools.sqlite.MMI2EngSqlite;
import com.sprd.validationtools.sqlite.EngSqlite;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;


public class MainResultActivity extends Activity implements OnClickListener{
	
	private Button mBtn_mm1, mBtn_mm2, mBtn_reset;
	private Context mContext;
    private MMI2EngSqlite mMMI2EngSqlite;
    private EngSqlite mEngSqlite;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.main_result);
		init();
		
		mContext = this;
        mMMI2EngSqlite = MMI2EngSqlite.getInstance(mContext);
        mEngSqlite = EngSqlite.getInstance(mContext);
	}
	
	private void init(){
		
		mBtn_mm1 = (Button) findViewById(R.id.mmi1_btn);
		mBtn_mm2 = (Button) findViewById(R.id.mmi2_btn);
		mBtn_reset = (Button) findViewById(R.id.reset_btn);
		mBtn_mm1.setOnClickListener(this);  
		mBtn_mm2.setOnClickListener(this);  
		mBtn_reset.setOnClickListener(this);  
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 switch (v.getId()) {  
         case R.id.mmi1_btn:  
        	 Intent intent = new Intent(this, TestResultActivity.class);
             startActivity(intent); 	
             break;            
         case R.id.mmi2_btn:  
              Intent intent2 = new Intent(this, Test2ResultActivity.class);
             startActivity(intent2); 
             break;    
         case R.id.reset_btn:  
             resetDB();
             break;        
         default:  
             break;  
     }  
	}
	
	private void resetDB(){
		showNormalDialog();		
	}
	
		private void showNormalDialog(){

        final AlertDialog.Builder normalDialog = 
            new AlertDialog.Builder(mContext);
     
        normalDialog.setTitle(R.string.mmi_reset);
        normalDialog.setMessage(R.string.reset_result_title);
        normalDialog.setPositiveButton(R.string.alertdialog_ok, 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
				mEngSqlite.deleteDB();
				mMMI2EngSqlite.deleteDB();
	//			Toast.makeText(mContext, R.string.mmi_reset, Toast.LENGTH_SHORT).show();	
				//dialog.dismiss();
            }
        });
        normalDialog.setNegativeButton(R.string.dialog_cancel, 
            new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	//dialog.dismiss();
            }
        });
        normalDialog.show();
    }

}
