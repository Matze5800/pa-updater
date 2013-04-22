package com.matze5800.paupdater;

import java.io.File;

import com.matze5800.paupdater.FileDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
String device;
Context context;
Boolean secondupdate=false;
String Dev;
String gappsURL;
int gooVer;
int localVer;
ProgressBar ProgressStatus = null;
ImageView ImageStatus = null;
TextView UpdateStatus = null;
Button updateButton = null;
SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //Display Main Activity
        
        context = this;
    	UpdateStatus = (TextView)findViewById(R.id.UpdateStatus); //Get Views
    	ImageStatus = (ImageView)findViewById(R.id.imageView1);
    	ProgressStatus = (ProgressBar)findViewById(R.id.progressBar1);
    	updateButton = (Button)findViewById(R.id.button1);
    	
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	prefs.edit().putBoolean("customZip", false).commit();
    	if(prefs.getBoolean("update_running", false)){updateButton.setEnabled(false);}
    	device = Functions.detectDevice(context);
    	if(device.equals("unsupported")){finish();}
    	
		localVer = Functions.getLocalVersion(this);
		
    	GetGooVersion GooTask = new GetGooVersion();
    	GooTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            Intent i = new Intent(this, UserSettingActivity.class);
            startActivity(i);
            break;
        case R.id.action_refresh:
        	refresh();
        	break;
        case R.id.action_open:
        	File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
            FileDialog fileDialog = new FileDialog(this, mPath);
            fileDialog.setFileEndsWith(".zip");
            fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                	String path = file.toString();
                    Log.i("CustomZip", "Selected file " + path);
                    prefs.edit().putBoolean("customZip", true).commit();
                    prefs.edit().putString("customZipPath", path).commit();
                    updateButton.setEnabled(true);
                }
            });
            fileDialog.showDialog();
        	break;
        }
        return true;
    }
 
    class GetGooVersion extends AsyncTask<Void, Void, String> {  
        @Override
        protected String doInBackground(Void... noargs) {
        	return Functions.CheckGoo(context);
        }
        
        @Override
        protected void onPostExecute(String result) {
        	if(!result.equals("err"))	{
        	gooVer = Integer.valueOf(result);
        	Log.i("gooVer", "goo Version: "+gooVer);
        	ProgressStatus.setVisibility(8);
        	ImageStatus.setVisibility(0);
        	if(prefs.getBoolean("prefDebugUpdate", false)){localVer = 1;} //DEBUG
        	if (gooVer > localVer)	{
        		UpdateStatus.setText(R.string.update_available);
        		if(!secondupdate)	{
        		float newY = ImageStatus.getY() + 32;
        		float newX = ImageStatus.getX() + 10;
        		ImageStatus.setY(newY);
        		ImageStatus.setX(newX);}
        		ImageStatus.setImageResource(R.drawable.update);
        		updateButton.setEnabled(true);
        	}else	{
        		UpdateStatus.setText(R.string.uptodate);
        		ImageStatus.setImageResource(R.drawable.ok);
        		updateButton.setEnabled(false);
        	}
        	if(prefs.getBoolean("update_running", false)){updateButton.setEnabled(false);}
        	} else {Toast.makeText(context, "Sorry, goo.im seems to be down!", Toast.LENGTH_LONG).show();}
        }
    }
    //View Changelog Button
    public void viewChangelog(View view) {
    	Intent myIntent = new Intent(MainActivity.this, Changelog.class);
    	MainActivity.this.startActivity(myIntent);
    }

    //Start Update Button
    public void startUpdate(View view) {
    	 AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		 myAlertDialog.setTitle("Start Update?");
		 StringBuilder sb = new StringBuilder();
		 if(prefs.getBoolean("customZip", false)){
			 sb.append("I'm going to use this ROM:\n" + prefs.getString("customZipPath", "none"));}
		 else {sb.append("I'm going to download this ROM:\n" + prefs.getString("gooFilename", "none"));}
		 if(prefs.getBoolean("prefFlashGapps", true)){
		 sb.append("\n\n");
		 sb.append("And this Gapps Package:\n" + prefs.getString("gappsFilename", "none"));}
		 myAlertDialog.setMessage(sb.toString());
		 myAlertDialog.setPositiveButton("Do it!", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface arg0, int arg1) {		
			  	if(Functions.WifiConnected(context)){
			    	processStartService();
			    	finish();
			  	} else {
			  		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(context);
			  		myAlertDialog.setTitle("You are not on WiFi!");
			  		myAlertDialog.setMessage("Starting the update will cause lots of traffic!\n\nAre you sure that you want to start it?");
			  		myAlertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			  			public void onClick(DialogInterface arg0, int arg1) {
					    	processStartService();
					    	finish();
			  			}});
			  		myAlertDialog.setNegativeButton("No", null);
			  		myAlertDialog.show();
			  	}}});
		 myAlertDialog.setNegativeButton("Cancel", null);
		 myAlertDialog.show();
    }
    
    //Start Update Button
    public void refresh() {
    	secondupdate = true;
    	UpdateStatus.setText(R.string.check);
    	ProgressStatus.setVisibility(0);
    	ImageStatus.setVisibility(8);
    	GetGooVersion GooTask = new GetGooVersion();
    	GooTask.execute();
    }
    
    private void processStartService() {
        Intent intent = new Intent(getApplicationContext(), UpdateService.class);
        intent.putExtra("gooShortURL", prefs.getString("gooShortURL", ""));
        startService(intent);
        //Request root access to ensure that its granted later
        Shell.su();
    }
}
