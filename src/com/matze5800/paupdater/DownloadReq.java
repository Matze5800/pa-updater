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

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DownloadReq extends Activity {
String URL;
String gooShortURL;
Boolean gapps;
WebView webview = null;
Activity thisActivity = this;
Timer timer = new Timer();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_req);
		webview = (WebView)findViewById(R.id.webView_dlreq);
		Bundle extras = getIntent().getExtras();
		gooShortURL = extras.getString("gooShortURL");
		gapps = extras.getBoolean("gapps");
		RequestROM();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

    @Override //disable back button if needed
    public void onBackPressed() {}
    
	@SuppressLint("SetJavaScriptEnabled")
	public void RequestROM() {
    	Functions.createDirIfNotExists("pa_updater");
    	webview.setVisibility(0);
    	WebSettings settings = webview.getSettings();
    	settings.setJavaScriptEnabled(true);
    	settings.setLoadWithOverviewMode(true);
    	settings.setUseWideViewPort(true);
		webview.setWebViewClient(new WebViewClient());
    	webview.loadUrl(gooShortURL);
    	timer.scheduleAtFixedRate(new TimerTask() {          
    	    @Override
    	    public void run() {
    	        webview.loadUrl(gooShortURL);
    	        Functions.Notify(thisActivity, "Failed! Retrying Request...");
    	    }
    	}, 21000, 21000);
    	webview.setDownloadListener(new DownloadListener() {
    		public void onDownloadStart(String url, String userAgent,
    		String contentDisposition, String mimetype,
    		long contentLength) {
    			webview.setDownloadListener(null);
    			timer.cancel();
    			URL = url;
    			processStopService();
    		   	processStartService();
    		   	finish();
    		}});

    	}
	
    private void processStartService() {
        Intent intent = new Intent(getApplicationContext(), UpdateService.class);
        if(gapps){intent.putExtra("mode", 2);}else{intent.putExtra("mode", 1);}
        intent.putExtra("url", URL);
        startService(intent);
    }
	
	private void processStopService() {
	    Intent intent = new Intent(getApplicationContext(), UpdateService.class);
	    stopService(intent);
	}
}
