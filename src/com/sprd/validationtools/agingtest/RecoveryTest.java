package packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.sprd.validationtools.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

//import com.android.internal.os.storage.ExternalStorageFormatter;
import android.util.Log;
import java.io.FileInputStream;
import android.app.Dialog;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import android.app.KeyguardManager;
import android.os.SystemProperties;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import android.os.storage.StorageVolume;
import android.os.storage.StorageManager;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class RecoveryTest extends StressBase{
	public static final String TAG = "RecoveryTest";
	
	public static final String RECOVERY_STATE_FILE = /*Environment.getExternalStorageDirectory().getPath() +*/ "/cache/recovery/Recovery_state";
	//public static final String RECOVERY_STATE_FILE_TF = "/mnt/external_sd/Recovery_state";
	public static String RECOVERY_STATE_FILE_TF = "/mnt/external_sd/Recovery_state";
    public String usb_dir=null;
	public String sdcard_dir=null;        
	
	private TextView mMaxView;
	private TextView mTestTimeTv;
	private TextView mCountdownTv;
	private TextView mWarning_tf;
	
	private CheckBox mEraseFlashCb;
	private CheckBox mWipeAllCb;
	private CheckBox mWifiCheckCb;
	private CheckBox mEthCheckCb;
	private CheckBox mCheckSys;
	private CountDownTimer mCountDownTimer;
	
	private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;
	private State mEthState;
	
	private int mStartTest = 0;
    private String RebootMode = null;

    private WakeLock mWakeLock;	
	private boolean mIsEraseFlash = false;
	private boolean mIsWipeAll = false;
	private boolean mIsCheckSys = false;
	private boolean mFT = false;
	
	private boolean mIsCheckWiFi = false;
	private boolean mIsCheckEth = false;
	
	public static final int MSG_START_TEST = 1;
	private String UMSstate = SystemProperties.get("ro.factory.hasUMS");
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recovery_test);
		setDefaultBtnId(R.id.start_btn, R.id.stop_btn, R.id.exit_btn,
				R.id.maxtime_btn, R.id.clear_maxtime_btn);
		mMaxView = (TextView) findViewById(R.id.maxtime_tv);
		mTestTimeTv = (TextView) findViewById(R.id.testtime_tv);
		mCountdownTv = (TextView) findViewById(R.id.countdown_tv);
		mWarning_tf = (TextView) findViewById(R.id.warning_tf);
		
		mEraseFlashCb = (CheckBox) findViewById(R.id.erase_cb);
		mWipeAllCb = (CheckBox) findViewById(R.id.wipeall_cb);
		mWifiCheckCb = (CheckBox) findViewById(R.id.wifi_check_cb_R);
		mEthCheckCb = (CheckBox) findViewById(R.id.eth_check_cb_R);
		mCheckSys = (CheckBox) findViewById(R.id.check_sys);
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
//	   ((KeyguardManager)getSystemService("keyguard")).newKeyguardLock("TestRecovery").disableKeyguard();
	    mWakeLock = ((PowerManager)getSystemService("power")).newWakeLock(PowerManager.FULL_WAKE_LOCK, "RecoveryTest");
        mWakeLock.acquire();
	/*	if(!UMSstate()){
		  mEraseFlashCb.setVisibility(View.GONE);
		  mWarning_tf.setVisibility(View.VISIBLE);
		}*/
        init_StoragePath(this);
        Log.d(TAG,"RECOVERY_STATE_FILE_TF:"+RECOVERY_STATE_FILE_TF);
		initData();
		updateUI();
		
		mEraseFlashCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
	
			@Override
		    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1)
				mWarning_tf.setVisibility(View.VISIBLE);
				else
				mWarning_tf.setVisibility(View.GONE);
			}});
            
		mWipeAllCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		    @Override
		    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if(arg1)
			mWarning_tf.setVisibility(View.VISIBLE);
			else
			mWarning_tf.setVisibility(View.GONE);
			}});
		
       mWifiCheckCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLED){
						Toast.makeText(RecoveryTest.this, "Wifi has already opened!", Toast.LENGTH_SHORT).show();
					}
					else{
						Toast.makeText(RecoveryTest.this, "open the wifi now", Toast.LENGTH_SHORT).show();
						mWifiManager.setWifiEnabled(true);

					}     
				}
				else {
					if (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_DISABLED) {
						Toast.makeText(RecoveryTest.this, "close wifi", Toast.LENGTH_SHORT).show();
					    mWifiManager.setWifiEnabled(false);
					}
				}
			}
		});
       
       mEthCheckCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mEthState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
					if (mEthState == State.CONNECTED){
						Toast.makeText(RecoveryTest.this, "Ethernet has already connected", Toast.LENGTH_SHORT).show();
					}
					else{
						Toast.makeText(RecoveryTest.this, "Please insert the cable", Toast.LENGTH_SHORT).show();
						mEthCheckCb.setChecked(false);
					}
				}
			}
		});

	    mCheckSys.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
		}});
        
		if (mStartTest == 1) {
			updateTestTimeTV();
			if(mIsCheckSys){
				if(isRebootError()){
					stopTest();
                    Log.e(TAG,"check system error,stop test!!");
					mTestTimeTv.setText(mTestTimeTv.getText()+" Test fail for system error!");
				}
		    }
            if(isNetworkError(true)){
                stopTest();
                mTestTimeTv.setText(mTestTimeTv.getText()+" Test fail for network error!");
            }
            if(mMaxTestCount == 0 || mCurrentCount < mMaxTestCount) {
				preStartTest();
			}else{
                stopTest();
                mTestTimeTv.setText(mTestTimeTv.getText()+" 测试完成!");
            }
		}
		
	}

    private boolean isNetworkError(boolean rebootflag){
    	if (mIsCheckWiFi) {
			if (mWifiManager.getWifiState() != mWifiManager.WIFI_STATE_ENABLED){
                if(rebootflag){
				    Toast.makeText(RecoveryTest.this, "open the wifi now", Toast.LENGTH_SHORT).show();
				    mWifiManager.setWifiEnabled(true);
                }else{
                    Toast.makeText(RecoveryTest.this, "wifi is not opened", Toast.LENGTH_SHORT).show();
                    return true;
                }
			}
			if (mWifiManager.getWifiState() == mWifiManager.WIFI_STATE_ENABLED){
                if (!mIsCheckEth){
				    Toast.makeText(RecoveryTest.this, "Wifi has opened!", Toast.LENGTH_SHORT).show();
                }
			}
		}
    	
    	if (mIsCheckEth && !rebootflag) {
			mEthState = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET).getState();
			if (mEthState == State.CONNECTED){
                if (mIsCheckWiFi){
				    Toast.makeText(RecoveryTest.this, "Wifi has opened and Ethernet has connected", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(RecoveryTest.this, " Ethernet has connected", Toast.LENGTH_SHORT).show();
                }
			}
			else{
                Toast.makeText(RecoveryTest.this, "Ethernet is disconnected", Toast.LENGTH_SHORT).show();
				return true;
			}			
		}
    	return false;
    	
    } 
    
    public void init_StoragePath(Context context) {
        StorageManager mStorageManager = (StorageManager) getSystemService(StorageManager.class);
		//flash dir
	    //	flash_dir = Environment.getExternalStorageDirectory().getPath();		
		final List<VolumeInfo> volumes = mStorageManager.getVolumes();
	        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
		for (VolumeInfo vol : volumes) {
            if (vol.getType() == VolumeInfo.TYPE_PUBLIC) {
                Log.d(TAG, "VolumeInfo.TYPE_PUBLIC");
                Log.d(TAG, "Volume path:"+vol.getPath());
                DiskInfo disk = vol.getDisk();
                if(disk != null) {
                	if(disk.isSd()) {
                		//sdcard dir
                		StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                		sdcard_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF=new String(sdcard_dir)+"/Recovery_state";
                	}else if(disk.isUsb()){
                		//usb dir
               		    StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
                		usb_dir = sv.getPath();
                        RECOVERY_STATE_FILE_TF=new String(usb_dir)+"/Recovery_state";
                	}
                }
            }
        }
	}

    private boolean UMSstate(){
		return UMSstate.equals("true");
	}
	
	private void initData() {
		updateBtnState();
		mStartTest = getIntent().getIntExtra("enable", 0);
		mCurrentCount = getIntent().getIntExtra("cur", 0);
		mMaxTestCount = getIntent().getIntExtra("max", 0);
		readState(RECOVERY_STATE_FILE);
        if(mMaxTestCount < 0)
            mMaxTestCount = 0;
		mIsWipeAll = getIntent().getBooleanExtra("wipeall", false);
		mIsEraseFlash = getIntent().getBooleanExtra("eraseflash", false);
		mIsCheckWiFi = getIntent().getBooleanExtra("checkwifi", false);
		mIsCheckEth = getIntent().getBooleanExtra("checketh", false);
		mIsCheckSys = getIntent().getBooleanExtra("checksys", false);
	}
		
	private void updateUI() {
		updateMaxTV();
		mEraseFlashCb.setChecked(mIsEraseFlash);
		mWipeAllCb.setChecked(mIsWipeAll);
	    mWifiCheckCb.setChecked(mIsCheckWiFi);
	    mEthCheckCb.setChecked(mIsCheckEth);
		mCheckSys.setChecked(mIsCheckSys);
	}

	@Override
	public void updateMaxTV() {
		super.updateMaxTV();
		if (mMaxTestCount <= 0) {
			mMaxView.setText("执行恢复出厂的最大次数: " + "未设置");
		}
		mMaxView.setText("执行恢复出厂的最大次数: " + mMaxTestCount);
	}
	
	public void updateTestTimeTV() {
		mTestTimeTv.setText("已恢复出厂次数: "+mCurrentCount);
		mTestTimeTv.setVisibility(View.VISIBLE);
	}
		
	@Override
	public void onStartClick() {
        mFT = true;
		preStartTest();
	}

	@Override
	public void onStopClick() {
		stopTest();
	}

	@Override
	public void onSetMaxClick() {
		
	}

	@Override
	public void onSetClearMaxClick() {
		writeRecoveryState(formatStateContent());
	}
	
	public void preStartTest() {
		mIsEraseFlash = mEraseFlashCb.isChecked();
		mIsWipeAll = mWipeAllCb.isChecked();
		mIsCheckWiFi = mWifiCheckCb.isChecked();
		mIsCheckEth = mEthCheckCb.isChecked();
		mIsCheckSys = mCheckSys.isChecked();
		mStartTest = 1;
		incCurCount();
		writeRecoveryState(formatStateContent());
		isRunning = true;
		updateBtnState();
		mCountDownTimer = new CountDownTimer(15000, 1000) {
			
			@Override
			public void onTick(long millisUntilFinished) {
				mCountdownTv.setText((millisUntilFinished/1000)+"");
				mCountdownTv.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onFinish() {
				mCountdownTv.setVisibility(View.INVISIBLE);
				if(mIsCheckSys){
					if(isSystemError()){
						 stopTest();
						 mTestTimeTv.setText(mTestTimeTv.getText()+" Test fail for system error!");
				    }
				}
                if(isNetworkError(false)){
                    stopTest();
                    mTestTimeTv.setText(mTestTimeTv.getText()+" Test fail for network error!");
                }
                else{
				    startTest();
				}
			}
		}.start();
	}
	
	private void startTest() {
		if (mIsWipeAll || mIsEraseFlash) {
			try {
				bootCommand(this, "--wipe_all");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
/*		} else if (mIsEraseFlash) {
			Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
            intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
            intent.putExtra(Intent.EXTRA_REASON, "WipeAllFlash");
            this.startService(intent);*/
		} else {
			Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
			intent.setPackage("android");
			intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
			intent.putExtra(Intent.EXTRA_REASON, "MainClearConfirm");
			intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
			intent.putExtra(Intent.EXTRA_WIPE_ESIMS, false);
			sendBroadcast(intent);
			// Intent handling is asynchronous -- assume it will happen soon.
		}
	}
	
	private void stopTest() {
		isRunning = false;
		updateBtnState();
		mStartTest = 0;
		mCurrentCount = 0;
		if (mCountDownTimer != null)
			mCountDownTimer.cancel();
		mCountdownTv.setVisibility(View.INVISIBLE);
		writeRecoveryState(formatStateContent());
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
					Log.e(TAG, "getBootMode fail!!!");
			}
			try {
					Thread.sleep(1000);
			} catch (InterruptedException e) {
					e.printStackTrace();
			}
			try {
	            String encoding="GBK";
	            File file=new File(filePath);
	            if(file.isFile() && file.exists()){ //
	                InputStreamReader read = new InputStreamReader(
	                new FileInputStream(file),encoding);//
	                BufferedReader bufferedReader = new BufferedReader(read);
	                String lineTxt = null;
	                while((lineTxt = bufferedReader.readLine()) != null){
	                	Log.d(TAG,lineTxt);
	                	int p1 = lineTxt.indexOf("(");
	                	int p2 = lineTxt.indexOf(")");
	                	RebootMode = lineTxt.substring(p1+1, p2);
	                		Toast.makeText(this,RebootMode,Toast.LENGTH_LONG ).show();
	                	
	                }
	                read.close();
	    }else{
	        Log.e(TAG,"not find the mnt/internal_sd/boot_mode.txt");
	    }
	    } catch (Exception e) {
	       Log.e(TAG,"read error!!");
	        e.printStackTrace();
	    }
	}
	private boolean isRebootError(){
		SavedRebootMode();
		
		if(RebootMode!=null){
                  if(Integer.valueOf(RebootMode) == 7){
			   Dialog dialog = new AlertDialog.Builder(
					   this)
			.setTitle("恢复出厂测试异常")
			.setMessage("检测到本次为panic重启，详情请看last_log")
			.setPositiveButton("确定" ,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					 dialog.cancel();
					}
			}).setNegativeButton("取消" ,new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					dialog.cancel();
			}}).create();
			dialog.show();
				return true;

		   }else if(Integer.valueOf(RebootMode) == 8){
			   Dialog dialog = new AlertDialog.Builder(
					   this)
			.setTitle("RecoveryTest Error")
			.setMessage("It's reboot for watchdog,see thelast_log for details")
			.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					dialog.cancel();
				}}).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int whichButton) {
					dialog.cancel();
				}}).create();
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
		//			Log.d("--hjc","-------------->>lineTxt:"+lineText);
			if(lineText.indexOf("Force finishing activity")!=-1||lineText.indexOf("backtrace:")!=-1){
		            Log.d("--hjc","------lineTxt:"+lineText);
					    Dialog dialog = new AlertDialog.Builder(this)
							.setTitle("RecoveryTest Error")
							.setMessage("It's reboot for system,see logcat for details")
							.setPositiveButton("Ok",new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,int whichButton) {
								   dialog.cancel();
									}
							}).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
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
			      Log.e(TAG,"process Runtime error!!");
					  e.printStackTrace();
				}
				}	
	 return false;
	}
	
	private String formatStateContent() {
		StringBuilder sb = new StringBuilder();
		sb.append("enable:").append(mStartTest).append("\n");
		sb.append("currenttime:").append(mCurrentCount).append("\n");
		sb.append("maxtime:").append(mMaxTestCount).append("\n");
		sb.append("wipeall:").append(mIsWipeAll?"1":"0").append("\n");
		sb.append("eraseflash:").append(mIsEraseFlash?"1":"0").append("\n");
		sb.append("checkwifi:").append(mIsCheckWiFi?"1":"0").append("\n");
		sb.append("checketh:").append(mIsCheckEth?"1":"0").append("\n");
		sb.append("checksys:").append(mIsCheckSys?"1":"0").append("\n");
		return sb.toString();
	}
	
	private void writeRecoveryState(String content) {
		FileOutputStream fos = null;
		File file;
		if (/*mIsEraseFlash ||*/ mIsWipeAll /*|| !UMSstate()*/ ) {
            file = new File(RECOVERY_STATE_FILE_TF);
		} else {
			file = new File(RECOVERY_STATE_FILE);
		}
		
		if (file != null && !file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			fos = new FileOutputStream(file);
			fos.write(content.getBytes());
                        fos.flush();
			fos.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopTest();
    mWakeLock.release();
	}
	
	
//===========================for A10 test===========================//	
    /**
     * Reboot into the recovery system with the supplied argument.
     * @param arg to pass to the recovery utility.
     * @throws IOException if something goes wrong.
     */
    private static File RECOVERY_DIR = new File("/cache/recovery");
    private static File COMMAND_FILE = new File(RECOVERY_DIR, "command");
    
    private static void bootCommand(Context context, String arg) throws IOException {
        RECOVERY_DIR.mkdirs();  // In case we need it
        COMMAND_FILE.delete();  // In case it's not writable

        FileWriter command = new FileWriter(COMMAND_FILE);
        try {
            command.write(arg);
            command.write("\n");
        } finally {
            command.close();
        }

        // Having written the command file, go ahead and reboot
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        pm.reboot("recovery");

        throw new IOException("Reboot failed (no permissions?)");
    }

	public boolean readState(String fileName) {
		File file = new File(fileName);
		if (file == null || !file.exists()) {
			return false;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				String[] temp = tempString.split(":");
				if (temp.length < 2) {
					Log.e(TAG, "recovery test state file phase err.");
					return false;
				}
				if (temp[0].equals("enable")) {
//					mIsStart = Integer.valueOf(temp[1]);
				} else if (temp[0].equals("currenttime")) {
//					mCurCount = Integer.valueOf(temp[1]);
				} else if (temp[0].equals("maxtime")){
					mMaxTestCount = Integer.valueOf(temp[1]);
				} else if (temp[0].equals("wipeall")) {
//					mIsWipeAll = (Integer.valueOf(temp[1]) == 1) ? true : false;
				} else if (temp[0].equals("eraseflash")) {
//					mIsEraseFlash = (Integer.valueOf(temp[1]) == 1) ? true : false;
				}else if (temp[0].equals("checkwifi")) {
//					mIsCheckWiFi = (Integer.valueOf(temp[1]) == 1) ? true : false;
				}else if (temp[0].equals("checketh")) {
//					mIsCheckEth = (Integer.valueOf(temp[1]) == 1) ? true : false;
				}
			}
			reader.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return false;
	}
}
