package packages.apps.ValidationTools.src.com.sprd.validationtools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TempBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "TempBroadcastReceiver";
    private static final String ACTION_START_TEMP_SERVER = "android.intent.action.START_TEMP_SERVER";
    private static final String ACTION_STOP_TEMP_SERVER = "android.intent.action.STOP_TEMP_SERVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "onReceive action = " + action);
        if (action == null) return;
        if (action.equals(ACTION_START_TEMP_SERVER)) {
            try {
                context.startService(new Intent(context, TempWatermarkService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (action.equals(ACTION_STOP_TEMP_SERVER)) {
            try {
                context.stopService(new Intent(context, TempWatermarkService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
