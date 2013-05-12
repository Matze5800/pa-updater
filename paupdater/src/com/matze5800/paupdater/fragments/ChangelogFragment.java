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

import com.matze5800.paupdater.Functions;
import com.matze5800.paupdater.MainActivity;
import com.matze5800.paupdater.R;

public class ChangelogFragment extends Fragment {
	
	SharedPreferences prefs;
	String device;
	Context context = MainActivity.getContext();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_changelog, container,
				false);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		device = Functions.detectDevice(context);
		
		final WebView webView1 = (WebView) rootView.findViewById(R.id.webView1);
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
		
		return rootView;
	}
}
