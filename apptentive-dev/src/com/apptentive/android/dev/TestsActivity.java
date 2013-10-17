/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.storage.PersonManager;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class TestsActivity extends ApptentiveActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tests);
	}

	public void testTweet(@SuppressWarnings("unused") View view) {
		try{
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_TEXT, "Testing...");
			intent.setType("text/plain");
			final PackageManager pm = getPackageManager();
			final List<ResolveInfo> activityList = pm.queryIntentActivities(intent, 0);
			for (ResolveInfo app : activityList) {
				if ("com.twitter.android.PostActivity".equals(app.activityInfo.name)) {
					final ActivityInfo activityInfo = app.activityInfo;
					final ComponentName name = new ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name);
					intent.addCategory(Intent.CATEGORY_LAUNCHER);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					intent.setComponent(name);
					startActivity(intent);
					break;
				}
			}
		} catch (final ActivityNotFoundException e) {
			android.util.Log.i("APPTENTIVE", "No native twitter app.", e);
		}
	}

	public void throwNpe(@SuppressWarnings("unused") View view) {
		throw new NullPointerException("This is just an exception to test out how the SDK handles it.");
	}

	public void deleteStoredMessages(@SuppressWarnings("unused") View view) {
		MessageManager.deleteAllMessages(TestsActivity.this);
	}

	public void addCustomDeviceData(View view) {
		EditText keyText = (EditText) findViewById(R.id.add_custom_device_data_key);
		EditText valueText = (EditText) findViewById(R.id.add_custom_device_data_value);
		String key = (keyText).getText().toString().trim();
		String value = (valueText).getText().toString().trim();
		keyText.setText(null);
		valueText.setText(null);
		Apptentive.addCustomDeviceData(this, key, value);
	}

	public void removeCustomDeviceData(View view) {
		EditText keyText = (EditText) findViewById(R.id.remove_custom_device_data_key);
		String key = (keyText).getText().toString().trim();
		keyText.setText(null);
		Apptentive.removeCustomDeviceData(this, key);
	}

	public void addCustomPersonData(View view) {
		EditText keyText = (EditText) findViewById(R.id.add_custom_person_data_key);
		EditText valueText = (EditText) findViewById(R.id.add_custom_person_data_value);
		String key = (keyText).getText().toString().trim();
		String value = (valueText).getText().toString().trim();
		keyText.setText(null);
		valueText.setText(null);
		Apptentive.addCustomPersonData(this, key, value);
	}

	public void removeCustomPersonData(View view) {
		EditText keyText = (EditText) findViewById(R.id.remove_custom_person_data_key);
		String key = (keyText).getText().toString().trim();
		keyText.setText(null);
		Apptentive.removeCustomPersonData(this, key);
	}

	public void setInitialPersonEmail(View view) {
		EditText emailText = (EditText) findViewById(R.id.set_initial_person_email);
		String email = (emailText).getText().toString().trim();
		emailText.setText(null);
		PersonManager.storeInitialPersonEmail(this, email);
	}
}
