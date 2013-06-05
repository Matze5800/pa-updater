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

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;

public class Cancel extends Activity {
SharedPreferences prefs;
DownloadManager manager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cancel);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}
	
	public void CancelUpdate(View view)	{
		Functions.Clear(this);
		prefs.edit().putBoolean("update_running", false).commit();
		processStopService();
		Long download_id = prefs.getLong("download_id", 0);
		manager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.remove(download_id);
	    int pid = android.os.Process.myPid();

	    android.os.Process.killProcess(pid);
		finish();
	}
	
	public void Continue(View view)	{
		finish();
	}

	private void processStopService() {
	    Intent intent = new Intent(getApplicationContext(), UpdateService.class);
	    stopService(intent);
	}
}
