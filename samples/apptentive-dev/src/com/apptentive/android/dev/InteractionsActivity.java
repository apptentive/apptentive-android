/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.apptentive.android.dev.util.FileUtil;
import com.apptentive.android.sdk.*;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.engagement.interaction.model.*;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.storage.ApptentiveDatabase;
import com.apptentive.android.sdk.util.Util;
import org.json.JSONException;

import java.util.*;

/**
 * @author Sky Kelsey
 */
public class InteractionsActivity extends ApptentiveActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interactions);

		// Populate an auto-complete text view of event names that can be engaged.
		final AutoCompleteTextView eventName = (AutoCompleteTextView) findViewById(R.id.event_name);
		String[] events = getResources().getStringArray(R.array.events);
		ArrayAdapter<String> eventAdapter = new ArrayAdapter<String>(InteractionsActivity.this, android.R.layout.simple_spinner_dropdown_item, events);
		eventName.setAdapter(eventAdapter);
		eventName.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				eventName.showDropDown();
				return false;
			}
		});
		eventName.setText(null);

		// Populate a dropdown of manually invokable Interactions.
		{
			List<String> files = FileUtil.getFileNamesInAssetsDirectory(this, "interactions");
			// Massage the files list before setting the dropdown contents.
			ListIterator<String> fileIterator = files.listIterator();
			while (fileIterator.hasNext()) {
				String fileName = fileIterator.next();
				// Remove non-JSON files
				if (!fileName.endsWith(".json")) {
					fileIterator.remove();
					continue;
				}
				// Trim off the JSON suffix.
				fileIterator.set(fileName.split("\\.")[0]);
			}
			Spinner spinner = (Spinner) findViewById(R.id.interaction_spinner);
			ArrayAdapter interactionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, files);
			interactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(interactionAdapter);
		}

		// Populate another dropdown for payloads.
		{
			List<String> files = FileUtil.getFileNamesInAssetsDirectory(this, "payloads");
			// Massage the files list before setting the dropdown contents.
			ListIterator<String> fileIterator = files.listIterator();
			while (fileIterator.hasNext()) {
				String fileName = fileIterator.next();
				// Remove non-JSON files
				if (!fileName.endsWith(".json")) {
					fileIterator.remove();
					continue;
				}
				// Trim off the JSON suffix.
				fileIterator.set(fileName.split("\\.")[0]);
			}
			Spinner spinner = (Spinner) findViewById(R.id.payload_spinner);
			ArrayAdapter interactionAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, files);
			interactionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinner.setAdapter(interactionAdapter);
		}

		// Set up Payload Polling toggle.
		ToggleButton pollForPayloadsToggle = (ToggleButton) findViewById(R.id.poll_for_payloads);
		boolean enabled = InteractionManager.isPollForInteractions(this);
		pollForPayloadsToggle.setChecked(enabled);
		findViewById(R.id.fetch_interactions).setEnabled(enabled);
	}

	public void engage(@SuppressWarnings("unused") View view) {
		doEngage(false);
	}

	public void engageInternal(@SuppressWarnings("unused") View view) {
		doEngage(true);
	}

	public void canShowInteraction(@SuppressWarnings("unused") View view) {
		AutoCompleteTextView eventName = (AutoCompleteTextView) findViewById(R.id.event_name);
		boolean canShowInteraction = Apptentive.canShowInteraction(this, eventName.getText().toString());
		Toast.makeText(this, canShowInteraction ? "Interaction can show." : "Interaction CANNOT show.", Toast.LENGTH_SHORT).show();
	}

	public void interaction(@SuppressWarnings("unused") View view) {
		Spinner interactionsSpinner = (Spinner) findViewById(R.id.interaction_spinner);
		String interactionName = (String) interactionsSpinner.getSelectedItem();
		if (!Util.isEmpty(interactionName)) {
			// Add back in the .json extension.
			Interaction interaction = loadInteractionFromAsset("interactions/" + interactionName + ".json");
			if (interaction != null) {
				Log.e("Launching interaction manually: %s", interactionName);
				EngagementModule.launchInteraction((Activity) view.getContext(), interaction);
				Log.e(CodePointStore.toString(getApplicationContext()));
				return;
			}
		}
		Log.e("No interaction name selected from list.");
	}

	public void replacePayloads(@SuppressWarnings("unused") View view) {
		Spinner spinner = (Spinner) findViewById(R.id.payload_spinner);
		// Add back in the .json extension.
		String payloadsFileName = "payloads/" + spinner.getSelectedItem() + ".json";
		Log.e("Replacing payloads with \"%s\"", payloadsFileName);
		String payloadString = FileUtil.loadTextAssetAsString(this, payloadsFileName);
		InteractionManager.storeInteractionsPayloadString(this, payloadString);
	}

	public void fetchInteractions(@SuppressWarnings("unused") View view) {
		InteractionManager.asyncFetchAndStoreInteractions(view.getContext());
	}

	public void onPollForPayloadsChanged(@SuppressWarnings("unused") View view) {
		ToggleButton button = (ToggleButton) view;
		boolean enabled = button.isChecked();
		InteractionManager.setPollForInteractions(this, enabled);
		findViewById(R.id.fetch_interactions).setEnabled(enabled);
	}

	public void launchRootInteraction(@SuppressWarnings("unused") View view) {
		Apptentive.engage(this, "launch");
	}

	public void resetSdkState(@SuppressWarnings("unused") View view) {
		getSharedPreferences("APPTENTIVE", Context.MODE_PRIVATE).edit().clear().commit();
		CodePointStore.clear(this);
		InteractionManager.clear(this);
		GlobalInfo.reset();
		ApptentiveDatabase.reset(this);
		Apptentive.onStart(this);
	}

	public void doEngage(boolean internal) {
		AutoCompleteTextView eventName = (AutoCompleteTextView) findViewById(R.id.event_name);
		CheckBox includeCustomData = (CheckBox) findViewById(R.id.include_custom_data_checkbox);
		CheckBox includeTime = (CheckBox) findViewById(R.id.include_time_checkbox);
		CheckBox includeLocation = (CheckBox) findViewById(R.id.include_location_checkbox);
		CheckBox includeCommerce = (CheckBox) findViewById(R.id.include_commerce_checkbox);

		Map<String, Object> customData = null;
		if (includeCustomData.isEnabled() && includeCustomData.isChecked()) {
			customData = new HashMap<String, Object>();
			customData.put("string", "bar");
			customData.put("number", 12345);
		}

		List<ExtendedData> extendedData = null;
		if (includeTime.isEnabled() && includeTime.isChecked()) {
			extendedData = new ArrayList<ExtendedData>();
			extendedData.add(new TimeExtendedData());
		}

		if (includeLocation.isEnabled() && includeLocation.isChecked()) {
			if (extendedData == null) {
				extendedData = new ArrayList<ExtendedData>();
			}
			extendedData.add(new LocationExtendedData(-122.349273, 47.620509));
		}

		if (includeCommerce.isEnabled() && includeCommerce.isChecked()) {
			if (extendedData == null) {
				extendedData = new ArrayList<ExtendedData>();
			}
			CommerceExtendedData commerce = null;
			try {
				commerce = new CommerceExtendedData("id", "affiliation", 100, 5, 10, "USD");
				CommerceExtendedData.Item item = new CommerceExtendedData.Item(12345, "name", "category", 20, 5, "USD");
				commerce.addItem(item);
				extendedData.add(commerce);
			} catch (JSONException e) {
				Log.e("Error creating commerce data.", e);
			}
		}

		if (!internal) {
			Log.e("Testing engage(%s)", eventName.getText().toString());
			long start = System.currentTimeMillis();
			if (extendedData != null) {
				Apptentive.engage(this, eventName.getText().toString(), customData, extendedData.toArray(new ExtendedData[extendedData.size()]));
			} else {
				Apptentive.engage(this, eventName.getText().toString(), customData);
			}
			long end = System.currentTimeMillis();
			Log.e("Engage call took %d millis", end - start);
			Log.e(CodePointStore.toString(getApplicationContext()));
		} else {
			Log.e("Testing engageInternal(%s)", eventName.getText().toString());
			long start = System.currentTimeMillis();
			EngagementModule.engageInternal(this, eventName.getText().toString());
			long end = System.currentTimeMillis();
			Log.e("Code point storage took %d millis", end - start);
			Log.e(CodePointStore.toString(getApplicationContext()));
		}
	}

	private Interaction loadInteractionFromAsset(String fileName) {
		return Interaction.Factory.parseInteraction(FileUtil.loadTextAssetAsString(this, fileName));
	}

	@Override
	protected void onPause() {
		super.onPause();
		findViewById(R.id.cover_image).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.cover_image).setVisibility(View.GONE);
	}
}
