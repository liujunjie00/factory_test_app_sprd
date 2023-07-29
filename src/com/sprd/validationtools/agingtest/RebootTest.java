package packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.PowerManager.WakeLock;
//import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import android.app.Dialog;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import com.sprd.validationtools.R;
import android.app.KeyguardManager;
import android.view.Window;

/*
 * Author	: huangjc
 * Date  	: 2013-05-06
 * Function	: Reboot Test
 */

public class RebootTest extends Activity implements OnClickListener {
	private final static String LOG_TAG = "RebootTest";

	private final static int MSG_REBOOT = 0;
	private final static int MSG_REBOOT_COUNTDOWN = 1;
	private final static int MSG_REBOOT_STARTCOUNT = 2;
    private final static int NETWORK_TEST = 3;
	
	private final static String SDCARD_PATH = "/mnt/external_sd";
	
	private final static int DELAY_TIME = 5;// x1000ms
	private final int REBOOT_OFF = 0;
	private final int REBOOT_ON = 1;

	private SharedPreferences mSharedPreferences;
	private SharedPreferences mSharedPreferencesAging;

	private TextView mCountTV;
	private TextView mCountdownTV;
	private TextView mMaxTV;
	private Button mStartButton;
	private Button mStopButton;
	private Button mExitBtn;
	private Button mSettingButton;
	private Button mSettingDelayButton;
	private Button mClearButton;
	private CheckBox mSdcardCheckCB;
	private CheckBox mWifiCheckCB;
	private CheckBox mEthCheckCB;
	
	private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;
	private State mEthState;
	private State mWifiState;
	
    private WakeLock mWakeLock;
	private int mState;
	private int mCount;
	private int mCountDownTime;
	private int mMaxTimes; // max times to reboot
	private int mDelayTime; // delay time to reboot
	private int flag = 0;
	private boolean mIsCheckSD = false;
	private boolean mIsCheckWIFI = false;
	private boolean mIsCheckETH = false;
	private boolean mFT = false;
	private String mSdState = null;
    private String RebootMode = null;
    private String mEthIpAddress = null;
    private Toast toast = null;
	private int mRebootCount;
	private boolean mIsReboot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reboot_test);
		// get the reboot flag and count.
		setTitle("重启测试");
		Window window = getWindow();
		window.getDecorView().setSystemUiVisibility(
				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LOW_PROFILE
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		Intent intent = getIntent();
		mRebootCount = intent.getIntExtra("rebootCount", 300);
		mIsReboot = intent.getBooleanExtra("isReboot", false);
		mSharedPreferencesAging = getSharedPreferences("aging", 0);

		mSharedPreferences = getSharedPreferences("state", 0);
		mState = mSharedPreferences.getInt("reboot_flag", 0);
		mCount = mSharedPreferences.getInt("reboot_count", 0);
		mMaxTimes = mSharedPreferences.getInt("reboot_max", 0);
		mDelayTime = mSharedPreferences.getInt("reboot_delay", 15);
		mIsCheckSD = mSharedPreferences.getBoolean("check_sd", false);
		mIsCheckWIFI = mSharedPreferences.getBoolean("check_wifi", false);
		mIsCheckETH = mSharedPreferences.getBoolean("check_eth", false);
//        ((KeyguardManager)getSystemService("keyguard")).newKeyguardLock("TestReboot").disableKeyguard();
		mWakeLock = ((PowerManager)getSystemService("power")).newWakeLock(PowerManager.FULL_WAKE_LOCK, "RebootTest");
        mWakeLock.acquire();
		// init resource
                
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);

        if (mIsReboot) {
        	initRes();
        	stopTest();
			onClearSetting();
			saveMaxTimes(mRebootCount);
			mMaxTimes = mRebootCount;
			initRes();
			mState = REBOOT_ON;
			mCountDownTime = mDelayTime;//DELAY_TIME / 1000; // ms->s
			updateBtnState();
			mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
		} else {
			initRes();

			if (mState == REBOOT_ON) {
			/*if (mIsCheckSD) {
				mSdState = getSdCardState();
				if (!mSdState.equals(Environment.MEDIA_MOUNTED)) {
					TextView tv =(TextView) findViewById(R.id.sdcard_check_tv); 
					tv.setText("Sdcard check:"+false);
					tv.setVisibility(View.VISIBLE);
					onStopClick();
					return ;
				}
			}*/

				if (mMaxTimes != 0 && mMaxTimes <= mCount) {
					mState = REBOOT_OFF;
					saveSharedPreferences(mState, 0);
					saveMaxTimes(0);
					updateBtnState();
					mCountTV.setText(mCountTV.getText() + " 测试完成!");
					if (mSharedPreferencesAging.getBoolean("isRebootSp", false)) {
						SharedPreferences.Editor edit = mSharedPreferencesAging.edit();
						edit.putBoolean("isRebootSp", false);
						edit.commit();
						Intent intentAging = new Intent(this, AgingTest.class);
						intentAging.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intentAging);
						finish();
					}
				} else if (/*isRebootError()*/false) {
					mState = REBOOT_OFF;
					saveSharedPreferences(mState, 0);
					saveMaxTimes(0);
					updateBtnState();
					mCountTV.setText(mCountTV.getText() + " Test fail for error!");
				} else {
					mCountDownTime = mDelayTime;//DELAY_TIME / 1000;
					mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
				}
			}
		}
	}

	private void initRes() {
		mCountTV = (TextView) findViewById(R.id.count_tv);
		mCountTV.setText("已重启次数: " + mCount);
		mMaxTV = (TextView) findViewById(R.id.maxtime_tv);
		if (mMaxTimes == 0) {
			mMaxTV.setText("执行重启的最大次数: "
					+ "未设置");
		} else {
			mMaxTV.setText("执行重启的最大次数: " + mMaxTimes);
		}

		mStartButton = (Button) findViewById(R.id.start_btn);
		mStartButton.setOnClickListener(this);

		mStopButton = (Button) findViewById(R.id.stop_btn);
		mStopButton.setOnClickListener(this);
		
		mExitBtn = (Button) findViewById(R.id.exit_btn);
		mExitBtn.setOnClickListener(this);

		mSettingButton = (Button) findViewById(R.id.setting_btn);
		mSettingButton.setOnClickListener(this);

        mSettingDelayButton = (Button) findViewById(R.id.setting_delay_btn);
        mSettingDelayButton.setOnClickListener(this);

		mClearButton = (Button) findViewById(R.id.clear_btn);
		mClearButton.setOnClickListener(this);
		
		mSdcardCheckCB = (CheckBox) findViewById(R.id.sdcard_check_cb);
		mSdcardCheckCB.setChecked(mIsCheckSD);
		/*mSdcardCheckCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					String state = getSdCardState();
					if (!state.equals(Environment.MEDIA_MOUNTED)) {
//						Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
						toastShow("Please insert sdcard!");
						buttonView.setChecked(false);
					}
				}
			}
		});*/
		
		mWifiCheckCB = (CheckBox) findViewById(R.id.wifi_check_cb);
		mWifiCheckCB.setChecked(mIsCheckWIFI);
		mWifiCheckCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLED){
//						Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
						toastShow("Wifi is opened!");
					}
					else{
//						Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
						toastShow("open the wifi now");
						mWifiManager.setWifiEnabled(true);

					}     
				}
				else {
					if (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_DISABLED) {
//						Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
						toastShow("close wifi");
					    mWifiManager.setWifiEnabled(false);
					}
				}
			}
		});
		
		mEthCheckCB = (CheckBox) findViewById(R.id.eth_check_cb);
		mEthCheckCB.setChecked(mIsCheckETH);
		mEthCheckCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mEthState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
					if (mEthState == State.CONNECTED){
//						mEthIpAddress = getLocalIpAddress();
//						Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
						toastShow("Ethernet is connected. ");
					}
					else{
//						Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
						toastShow("Please insert the cable");
						mEthCheckCB.setChecked(false);
					}
				}
			}
		});
		
        
		updateBtnState();

		mCountdownTV = (TextView) findViewById(R.id.countdown_tv);
	}
	
	private void NetworkTest(){
        if (mIsCheckWIFI) {
            if (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_ENABLED){
//			    Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
                toastShow("open the wifi now");
                mWifiManager.setWifiEnabled(true);
            }
            if (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLED){
//			    Toast.makeText(RebootTest.this, "Wifi has already opened!", Toast.LENGTH_LONG).show();
                mWifiState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                if (mWifiState == State.CONNECTED){
//                   	Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
                    if (!mIsCheckETH){
                        toastShow("Wifi is connecteded.");
                        mHandler.sendEmptyMessageDelayed(MSG_REBOOT, 1000);
                    }
                }
                else{
                    toastShow("Wifi is disconnecteded.");
                    mHandler.sendEmptyMessageDelayed(NETWORK_TEST, 3000);
                    return ;
                }	
            }
        }
		
        if (mIsCheckETH) {
            mEthState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
            if (mEthState == State.CONNECTED){				
//			    mEthIpAddress = getLocalIpAddress();
//			    Toast.makeText(RebootTest.this, "", Toast.LENGTH_SHORT).show();
                if (mIsCheckWIFI){
                    toastShow("Wifi and ethernet are connected. ");
                }
                else{
                    toastShow("Ethernet is connected. ");
                }
                mHandler.sendEmptyMessageDelayed(MSG_REBOOT, 1000);
            }
            else{
                toastShow("Ethernet is disconnecteded.");
            }
        }
        mHandler.sendEmptyMessageDelayed(NETWORK_TEST, 3000);
	}

    public void toastShow(String text) {  
        if(toast == null)  
        {  
            toast = Toast.makeText(RebootTest.this, text, Toast.LENGTH_SHORT);  
        }  
        else {  
            toast.setText(text);  
        }  
        toast.show();  
    }
    
	private void reboot() {
		// save state
		saveSharedPreferences(mState, mCount + 1);
		SharedPreferences.Editor edit = mSharedPreferencesAging.edit();
		edit.putInt("runRebootCount", mCount + 1);
		edit.commit();

		// 重启
		/*
		 * String str = "重启"; try { str = runCmd("reboot", "/system/bin"); }
		 * catch (IOException e) { e.printStackTrace(); }
		 */
		/*
		 * Intent reboot = new Intent(Intent.ACTION_REBOOT);
		 * reboot.putExtra("nowait", 1); reboot.putExtra("interval", 1);
		 * reboot.putExtra("window", 0); sendBroadcast(reboot);
		 */
		PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		pManager.reboot(null);
		System.out.println("execute cmd--> reboot\n" + "重启");
	}

	private void saveSharedPreferences(int flag, int count) {
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putInt("reboot_flag", flag);
		edit.putInt("reboot_count", count);
		edit.putBoolean("check_sd", mIsCheckSD);
		edit.putBoolean("check_wifi", mIsCheckWIFI);
		edit.putBoolean("check_eth", mIsCheckETH);
		edit.commit();
	}
	
	private void saveMaxTimes(int max) {
		SharedPreferences.Editor edit = mSharedPreferences.edit();
		edit.putInt("reboot_max", max);
		edit.commit();
	}

    private void saveDelayTimes(int time) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt("reboot_delay", time);
        edit.commit();
//        Toast.makeText(this,"Set delay time:"+time+"s",Toast.LENGTH_LONG ).show();
    }

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
                case MSG_REBOOT:
                    if (mState == 1){
                        reboot();
                        Toast.makeText(RebootTest.this, "Start reboot now!!",Toast.LENGTH_LONG).show(); 
                    }
                    break;

                case MSG_REBOOT_COUNTDOWN:
                    if (mState == 0)
                        return;
                       //                 Log.d("hjc","==MSG_REBOOT_COUNTDOWN===beforce mCountDownTime:"+mCountDownTime);
                    if (mCountDownTime != 0) {
                        mCountdownTV.setText("系统将重新启动 : "
                                + mCountDownTime);
                        mCountdownTV.setVisibility(View.VISIBLE);
                        mCountDownTime--;
                         //               Log.d("hjc","==MSG_REBOOT_COUNTDOWN===mCountDownTime====="+mCountDownTime);
                        sendEmptyMessageDelayed(MSG_REBOOT_COUNTDOWN, 1000);
                    } else {
                        if(/*isSystemError()*/false){
                            mState = REBOOT_OFF;
                            saveSharedPreferences(mState, 0);
                            saveMaxTimes(0);
                            updateBtnState();
                            mCountTV.setText(mCountTV.getText()+" Test fail for error!");
							 
                        }else{
                            mCountdownTV.setText("系统将重新启动 : "
                                + mCountDownTime);
                            mCountdownTV.setVisibility(View.VISIBLE);
                             //           Log.d("hjc","===UnCheckSys====send MSG_REBOOT==now====mCountDownTime:"+mCountDownTime);
                            if (mIsCheckWIFI || mIsCheckETH){				
                                sendEmptyMessageDelayed(NETWORK_TEST, 1000);	
                            } else {
                                sendEmptyMessage(MSG_REBOOT);
                            }
                        }
                    }
                    break;
                case MSG_REBOOT_STARTCOUNT:
                    sendEmptyMessage(MSG_REBOOT_COUNTDOWN);
                    break;
                case NETWORK_TEST:
                    NetworkTest();
                    break;                    
                default:
                    break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_btn:
			onStartClick();
			break;
		case R.id.stop_btn:
			onStopClick();
			break;
		case R.id.exit_btn:
			stopTest();
			finish();
			break;
		case R.id.setting_btn:
			onSettingClick();
			break;
                case R.id.setting_delay_btn:
                        onDelayTimeClick();
                        break;
		case R.id.clear_btn:
			onClearSetting();
			break;
		default:
			break;
		}

	}

       @Override
     protected void onDestroy() {
		   super.onDestroy();
		   //       stopTest();
		   mWakeLock.release();
		   stopTest();
     }
       
    private void stopTest() {
		mHandler.removeMessages(MSG_REBOOT);
		mCountdownTV.setVisibility(View.INVISIBLE);
		mState = REBOOT_OFF;
		updateBtnState();
		mIsCheckSD = false;
		mIsCheckWIFI = false;
		mIsCheckETH = false;
		saveSharedPreferences(mState, 0);
    }

	private void onStartClick() {
		mFT = true;
		flag = 0;
        /*String MessageString = "是否开始执行重启测试";
		new AlertDialog.Builder(RebootTest.this)
		.setTitle("Reboot")
		.setMessage(MessageString)
		.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,int which) {
			    mState = REBOOT_ON;
				mIsCheckSD = mSdcardCheckCB.isChecked();
				mIsCheckWIFI = mWifiCheckCB.isChecked();
				mIsCheckETH = mEthCheckCB.isChecked();
//				mIsCheckSys = mAutoCheckSys.isChecked();
				mCountDownTime = mDelayTime;//DELAY_TIME / 1000; // ms->s
				updateBtnState();
				mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
			}
		}).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				@Override
			public void onClick(DialogInterface dialog,int which) {
				dialog.cancel();
			}
		}).show();*/
		mState = REBOOT_ON;
		mCountDownTime = mDelayTime;//DELAY_TIME / 1000; // ms->s
		updateBtnState();
		mHandler.sendEmptyMessage(MSG_REBOOT_STARTCOUNT);
	}

	private void onStopClick() {
		stopTest();
	}
	
	private void onSettingClick() {
		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER);
		new AlertDialog.Builder(this)
			.setTitle("设置执行重启测试的最大次数")
			.setView(editText)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(!editText.getText().toString().trim().equals("")) {
						mMaxTimes = Integer.valueOf(editText.getText().toString());
						saveMaxTimes(mMaxTimes);
						mMaxTV.setText("执行重启的最大次数: "+mMaxTimes);
					}
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
				
			}).show();
    }

	private void onClearSetting() {
		mMaxTimes = 0;
        mDelayTime = 15;
		saveMaxTimes(mMaxTimes);
        saveDelayTimes(mDelayTime);
        mIsCheckSD = false;
        mIsCheckWIFI = false;
        mIsCheckETH = false;
        mSdcardCheckCB.setChecked(mIsCheckSD);
        mWifiCheckCB.setChecked(mIsCheckWIFI);
        mEthCheckCB.setChecked(mIsCheckETH);
             
		mMaxTV.setText("执行重启的最大次数: "
				+ "未设置");
                
	}
    private void onDelayTimeClick() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
            .setTitle("Set DelayTime")
            .setView(editText)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(!editText.getText().toString().trim().equals("")) {
                         mDelayTime = Integer.valueOf(editText.getText().toString());
                         saveDelayTimes(mDelayTime);
                    }
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).show();
    }
	
	private void SavedRebootMode() {
		Process process = null;
		String filePath = "mnt/internal_sd/boot_mode.txt";
		File file1 = new File(filePath);
		if (file1.isFile() && file1.exists()) {
				file1.delete();
		}
		try {
			StresstestUtil.getBootMode(true);
		} catch (Exception e) {
			Log.e(LOG_TAG, "getBootMode fail!!!");
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try {
	        String encoding="GBK";
	        File file=new File(filePath);
	        if(file.isFile() && file.exists()){ //判断文件是否存在
	            InputStreamReader read = new InputStreamReader(
	                              new FileInputStream(file),encoding);//考虑到编码格式
	            BufferedReader bufferedReader = new BufferedReader(read);
	            String lineTxt = null;
	            while((lineTxt = bufferedReader.readLine()) != null){
	                Log.d(LOG_TAG,lineTxt);
	                int p1 = lineTxt.indexOf("(");
	                int p2 = lineTxt.indexOf(")");
	                RebootMode = lineTxt.substring(p1+1, p2);
	                Toast.makeText(this,RebootMode,Toast.LENGTH_LONG ).show(); 	
	            }
	            read.close();
	        }else{
	            Log.e(LOG_TAG,"not find the mnt/internal_sd/boot_mode.txt");
	        }
	    } catch (Exception e) {
	       Log.e(LOG_TAG,"read error!!");
	       e.printStackTrace();
	    }
	}
	
	private boolean isRebootError(){
		SavedRebootMode();
		if(RebootMode!=null){
            if(Integer.valueOf(RebootMode) == 7){
			    Dialog dialog = new AlertDialog.Builder(this)
			    .setTitle("重启测试异常")
			    .setMessage("检测到本次为panic重启，详情请看last_log")
			    .setPositiveButton("确定",new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog,int whichButton) {
					    dialog.cancel();
					    }
			    	})
			    .setNegativeButton("取消",new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int whichButton) {
					    dialog.cancel();
			            }
				    }).create();
			    dialog.show();
				return true;
			}else if(Integer.valueOf(RebootMode) == 8){
			    Dialog dialog = new AlertDialog.Builder(this)
			    .setTitle("重启测试异常")
			    .setMessage("检测到本次为watchdog重启，详情请看last_log")
			    .setPositiveButton("确定",new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int whichButton) {
					    dialog.cancel();
				        }
				    })
				.setNegativeButton("取消",new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog,int whichButton) {
					    dialog.cancel();
					    }
				    }).create();
				dialog.show();
				return true;
			}
	        return false;
	    }
	    return false;
	}
	
	private boolean isSystemError(){
		InputStreamReader reader = null;
		BufferedReader bufferedReader = null;
		Process process = null;
		String lineText = null;
		if(!mFT){
		try{
		process = Runtime.getRuntime().exec("logcat -d");
		reader = new InputStreamReader(process.getInputStream());
		bufferedReader = new BufferedReader(reader);
				Log.d("--hjc","-------------->>lineTxt:"+lineText);
		while((lineText = bufferedReader.readLine()) != null){
			//	Log.d("--hjc","-------------->>lineTxt:"+lineText);
         if(lineText.indexOf("Force finishing activity")!=-1||lineText.indexOf("backtrace:")!=-1){
				 Log.d("--hjc","------lineTxt:"+lineText);
         			   Dialog dialog = new AlertDialog.Builder(
					   this)
			.setTitle("重启测试异常")
			.setMessage("检测到系统异常，详情请看logcat")
			.setPositiveButton("确定",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					 dialog.cancel();
					}
			}).setNegativeButton("取消",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					dialog.cancel();
			}}).create();
			dialog.show();
			reader.close();
			bufferedReader.close();
			return true;}
	   }
	   reader.close();
			bufferedReader.close();
			return false;
		} catch (Exception e) {
			Log.e(LOG_TAG,"process Runtime error!!");
		  e.printStackTrace();
		}
		}
	return false;
	
	}
	
	private void updateBtnState() {
		mStartButton.setEnabled(mState == REBOOT_OFF);
		mClearButton.setEnabled(mState == REBOOT_OFF);
		mSettingButton.setEnabled(mState == REBOOT_OFF);
		mSettingDelayButton.setEnabled(mState == REBOOT_OFF);
		mStopButton.setEnabled(mState == REBOOT_ON);
	}
	
	/*public static String getSdCardState() {
        try {
        	IMountService mMntSvc = null;
            if (mMntSvc == null) {
                mMntSvc = IMountService.Stub.asInterface(ServiceManager
                                                         .getService("mount"));
                }
            return mMntSvc.getVolumeState(SDCARD_PATH);
        } catch (Exception rex) {
            return Environment.MEDIA_REMOVED;
        }

    }*/
	
	public static String getLocalIpAddress() {  
	    try {  
	        for (Enumeration<NetworkInterface> en = NetworkInterface  
	                    .getNetworkInterfaces(); en.hasMoreElements();) {  
	            NetworkInterface intf = en.nextElement();   
	            for (Enumeration<InetAddress> enumIpAddr = intf  
	                         .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
	                InetAddress inetAddress = enumIpAddr.nextElement();  
	                if (!inetAddress.isLoopbackAddress()) {   
	                      return inetAddress.getHostAddress().toString();  
	                }  
	            }  
	        }  
	    }catch (SocketException ex) {  
	         System.out.println("WifiPreference IpAddress"+ex.toString());   
	    }
        return null;  
	}

}
