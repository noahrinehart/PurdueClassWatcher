package com.nrinehart.purdueclasswatcher.activities;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

import com.nrinehart.purdueclasswatcher.R;
import com.nrinehart.purdueclasswatcher.services.NotificationAlarmReceiver;

/**
 * Created by noahrinehart on 8/29/16.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }


    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.prefs_main);
            setHasOptionsMenu(true);

            ListPreference syncPreference = (ListPreference) findPreference("sync_frequency");
            syncPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.d(TAG, "New value is " + o);
                    scheduleNotifications(getActivity(), o.toString());

                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().finish();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


        // Setup recurring alarm based on preference
        public static void scheduleNotifications(Activity activity, String time) {
            // Intent to start AlarmReceiver

            Intent intent = new Intent(activity.getApplicationContext(), NotificationAlarmReceiver.class);
            // PendingIntent starts after alarm goes off
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(activity.getApplicationContext(), NotificationAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            // Cancels alarm if it exists already, changing sync time
            cancelAlarmIfExists(intent, activity);
            // Gets current time
            long systemMillis = System.currentTimeMillis();
            // Initiates alarm manager off of service
            AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            // Sets repeating alarm from alarm manager
            switch (time) {
                case "15": // 15 Minutes
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, systemMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
                    Log.d(TAG, "Setting alarm for 15 minutes");
                    break;
                case "30": // 30 minutes
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, systemMillis, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);
                    Log.d(TAG, "Setting alarm for 30 minutes");
                    break;
                case "60": // 1 hour
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, systemMillis, AlarmManager.INTERVAL_HOUR, pendingIntent);
                    Log.d(TAG, "Setting alarm for 1 hour");
                    break;
                case "720": // 12 hours
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, systemMillis, AlarmManager.INTERVAL_HALF_DAY, pendingIntent);
                    Log.d(TAG, "Setting alarm for 12 hours");
                    break;
                case "1440": // 1 day
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, systemMillis, AlarmManager.INTERVAL_DAY, pendingIntent);
                    Log.d(TAG, "Setting alarm for 1 day");
                    break;
                default:
                    break;
            }
        }

        public static void cancelAlarmIfExists(Intent intent, Activity activity){
            try{
                PendingIntent pendingIntent = PendingIntent.getBroadcast(activity.getApplicationContext(), NotificationAlarmReceiver.REQUEST_CODE, intent, 0);
                AlarmManager am = (AlarmManager) activity.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                am.cancel(pendingIntent);
                Log.d(TAG, "Pending intent alarm canceled");
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "Pending intent not found!");
            }
        }
    }



}
