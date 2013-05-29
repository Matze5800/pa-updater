package com.matze5800.paupdater;

import java.io.File;

import android.graphics.Color;
import android.support.v4.view.PagerTabStrip;
import com.matze5800.paupdater.AppSectionsPagerAdapter;
import com.matze5800.paupdater.fragments.UpdateFragment;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity{

	AppSectionsPagerAdapter mAppSectionsPagerAdapter;
	static Context context;
	ViewPager mViewPager;
	SharedPreferences prefs;
	static String tab3;
	String device;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tab3 = getString(R.string.title_activity_settings);
		// Set View
		setContentView(R.layout.activity_main);
		context = this;
		// Get SharedPreferences
		prefs = PreferenceManager.getDefaultSharedPreferences(context);

		// ActionBar / PagerAdapter
		mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
				getFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mAppSectionsPagerAdapter);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_title_strip);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(Color.rgb(0, 153, 204));

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
		}
		return true;
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
				boolean secondupdate = UpdateFragment.getSecondupdate();

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
					if (!secondupdate) {
						float newY = UpdateFragment.getImageStatus().getY() + 32;
						float newX = UpdateFragment.getImageStatus().getX() + 10;
						UpdateFragment.getImageStatus().setY(newY);
						UpdateFragment.getImageStatus().setX(newX);
					}
					UpdateFragment.getImageStatus().setImageResource(
							R.drawable.update);
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
				Toast.makeText(context, "Sorry, goo.im seems to be down!",
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

	// UpdateButton
	public void selectROMZip(View view) {
		File mPath = new File(Environment.getExternalStorageDirectory()
				+ "//DIR//");
		FileDialog fileDialog = new FileDialog(this, mPath);
		fileDialog.setFileEndsWith(".zip");
		fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
			public void fileSelected(File file) {
				String path = file.toString();
				Log.i("CustomZip", "Selected file " + path);
				prefs.edit().putBoolean("customZip", true).commit();
				prefs.edit().putString("customZipPath", path).commit();
				UpdateFragment.getUpdateButton().setEnabled(true);
			}
		});
		fileDialog.showDialog();
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
		@SuppressWarnings("unused")
		boolean secondupdate = UpdateFragment.getSecondupdate();

		secondupdate = true;
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

	// Localization
	public static String getTab3() {
		return tab3;
	}
}
