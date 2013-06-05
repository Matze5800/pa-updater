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

import com.matze5800.paupdater.Shell.ShellException;

import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class Reboot extends Activity {
SharedPreferences prefs;
Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_reboot);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		context = this;
		myThread.start(); //starten
	}

	Thread myThread = new Thread(new Runnable() {
	    @SuppressLint("SdCardPath")
		@Override
	    public void run() {
	    	//Get preferences
	    	Boolean DelBackup = prefs.getBoolean("prefBackupDel", false);
	    	Boolean DoBackup = prefs.getBoolean("prefBackup", true);
	    	Boolean WipeCache = prefs.getBoolean("prefWipeCache", true);
	    	Boolean WipeDalvik = prefs.getBoolean("prefWipeDalvik", true);
	    	Boolean Boot = prefs.getBoolean("prefBackupBoot", true);
	    	Boolean System = prefs.getBoolean("prefBackupSystem", true);
	    	Boolean Data = prefs.getBoolean("prefBackupData", true);
	    	Boolean Compress = prefs.getBoolean("prefBackupCompress", true);
	    	Boolean md5 = prefs.getBoolean("prefBackupMd5", true);
	    	Boolean Gapps = prefs.getBoolean("prefFlashGapps", true);
	    	Boolean Kernel = prefs.getBoolean("prefKernelRestore", true);
	    	Boolean Prefs = prefs.getBoolean("prefPrefsRestore", true);
	    	Boolean CustomZip = prefs.getBoolean("customZip", false);
	    	
	    	if(DelBackup) {
	    		File dir = new File(Environment.getExternalStorageDirectory()+"/TWRP/BACKUPS");
	    		Functions.deleteDirectory(dir);
	    	}
	    	
	    	//Build backup string
	    	String backup = "echo backup ";
	    	if(DoBackup){
	    		if(Boot){backup=backup+"B";}
	    		if(System){backup=backup+"S";}
	    		if(Data){backup=backup+"D";}
	    		if(Compress){backup=backup+"O";}
	    		if(!md5){backup=backup+"M";}
	    	}
	    	
			String cmd = " >> /cache/recovery/openrecoveryscript";
			
			try {			
			if(DoBackup){Shell.sudo(backup+cmd);}
			if(CustomZip){
				Shell.sudo("echo install "+prefs.getString("customZipPath", "/sdcard/pa_updater/rom.zip")+cmd);
			} else {Shell.sudo("echo install /sdcard/pa_updater/rom.zip"+cmd);}
			if(Gapps){Shell.sudo("echo install /sdcard/pa_updater/gapps.zip"+cmd);}
			if(Kernel){Shell.sudo("echo install /sdcard/pa_updater/kernel.zip"+cmd);}
			if(Prefs){Shell.sudo("echo install /sdcard/pa_updater/pref.zip"+cmd);}
			if(WipeCache){Shell.sudo("echo wipe cache"+cmd);}
			if(WipeDalvik){Shell.sudo("echo wipe dalvik"+cmd);}
			Shell.sudo("reboot recovery");
			} catch (ShellException e) {
				Toast.makeText(context, "Error while setting up OpenRecoveryScript! Please perform the actions manually!", Toast.LENGTH_LONG).show();
				Log.e("reboot", "Error while setting up OpenRecoveryScript!");
				e.printStackTrace();
			}
	    }
	});
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

}
