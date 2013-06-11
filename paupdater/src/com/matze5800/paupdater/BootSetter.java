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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;

public class BootSetter extends BroadcastReceiver {

    private Context Context;
    private SharedPreferences prefs;
    private int freq;

    @Override
    public void onReceive(Context context, Intent intent) {
        Context = context.getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(Context);
        freq = Integer.valueOf(prefs.getString("prefUpdateCheckFreq", "1"));
        if (freq > 0) {
            Log.i("boot", "Started!");
            Log.i("boot", "UpdateCheckFreq: " + freq);
            Intent alarmintent = new Intent(Context, AlarmReceiver.class);
            alarmintent.setAction("com.paranoid.updater.ACTION");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, alarmintent, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            AlarmManager alarm = (AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);
            alarm.cancel(pendingIntent);
            if (freq == 1) {
                alarm.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
            } else {
                long interval = AlarmManager.INTERVAL_HOUR * freq;
                Log.i("boot", "Setting up Alarm with Interval: " + interval);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, 0, interval, pendingIntent);
            }
        }

    }
}
    
 
