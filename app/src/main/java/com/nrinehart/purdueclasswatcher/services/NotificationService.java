package com.nrinehart.purdueclasswatcher.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;

/**
 * Created by noahrinehart on 8/31/16.
 */
public class NotificationService extends IntentService {

    private static final String TAG = "NotificationService";

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("NotificationService", "Handling intent for thing");









//        if (isForeground()) {
//            Log.d(TAG, "running in foreground");
//        } else {
//            Log.d(TAG, "running in background");
//        }
    }

//    private boolean isForeground() {
//        ActivityManager am = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
//            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                for (String activeProcess : processInfo.pkgList) {
//                    if (activeProcess.equals(getApplicationContext().getPackageName())) {
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
}
