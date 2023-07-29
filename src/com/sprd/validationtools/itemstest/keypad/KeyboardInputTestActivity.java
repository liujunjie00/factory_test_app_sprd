package com.sprd.validationtools.itemstest.keypad;

import java.util.HashMap;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.ValidationToolsUtils;

public class KeyboardInputTestActivity extends BaseActivity {
    private static final String TAG = "KeyboardInputTestActivity";
    private TextView mTV;
	private EditText mET;
	private int mNub = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
            setContentView(R.layout.keyinput_test);
            setTitle(R.string.keyinput_test);
            
            mPassButton.setVisibility(View.GONE);
            
			mTV = (TextView) findViewById(R.id.input_tv);
			mET = (EditText) findViewById(R.id.input_et);
			mET.setInputType(InputType.TYPE_NULL);
			mET.addTextChangedListener(new EditChangedListener());
    }

	   class EditChangedListener implements TextWatcher {


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
           
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          /* if(mET.getText().toString().length() > 3 ){
			  mPassButton.setVisibility(View.VISIBLE);
		  } */

        }
        @Override
        public void afterTextChanged(Editable s) {
           

        }

    }
	
		@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	// TODO Auto-generated method stub
//Log.d(TAG, "tqy++++++dispatchKeyEvent--event.getKeyCode()="+event.getKeyCode());			
    	if (event.getAction() == KeyEvent.ACTION_DOWN) {

		mNub++;	
		String str = /* mNub+ */" keycode="+event.getKeyCode();
		/* Toast.makeText(KeyboardInputTestActivity.this, str,
                        Toast.LENGTH_SHORT).show();	 */
		mTV.setText(str);
			if(mNub > 3){
				mPassButton.setVisibility(View.VISIBLE);
			}
    	}
	
    	return super.dispatchKeyEvent(event);
    }

     @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//Log.d(TAG, "tqy+++++onKeyDown keyCode=" + keyCode);


        return super.onKeyDown(keyCode, event);
    } 

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
//Log.d(TAG, "tqy+++++onKeyUp keyCode=" + keyCode);		
        return super.onKeyUp(keyCode, event);
    }
	
	
    @Override
    public void onResume() {
        super.onResume();
       
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}
