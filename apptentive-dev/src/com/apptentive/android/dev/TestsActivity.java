/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionModule;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.UpgradeMessageInteraction;
import com.apptentive.android.sdk.module.messagecenter.MessageManager;
import com.apptentive.android.sdk.storage.PersonManager;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		try {
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

	public void launchMessageCenterWithCustomData(@SuppressWarnings("unused") View view) {
		EditText keyText = (EditText) findViewById(R.id.message_center_custom_data_key);
		EditText valueText = (EditText) findViewById(R.id.message_center_custom_data_value);
		String key = (keyText).getText().toString().trim();
		String value = (valueText).getText().toString().trim();
		keyText.setText(null);
		valueText.setText(null);
		Map<String, String> customData = null;
		if (key != null && key.length() != 0) {
			customData = new HashMap<String, String>();
			customData.put(key, value);
		}
		Apptentive.showMessageCenter(this, customData);
	}

	public void engage(@SuppressWarnings("unused") View view) {
		Spinner codePointSpinner = (Spinner) findViewById(R.id.code_point_spinner);
		String codePoint = (String) codePointSpinner.getSelectedItem();
		Log.e("Testing engage(%s)", codePoint);
		long start = System.currentTimeMillis();
		Apptentive.engage(this, codePoint);
		long end = System.currentTimeMillis();
		Log.e("Code point storage took %d millis", end - start);
		Log.e(CodePointStore.toString(getApplicationContext()));
	}

	private static final String UPGRADE_MESSAGE_INTERACTION =
			"{\n" +
			"  \"id\": \"528d14854712c7bfd7000002\",\n" +
			"  \"priority\": 1,\n" +
			"  \"criteria\": {\n" +
			"    \"code_point/app.launch/invokes/version\": 1,\n" +
			"    \"application_version\": \"4.0\"\n" +
			"  },\n" +
			"  \"type\": \"UpgradeMessage\",\n" +
			"  \"version\": null,\n" +
			"  \"active\": true,\n" +
			"  \"configuration\": {\n" +
			"    \"active\": true,\n" +
			"    \"app_version\": \"4.0\",\n" +
			"    \"show_app_icon\": true,\n" +
			"    \"show_powered_by\": true,\n" +
			"    \"body\": \"<html><head><style>\\nbody {\\n\\tfont-family: \\\"Helvetica Neue\\\", Helvetica;\\n\\tcolor: #4d4d4d;\\n\\tfont-size: .875em;\\n\\tline-height: 1.36em;\\n\\t-webkit-text-size-adjust:none;\\n}\\n\\nh1, h2, h3, h4, h5, h6 {\\n\\tcolor: #000000;\\n\\tline-height: 1.25em;\\n\\ttext-align: center;\\n}\\n\\nh1 {font-size: 22px;}\\nh2 {font-size: 18px;}\\nh3 {font-size: 16px;}\\nh4 {font-size: 14px;}\\nh5, h6 {font-size: 12px;}\\nh6 {font-weight: normal;}\\n\\nblockquote {\\n\\tmargin: 1em 1.75em;\\n\\tfont-style: italic;\\n}\\n\\nul, ol {\\n\\tpadding-left: 1.75em;\\n}\\n\\ntable {\\n\\tborder-collapse: collapse;\\n\\tborder-spacing: 0;\\n\\tempty-cells: show;\\n}\\n\\ntable caption {\\n\\tpadding: 1em 0;\\n\\ttext-align: center;\\n}\\n\\ntable td,\\ntable th {\\n\\tborder-left: 1px solid #cbcbcb;\\n\\tborder-width: 0 0 0 1px;\\n\\tfont-size: inherit;\\n\\tmargin: 0;\\n\\tpadding: .25em .5em;\\n\\n}\\ntable td:first-child,\\ntable th:first-child {\\n\\tborder-left-width: 0;\\n}\\ntable th:first-child {\\n\\tborder-radius: 4px 0 0 4px;\\n}\\ntable th:last-child {\\n\\tborder-radius: 0 4px 4px 0;\\n}\\n\\ntable thead {\\n\\tbackground: #E5E5E5;\\n\\tcolor: #000;\\n\\ttext-align: left;\\n\\tvertical-align: bottom;\\n}\\n\\ntable td {\\n\\tbackground-color: transparent;\\n\\tborder-bottom: 1px solid #E5E5E5;\\n}\\n</style></head><body><p>Testing upgrade messaging.</p></body></html>\"\n" +
			"  }\n" +
			"}";

	public void interaction(@SuppressWarnings("unused") View view) {
		Spinner interactionsSpinner = (Spinner) findViewById(R.id.interaction_spinner);
		String interactionName = (String) interactionsSpinner.getSelectedItem();
		Log.e("Testing engage(%s)", interactionName);
		long start = System.currentTimeMillis();
		Apptentive.engage(this, interactionName);
		// TODO: Make this actually use the spinner.
		try {
			Interaction interaction = null;
			if (interactionName.equals("UpgradeMessage")) {
				interaction = new UpgradeMessageInteraction(UPGRADE_MESSAGE_INTERACTION);
			}
			if (interaction != null) {
				InteractionModule.launchInteraction((Activity) view.getContext(), interaction);
			}
		} catch (JSONException e) {
		}
		long end = System.currentTimeMillis();
		Log.e("Interaction storage took %d millis", end - start);
		Log.e(CodePointStore.toString(getApplicationContext()));
	}
}
