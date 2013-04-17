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
