package com.nrinehart.purdueclasswatcher.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nrinehart.purdueclasswatcher.PurdueClass;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by noahrinehart on 8/31/16.
 */
public class NotificationService extends IntentService {

    private static final String TAG = "NotificationService";

    public NotificationService() {
        super("NotificationService");
    }

    protected RealmConfiguration realmConfiguration;
    protected Realm realm;

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("NotificationService", "Handling intent for thing");


        if (realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
            Realm.setDefaultConfiguration(realmConfiguration);

        }
        realm = Realm.getDefaultInstance();

        final RealmResults<PurdueClass> classes = realm
                .where(PurdueClass.class)
                .findAll();






        //For if we want no notification when app open

        if (isForeground(getApplicationContext())) {
            Log.d(TAG, "running in foreground");
        } else {
            Log.d(TAG, "running in background");
            for(PurdueClass purdueClass : classes) {
                Log.d(TAG,purdueClass.getName() + " isn't notified yet");
                DownloadClassInfoAsyncTask.downloadClass(purdueClass.getCrn());
                Log.d(TAG,purdueClass.getName() + " was already notified");
            }
        }
    }

    public static boolean isForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (String activeProcess : processInfo.pkgList) {
                    if (activeProcess.equals(context.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
