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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;
import com.matze5800.paupdater.AlarmReceiver;
import com.matze5800.paupdater.R;

import java.util.Calendar;

public class SettingsFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {

    private Context Context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        Context = getActivity();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals("prefPrefsRestore")) {
            if (sharedPreferences.getBoolean("NoPA", false)) {
                Toast.makeText(Context, "This function is only available when running PA!", Toast.LENGTH_SHORT).show();
                sharedPreferences.edit().putBoolean("prefPrefsRestore", false).commit();
            }
        }
        if (key.equals("prefDebugUpdate")) {
            Toast.makeText(Context, "Please restart PA Updater to see the changes!", Toast.LENGTH_LONG).show();
        }
        if (key.equals("prefUpdateCheckFreq")) {
            Log.i("pref", "Update Check Freq changed!");
            int freq = Integer.valueOf(sharedPreferences.getString("prefUpdateCheckFreq", "1"));
            Intent alarmintent = new Intent(Context, AlarmReceiver.class);
            alarmintent.setAction("com.paranoid.updater.ACTION");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(Context,
                    0, alarmintent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            AlarmManager alarm = (AlarmManager) Context.getSystemService(android.content.Context.ALARM_SERVICE);
            alarm.cancel(pendingIntent);
            if (freq > 1) {
                long interval = AlarmManager.INTERVAL_HOUR * freq;
                Log.i("pref", "Setting up Alarm with Interval: " + interval);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, interval, interval, pendingIntent);
            }
        }
    }


}
