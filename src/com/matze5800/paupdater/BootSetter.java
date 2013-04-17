package com.matze5800.paupdater;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootSetter extends BroadcastReceiver {
	Context Context;
	SharedPreferences prefs;
	int freq;
    @Override
    public void onReceive(Context context, Intent intent) {
        Context = context.getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(Context);
        freq = Integer.valueOf(prefs.getString("prefUpdateCheckFreq", "1"));
    	if(freq>0)	{
    		Log.i("boot", "Started!");
    		Log.i("boot", "UpdateCheckFreq: "+freq);
		        Intent alarmintent = new Intent(Context, AlarmReceiver.class);
		        alarmintent.setAction("com.matze5800.paupdater.ACTION");
		        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
		                    0, alarmintent, PendingIntent.FLAG_CANCEL_CURRENT);
		        Calendar calendar = Calendar.getInstance();
		        calendar.setTimeInMillis(System.currentTimeMillis());
		                AlarmManager alarm = (AlarmManager) context.getSystemService(android.content.Context.ALARM_SERVICE);
		        alarm.cancel(pendingIntent);
		        if(freq==1){alarm.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
		        } else {
		        long interval = AlarmManager.INTERVAL_HOUR * freq;
		        Log.i("boot", "Setting up Alarm with Interval: "+interval);
		        alarm.setRepeating(AlarmManager.RTC_WAKEUP, 0, interval, pendingIntent);
		        }
	        }

	    	}
        }
    
 
