package sk.xanion.routerconfig;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;

import sk.xanion.routerconfig.fragment.SettingsFragment;
import sk.xanion.routerconfig.fragment.SetupWirelessFragment;
import sk.xanion.routerconfig.model.WirelessStatus;
import sk.xanion.routerconfig.receiver.RouterConfigAlarmReceiver;
import sk.xanion.routerconfig.util.Settings;
import sk.xanion.routerconfig.util.SettingsValidator;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, RequestServerData.RequestServerDataListener {
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private boolean loadProperties() {

        return true;
    }

    private void startBlock() {
        PendingIntent intent = getBlockIntent();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 5);
        calendar.set(Calendar.MINUTE, 0);
        getAlarmMng().setInexactRepeating(AlarmManager.RTC_WAKEUP, -System.currentTimeMillis() + 5 * 1000L, AlarmManager.INTERVAL_DAY, intent);
        //getAlarmMng().setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, intent);
    }

    private void startUnblock() {
        PendingIntent intent = getUnBlockIntent();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);

        getAlarmMng().setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5 * 1000L, AlarmManager.INTERVAL_DAY, intent);
        //getAlarmMng().setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, intent);
    }

    private void stopAlarm() {
        AlarmManager mng = getAlarmMng();
        mng.cancel(getBlockIntent());
        mng.cancel(getUnBlockIntent());
    }

    private AlarmManager getAlarmMng() {
        return (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    }

    private PendingIntent getBlockIntent() {
        Intent intent = new Intent(this, RouterConfigAlarmReceiver.class);
        intent.putExtra(RouterConfigAlarmReceiver.KEY_METHOD_TYPE, RequestServerData.METHOD_BLOCK);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    private PendingIntent getUnBlockIntent() {
        Intent intent = new Intent(this, RouterConfigAlarmReceiver.class);
        intent.putExtra(RouterConfigAlarmReceiver.KEY_METHOD_TYPE, RequestServerData.METHOD_UNBLOCK);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    private boolean isValidConfig() {
        if (!TextUtils.isEmpty(SettingsValidator.validate(this))) {
            return false;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isValidConfig()) {
            navigateWirelesSSettings();
        } else if (loadProperties()) {
            navigateWirelesSSettings();
        } else {
            navigateAppSettings();
        }
    }


    private void navigateWirelesSSettings() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_camera);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));
    }

    private void navigateAppSettings() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_gallery);
        onNavigationItemSelected(navigationView.getMenu().getItem(1));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            navigateAppSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment f = null;
        if (id == R.id.nav_camera) {
            f = SetupWirelessFragment.newInstance(null, null);
        } else if (id == R.id.nav_gallery) {
            f = SettingsFragment.newInstance(null, null);
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        if (f != null) {
            getFragmentManager().beginTransaction().replace(R.id.frame_content, f).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onButtonPressed(View v) {
        switch (v.getId()) {
            case R.id.btnBlokuj: {
                new RequestServerData(this, RequestServerData.METHOD_BLOCK).execute();
                break;
            }
            case R.id.btnOdblokuj: {
                new RequestServerData(this, RequestServerData.METHOD_UNBLOCK).execute();
                break;
            }
            case R.id.btnAutomatickeBlokovanieOn: {
                startBlock();
                startUnblock();
                break;
            }
            case R.id.btnAutomatickeBlokovanieOff: {
                stopAlarm();
            }
        }
    }

    @Override
    public void onPostExecute(Context ctx, Bundle result, int methodType) {
        if (this == ctx) {

            if (result != null) {
                TextView tv = null;
                if (methodType == 3) {
                    if (result.containsKey("error")) {
                        tv = (TextView) findViewById(R.id.tvResult);
                        tv.setText(result.getString("error", ""));
                    } else if (result.containsKey(RequestServerData.KEY_WIRELESS_STATUS)) {
                        WirelessStatus status = (WirelessStatus) result.getSerializable(RequestServerData.KEY_WIRELESS_STATUS);
                        if (status.exception != null) {
                            tv = (TextView) findViewById(R.id.tvResult);
                            tv.setText(result.getString("error", status.exception));
                        } else {
                            tv = (TextView) findViewById(R.id.tvAktualnyStav);
                            tv.setText(getString(R.string.aktualnyStav) + (Boolean.TRUE.equals(status.active) ? "Blokované" : "Povolené"));
                            ListView v = (ListView) findViewById(R.id.lvMacAdreses);
                            String defMac = "00:00:00:00:00:00";
                            if (status.macAdresses == null) {
                                status.macAdresses = new ArrayList<>();
                            }
                            while (status.macAdresses.size() < 8) {
                                status.macAdresses.add(defMac);
                            }
                            for (int i = 1; i <= 8; i++) {
                                String mac = status.macAdresses.get(i - 1);
                                mac = i + ". " + mac;
                                status.macAdresses.set(i - 1, mac);
                            }
                            v.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, status.macAdresses.subList(0, 8)));
                        }
                    }
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                } else {
                    tv = (TextView) findViewById(R.id.tvResult);
                    tv.setText(result.getString("error", ""));
                    new RequestServerData(this, RequestServerData.METHOD_STATUS).execute();
                }
            }
        }
    }

    @Override
    public void onPreexecute(int methodType) {
        if (mDialog == null) {
            mDialog = new ProgressDialog.Builder(this)
                    .setMessage("Kontaktujem router.")
                    .create();
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }
}
