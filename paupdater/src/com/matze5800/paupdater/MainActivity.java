/*
 * Copyright (C) 2013 PA Updater (Simon Matzeder and Parthipan Ramesh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matze5800.paupdater;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.matze5800.paupdater.fragments.UpdateFragment;

public class MainActivity extends Activity implements ActionBar.TabListener {

    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private static Context context;
    private ViewPager mViewPager;
    private SharedPreferences prefs;
    private String device;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set View
        setContentView(R.layout.activity_main);
        context = this;
        // Get SharedPreferences
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // ActionBar / PagerAdapter
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
                getFragmentManager());

        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager
                .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

        for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mAppSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }

        // Finishes Activity if device is not supported
        device = Functions.detectDevice(context);
        if (device.equals("unsupported")) {
            finish();
        }
    }

    // ActionBar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mViewPager.setCurrentItem(0);
                refresh();
                break;
            case R.id.action_flashM:
                Intent intent = new Intent(this, FlashCustomFiles.class);
                startActivity(intent);
                break;
            case R.id.action_settings:
                Intent i = new Intent(this, UserSettingActivity.class);
                startActivity(i);
                break;
        }
        return true;
    }

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }

    // Gets Goo Version
    public class GetGooVersion extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... noargs) {
            return Functions.CheckGoo(context);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("err")) {
                int gooVer = UpdateFragment.getGooVer();
                int localVer = UpdateFragment.getLocalVer();

                gooVer = Integer.valueOf(result);
                Log.i("gooVer", "goo Version: " + gooVer);
                UpdateFragment.getProgressStatus().setVisibility(8);
                UpdateFragment.getImageStatus().setVisibility(0);
                if (prefs.getBoolean("prefDebugUpdate", false)) {
                    localVer = 1;
                } // DEBUG
                if (gooVer > localVer) {
                    UpdateFragment.getUpdateStatus().setText(
                            R.string.update_available);
                    UpdateFragment.getImageStatus().setImageResource(
                            R.drawable.update);
                    float updateY = UpdateFragment.getUpdate_y();
                    float updateX = UpdateFragment.getUpdate_x();
                    UpdateFragment.getImageStatus().setY(updateY);
                    UpdateFragment.getImageStatus().setX(updateX);
                    UpdateFragment.getUpdateButton().setEnabled(true);
                } else {
                    UpdateFragment.getUpdateStatus().setText(R.string.uptodate);
                    UpdateFragment.getImageStatus().setImageResource(
                            R.drawable.ok);
                    UpdateFragment.getUpdateButton().setEnabled(false);
                }
                if (prefs.getBoolean("update_running", false)) {
                    UpdateFragment.getUpdateButton().setEnabled(false);
                }
            } else {
                Toast.makeText(context, R.string.error_goo_down,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // UpdateButton
    public void startUpdate(View view) {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
        myAlertDialog.setTitle(R.string.update_dialog_title);
        StringBuilder sb = new StringBuilder();
        if (prefs.getBoolean("customZip", false)) {
            String s = getString(R.string.update_dialog_use_rom);
            sb.append(s + prefs.getString("customZipPath", "none"));
        } else {
            String s = getString(R.string.update_dialog_download_rom);
            sb.append(s + prefs.getString("gooFilename", "none"));
        }
        if (prefs.getBoolean("prefFlashGapps", true)) {
            sb.append("\n\n");
            String s = getString(R.string.update_dialog_gapps);
            sb.append(s + prefs.getString("gappsFilename", "none"));
        }
        myAlertDialog.setMessage(sb.toString());
        myAlertDialog.setPositiveButton(R.string.update_dialog_do_it,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (Functions.WifiConnected(context)) {
                            processStartService();
                            finish();
                        } else {
                            AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(
                                    context);
                            myAlertDialog
                                    .setTitle(R.string.update_dialog_no_wifi);
                            myAlertDialog
                                    .setMessage(R.string.update_dialog_lot_traffic);
                            myAlertDialog.setPositiveButton(
                                    R.string.update_dialog_yes,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface arg0, int arg1) {
                                            processStartService();
                                            finish();
                                        }
                                    });
                            myAlertDialog.setNegativeButton(
                                    R.string.update_dialog_no, null);
                            myAlertDialog.show();
                        }
                    }
                });
        myAlertDialog.setNegativeButton(R.string.update_dialog_cancel, null);
        myAlertDialog.show();
    }

    // StartUpdateService
    private void processStartService() {
        Intent intent = new Intent(context.getApplicationContext(),
                UpdateService.class);
        intent.putExtra("gooShortURL", prefs.getString("gooShortURL", ""));
        context.startService(intent);
        // Request root access to ensure that its granted later
        Shell.su();
    }

    // Refresh Button from ActionBar
    public void refresh() {
        UpdateFragment.getUpdateStatus().setText(R.string.check);
        UpdateFragment.getProgressStatus().setVisibility(0);
        UpdateFragment.getImageStatus().setVisibility(8);
        GetGooVersion GooTask = new GetGooVersion();
        GooTask.execute();
    }

    // Get context from MainActivity for Fragments
    public static Context getContext() {
        return context;
    }
}
