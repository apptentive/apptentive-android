/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.storage.PersonManager;

/**
 * @author Sky Kelsey
 */
public class DataActivity extends ApptentiveActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.data);
	}

	public void addCustomDeviceData(@SuppressWarnings("unused") View view) {
		EditText keyText = (EditText) findViewById(R.id.add_custom_device_data_key);
		EditText valueText = (EditText) findViewById(R.id.add_custom_device_data_value);
		String key = (keyText).getText().toString().trim();
		String value = (valueText).getText().toString().trim();
		keyText.setText(null);
		valueText.setText(null);
		Apptentive.addCustomDeviceData(this, key, value);
	}

	public void removeCustomDeviceData(@SuppressWarnings("unused") View view) {
		EditText keyText = (EditText) findViewById(R.id.remove_custom_device_data_key);
		String key = (keyText).getText().toString().trim();
		keyText.setText(null);
		Apptentive.removeCustomDeviceData(this, key);
	}

	public void addCustomPersonData(@SuppressWarnings("unused") View view) {
		EditText keyText = (EditText) findViewById(R.id.add_custom_person_data_key);
		EditText valueText = (EditText) findViewById(R.id.add_custom_person_data_value);
		String key = (keyText).getText().toString().trim();
		String value = (valueText).getText().toString().trim();
		keyText.setText(null);
		valueText.setText(null);
		Apptentive.addCustomPersonData(this, key, value);
	}

	public void removeCustomPersonData(@SuppressWarnings("unused") View view) {
		EditText keyText = (EditText) findViewById(R.id.remove_custom_person_data_key);
		String key = (keyText).getText().toString().trim();
		keyText.setText(null);
		Apptentive.removeCustomPersonData(this, key);
	}

	public void setInitialPersonEmail(@SuppressWarnings("unused") View view) {
		EditText emailText = (EditText) findViewById(R.id.set_initial_person_email);
		String email = (emailText).getText().toString().trim();
		emailText.setText(null);
		PersonManager.storeInitialPersonEmail(this, email);
	}

	public void setInitialPersonUserName(@SuppressWarnings("unused") View view) {
		EditText userNameText = (EditText) findViewById(R.id.set_initial_person_user_name);
		String userName = (userNameText).getText().toString().trim();
		userNameText.setText(null);
		PersonManager.storeInitialPersonUserName(this, userName);
	}
}
