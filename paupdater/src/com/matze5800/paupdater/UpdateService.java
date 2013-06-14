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

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.matze5800.paupdater.Shell.ShellException;

import java.io.*;

public class UpdateService extends Service {
    //    Variables
    public Context context;
    public boolean flashGapps;
    public int mode;
    public String rom_md5, romURL,gappsURL;

    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    public SharedPreferences prefs;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        WakeLock
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PA Updater");
        mWakeLock.acquire();

        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        flashGapps = prefs.getBoolean("prefFlashGapps", true);
        rom_md5 = prefs.getString("rom_md5", "error reading!");
        if (intent != null) {
            mode = intent.getIntExtra("mode", 0);
            switch (mode) {
                case 0:    //First Start: Run DownloadReq for ROM
                    prefs.edit().putBoolean("update_running", true).commit();
                    prefs.edit().putBoolean("rom_checked", false).commit();
                    if (prefs.getBoolean("customZip", false)) {
                        prefs.edit().putBoolean("RomDownloaded", true).commit();
                        prefs.edit().putBoolean("rom_checked", true).commit();
                        if (flashGapps) {
                            RequestGappsDl();
                        } else {
                            prefs.edit().putBoolean("GappsDownloaded", true).commit();
                            DownloadFinished();
                        }
                    }
                    File rom = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "rom.zip");
                    if (rom.exists()) {
                        Functions.Notify(this, "Found ROM, checking MD5...");
                        if (Functions.checkMD5(rom_md5, rom)) {
                            prefs.edit().putBoolean("RomDownloaded", true).commit();
                            prefs.edit().putBoolean("rom_checked", true).commit();
                            if (flashGapps) {
                                RequestGappsDl();
                            } else {
                                prefs.edit().putBoolean("GappsDownloaded", true).commit();
                                DownloadFinished();
                            }
                        } else {
                            Log.i("romCheck", "MD5 check failed!");
                            RequestRomUrl(intent);
                        }
                    } else {
                        RequestRomUrl(intent);
                    }
                    break;
                case 1: //Second Start: Got URL for ROM
                    romURL = intent.getStringExtra("url");
                    new DownloadRomTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
                    if (flashGapps) {
                        RequestGappsDl();
                    }
                    break;
                case 2: //Third Start: Got URL for Gapps
                    gappsURL = intent.getStringExtra("url");
                    new DownloadGappsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
                    break;
            }
        } else {
            new ResumeRomTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
            if (flashGapps) {
                new ResumeGappsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, 1);
            }

            Log.w("UpdateService", "Resuming downloads");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void RequestRomUrl(Intent intent) {
        Functions.Notify(this, "Requesting ROM-Download...");
        Intent i = new Intent();
        i.setClass(this, DownloadReq.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        i.putExtra("gooShortURL", intent.getStringExtra("gooShortURL"));
        startActivity(i);
    }

    private class DownloadRomTask extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... arg) {
            prefs.edit().putString("rom_url", romURL).commit();
            Functions.DownloadFile(romURL, "rom.zip", context, 1);
            return 1;
        }

        protected void onPostExecute(Integer result) {
            prefs.edit().putBoolean("RomDownloaded", true).commit();
            Log.i("DownloadRomTask", "Rom download complete!");
            DownloadFinished();
        }
    }

    private class ResumeRomTask extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... arg) {
            romURL = prefs.getString("rom_url", "");
            Functions.ResumeDownloadFile(romURL, "rom.zip", context, 1);
            return 1;
        }

        protected void onPostExecute(Integer result) {
            prefs.edit().putBoolean("RomDownloaded", true).commit();
            Log.i("DownloadRomTask", "Rom download complete!");
            DownloadFinished();
        }
    }

    private class DownloadGappsTask extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... arg) {
            prefs.edit().putString("gapps_url", gappsURL).commit();
            Functions.DownloadFile(gappsURL, "gapps.zip", context, 2);
            return 1;
        }

        protected void onPostExecute(Integer result) {
            prefs.edit().putBoolean("GappsDownloaded", true).commit();
            Log.i("DownloadGappsTask", "Gapps download complete!");
            DownloadFinished();
        }
    }

    private class ResumeGappsTask extends AsyncTask<Integer, Integer, Integer> {
        protected Integer doInBackground(Integer... arg) {
            gappsURL = prefs.getString("gapps_url", "");
            Functions.ResumeDownloadFile(gappsURL, "gapps.zip", context, 2);
            return 1;
        }

        protected void onPostExecute(Integer result) {
            prefs.edit().putBoolean("GappsDownloaded", true).commit();
            Log.i("DownloadGappsTask", "Gapps download complete!");
            DownloadFinished();
        }
    }

    public void DownloadFinished() {
        if (prefs.getBoolean("GappsDownloaded", false) && prefs.getBoolean("RomDownloaded", false)) {
            Log.i("DownloadFinished", "All downloads are complete!");
            if (!prefs.getBoolean("rom_checked", false)) {
                Functions.Notify(context, "Checking Rom MD5...");
                File rom = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "rom.zip");
                if (!Functions.checkMD5(rom_md5, rom)) {
                    Toast.makeText(getApplicationContext(), R.string.error_md5_rom, Toast.LENGTH_LONG).show();
                    rom.delete();
                    Abort();
                }
            }
            if (!prefs.getBoolean("gapps_checked", false)) {
                if (flashGapps) {
                    Functions.Notify(context, "Checking Gapps MD5...");
                    File gapps = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "gapps.zip");
                    if (!Functions.checkMD5(prefs.getString("gappsmd5", ""), gapps)) {
                        Toast.makeText(getApplicationContext(), R.string.error_md5_gapps, Toast.LENGTH_LONG).show();
                        gapps.delete();
                        Abort();
                    }
                }
            }
            CheckedMD5();
        }
    }

    public void RequestGappsDl() {
        prefs.edit().putBoolean("gapps_checked", false).commit();
        File gapps = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "gapps.zip");
        if (gapps.exists()) {
            Functions.Notify(context, "Found Gapps... Checking MD5...");
            if (Functions.checkMD5(prefs.getString("gappsmd5", ""), gapps)) {
                prefs.edit().putBoolean("gapps_checked", true).commit();
                prefs.edit().putBoolean("GappsDownloaded", true).commit();
                DownloadFinished();
            } else {
                RequestGappsUrl();
            }
        } else {
            RequestGappsUrl();
        }
    }

    public void RequestGappsUrl() {
        Functions.Notify(context, "Requesting Gapps-Download...");
        Intent i = new Intent();
        i.setClass(context, DownloadReq.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("gooShortURL", prefs.getString("gappsURL", null));
        i.putExtra("gapps", true);
        startActivity(i);
    }

    public void Abort() {
        Functions.Clear(this);
        prefs.edit().putBoolean("update_running", false).commit();
        this.stopSelf();
    }

    public void setupRebootNotification() {
        Log.i("prepared", "All zips are prepared!");
        Notification.Builder nBuilder;
        nBuilder = new Notification.Builder(context);
        Intent resultIntent = new Intent(context, Reboot.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(Reboot.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        nBuilder.setContentIntent(resultPendingIntent);
        nBuilder.setSmallIcon(R.drawable.ic_launcher);
        nBuilder.setContentTitle("I'm done!");
        nBuilder.setContentText("Touch this notification to reboot to Recovery.");
        nBuilder.setAutoCancel(false);
        nBuilder.setOngoing(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, nBuilder.build());
        prefs.edit().putBoolean("update_running", false).commit();
        mWakeLock.release();
    }

    public void CheckedMD5() {
        if (prefs.getBoolean("prefKernelRestore", true)) {
            Functions.Notify(context, "Creating boot image...");
            File f = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "boot.img");
            f.delete();
            String bootPart = Functions.getBootPartition(context);
            try {
                Shell.sudo("dd if=" + bootPart + " of=" + Environment.getExternalStorageDirectory().getPath() + "/pa_updater/boot.img");
            } catch (ShellException e) {
                Log.e("boot_img", "Error creating boot.img!");
                Abort();
                e.printStackTrace();
            }
            Functions.Notify(context, "Creating kernel restore zip...");
            File kernelZip = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "kernel.zip");
            kernelZip.delete();
            try {
                InputStream in = context.getResources().openRawResource(R.raw.kernel);
                OutputStream out;
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/pa_updater", "kernel.zip"));
                byte[] buf = new byte[1024];
                int len;
                try {
                    while ((len = in.read(buf, 0, buf.length)) != -1) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                Log.e("kernel_restore", "Error writing empty kernel zip!");
                e.printStackTrace();
            }
            Log.i("kernel_restore", "Extracted empty zip.");
            try {
                File zipFile = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "kernel.zip");
                File[] file = {new File(Environment.getExternalStorageDirectory() + "/pa_updater", "boot.img")};
                Functions.addFilesToExistingZip(zipFile, file);
            } catch (IOException e) {
                Log.e("kernel_restore", "Error inserting kernel into zip!");
                e.printStackTrace();
            }
        }
        if (prefs.getBoolean("prefPrefsRestore", true)) {
            Functions.Notify(context, "Creating PA prefs restore zip...");
            File prefFile = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "properties.conf");
            prefFile.delete();
            try {
                Shell.sudo("cp /system/etc/paranoid/properties.conf " + Environment.getExternalStorageDirectory() + "/pa_updater/properties.conf");
            } catch (ShellException e) {
                Log.e("pref_restore", "Error getting current properties.conf");
                e.printStackTrace();
            }
            File prefZip = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "pref.zip");
            prefZip.delete();
            try {
                InputStream in = context.getResources().openRawResource(R.raw.pref);
                OutputStream out;
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/pa_updater", "pref.zip"));
                byte[] buf = new byte[1024];
                int len;
                try {
                    while ((len = in.read(buf, 0, buf.length)) != -1) {
                        out.write(buf, 0, len);
                    }
                } finally {
                    in.close();
                    out.close();
                }
            } catch (IOException e) {
                Log.e("pref_restore", "Error writing empty pref zip!");
                e.printStackTrace();
            }
            Log.i("pref_restore", "extracted empty pref zip!");
            File zipFile = new File(Environment.getExternalStorageDirectory() + "/pa_updater", "pref.zip");
            File[] file = {new File(Environment.getExternalStorageDirectory() + "/pa_updater", "properties.conf")};
            try {
                Functions.addFilesToExistingZip(zipFile, file);
            } catch (IOException e) {
                Log.e("pref_restore", "Error generating pref zip!");
                e.printStackTrace();
            }
        }
        Functions.Clear(context);
        setupRebootNotification();
    }
}
