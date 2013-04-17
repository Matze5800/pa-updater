package com.matze5800.paupdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Functions {
	public static void addFilesToExistingZip(File zipFile,
	         File[] files) throws IOException {
		File tempFile = new File(Environment.getExternalStorageDirectory()+"/pa_updater", "temp_kernel.zip");
	    tempFile.delete();

	    boolean renameOk=zipFile.renameTo(tempFile);
	    if (!renameOk)
	    {
	        throw new RuntimeException("could not rename the file "+zipFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
	    }
	    byte[] buf = new byte[1024];

	    ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

	    ZipEntry entry = zin.getNextEntry();
	    while (entry != null) {
	        String name = entry.getName();
	        boolean notInFiles = true;
	        for (File f : files) {
	            if (f.getName().equals(name)) {
	                notInFiles = false;
	                break;
	            }
	        }
	        if (notInFiles) {

	            out.putNextEntry(new ZipEntry(name));

	            int len;
	            while ((len = zin.read(buf)) > 0) {
	                out.write(buf, 0, len);
	            }
	        }
	        entry = zin.getNextEntry();
	    }    
	    zin.close();

	    for (int i = 0; i < files.length; i++) {
	        InputStream in = new FileInputStream(files[i]);
	        out.putNextEntry(new ZipEntry(files[i].getName()));

	        int len;
	        while ((len = in.read(buf)) > 0) {
	            out.write(buf, 0, len);
	        }
	        out.closeEntry();
	        in.close();
	    }
	    out.close();
	    tempFile.delete();
	}
	
    public static boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("Create Dir", "Error creating folder "+path);
                ret = false;
            }
        }
        return ret;
    }   
    
    public static boolean checkMD5(String md5, File updateFile) {
    	String DEBUG_TAG = "md5check";
        if (md5 == null || md5.equals("") || updateFile == null) {
            Log.e(DEBUG_TAG, "MD5 String NULL!!!");
            if (updateFile == null) {
            	Log.e(DEBUG_TAG, "UpdateFile NULL!!!");
            }
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            Log.e(DEBUG_TAG, "calculatedDigest NULL");
            return false;
        }

        Log.i(DEBUG_TAG, "Calculated digest: " + calculatedDigest);
        Log.i(DEBUG_TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }
    
    public static String calculateMD5(File updateFile) {
    	String DEBUG_TAG = "md5calc";
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            Log.e(DEBUG_TAG, "Exception while getting Digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            Log.e(DEBUG_TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
    
    public static void Notify(Context context, String text)	{
    	Log.i("notify", text);
    	Notification.Builder nBuilder;
    	nBuilder = new Notification.Builder(context);
    	Intent resultIntent = new Intent(context, Cancel.class);
    	TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
    	stackBuilder.addParentStack(Cancel.class);
    	stackBuilder.addNextIntent(resultIntent);
    	PendingIntent resultPendingIntent =
    	        stackBuilder.getPendingIntent(
    	            0,
    	            PendingIntent.FLAG_UPDATE_CURRENT
    	        );
    	nBuilder.setContentIntent(resultPendingIntent);
    	nBuilder.setSmallIcon(R.drawable.ic_launcher);
    	nBuilder.setContentTitle("PA Updater");
    	nBuilder.setContentText(text);
    	nBuilder.setAutoCancel(false);
    	nBuilder.setOngoing(true);
    	NotificationManager mNotificationManager =
    		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationManager.notify(1, nBuilder.build());
    }
    
    public static void Clear(Context context)	{
    	NotificationManager mNotificationManager =
    		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    	mNotificationManager.cancelAll();
    }
    
    public static String detectDevice(Context context)	{
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String device = android.os.Build.DEVICE;
    	String savedDevice = prefs.getString("device", "");
    	String result = "";
    	if (savedDevice.equals(""))	{
    		if(device.equalsIgnoreCase("mako")){result="mako";}
    		else if(device.equalsIgnoreCase("maguro")){result="maguro";}
    		else if(device.equalsIgnoreCase("grouper")){result="grouper";}
    		else if(device.equalsIgnoreCase("m0") || device.equalsIgnoreCase("i9300") || device.equalsIgnoreCase("GT-I9300")){result="i9300";}
    		else if(device.equalsIgnoreCase("manta")){result="manta";}
    		else if(device.equalsIgnoreCase("t03g") || device.equalsIgnoreCase("n7100") || device.equalsIgnoreCase("GT-N7100")){result="n7100";}
    		else if(device.equalsIgnoreCase("tilapia")){result="tilapia";}
    		else if(device.equalsIgnoreCase("toro")){result="toro";}
    		else if(device.equalsIgnoreCase("toroplus")){result="toroplus";}
    		else {
    			Toast.makeText(context, "Sorry, your device is not supported yet!", Toast.LENGTH_LONG).show();
    			result = "unsupported";
    		}
    		prefs.edit().putString("device", result).commit();
    	} else {result = savedDevice;}
    	Log.i("detectDevice", result+" detected.");
    	return result;
    }
    
    public static String getBootPartition(Context context)	{
    	String device = detectDevice(context);
    	String result = "";
    	if(device.equals("mako")){result="/dev/block/platform/msm_sdcc.1/by-name/boot";}
    	else if(device.equals("maguro") || device.equals("toro") || device.equals("toroplus")){result="/dev/block/platform/omap/omap_hsmmc.0/by-name/boot";}
    	else if(device.equals("grouper") || device.equals("tilapia")){result="/dev/block/platform/sdhci-tegra.3/by-name/LNX";}
    	else if(device.equals("i9300")){result="/dev/block/mmcblk0p5";}
    	else if(device.equals("n7100")){result="/dev/block/mmcblk0p8";}
    	else if(device.equals("manta")){result="/dev/block/platform/dw_mmc.0/by-name/boot";}
    	Log.i("getBootPartition", "Boot partition for "+device+": "+result);
    	return result;
    }
    
    public static boolean deleteDirectory(File path) {
    	Log.i("deleteDir", "Deleting "+path.getPath());
        if( path.exists() ) {
          File[] files = path.listFiles();
          if (files == null) {
              return true;
          }
          for(int i=0; i<files.length; i++) {
             if(files[i].isDirectory()) {
               deleteDirectory(files[i]);
             }
             else {
               files[i].delete();
             }
          }
        }
        return( path.delete() );
      }
    
    public static boolean WifiConnected(Context context)	{
    	ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
    }
    
    public static String getDevId(Context context)	{
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Process proc;
		BufferedReader reader;
		String Dev = null;
		try {
		proc = Runtime.getRuntime().exec(new String[]{"/system/bin/getprop", "ro.goo.developerid"});
		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		Dev = reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(Dev.equals(null)){Dev="Error parsing ro.goo.developerid!";}
		Log.i("Local Parser", "Developer: "+Dev);
		prefs.edit().putString("Dev", Dev).commit();
		return Dev;
    }
    
    public static int getLocalVersion(Context context)	{
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Process proc;
		BufferedReader reader;
		int localVer = 1;
		try {
		proc = Runtime.getRuntime().exec(new String[]{"/system/bin/getprop", "ro.goo.version"});
        reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        String result = reader.readLine();
        
        if(result.equals("") || result.equals(null))	{
        	Log.i("Local Parser", "Not running on PA!!!");
        	Log.i("Local Parser", "Disabling PA Prefs Restore");
        	Toast.makeText(context, "You are not running PA! You should wipe data after flash!", Toast.LENGTH_LONG).show();
        	prefs.edit().putBoolean("prefPrefsRestore", false).commit();
        	prefs.edit().putBoolean("NoPA", true).commit();
        } else {
        	localVer = Integer.valueOf(result);
        	prefs.edit().putBoolean("NoPA", false).commit();
        }
        Log.i("Local Parser", "Local version: "+localVer);
	} catch (IOException e) {
		Log.e("Local Parser", "Error parsing local version!");
		Log.e("Local Parser", e.toString());
	}
		return localVer;
    }
    
    public static String CheckGoo(Context context)	{
    	String Dev = getDevId(context);
    	String device = detectDevice(context);
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	String result = null;
        try{
        	JSONArray files = null;
        	JSONObject json = null;
        	JSONObject e;
        	int item = 1;
        	if (prefs.getBoolean("prefCheckDev", false) || Dev.equals("dsmitty166"))	{
        		Log.i("Goo Parser", "Check dev folder enabled!");
        		if (Dev.equals("dsmitty166"))	{
        			json = JSONfunctions.getJSONfromURL("http://goo.im/json2&action=search&query=dsmitty166");
        		} else {
        			json = JSONfunctions.getJSONfromURL("http://goo.im/json2&action=search&query=pa_"+device);
        		}
        		if (json != null)	{
        		files = json.getJSONArray("search_result");
        		item = 0;
        	}} else {
        		Log.i("Goo Parser", "Check dev folder disabled!");
        		json = JSONfunctions.getJSONfromURL("http://goo.im/json2&path=/devs/paranoidandroid/roms/"+device);
        		if (json != null)	{
        		files = json.getJSONArray("list");
        		String version;
        		item = 0;
        		e = files.getJSONObject(item);
        		try{version = e.getString("ro_version");} catch(JSONException ex) {version = null;}
        		while(version == null) {
        			Log.i("Goo Parser", "No file, skipping item "+item);
        			item = item + 1;
            		e = files.getJSONObject(item);
            		try{version = e.getString("ro_version");} catch(JSONException ex) {version = null;}
        		}
        	}}
        	if (json != null){
        	e = files.getJSONObject(item);
        	String gooDev = e.getString("ro_developerid");
        	while(!gooDev.equals(Dev))	{
        		item = item + 1;
        		Log.i("Goo Parser", "Wrong dev, skipping item "+item);
        		e = files.getJSONObject(item);
        		gooDev = e.getString("ro_developerid");
        	}
        	e = files.getJSONObject(item);
        	result = e.getString("ro_version");
        	prefs.edit().putString("gooFilename", e.getString("filename")).commit();
        	prefs.edit().putString("gooShortURL", e.getString("short_url")).commit();
        	prefs.edit().putString("rom_md5", e.getString("md5")).commit();
        	json = JSONfunctions.getJSONfromURL("http://goo.im/json2&path=/devs/paranoidandroid/roms/gapps");
        	files = json.getJSONArray("list");
        	item=0;
        	e = files.getJSONObject(item);
        	prefs.edit().putString("gappsURL", e.getString("short_url")).commit();
        	prefs.edit().putString("gappsmd5", e.getString("md5")).commit();
        	prefs.edit().putString("gappsFilename", e.getString("filename")).commit();
        	} else {return "err";}
        }catch(Exception e)		{
        	Log.e("Goo Parser", "Error parsing data "+e.toString());
        }
        return result;
    }
}
