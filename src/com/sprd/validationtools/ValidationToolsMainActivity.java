
package com.sprd.validationtools;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;

import com.sprd.validationtools.itemstest.AutoListItemTestActivity;
import com.sprd.validationtools.nonpublic.SystemPropertiesProxy;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.ComponentName;
import com.sprd.validationtools.background.BackgroundBtTest;
import com.sprd.validationtools.background.BackgroundGpsTest;
import com.sprd.validationtools.background.BackgroundSdTest;
import com.sprd.validationtools.background.BackgroundSimTest;
import com.sprd.validationtools.background.BackgroundTest;
import com.sprd.validationtools.background.BackgroundTestActivity;
import com.sprd.validationtools.background.BackgroundWifiTest;
import com.sprd.validationtools.itemstest.ListItemTestActivity;
import com.sprd.validationtools.itemstest.MMI2TestActivity;
import com.sprd.validationtools.itemstest.TestResultActivity;
import com.sprd.validationtools.modules.FullTestItemList;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.sqlite.EngSqlite;
import com.sprd.validationtools.testinfo.GoogleDRMVersionTest;
import com.sprd.validationtools.testinfo.TestInfoMainActivity;
import com.sprd.validationtools.testinfo.GoogleKeyActivity;
import com.sprd.validationtools.testinfo.MainResultActivity;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import com.sprd.validationtools.utils.VolumeUtil;//tqy 230325
import packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest.AgingTest;


public class ValidationToolsMainActivity extends Activity implements
        AdapterView.OnItemClickListener {
    private final static String TAG = "ValidationToolsMainActivity";
    private final static int FULL_TEST = 0;
    private final static int UNIT_TEST = 1;
    private final static int TEST_INFO = 2;
    private final static int CAMERA_CALI_VERIFY = 3;
    private final static int RESET = 4;
    private final static boolean SUPPORT_CAMERA_FEATURE = true;
    private String[] mListItemString;
    private ListView mListView;
    private ArrayList<TestItem> mFullTestArray = null;
    private int mFullTestCur = 0;
    private int mUserId;

    private ArrayList<BackgroundTest> mBgTest = null;

    private  boolean mIsTested = false;
    public final static String IS_SYSTEM_TESTED = "is_system_tested";
    private SharedPreferences mPrefs;
    private long time = 0;
    //Save full test used time
    public final static String FULL_TEST_USED_TIME = "fulltest_used_time";
    private long mFullTestUsedtime = 0;
    private com.sprd.validationtools.PhaseCheckParse mPhaseCheckParse = null;

    public final static String ACTION_CAMERA_CALI_VERUFY = "com.sprd.cameracalibration.START_CAMERACALIBRATION";
    public final static String EXTRA_GET_PHASECHECK = "phasecheck_result";
    public final static String WIPE_EXTERNAL_STORAGE = "android.intent.extra.WIPE_EXTERNAL_STORAGE";

    private ArrayAdapter<String> mArrayAdapter = null;
	//tqy230325
	private VolumeUtil mVolumeUtil;
	private int mMusicVol;
	private int mCallVol;
	private int mSystemVol;
	private int mAlermVol;
	//tqy230325
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "oncreate start!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation_tools_main);
        if(SUPPORT_CAMERA_FEATURE && com.sprd.validationtools.Const.isSupportCameraCaliVeri()){
            mListItemString = new String[] {
             /*       this.getString(R.string.full_test),
                    this.getString(R.string.item_test),
                    this.getString(R.string.test_info),
                    this.getString(R.string.camera_cali_verify),
                    this.getString(R.string.reset) */
			  /*this.getString(R.string.test_mmi1),*/
			  /*this.getString(R.string.test_mmi2),*/
			  this.getString(R.string.test_manual), //手动测试
                    this.getString(R.string.test_auto),
			  this.getString(R.string.test_aging),
			  this.getString(R.string.google_key),
			  this.getString(R.string.dmr_key),
//			  this.getString(R.string.test_info),
			  this.getString(R.string.test_case_result)

            };
        }else{
            mListItemString = new String[] {
                /*    this.getString(R.string.full_test),
                    this.getString(R.string.item_test),
                    this.getString(R.string.test_info),
                    this.getString(R.string.reset) */
			 /*this.getString(R.string.test_mmi1),*/
			 /*this.getString(R.string.test_mmi2),*/
                    this.getString(R.string.test_manual),
                    this.getString(R.string.test_auto),
			 this.getString(R.string.test_aging),
			 this.getString(R.string.google_key),

                    this.getString(R.string.dmr_key),
//			 this.getString(R.string.test_info),
			 this.getString(R.string.test_case_result)
            };
        }
        mListView = (ListView) findViewById(R.id.ValidationToolsList);
        mArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_list_item, mListItemString);

        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mIsTested = mPrefs.getBoolean(IS_SYSTEM_TESTED, false);
        mUserId = UserHandle.myUserId();
        mPhaseCheckParse = com.sprd.validationtools.PhaseCheckParse.getInstance();
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        ValidationToolsUtils.parsePCBAConf();
        startValidationToolsService(this, true);
		
		////tqy 230325
		//maxVal();
		////tqy 230325
    }
	
	////tqy 230325
	private void maxVal(){
		mVolumeUtil = new VolumeUtil(getApplicationContext());
		mMusicVol = mVolumeUtil.getMediaVolume();
		mCallVol = mVolumeUtil.getCallVolume();
		mSystemVol = mVolumeUtil.getSystemVolume();
		mAlermVol = mVolumeUtil.getAlermVolume();
		mVolumeUtil.setMediaVolume(mVolumeUtil.getMediaMaxVolume());
		mVolumeUtil.setCallVolume(mVolumeUtil.getCallMaxVolume());
		mVolumeUtil.setSystemVolume(mVolumeUtil.getSystemMaxVolume());
		mVolumeUtil.setAlermVolume(mVolumeUtil.getAlermMaxVolume());
	}
	private void userVal(){
		mVolumeUtil.setMediaVolume(mMusicVol);
		mVolumeUtil.setCallVolume(mCallVol);
		mVolumeUtil.setSystemVolume(mSystemVol);
		mVolumeUtil.setAlermVolume(mAlermVol);
	}
	////tqy 230325

    @Override
    public void onPause() {
        if(mUserId==0){
            saveTestInfo();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //stop service
        startValidationToolsService(this, false);
        super.onDestroy();
		////tqy 230325
		//userVal();
		////tqy 230325
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == com.sprd.validationtools.Const.TEST_ITEM_DONE) {
            fullTest();
        }
    }

    private void startValidationToolsService(Context context,boolean startService){
        if(context == null) return;
        Intent intent = new Intent(context, com.sprd.validationtools.ValidationToolsService.class);
        if(startService){
            intent.setFlags(com.sprd.validationtools.ValidationToolsService.FLAG_START_FOREGROUND);
            context.startService(intent);
        }else{
            intent.setFlags(com.sprd.validationtools.ValidationToolsService.FLAG_STOP_FOREGROUND);
            context.stopService(intent);
        }
    }

    private void storePhaseCheck() {
        try {
            String station = com.sprd.validationtools.BaseActivity.STATION_MMIT_VALUE;
            if(mPhaseCheckParse == null){
                return;
            }
            EngSqlite engSqlite = EngSqlite.getInstance(this);
            if (engSqlite == null){
                return;
            }
            Log.d(TAG, "storePhaseCheck: fail = "+engSqlite.queryFailCount() + ", NotTest = " + engSqlite.queryNotTestCount());
            mPhaseCheckParse.writeStationTested(station);
            if (engSqlite.queryFailCount() == 0 && engSqlite.queryNotTestCount()== 0) {
                mPhaseCheckParse.writeStationPass(station);
            }else {
                mPhaseCheckParse.writeStationFail(station);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void fullTest() {
        if (mFullTestArray != null && mFullTestCur < mFullTestArray.size()) {
            Intent intent = new Intent();
            intent.setClassName(this, mFullTestArray.get(mFullTestCur).getTestClassName());
            intent.putExtra(com.sprd.validationtools.Const.INTENT_PARA_TEST_NAME,
                    mFullTestArray.get(mFullTestCur).getTestName());
            intent.putExtra(com.sprd.validationtools.Const.INTENT_PARA_TEST_INDEX, mFullTestCur);
            startActivityForResult(intent, 0);

            mFullTestCur++;
        } else if (mBgTest != null && mFullTestArray != null) {
            EngSqlite engSqlite = EngSqlite.getInstance(this);
            addFailedBgTestToTestlist();
            StringBuffer buffer = new StringBuffer("");
            buffer.append(getResources().getString(R.string.bg_test_notice) + "\n\n");
            for (BackgroundTest bgTest : mBgTest) {
                bgTest.stopTest();
                engSqlite.updateDB(bgTest.getTestItem(getApplicationContext()).getTestClassName(),
                        bgTest.getResult() == BackgroundTest.RESULT_PASS ? com.sprd.validationtools.Const.SUCCESS
                                : com.sprd.validationtools.Const.FAIL);
                buffer.append(bgTest.getResultStr());
                buffer.append("\n\n");
            }

            //Restore pharsecheck.
            storePhaseCheck();

            Intent intent = new Intent(ValidationToolsMainActivity.this,
                    BackgroundTestActivity.class);
            intent.putExtra(com.sprd.validationtools.Const.INTENT_BACKGROUND_TEST_RESULT, buffer.toString());
            startActivityForResult(intent, 0);
            mBgTest = null;
        } else {
            mFullTestUsedtime = System.currentTimeMillis() - time;
            saveFullTestUsedTime();
            Intent intent = new Intent(ValidationToolsMainActivity.this,
                    TestResultActivity.class);
            intent.putExtra("start_time", mFullTestUsedtime);
            startActivity(intent);
        }
    }

    private void addFailedBgTestToTestlist() {
        for (BackgroundTest bgTest : mBgTest) {
            if (bgTest.getResult() != BackgroundTest.RESULT_PASS) {
                TestItem item = bgTest.getTestItem(getApplicationContext());
                mFullTestArray.add(item);
            }
        }
    }

    private void startBackgroundTest() {
        mBgTest = new ArrayList<BackgroundTest>();
        mBgTest.add(new BackgroundBtTest(this));
        mBgTest.add(new BackgroundWifiTest(this));
        mBgTest.add(new BackgroundGpsTest(this));
        mBgTest.add(new BackgroundSimTest(this));
        mBgTest.add(new BackgroundSdTest(this));
        for (BackgroundTest bgTest : mBgTest) {
            bgTest.startTest();
        }
    }

    public void saveTestInfo() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(IS_SYSTEM_TESTED, mIsTested);
        editor.apply();
    }

    public void saveFullTestUsedTime() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong(FULL_TEST_USED_TIME, mFullTestUsedtime);
        editor.apply();
    }

    public void onResume(){
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView l, View v, int position, long id) {
        /* SPRD: bug453083 ,Multi user mode, set button is not click. {@ */
        if (mUserId != 0) {
            Toast.makeText(getApplicationContext(), R.string.multi_user_hint, Toast.LENGTH_LONG).show();
            return;
        }
        /* @} */
        Log.d(TAG, "position:"+position+",id="+id);
        if(mArrayAdapter != null){
            String clickItem = mArrayAdapter.getItem(position);
            Log.d(TAG, "clickItem:"+clickItem);
            if(getString(R.string.full_test).equals(clickItem)){
                time = System.currentTimeMillis();
                mFullTestArray = FullTestItemList.getInstance(getApplicationContext()).getTestItemList();
                mFullTestCur = 0;
                mIsTested = true;
                startBackgroundTest();
                fullTest();
            }else if(getString(R.string.test_mmi1/* item_test */).equals(clickItem)){
                Intent intent = new Intent(this, ListItemTestActivity.class);
                startActivity(intent);
			}else if(getString(R.string.test_mmi2).equals(clickItem)){
                 Intent intent = new Intent(this, MMI2TestActivity.class);
                startActivity(intent);	 
			}else if(getString(R.string.test_aging).equals(clickItem)){
				/*Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ComponentName comp = new ComponentName("com.mediatek.factorymode", "com.mediatek.factorymode.agingtest.AgingTestBegin");
				intent.setComponent(comp);
				startActivity(intent);*/
                Intent intent = new Intent(this, AgingTest.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
			}else if(getString(R.string.google_key).equals(clickItem)){
                Intent intent = new Intent(this, GoogleKeyActivity.class);
                startActivity(intent);	
			}else if(getString(R.string.test_case_result).equals(clickItem)){
                /*Intent intent = new Intent(this, MainResultActivity.class);
                startActivity(intent);*/

                Intent intent = new Intent(this, TestResultActivity.class);
                startActivity(intent);
            }else if(getString(R.string.test_info).equals(clickItem)){
                Intent intent = new Intent(this, TestInfoMainActivity.class);
                intent.putExtra(IS_SYSTEM_TESTED, mIsTested);
                startActivity(intent);
            }else if(getString(R.string.camera_cali_verify).equals(clickItem)){
                launcherCameraCaliVerify();
            }else if(getString(R.string.reset).equals(clickItem)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.reset)
                .setMessage(R.string.factory_reset_message)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                        intent.setPackage("android");
                        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                        intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
                        intent.putExtra(WIPE_EXTERNAL_STORAGE, false);
                        ValidationToolsMainActivity.this.sendBroadcast(intent);
                    }
                })
                .setPositiveButton(android.R.string.cancel, null);
                builder.show();
            }else if (getString(R.string.test_auto).equals(clickItem)){
                Intent intent = new Intent(this, AutoListItemTestActivity.class);
                startActivity(intent);
            }else if (getString(R.string.test_manual).equals(clickItem)){
                //test_manual
                Intent intent = new Intent(this, ListItemTestActivity.class);
                startActivity(intent);
            }else if (getString(R.string.dmr_key).equals(clickItem)){
                //test_manual
                Intent intent = new Intent(this, GoogleDRMVersionTest.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mUserId != 0) {
            finish();
            return;
        }
        if (!SystemPropertiesProxy.get("ro.bootmode").contains("engtest")) {
            super.onBackPressed();
        }
    }

    private void launcherCameraCaliVerify() {
        int mNumberOfCameras = android.hardware.Camera.getNumberOfCameras();
        if (mNumberOfCameras <= 0) {
            Toast.makeText(getApplicationContext(), "camera num is 0!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String phasecheck = com.sprd.validationtools.PhaseCheckParse.getInstance().getPhaseCheck();
            Intent intent = new Intent(ACTION_CAMERA_CALI_VERUFY);
            intent.putExtra(EXTRA_GET_PHASECHECK, phasecheck);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), "com.sprd.cameracalibration not found!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }
    }
}
