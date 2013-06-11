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

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.matze5800.paupdater.Shell.ShellException;

import java.io.File;

public class FlashCustomFiles extends Activity implements OnClickListener {

    // Views
    private Button file1down, file2down, file3down, file4down, file5down,
            file6down, file7down, file8down, file9down, file2up, file3up,
            file4up, file5up, file6up, file7up, file8up, file9up, file10up;
    private CheckBox createBackup, wipeCache;
    private TextView tvFilepath1, tvFilepath2, tvFilepath3, tvFilepath4,
            tvFilepath5, tvFilepath6, tvFilepath7, tvFilepath8, tvFilepath9,
            tvFilepath10;

    // FileDialog SetUp
    private Context mContext;
    private int files = 0;
    private File mPath;
    private FileDialog mFileDialog;
    private String backup = null, wipe_cache = null, wipe_dalvikCache = null,
            File1 = null, File2 = null, File3 = null, File4 = null,
            File5 = null, File6 = null, File7 = null, File8 = null,
            File9 = null, File10 = null, thisFile = null, nextFile = null;

    // Localization
    private String confirm_flash, damage_hint,
            error_setting_up_openrecovery_script, no_files_to_flash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcustomfiles);
        mContext = MainActivity.getContext();
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        initialize();
        // Localization SetUp
        confirm_flash = getString(R.string.confirm_flash);
        damage_hint = getString(R.string.damage_hint);
        error_setting_up_openrecovery_script = getString(R.string.error_setting_up_openrecovery_script);
        no_files_to_flash = getString(R.string.no_files_to_flash);
    }

    // ActionBar

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.flash_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.action_add:
                AddFiles();
                break;
            case R.id.action_flash:
                if (files == 0)
                    Toast.makeText(this, no_files_to_flash, Toast.LENGTH_SHORT)
                            .show();
                else
                    startFlashProcess();

                break;
            case R.id.clear_list:
                reset();
                break;
        }
        return true;
    }

    private void reset() {
        files = 0;
        File1 = null;
        File2 = null;
        File3 = null;
        File4 = null;
        File5 = null;
        File6 = null;
        File7 = null;
        File8 = null;
        File9 = null;
        File10 = null;
        tvFilepath1.setText("");
        tvFilepath2.setText("");
        tvFilepath3.setText("");
        tvFilepath4.setText("");
        tvFilepath5.setText("");
        tvFilepath6.setText("");
        tvFilepath7.setText("");
        tvFilepath8.setText("");
        tvFilepath9.setText("");
        tvFilepath10.setText("");
        file1down.setVisibility(View.INVISIBLE);
        file2down.setVisibility(View.INVISIBLE);
        file3down.setVisibility(View.INVISIBLE);
        file4down.setVisibility(View.INVISIBLE);
        file5down.setVisibility(View.INVISIBLE);
        file6down.setVisibility(View.INVISIBLE);
        file7down.setVisibility(View.INVISIBLE);
        file8down.setVisibility(View.INVISIBLE);
        file9down.setVisibility(View.INVISIBLE);
        file2up.setVisibility(View.INVISIBLE);
        file3up.setVisibility(View.INVISIBLE);
        file4up.setVisibility(View.INVISIBLE);
        file5up.setVisibility(View.INVISIBLE);
        file6up.setVisibility(View.INVISIBLE);
        file7up.setVisibility(View.INVISIBLE);
        file8up.setVisibility(View.INVISIBLE);
        file9up.setVisibility(View.INVISIBLE);
        file10up.setVisibility(View.INVISIBLE);
    }

    // Initialize Views
    private void initialize() {
        // TextViews
        tvFilepath1 = (TextView) findViewById(R.id.filepath1);
        tvFilepath2 = (TextView) findViewById(R.id.filepath2);
        tvFilepath3 = (TextView) findViewById(R.id.filepath3);
        tvFilepath4 = (TextView) findViewById(R.id.filepath4);
        tvFilepath5 = (TextView) findViewById(R.id.filepath5);
        tvFilepath6 = (TextView) findViewById(R.id.filepath6);
        tvFilepath7 = (TextView) findViewById(R.id.filepath7);
        tvFilepath8 = (TextView) findViewById(R.id.filepath8);
        tvFilepath9 = (TextView) findViewById(R.id.filepath9);
        tvFilepath10 = (TextView) findViewById(R.id.filepath10);
        // Down Button
        file1down = (Button) findViewById(R.id.file1down);
        file2down = (Button) findViewById(R.id.file2down);
        file3down = (Button) findViewById(R.id.file3down);
        file4down = (Button) findViewById(R.id.file4down);
        file5down = (Button) findViewById(R.id.file5down);
        file6down = (Button) findViewById(R.id.file6down);
        file7down = (Button) findViewById(R.id.file7down);
        file8down = (Button) findViewById(R.id.file8down);
        file9down = (Button) findViewById(R.id.file9down);
        // Down Button
        file2up = (Button) findViewById(R.id.file2up);
        file3up = (Button) findViewById(R.id.file3up);
        file4up = (Button) findViewById(R.id.file4up);
        file5up = (Button) findViewById(R.id.file5up);
        file6up = (Button) findViewById(R.id.file6up);
        file7up = (Button) findViewById(R.id.file7up);
        file8up = (Button) findViewById(R.id.file8up);
        file9up = (Button) findViewById(R.id.file9up);
        file10up = (Button) findViewById(R.id.file10up);
        // Set OnClickListener
        file1down.setOnClickListener(this);
        file2down.setOnClickListener(this);
        file3down.setOnClickListener(this);
        file4down.setOnClickListener(this);
        file5down.setOnClickListener(this);
        file6down.setOnClickListener(this);
        file7down.setOnClickListener(this);
        file8down.setOnClickListener(this);
        file9down.setOnClickListener(this);
        file2up.setOnClickListener(this);
        file3up.setOnClickListener(this);
        file4up.setOnClickListener(this);
        file5up.setOnClickListener(this);
        file6up.setOnClickListener(this);
        file7up.setOnClickListener(this);
        file8up.setOnClickListener(this);
        file9up.setOnClickListener(this);
        file10up.setOnClickListener(this);
        // CheckBox SetUp
        createBackup = (CheckBox) findViewById(R.id.create_backup);
        createBackup
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked)
                            backup = "echo backup BSDOM";
                    }
                });
        wipeCache = (CheckBox) findViewById(R.id.wipe_caches);
        wipeCache
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            wipe_cache = "echo wipe cache";
                            wipe_dalvikCache = "echo wipe dalvik";
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Down Buttons
            case R.id.file1down:
                thisFile = File1;
                nextFile = File2;
                File1 = nextFile;
                File2 = thisFile;
                tvFilepath1.setText(File1);
                tvFilepath2.setText(File2);
                break;
            case R.id.file2down:
                thisFile = File2;
                nextFile = File3;
                File2 = nextFile;
                File3 = thisFile;
                tvFilepath2.setText(File2);
                tvFilepath3.setText(File3);
                break;
            case R.id.file3down:
                String thisFile = File3;
                String nextFile = File4;
                File3 = nextFile;
                File4 = thisFile;
                tvFilepath3.setText(File3);
                tvFilepath4.setText(File4);
                break;
            case R.id.file4down:
                thisFile = File4;
                nextFile = File5;
                File4 = nextFile;
                File5 = thisFile;
                tvFilepath4.setText(File4);
                tvFilepath5.setText(File5);
                break;
            case R.id.file5down:
                thisFile = File5;
                nextFile = File6;
                File5 = nextFile;
                File6 = thisFile;
                tvFilepath5.setText(File5);
                tvFilepath6.setText(File6);
                break;
            case R.id.file6down:
                thisFile = File6;
                nextFile = File7;
                File6 = nextFile;
                File7 = thisFile;
                tvFilepath6.setText(File6);
                tvFilepath7.setText(File7);
                break;
            case R.id.file7down:
                thisFile = File7;
                nextFile = File8;
                File7 = nextFile;
                File8 = thisFile;
                tvFilepath7.setText(File7);
                tvFilepath8.setText(File8);
                break;
            case R.id.file8down:
                thisFile = File8;
                nextFile = File9;
                File8 = nextFile;
                File9 = thisFile;
                tvFilepath8.setText(File8);
                tvFilepath9.setText(File9);
                break;
            case R.id.file9down:
                thisFile = File9;
                nextFile = File10;
                File9 = nextFile;
                File10 = thisFile;
                tvFilepath9.setText(File9);
                tvFilepath10.setText(File10);
                break;
            // Up Buttons
            case R.id.file2up:
                thisFile = File2;
                nextFile = File1;
                File2 = nextFile;
                File1 = thisFile;
                tvFilepath2.setText(File2);
                tvFilepath1.setText(File1);
                break;
            case R.id.file3up:
                thisFile = File3;
                nextFile = File2;
                File3 = nextFile;
                File2 = thisFile;
                tvFilepath3.setText(File3);
                tvFilepath2.setText(File2);
                break;
            case R.id.file4up:
                thisFile = File4;
                nextFile = File3;
                File4 = nextFile;
                File3 = thisFile;
                tvFilepath4.setText(File4);
                tvFilepath3.setText(File3);
                break;
            case R.id.file5up:
                thisFile = File5;
                nextFile = File4;
                File5 = nextFile;
                File4 = thisFile;
                tvFilepath5.setText(File5);
                tvFilepath4.setText(File4);
                break;
            case R.id.file6up:
                thisFile = File6;
                nextFile = File5;
                File6 = nextFile;
                File5 = thisFile;
                tvFilepath6.setText(File6);
                tvFilepath5.setText(File5);
                break;
            case R.id.file7up:
                thisFile = File7;
                nextFile = File6;
                File7 = nextFile;
                File6 = thisFile;
                tvFilepath7.setText(File7);
                tvFilepath6.setText(File6);
                break;

            case R.id.file8up:
                thisFile = File8;
                nextFile = File7;
                File8 = nextFile;
                File7 = thisFile;
                tvFilepath8.setText(File8);
                tvFilepath7.setText(File7);
                break;
            case R.id.file9up:
                thisFile = File9;
                nextFile = File8;
                File9 = nextFile;
                File8 = thisFile;
                tvFilepath9.setText(File9);
                tvFilepath8.setText(File8);
                break;
            case R.id.file10up:
                thisFile = File10;
                nextFile = File9;
                File10 = nextFile;
                File9 = thisFile;
                tvFilepath10.setText(File10);
                tvFilepath9.setText(File9);
                break;
        }

    }

    private void AddFiles() {
        if (files == 0) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File1 = file.toString();
                    Log.i("CustomFlashZip1", "Selected file " + File1);
                    tvFilepath1.setText(File1);
                    files = 1;
                }
            });
            mFileDialog.showDialog();
        }

        if (files == 1) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File2 = file.toString();
                    Log.i("CustomFlashZip1", "Selected file " + File2);
                    tvFilepath2.setText(File2);
                    file1down.setVisibility(View.VISIBLE);
                    file2up.setVisibility(View.VISIBLE);
                    files = 2;
                }
            });
            mFileDialog.showDialog();
        }

        if (files == 2) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File3 = file.toString();
                    Log.i("CustomFlashZip2", "Selected file " + File3);
                    tvFilepath3.setText(File3);
                    file2down.setVisibility(View.VISIBLE);
                    file3up.setVisibility(View.VISIBLE);
                    files = 3;
                }
            });
            mFileDialog.showDialog();
        }
        if (files == 3) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File4 = file.toString();
                    Log.i("CustomFlashZip3", "Selected file " + File4);
                    tvFilepath4.setText(File4);
                    file3down.setVisibility(View.VISIBLE);
                    file4up.setVisibility(View.VISIBLE);
                    files = 4;
                }
            });
            mFileDialog.showDialog();
        }
        if (files == 4) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File5 = file.toString();
                    Log.i("CustomFlashZip4", "Selected file " + File5);
                    tvFilepath5.setText(File5);
                    file4down.setVisibility(View.VISIBLE);
                    file5up.setVisibility(View.VISIBLE);
                    files = 5;
                }
            });
            mFileDialog.showDialog();
        }
        if (files == 5) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File6 = file.toString();
                    Log.i("CustomFlashZip5", "Selected file " + File6);
                    tvFilepath6.setText(File6);
                    file5down.setVisibility(View.VISIBLE);
                    file6up.setVisibility(View.VISIBLE);
                    files = 6;
                }
            });
            mFileDialog.showDialog();
        }
        if (files == 6) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File7 = file.toString();
                    Log.i("CustomFlashZip6", "Selected file " + File7);
                    tvFilepath7.setText(File7);
                    file6down.setVisibility(View.VISIBLE);
                    file7up.setVisibility(View.VISIBLE);
                    files = 7;
                }
            });
            mFileDialog.showDialog();
        }
        if (files == 7) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File8 = file.toString();
                    Log.i("CustomFlashZip7", "Selected file " + File8);
                    tvFilepath8.setText(File8);
                    file7down.setVisibility(View.VISIBLE);
                    file8up.setVisibility(View.VISIBLE);
                    files = 8;
                }
            });
            mFileDialog.showDialog();
        }
        if (files == 8) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File9 = file.toString();
                    Log.i("CustomFlashZip8", "Selected file " + File9);
                    tvFilepath9.setText(File9);
                    file8down.setVisibility(View.VISIBLE);
                    file9up.setVisibility(View.VISIBLE);
                    files = 9;
                }
            });
            mFileDialog.showDialog();
        }
        if (files == 9) {
            mPath = new File(Environment.getExternalStorageDirectory()
                    + "//DIR//");
            mFileDialog = new FileDialog(this, mPath);
            mFileDialog.setFileEndsWith(".zip");
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    File10 = file.toString();
                    Log.i("CustomFlashZip9", "Selected file " + File10);
                    tvFilepath10.setText(File10);
                    file9down.setVisibility(View.VISIBLE);
                    file10up.setVisibility(View.VISIBLE);
                    files = 10;
                }
            });
            mFileDialog.showDialog();
        }

        if (files == 10)
            Toast.makeText(mContext, "At the moment we only support 10 files!",
                    Toast.LENGTH_SHORT).show();
    }

    private void startFlashProcess() {
        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle(confirm_flash);
        myAlertDialog.setMessage(damage_hint);
        myAlertDialog.setPositiveButton(R.string.update_dialog_do_it,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        String cmd = " >> /cache/recovery/openrecoveryscript";
                        try {
                            if (backup != null)
                                Shell.sudo(backup + cmd);
                            if (wipe_cache != null)
                                Shell.sudo(wipe_cache + cmd);
                            if (wipe_dalvikCache != null)
                                Shell.sudo(wipe_dalvikCache + cmd);
                            if (File1 != null)
                                Shell.sudo("echo install " + File1 + cmd);
                            if (File2 != null)
                                Shell.sudo("echo install " + File2 + cmd);
                            if (File3 != null)
                                Shell.sudo("echo install " + File3 + cmd);
                            if (File4 != null)
                                Shell.sudo("echo install " + File4 + cmd);
                            if (File5 != null)
                                Shell.sudo("echo install " + File5 + cmd);
                            if (File6 != null)
                                Shell.sudo("echo install " + File6 + cmd);
                            if (File7 != null)
                                Shell.sudo("echo install " + File7 + cmd);
                            if (File8 != null)
                                Shell.sudo("echo install " + File8 + cmd);
                            if (File9 != null)
                                Shell.sudo("echo install " + File9 + cmd);
                            if (File10 != null)
                                Shell.sudo("echo install " + File10 + cmd);
                            Shell.sudo("reboot recovery");
                        } catch (ShellException e) {
                            Toast.makeText(mContext,
                                    error_setting_up_openrecovery_script,
                                    Toast.LENGTH_LONG).show();
                            Log.e("reboot",
                                    "Error while setting up OpenRecoveryScript!");
                            e.printStackTrace();
                        }
                    }
                });
        myAlertDialog.setNegativeButton(R.string.update_dialog_cancel, null);
        myAlertDialog.show();
    }
}
