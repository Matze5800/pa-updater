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
 
package com.matze5800.paupdater.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.matze5800.paupdater.MainActivity;
import com.matze5800.paupdater.R;

public class ChangelogFragment extends Fragment {
	
	SharedPreferences prefs;
	Context context = MainActivity.getContext();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_changelog, container,
				false);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		final WebView webView1 = (WebView) rootView.findViewById(R.id.webView1);
		WebSettings settings = webView1.getSettings();
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView1.setWebViewClient(new WebViewClient());
		if(prefs.getString("Dev", "paranoidandroid").equals("dsmitty166")){
			webView1.loadUrl("https://dl.dropboxusercontent.com/u/569065/changelog.html");
		} else {
            webView1.loadUrl(("https://plus.google.com/app/basic/107979589566958860409/posts"));}
            //URL to old changelog:
            //http://matze5800.de/changelog/"+device

        //Disable navigation and open in the Browser when clicking a link
		webView1.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				webView1.setWebViewClient(null);
				return false;
			}
        });
		
		return rootView;
	}
}
