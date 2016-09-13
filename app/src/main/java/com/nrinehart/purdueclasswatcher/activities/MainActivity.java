package com.nrinehart.purdueclasswatcher.activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nrinehart.purdueclasswatcher.DownloadClassInfoAsyncTask;
import com.nrinehart.purdueclasswatcher.PurdueClass;
import com.nrinehart.purdueclasswatcher.PurdueClassRealmAdapter;
import com.nrinehart.purdueclasswatcher.R;
import com.nrinehart.purdueclasswatcher.eventbus.ClassInfoResultEvent;
import com.nrinehart.purdueclasswatcher.eventbus.EventBus;
import com.nrinehart.purdueclasswatcher.eventbus.RemoveClassResult;
import com.nrinehart.purdueclasswatcher.services.NotificationAlarmReceiver;
import com.squareup.otto.Subscribe;

import java.util.List;

import co.moonmonkeylabs.realmrecyclerview.RealmRecyclerView;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    protected RealmConfiguration realmConfiguration;
    protected Realm realm;
    protected RealmRecyclerView realmRecyclerView;
    protected PurdueClassRealmAdapter adapter;

    protected ListPreference syncFrequencyListPreference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        //TODO: Fetch new class
        //TODO: Notification services (notification, vibrate, sync freq)
        //TODO: Comment everything
        //TODO: Orientation
        //TODO: Strings

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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
                            downloadClass(input.toString());
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

    private void downloadClass(String crn) {
        DownloadClassInfoAsyncTask downloadClassInfoAsyncTask = new DownloadClassInfoAsyncTask();
        Log.d(TAG, crn + "sent to async");
        downloadClassInfoAsyncTask.execute(crn);
    }

    @Subscribe
    public void onClassDownloaded(ClassInfoResultEvent event) {
        String crn = event.getPurdueClass().getCrn();
        if (event.getPurdueClass() != null) {
            Log.d(TAG, "Adding/updating new Class to realm " + crn);
            if (realm != null) {
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(event.getPurdueClass());
                realm.commitTransaction();
            } else {
                Log.d(TAG, "Realm was null!");
            }

        } else {
            Log.d(TAG, "Class not found! " + crn);
            new MaterialDialog.Builder(this)
                    .title("Class not found!")
                    .positiveText("Ok")
                    .show();
        }

    }

    @Subscribe
    public void onRemoveClassResult(RemoveClassResult result) {
        int position = result.getPurdueClass();
        if (adapter != null && result != null)
            adapter.onItemSwipedDismiss(position);
    }

    private void refreshClassses() {
        final RealmResults<PurdueClass> classes = realm
                .where(PurdueClass.class)
                .findAll();

        for (PurdueClass clas : classes) {
            Log.d(TAG, "Updating class: " + clas.getCrn());
            downloadClass(clas.getCrn());
        }
    }


}
