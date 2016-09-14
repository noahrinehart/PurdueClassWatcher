package com.nrinehart.purdueclasswatcher.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by noahrinehart on 8/31/16.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 1337;
    public static final String ACTION = "com.nrinehart.purdueclasswatcher.services.alarm";


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NotificationService.class);
        context.startService(i);
    }
}
