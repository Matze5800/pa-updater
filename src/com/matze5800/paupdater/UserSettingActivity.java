package com.matze5800.paupdater;

import android.app.Activity;
import android.os.Bundle;

public class UserSettingActivity extends Activity {								 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}