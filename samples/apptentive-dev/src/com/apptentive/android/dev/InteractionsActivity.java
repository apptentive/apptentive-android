/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
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
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.engagement.interaction.model.*;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.metric.MetricModule;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sky Kelsey
 */
public class InteractionsActivity extends ApptentiveActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interactions);

		final AutoCompleteTextView eventName = (AutoCompleteTextView) findViewById(R.id.event_name);
		String[] events = getResources().getStringArray(R.array.events);
		ArrayAdapter<String> eventAdapter = new ArrayAdapter<String>(InteractionsActivity.this, android.R.layout.simple_dropdown_item_1line, events);
		eventName.setAdapter(eventAdapter);
		eventName.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				eventName.showDropDown();
				return false;
			}
		});
		eventName.setText(null);
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
			if (extendedData == null) {
				extendedData = new ArrayList<ExtendedData>();
			}
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

	public void engage(@SuppressWarnings("unused") View view) {
		doEngage(false);
	}

	public void engageInternal(@SuppressWarnings("unused") View view) {
		doEngage(true);
	}

	public void willShowInteraction(@SuppressWarnings("unused") View view) {
		AutoCompleteTextView eventName = (AutoCompleteTextView) findViewById(R.id.event_name);
		boolean willShowInteraction = Apptentive.willShowInteraction(this, eventName.getText().toString());
		Toast.makeText(this, willShowInteraction ? "Interaction will show." : "Interaction will NOT show.", Toast.LENGTH_SHORT).show();
	}

	public void interaction(@SuppressWarnings("unused") View view) {
		Spinner interactionsSpinner = (Spinner) findViewById(R.id.interaction_spinner);
		String interactionName = (String) interactionsSpinner.getSelectedItem();
		Log.e("Testing engage(%s)", interactionName);
		Interaction interaction = null;
		if (interactionName.equals("Upgrade Message With Branding")) {
			interaction = loadInteractionFromAsset("interactions/upgradeMessageWithBranding.json");
		} else if (interactionName.equals("Upgrade Message No Branding")) {
			interaction = loadInteractionFromAsset("interactions/upgradeMessageNoBranding.json");
		} else if (interactionName.equals("Enjoyment Dialog")) {
			interaction = loadInteractionFromAsset("interactions/enjoymentDialog.json");
		} else if (interactionName.equals("Rating Dialog")) {
			interaction = loadInteractionFromAsset("interactions/ratingDialog.json");
		} else if (interactionName.equals("App Store Rating")) {
			interaction = loadInteractionFromAsset("interactions/appStoreRating.json");
		} else if (interactionName.equals("Feedback Dialog")) {
			interaction = loadInteractionFromAsset("interactions/feedbackDialog.json");
		} else if (interactionName.equals("Survey")) {
			interaction = loadInteractionFromAsset("interactions/survey.json");
		} else if (interactionName.equals("TextModal 1 Button")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredOneButton.json");
		} else if (interactionName.equals("TextModal 2 Buttons")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredTwoButtons.json");
		} else if (interactionName.equals("TextModal 3 Buttons")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredThreeButtons.json");
		} else if (interactionName.equals("TextModal 4 Buttons")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredFourButtons.json");
		} else if (interactionName.equals("TextModal 2 Long Buttons")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredTwoLongButtons.json");
		} else if (interactionName.equals("TextModal 2 Really Long Buttons")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredTwoReallyLongButtons.json");
		} else if (interactionName.equals("TextModal Colors 1")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredCustomColorsOne.json");
		} else if (interactionName.equals("TextModal Colors 2")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredCustomColorsTwo.json");
		} else if (interactionName.equals("TextModal Colors 3")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredCustomColorsThree.json");
		} else if (interactionName.equals("TextModal Bottom")) {
			interaction = loadInteractionFromAsset("interactions/textModalBottom.json");
		} else if (interactionName.equals("TextModal Centered Long Content")) {
			interaction = loadInteractionFromAsset("interactions/textModalCenteredLongContent.json");
		} else if (interactionName.equals("TextModal Bottom Long Content")) {
			interaction = loadInteractionFromAsset("interactions/textModalBottomLongContent.json");
		} else if (interactionName.equals("Fullscreen HTML")) {
			interaction = loadInteractionFromAsset("interactions/fullscreenHtml.json");
		}
		if (interaction != null) {
			EngagementModule.launchInteraction((Activity) view.getContext(), interaction);
		}
		Log.e(CodePointStore.toString(getApplicationContext()));
	}

	public void replacePayload(@SuppressWarnings("unused") View view) {
		Spinner payloadsSpinner = (Spinner) findViewById(R.id.payload_spinner);
		String payloadsFileName = "payloads/" + payloadsSpinner.getSelectedItem() + ".json";
		Log.e("Replacing payloads with \"%s\"", payloadsFileName);
		String payloadString = FileUtil.loadTextAssetAsString(this, payloadsFileName);
		InteractionManager.storeInteractionsPayloadString(this, payloadString);
	}

	private Interaction loadInteractionFromAsset(String fileName) {
		return Interaction.Factory.parseInteraction(FileUtil.loadTextAssetAsString(this, fileName));
	}

	public void fetchInteractions(View view) {
		InteractionManager.asyncFetchAndStoreInteractions(view.getContext());
	}

	public void forceRatingsPrompt(View view) {
		String eventName = ((EditText) findViewById(R.id.force_ratings_prompt_event_name)).getText().toString();
		boolean shown = forceShowRatingsPromptInteraction(this, eventName);
		Log.e("Force showed Ratings Prompt? %b", shown);
	}

	public static boolean forceShowRatingsPromptInteraction(Activity activity, String eventName) {
		try {
			Log.d("Force Showing Ratings Prompt.");

			Interaction interaction = getRatingsPromptInteraction(activity);

			if (interaction != null) {
				CodePointStore.storeInteractionForCurrentAppVersion(activity, interaction.getId());
				EngagementModule.launchInteraction(activity, interaction);
				return true;
			} else {
				Toast.makeText(activity, "No Ratings Prompt available for that Interaction.", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			MetricModule.sendError(activity.getApplicationContext(), e, null, null);
			Log.e("Error:", e);
		}
		return false;
	}

	public static Interaction getRatingsPromptInteraction(Context context) {
		Interactions interactions = InteractionManager.getInteractions(context);
		List<Interaction> interactionList = interactions.getInteractionList();

		for (Interaction interaction : interactionList) {
			switch (interaction.getType()) {
				case EnjoymentDialog:
					return interaction;
			}
		}
		return null;
	}
}
