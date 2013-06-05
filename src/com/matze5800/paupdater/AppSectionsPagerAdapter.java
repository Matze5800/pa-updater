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

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import com.matze5800.paupdater.fragments.ChangelogFragment;
import com.matze5800.paupdater.fragments.SettingsFragment;
import com.matze5800.paupdater.fragments.UpdateFragment;

public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

	private String preferences = MainActivity.getTab3();
	private final String[] titles = { "Update", "Changelog", preferences };

	public AppSectionsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int i) {
		switch (i) {
		case 0:
			return new UpdateFragment();

		case 1:
			return new ChangelogFragment();

		case 2:
			return new SettingsFragment();

		default:
			return null;
		}
	}

	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return titles[position];
	}
}
