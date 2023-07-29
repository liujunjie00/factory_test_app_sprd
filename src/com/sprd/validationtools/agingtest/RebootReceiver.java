package packages.apps.ValidationTools.src.com.sprd.validationtools.agingtest;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class RebootReceiver extends BroadcastReceiver {
	private SharedPreferences mSharedPreferences;
	private SharedPreferences mSharedPreferencesAging;
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			mSharedPreferences = context.getSharedPreferences("state", 0);
			mSharedPreferencesAging = context.getSharedPreferences("aging", 0);
			boolean isPlayVideoSp = mSharedPreferencesAging.getBoolean("isPlayVideoSp", false);
			boolean isRebootSp = mSharedPreferencesAging.getBoolean("isRebootSp", false);
			int rebootFlag = mSharedPreferences.getInt("reboot_flag", 0);
			int rebootCount= mSharedPreferences.getInt("reboot_count", 0);
			int maxtime= mSharedPreferences.getInt("reboot_max", 0);
			Log.d("StressTest", "=========onReceive===rebootFlag:" + rebootFlag);
			if (isPlayVideoSp) {
				Intent intentPv = new Intent(context, AgingTest.class);
				intentPv.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentPv);
			} else {
				if (rebootFlag == 0) {
					/*if (isRebootSp) {
						Intent intentReboot = new Intent(context, AgingTest.class);
						intentReboot.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(intentReboot);
					}*/
				} else {
					Intent pintent = new Intent(context, RebootTest.class);
					pintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(pintent);
				}
			}
		}
	}

}
