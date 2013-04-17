package com.matze5800.paupdater;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Changelog extends Activity {
SharedPreferences prefs;
String device;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changelog);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		device = Functions.detectDevice(this);
		final WebView webView1 = (WebView)findViewById(R.id.webView1);
		WebSettings settings = webView1.getSettings();
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView1.setWebViewClient(new WebViewClient());
		if(prefs.getString("Dev", "paranoidandroid").equals("dsmitty166")){
			webView1.loadUrl("https://dl.dropboxusercontent.com/u/569065/changelog.html");
		} else {webView1.loadUrl("http://matze5800.de/changelog/"+device);}
		webView1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				webView1.setWebViewClient(null);
				return false;
			}
        }); 
	}  

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.changelog, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
 
        case R.id.action_settings:
            Intent i = new Intent(this, UserSettingActivity.class);
            startActivity(i);
            break;
            
        case R.id.content_edit:
        	Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://matze5800.de/changelog/"+device+"/edit.php"));
        	startActivity(intent);
            break;
 
        }
        return true;
    }

}
