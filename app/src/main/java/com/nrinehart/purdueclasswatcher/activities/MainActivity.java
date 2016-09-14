package com.nrinehart.purdueclasswatcher.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nrinehart.purdueclasswatcher.services.DownloadClassInfoAsyncTask;
import com.nrinehart.purdueclasswatcher.PurdueClass;
import com.nrinehart.purdueclasswatcher.PurdueClassRealmAdapter;
import com.nrinehart.purdueclasswatcher.R;
import com.nrinehart.purdueclasswatcher.eventbus.ClassInfoResultEvent;
import com.nrinehart.purdueclasswatcher.eventbus.EventBus;
import com.nrinehart.purdueclasswatcher.eventbus.RemoveClassResult;
import com.nrinehart.purdueclasswatcher.services.NotificationService;
import com.squareup.otto.Subscribe;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    protected RealmConfiguration realmConfiguration;
    protected Realm realm;
    protected RealmRecyclerView realmRecyclerView;
    protected PurdueClassRealmAdapter adapter;
    protected RealmResults<PurdueClass> classes;


    protected ListPreference syncFrequencyListPreference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //TODO: Fetch new class
        //TODO: Notification services (notification, vibrate, sync freq) repeated update?
        //TODO: Comment everything
        //TODO: Orientation
        //TODO: Strings
        //TODO: Kotlin?
        //TODO: Reminders
        //TODO: Applictation licensing

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String timingValue = settings.getString("sync_frequency", "60");
        SettingsActivity.SettingsFragment.scheduleNotifications(MainActivity.this, timingValue);

        EventBus.getBus().register(this);

        setupRealm();

        final RealmResults<PurdueClass> classes = realm
                .where(PurdueClass.class)
                .findAll();
        adapter = new PurdueClassRealmAdapter(this, classes, true, true);
        final RealmRecyclerView realmRecyclerView = (RealmRecyclerView) findViewById(R.id.realm_recycler_view);
        realmRecyclerView.setAdapter(adapter);
        realmRecyclerView.setOnRefreshListener(new RealmRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshClassses();
                realmRecyclerView.setRefreshing(false);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInputDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.refresh) {
            RealmRecyclerView realmRecyclerView = (RealmRecyclerView) findViewById(R.id.realm_recycler_view);
            refreshClassses();
            realmRecyclerView.setRefreshing(false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    private void setupRealm() {
        if (realmConfiguration == null) {
            realmConfiguration = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded().build();
            Realm.setDefaultConfiguration(realmConfiguration);

        }
        realm = Realm.getDefaultInstance();
    }

    private void showInputDialog() {
        new MaterialDialog.Builder(this)
                .title("Input CRN")
                .inputRange(5, 5)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("13377...", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        RealmResults<PurdueClass> results = realm
                                .where(PurdueClass.class)
                                .equalTo("crn", input.toString())
                                .findAll();
                        if (results == null || results.isEmpty()) {
                            DownloadClassInfoAsyncTask.downloadClass(input.toString());
                        } else {
                            Log.d(TAG, "Class already exists! " + input.toString());
                            new MaterialDialog.Builder(dialog.getContext())
                                    .title("Class already added!")
                                    .positiveText("Ok")
                                    .show();
                        }
                    }
                })
                .negativeText("Cancel")
                .show();
    }

    //TODO: Make code more modular
    @Subscribe
    public void onClassDownloaded(ClassInfoResultEvent event) {
        PurdueClass purdueClass = event.getPurdueClass();
        if (purdueClass != null) {
            String crn = purdueClass.getCrn();
            if (classExists(purdueClass)) {
                Log.d(TAG, purdueClass.getName() + " is already added");
                if (seatsAvailable(purdueClass) && !purdueClass.isNotified()) {
                    Log.d(TAG, purdueClass.getName() + "has seats available!");
                    sendNotification(purdueClass);
                    purdueClass.setNotified(true);
                    copyOrUpdateToRealm(purdueClass);
                } else {
                    Log.d(TAG, purdueClass.getName() + " is still full");
                }
            } else {
                Log.d(TAG, "Adding/updating new Class to realm " + crn);
                copyOrUpdateToRealm(purdueClass);

            }
        } else {
            Log.d(TAG, "Class not found! ");
            new MaterialDialog.Builder(this)
                    .title("Class not found!")
                    .positiveText("Ok")
                    .show();
        }

    }

    @Subscribe
    public void onRemoveClassResult(final RemoveClassResult result) {

        final RealmResults<PurdueClass> classes = realm
                .where(PurdueClass.class)
                .findAll();
        if (classes.size() > 0) {


            PurdueClass purdueClass = classes.get(result.getPurdueClassPosition());


            if (adapter != null && result != null && purdueClass.getCrn() != null) {
                Log.d(TAG, "Removing class");
//            int position = result.getPurdueClassPosition();
//            adapter.onItemSwipedDismiss(position);
//            Log.d(TAG, "Class has been removed");


                realm.beginTransaction();
                if (classes.size() <= 1) {
                    classes.deleteFromRealm(result.getPurdueClassPosition());
                } else {
                    classes.deleteAllFromRealm();
                }
                adapter.notifyDataSetChanged();
                realm.commitTransaction();


            } else {
                Log.d(TAG, "Class doesn't exist or adapter is null when removed!");
            }
        }
    }

    private void refreshClassses() {
        final RealmResults<PurdueClass> classes = realm
                .where(PurdueClass.class)
                .findAll();

        for (PurdueClass clas : classes) {
            Log.d(TAG, "Updating class: " + clas.getCrn());
            DownloadClassInfoAsyncTask.downloadClass(clas.getCrn());
        }
    }

    private boolean classExists(PurdueClass purdueClass) {
        RealmQuery<PurdueClass> query = realm.where(PurdueClass.class)
                .equalTo("crn", purdueClass.getCrn());

        return query.count() != 0;
    }


    private void sendNotification(PurdueClass purdueClass) {


        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationValue = settings.getBoolean("notification_toggle", true);
        boolean vibrateValue = settings.getBoolean("vibrate_toggle", false);


        if(notificationValue && !NotificationService.isForeground(this)) {
            Log.d(TAG, "Sent notification for " + purdueClass.getName());
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setSmallIcon(R.mipmap.ic_launcher);
            mBuilder.setContentTitle("Empty spots in " + purdueClass.getName() + "!");
            mBuilder.setContentText(purdueClass.getActual() + "/" + purdueClass.getCapactiy());
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setAutoCancel(true);
            if (vibrateValue)
                mBuilder.setVibrate(new long[]{500, 500, 500, 500});
            mBuilder.setLights(Color.rgb(177, 129, 11), 500, 500);


            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());
        }
    }

    private boolean seatsAvailable(PurdueClass purdueClass) {
        return Integer.parseInt(purdueClass.getActual()) < Integer.parseInt(purdueClass.getCapactiy());
    }

    private void copyOrUpdateToRealm(PurdueClass purdueClass) {
        if (realm != null) {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(purdueClass);
            realm.commitTransaction();
        } else {
            Log.d(TAG, "Realm was null!");
        }
    }

}
