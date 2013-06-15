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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.matze5800.paupdater.Shell.ShellException;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class UpdateService extends Service{
    int mode;
    String rom_md5;
    String romURL;
    DownloadManager manager;
    SharedPreferences prefs;
    Context context;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //handleCommand(intent);
        context = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(intent!=null)	{
            mode = intent.getIntExtra("mode", 0);
            switch (mode) {
                case 0:	//First Start: Run DownloadReq for ROM
                    prefs.edit().putBoolean("update_running", true).commit();
                    Intent i = new Intent();
                    i.setClass(this, DownloadReq.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    i.putExtra("gooShortURL", intent.getStringExtra("gooShortURL"));
                    startActivity(i);
                    break;
                case 1: //Second Start: Got URL for ROM
                    rom_md5 = prefs.getString("rom_md5", null);
                    romURL = intent.getStringExtra("url");
                    registerReceiver(onComplete,
                            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    downloadFileWithNotification("Downloading ROM...", romURL, "rom.zip");
                    break;
                case 2: //Third Start: Got URL for Gapps
                    romURL = intent.getStringExtra("url");
                    registerReceiver(onComplete,
                            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                    downloadFileWithNotification("Downloading Gapps...", romURL, "gapps.zip");
                    break;
            }
        } else {
            registerReceiver(onComplete,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mode > 0)
            this.unregisterReceiver(onComplete);
    }

    public void downloadFileWithNotification(String title, String url, String destName) {
        // delete file
        File f = new File(Environment.getExternalStorageDirectory()+"/pa_updater", destName);
        f.delete();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("PA Updater");
        request.setTitle(title);
        // min SDK android 3.2!!!
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalPublicDir("pa_updater", destName);

        manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
        prefs.edit().putLong("download_id", manager.enqueue(request)).commit();
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            prefs.edit().putLong("download_id", 0).commit();
            if (mode == 2) { //Second run: Gapps Download finished
                GotGappsDownload();
            }else{ //First run: ROM Download finished
                Functions.Notify(context, "Checking MD5...");
                File rom = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "rom.zip");
                if (!Functions.checkMD5(rom_md5, rom))	{
                    Toast.makeText(getApplicationContext(), "ROM MD5 Mismatch!!! Aborting!", Toast.LENGTH_LONG).show();
                    Abort();
                }
                if(prefs.getBoolean("prefFlashGapps", true)){
                    File gapps = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "gapps.zip");
                    if(gapps.exists()){
                        if(Functions.checkMD5(prefs.getString("gappsmd5", ""), gapps)){
                            GotGappsDownload();
                        } else {RequestGappsDl();}
                    } else {RequestGappsDl();}
                } else {GotGappsDownload();}
            }
        }
    };

    public void RequestGappsDl()	{
        Functions.Notify(context, "Requesting Gapps-Download...");
        Intent i = new Intent();
        i.setClass(context, DownloadReq.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("gooShortURL", prefs.getString("gappsURL", null));
        i.putExtra("gapps", true);
        startActivity(i);
    }

    public void Abort()	{
        Functions.Clear(this);
        prefs.edit().putBoolean("update_running", false).commit();
        this.stopSelf();
    }

    public void setupRebootNotification()	{
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
    }

    public void GotGappsDownload() {
        if (prefs.getBoolean("prefKernelRestore", true)) {
            Functions.Notify(context, "Creating boot image...");
            File f = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "boot.img");
            f.delete();
            String bootPart = Functions.getBootPartition(context);
            try {
                Shell.sudo("dd if="+bootPart+" of="+Environment.getExternalStorageDirectory().getPath()+"/pa_updater/boot.img");
            } catch (ShellException e) {
                Log.e("boot_img", "Error creating boot.img!");
                Abort();
                e.printStackTrace();
            }
            Functions.Notify(context, "Creating kernel restore zip...");
            File kernelZip = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "kernel.zip");
            kernelZip.delete();
            try {
                InputStream in = context.getResources().openRawResource(R.raw.kernel);
                OutputStream out;
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/pa_updater", "kernel.zip"));
                byte[] buf = new byte[1024];
                int len;
                try {
                    while ( (len = in.read(buf, 0, buf.length)) != -1){
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
                File zipFile = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "kernel.zip");
                File[] file = {new File(Environment.getExternalStorageDirectory()+"/pa_updater", "boot.img")};
                Functions.addFilesToExistingZip(zipFile, file);
            } catch (IOException e) {
                Log.e("kernel_restore", "Error inserting kernel into zip!");
                e.printStackTrace();
            }
        }
        if (prefs.getBoolean("prefPrefsRestore", true)) {
            Functions.Notify(context, "Creating PA prefs restore zip...");
            File prefFile = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "properties.conf");
            prefFile.delete();
            try {
                Shell.sudo("cp /system/etc/paranoid/properties.conf "+Environment.getExternalStorageDirectory()+"/pa_updater/properties.conf");
            } catch (ShellException e) {
                Log.e("pref_restore", "Error getting current properties.conf");
                e.printStackTrace();
            }
            File prefZip = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "pref.zip");
            prefZip.delete();
            try {
                InputStream in = context.getResources().openRawResource(R.raw.pref);
                OutputStream out;
                out = new FileOutputStream(new File(Environment.getExternalStorageDirectory()+"/pa_updater", "pref.zip"));
                byte[] buf = new byte[1024];
                int len;
                try {
                    while ( (len = in.read(buf, 0, buf.length)) != -1){
                        out.write(buf, 0, len);
                    }
                } finally {
                    in.close();
                    out.close();
                }} catch (IOException e) {
                Log.e("pref_restore", "Error writing empty pref zip!");
                e.printStackTrace();
            }
            Log.i("pref_restore", "extracted empty pref zip!");
            File zipFile = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "pref.zip");
            File[] file = {new File(Environment.getExternalStorageDirectory()+"/pa_updater", "properties.conf")};
            try {
                Functions.addFilesToExistingZip(zipFile, file);
            } catch (IOException e) {
                Log.e("pref_restore", "Error generating pref zip!");
                e.printStackTrace();
            } }
        Functions.Clear(context);
        setupRebootNotification();
    }
}