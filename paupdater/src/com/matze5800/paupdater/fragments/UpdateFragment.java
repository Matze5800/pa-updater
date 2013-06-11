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
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.matze5800.paupdater.Functions;
import com.matze5800.paupdater.MainActivity;
import com.matze5800.paupdater.R;

public class UpdateFragment extends Fragment {

    private Context context = MainActivity.getContext();
    private int update_available_icon = 0;
    private static float update_x, update_y;
    private static int gooVer;
    private static int localVer;
    private static ProgressBar ProgressStatus = null;
    private static ImageView ImageStatus = null;
    private static TextView UpdateStatus = null;
    private static Button updateButton = null;
    SharedPreferences prefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_update, container,
                false);

        // Initialize Views
        UpdateStatus = (TextView) rootView.findViewById(R.id.UpdateStatus);
        ImageStatus = (ImageView) rootView.findViewById(R.id.imageView1);
        ProgressStatus = (ProgressBar) rootView.findViewById(R.id.progressBar1);
        updateButton = (Button) rootView.findViewById(R.id.startUpdate);

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean("customZip", false).commit();
        if (prefs.getBoolean("update_running", false)) {
            updateButton.setEnabled(false);
        }

        localVer = Functions.getLocalVersion(context);
        GetGooVersion GooTask = new GetGooVersion();
        GooTask.execute();

        return rootView;
    }

    // Gets GooVersion
    class GetGooVersion extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... noargs) {
            return Functions.CheckGoo(context);
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.equals("err")) {
                gooVer = Integer.valueOf(result);
                Log.i("gooVer", "goo Version: " + gooVer);
                ProgressStatus.setVisibility(8);
                ImageStatus.setVisibility(0);
                if (prefs.getBoolean("prefDebugUpdate", false)) {
                    localVer = 1;
                } // DEBUG
                if (gooVer > localVer) {
                    UpdateStatus.setText(R.string.update_available);
                    if (update_available_icon == 0) {
                        update_y = UpdateFragment.getImageStatus().getY() + 32;
                        update_x = UpdateFragment.getImageStatus().getX() + 10;
                        update_available_icon = 1;
                    }
                    UpdateFragment.getImageStatus().setY(update_y);
                    UpdateFragment.getImageStatus().setX(update_x);
                    ImageStatus.setImageResource(R.drawable.update);
                    updateButton.setEnabled(true);
                } else {
                    UpdateStatus.setText(R.string.uptodate);
                    ImageStatus.setImageResource(R.drawable.ok);
                    updateButton.setEnabled(false);
                }
                if (prefs.getBoolean("update_running", false)) {
                    updateButton.setEnabled(false);
                }
            } else {
                Toast.makeText(context, "Sorry, goo.im seems to be down!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Getters for MainAcitivty
    public static ProgressBar getProgressStatus() {
        return ProgressStatus;
    }

    public static ImageView getImageStatus() {
        return ImageStatus;
    }

    public static TextView getUpdateStatus() {
        return UpdateStatus;
    }

    public static Button getUpdateButton() {
        return updateButton;
    }

    public static int getGooVer() {
        return gooVer;
    }

    public static int getLocalVer() {
        return localVer;
    }

    public static float getUpdate_y() {
        return update_y;
    }

    public static float getUpdate_x() {
        return update_x;
    }
}
