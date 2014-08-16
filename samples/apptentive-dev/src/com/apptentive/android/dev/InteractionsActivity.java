/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.dev;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.apptentive.android.dev.util.FileUtil;
import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveActivity;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.engagement.EngagementModule;
import com.apptentive.android.sdk.module.engagement.interaction.InteractionManager;
import com.apptentive.android.sdk.module.engagement.interaction.model.*;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;
import com.apptentive.android.sdk.module.metric.MetricModule;
import org.json.JSONException;
import org.json.JSONObject;

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

		CheckBox engageInternalEvent = (CheckBox) findViewById(R.id.internal_event_checkbox);
		engageInternalEvent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean engageInternalEvent) {
				if (engageInternalEvent) {
					setupForInternalEvent();
				} else {
					setupForExternalEvent();
				}
			}
		});
		setupForExternalEvent();
	}

	private void setupForInternalEvent() {
		CheckBox includeCustomData = (CheckBox) findViewById(R.id.include_custom_data_checkbox);
		CheckBox includeTime = (CheckBox) findViewById(R.id.include_time_checkbox);
		CheckBox includeLocation = (CheckBox) findViewById(R.id.include_location_checkbox);
		CheckBox includeCommerce = (CheckBox) findViewById(R.id.include_commerce_checkbox);
		includeCustomData.setEnabled(false);
		includeTime.setEnabled(false);
		includeLocation.setEnabled(false);
		includeCommerce.setEnabled(false);

		final AutoCompleteTextView eventName = (AutoCompleteTextView) findViewById(R.id.event_name);
		eventName.setText(null);
		String[] events = getResources().getStringArray(R.array.internal_events);
		ArrayAdapter<String> eventAdapter = new ArrayAdapter<String>(InteractionsActivity.this, android.R.layout.simple_dropdown_item_1line, events);
		eventName.setAdapter(eventAdapter);
		eventName.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				eventName.showDropDown();
				return false;
			}
		});
		eventName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		eventName.setText(null);
	}

	private void setupForExternalEvent() {
		CheckBox includeCustomData = (CheckBox) findViewById(R.id.include_custom_data_checkbox);
		CheckBox includeTime = (CheckBox) findViewById(R.id.include_time_checkbox);
		CheckBox includeLocation = (CheckBox) findViewById(R.id.include_location_checkbox);
		CheckBox includeCommerce = (CheckBox) findViewById(R.id.include_commerce_checkbox);
		includeCustomData.setEnabled(true);
		includeTime.setEnabled(true);
		includeLocation.setEnabled(true);
		includeCommerce.setEnabled(true);

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
		eventName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});
		eventName.setText(null);
	}

	public void engage(@SuppressWarnings("unused") View view) {
		AutoCompleteTextView eventName = (AutoCompleteTextView) findViewById(R.id.event_name);
		CheckBox engageInternalEvent = (CheckBox) findViewById(R.id.internal_event_checkbox);
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
			CommerceExtendedData commerce = new CommerceExtendedData("id", "affiliation", 100, 5, 10, "USD");
			commerce.addItem("id", "name", "category", 20, 5, "USD");
			extendedData.add(commerce);
		}

		if (!engageInternalEvent.isChecked()) {
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

	private static final String UPGRADE_MESSAGE_BRANDING_INTERACTION = "" +
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
		"    \"body\": \"<html><head><style>\\nbody {\\n\\tfont-family: \\\"Helvetica Neue\\\", Helvetica;\\n\\tcolor: #4d4d4d;\\n\\tfont-size: .875em;\\n\\tline-height: 1.36em;\\n\\t-webkit-text-size-adjust:none;\\n}\\n\\nh1, h2, h3, h4, h5, h6 {\\n\\tcolor: #000000;\\n\\tline-height: 1.25em;\\n\\ttext-align: center;\\n}\\n\\nh1 {font-size: 22px;}\\nh2 {font-size: 18px;}\\nh3 {font-size: 16px;}\\nh4 {font-size: 14px;}\\nh5, h6 {font-size: 12px;}\\nh6 {font-weight: normal;}\\n\\nblockquote {\\n\\tmargin: 1em 1.75em;\\n\\tfont-style: italic;\\n}\\n\\nul, ol {\\n\\tpadding-left: 1.75em;\\n}\\n\\ntable {\\n\\tborder-collapse: collapse;\\n\\tborder-spacing: 0;\\n\\tempty-cells: show;\\n}\\n\\ntable caption {\\n\\tpadding: 1em 0;\\n\\ttext-align: center;\\n}\\n\\ntable td,\\ntable th {\\n\\tborder-left: 1px solid #cbcbcb;\\n\\tborder-width: 0 0 0 1px;\\n\\tfont-size: inherit;\\n\\tmargin: 0;\\n\\tpadding: .25em .5em;\\n\\n}\\ntable td:first-child,\\ntable th:first-child {\\n\\tborder-left-width: 0;\\n}\\ntable th:first-child {\\n\\tborder-radius: 4px 0 0 4px;\\n}\\ntable th:last-child {\\n\\tborder-radius: 0 4px 4px 0;\\n}\\n\\ntable thead {\\n\\tbackground: #E5E5E5;\\n\\tcolor: #000;\\n\\ttext-align: left;\\n\\tvertical-align: bottom;\\n}\\n\\ntable td {\\n\\tbackground-color: transparent;\\n\\tborder-bottom: 1px solid #E5E5E5;\\n}\\n</style></head><body><p>Hello. A lot has happened since the last release. Here's a quick list of changes.</p><hr><ul><li>I built the thing you are seeing here. It will let me tell you about updates to this app.</li><li>It's super useful, since Google Play now downloads updates automatically, and you never see release notes otherwise.</li></ul><p>Some other things:</p><pre>I wrote a lot of code.</pre><p><strong>I made some bold statements.</strong></p><blockquote>I quoted some quotes.</blockquote><p><em>I italicized some itals.</em></p><p><del>I struck through some suckers.</del></p><p>Some plans for the future:</p><ol><li>Use the new Engagement Framework for Ratings as well.</li><li>Make sure everything is tested and documented before I release this to valentines.</li><li>Get everyone on the team to test it out :)</li></ol><p style=\\\"text-align: center;\\\">Center text</p><p style=\\\"text-align: right;\\\">Right aligned</p><p style=\\\"text-align: justify;\\\">Some justified text. What will this look like on a mobile device anyway? Will if look good, bad, ugly, beautiful, horrendously hideous, or possess the qualities of an altogether different adjective? Your guess is as good as mine.</p><p>Some unjustified text. What will it look like on a mobile device anyway?\\n Will if look good, bad, ugly, beautiful, horrendously hideous, or \\npossess the qualities of an altogether different adjective? Your guess is as good as mine.</p><p>Some</p><p style=\\\"margin-left: 20px;\\\">Indented</p><p style=\\\"margin-left: 40px;\\\">Text</p><p style=\\\"margin-left: 60px;\\\">Will</p><p style=\\\"margin-left: 80px;\\\">This</p><p style=\\\"margin-left: 100px;\\\">Work?</p></body></html>\"\n" +
		"  }\n" +
		"}";

	private static final String UPGRADE_MESSAGE_NO_BRANDING_INTERACTION = "" +
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
		"    \"show_powered_by\": false,\n" +
		"    \"body\": \"<html><head><style>\\nbody {\\n\\tfont-family: \\\"Helvetica Neue\\\", Helvetica;\\n\\tcolor: #4d4d4d;\\n\\tfont-size: .875em;\\n\\tline-height: 1.36em;\\n\\t-webkit-text-size-adjust:none;\\n}\\n\\nh1, h2, h3, h4, h5, h6 {\\n\\tcolor: #000000;\\n\\tline-height: 1.25em;\\n\\ttext-align: center;\\n}\\n\\nh1 {font-size: 22px;}\\nh2 {font-size: 18px;}\\nh3 {font-size: 16px;}\\nh4 {font-size: 14px;}\\nh5, h6 {font-size: 12px;}\\nh6 {font-weight: normal;}\\n\\nblockquote {\\n\\tmargin: 1em 1.75em;\\n\\tfont-style: italic;\\n}\\n\\nul, ol {\\n\\tpadding-left: 1.75em;\\n}\\n\\ntable {\\n\\tborder-collapse: collapse;\\n\\tborder-spacing: 0;\\n\\tempty-cells: show;\\n}\\n\\ntable caption {\\n\\tpadding: 1em 0;\\n\\ttext-align: center;\\n}\\n\\ntable td,\\ntable th {\\n\\tborder-left: 1px solid #cbcbcb;\\n\\tborder-width: 0 0 0 1px;\\n\\tfont-size: inherit;\\n\\tmargin: 0;\\n\\tpadding: .25em .5em;\\n\\n}\\ntable td:first-child,\\ntable th:first-child {\\n\\tborder-left-width: 0;\\n}\\ntable th:first-child {\\n\\tborder-radius: 4px 0 0 4px;\\n}\\ntable th:last-child {\\n\\tborder-radius: 0 4px 4px 0;\\n}\\n\\ntable thead {\\n\\tbackground: #E5E5E5;\\n\\tcolor: #000;\\n\\ttext-align: left;\\n\\tvertical-align: bottom;\\n}\\n\\ntable td {\\n\\tbackground-color: transparent;\\n\\tborder-bottom: 1px solid #E5E5E5;\\n}\\n</style></head><body><p>Hello. A lot has happened since the last release. Here's a quick list of changes.</p><hr><ul><li>I built the thing you are seeing here. It will let me tell you about updates to this app.</li><li>It's super useful, since Google Play now downloads updates automatically, and you never see release notes otherwise.</li></ul><p>Some other things:</p><pre>I wrote a lot of code.</pre><p><strong>I made some bold statements.</strong></p><blockquote>I quoted some quotes.</blockquote><p><em>I italicized some itals.</em></p><p><del>I struck through some suckers.</del></p><p>Some plans for the future:</p><ol><li>Use the new Engagement Framework for Ratings as well.</li><li>Make sure everything is tested and documented before I release this to valentines.</li><li>Get everyone on the team to test it out :)</li></ol><p style=\\\"text-align: center;\\\">Center text</p><p style=\\\"text-align: right;\\\">Right aligned</p><p style=\\\"text-align: justify;\\\">Some justified text. What will this look like on a mobile device anyway? Will if look good, bad, ugly, beautiful, horrendously hideous, or possess the qualities of an altogether different adjective? Your guess is as good as mine.</p><p>Some unjustified text. What will it look like on a mobile device anyway?\\n Will if look good, bad, ugly, beautiful, horrendously hideous, or \\npossess the qualities of an altogether different adjective? Your guess is as good as mine.</p><p>Some</p><p style=\\\"margin-left: 20px;\\\">Indented</p><p style=\\\"margin-left: 40px;\\\">Text</p><p style=\\\"margin-left: 60px;\\\">Will</p><p style=\\\"margin-left: 80px;\\\">This</p><p style=\\\"margin-left: 100px;\\\">Work?</p></body></html>\"\n" +
		"  }\n" +
		"}";

	private static final String UPGRADE_MESSAGE_REAL_EXAMPLE = "" +
		"{\n" +
		"  \"id\": \"52e8091f7724c5cf1f00007b\",\n" +
		"  \"priority\": 2,\n" +
		"  \"criteria\": {\n" +
		"  \"code_point/app.launch/invokes/version\": 1,\n" +
		"  \"application_version\": \"4.1\"\n" +
		"  },\n" +
		"    \"type\": \"UpgradeMessage\",\n" +
		"    \"configuration\": {\n" +
		"    \"show_app_icon\": true,\n" +
		"    \"show_powered_by\": true,\n" +
		"    \"body\": \"<html><head><style>\\nbody {\\n\\tfont-family: \\\"Helvetica Neue\\\", Helvetica;\\n\\tcolor: #4d4d4d;\\n\\tfont-size: .875em;\\n\\tline-height: 1.36em;\\n\\t-webkit-text-size-adjust:none;\\n}\\n\\nh1, h2, h3, h4, h5, h6 {\\n\\tcolor: #000000;\\n\\tline-height: 1.25em;\\n\\ttext-align: center;\\n}\\n\\nh1 {font-size: 22px;}\\nh2 {font-size: 18px;}\\nh3 {font-size: 16px;}\\nh4 {font-size: 14px;}\\nh5, h6 {font-size: 12px;}\\nh6 {font-weight: normal;}\\n\\nblockquote {\\n\\tmargin: 1em 1.75em;\\n\\tfont-style: italic;\\n}\\n\\nul, ol {\\n\\tpadding-left: 1.75em;\\n}\\n\\ntable {\\n\\tborder-collapse: collapse;\\n\\tborder-spacing: 0;\\n\\tempty-cells: show;\\n}\\n\\ntable caption {\\n\\tpadding: 1em 0;\\n\\ttext-align: center;\\n}\\n\\ntable td,\\ntable th {\\n\\tborder-left: 1px solid #cbcbcb;\\n\\tborder-width: 0 0 0 1px;\\n\\tfont-size: inherit;\\n\\tmargin: 0;\\n\\tpadding: .25em .5em;\\n\\n}\\ntable td:first-child,\\ntable th:first-child {\\n\\tborder-left-width: 0;\\n}\\ntable th:first-child {\\n\\tborder-radius: 4px 0 0 4px;\\n}\\ntable th:last-child {\\n\\tborder-radius: 0 4px 4px 0;\\n}\\n\\ntable thead {\\n\\tbackground: #E5E5E5;\\n\\tcolor: #000;\\n\\ttext-align: left;\\n\\tvertical-align: bottom;\\n}\\n\\ntable td {\\n\\tbackground-color: transparent;\\n\\tborder-bottom: 1px solid #E5E5E5;\\n}\\n</style></head><body><p style=\\\"text-align: center;\\\"><strong>Apptentive SDK</strong></p><hr><p>New in version 1.2.8:</p><ul><li>A new system for sending engagements to the user.</li><li>A new engagement: Upgrade Messages!</li><li>Support for Push Notifications through Urban Airship.</li><li>Several bug fixes, and a nicer looking dev app.</li></ul><p>Enjoy!</p></body></html>\"\n" +
		"  }\n" +
		"}";

	private static final String ENJOYMENT_DIALOG_INTERACTION = "" +
		"{" +
		"  \"priority\": 1," +
		"  \"criteria\": {" +
		"    \"interactions/enjoyment_dialog_interaction_1234567890/invokes/total\": 0" +
		"  }," +
		"  \"id\": \"enjoyment_dialog_interaction_1234567890\"," +
		"  \"type\": \"EnjoymentDialog\"," +
		"  \"configuration\": {" +
		"  }" +
		"}";

	private static final String RATING_DIALOG_INTERACTION = "" +
		"{" +
		"  \"priority\": 1," +
		"  \"criteria\": {" +
		"  }," +
		"  \"id\": \"rating_dialog_interaction_1234567890\"," +
		"  \"type\": \"RatingDialog\"," +
		"  \"configuration\": {" +
		"    \"title\": \"Thank You\"," +
		"    \"body\": \"We're so happy to hear that you love Urbanspoon! It'd be really helpful if you rated us. Thanks so much for spending some time with us.\"," +
		"    \"rate_text\": \"Rate\"," +
		"    \"remind_text\": \"Remind Me Later\"," +
		"    \"no_text\": \"No Thanks\"" +
		"  }" +
		"}";

	private static final String FEEDBACK_DIALOG_INTERACTION = "" +
		"{" +
		"  \"priority\": 1," +
		"  \"criteria\": {" +
		"  }," +
		"  \"id\": \"<interaction_id>\"," +
		"  \"type\": \"FeedbackDialog\"," +
		"  \"configuration\": {" +
		"    \"ask_for_email\": true," +
		"    \"email_required\": false," +
		"    \"enable_message_center\": true," +
		"    \"title\": \"Thanks for your feedback!\"," +
		"    \"body\": \"Please let us know how to make this app better for you!\"," +
		"    \"email_hint_text\": \"Your Email Address\"," +
		"    \"message_hint_text\": \"Tell us how we can help. (required)\"," +
		"    \"no_text\": \"No Thanks\"," +
		"    \"send_text\": \"Send\"," +
		"    \"thank_you_title\": \"Thanks!\"," +
		"    \"thank_you_body\": \"Your response has been saved in the Message Center, where you\\'ll be able to view replies and send us other messages.\"," +
		"    \"thank_you_close_text\": \"Close\"," +
		"    \"thank_you_view_messages_text\": \"View Messages\"" +
		"  }" +
		"}";

	private static final String APP_STORE_RATING_INTERACTION = "" +
		"{" +
		"  \"priority\": 1," +
		"  \"criteria\": {" +
		"  }," +
		"  \"id\": \"app_store_rating_interaction\"," +
		"  \"type\": \"AppStoreRating\"," +
		"  \"configuration\": {" +
		"    store_id: \"12345678\"," +
		"    method: \"magic\"," +
		"    url: \"itms-apps://itunes.apple.com/WebObjects/MZStore.woa/wa/viewContentsUserReviews?id=12345678&pageNumber=0&sortOrdering=1\"" +
		"  }" +
		"}";

	private static final String SURVEY_INTERACTION = "" +
		"{" +
		"    \"id\": \"526fe2836dd8bf546a00000c\"," +
		"    \"priority\": 3," +
		"    \"criteria\": {}," +
		"    \"type\": \"Survey\"," +
		"    \"configuration\": {" +
		"        \"questions\": [" +
		"            {" +
		"                \"id\": \"multi-choice-question\"," +
		"                \"answer_choices\": [" +
		"                    {" +
		"                        \"id\": \"multi-choice-answer-0\"," +
		"                        \"value\": \"Better user interface\"" +
		"                    }," +
		"                    {" +
		"                        \"id\": \"multi-choice-answer-1\"," +
		"                        \"value\": \"Cloud support\"" +
		"                    }," +
		"                    {" +
		"                        \"id\": \"multi-choice-answer-2\"," +
		"                        \"value\": \"Login with Facebook / Google / Twitter\"" +
		"                    }" +
		"                ]," +
		"                \"instructions\": \"select one\"," +
		"                \"value\": \"Which would you like to see first?\"," +
		"                \"type\": \"multichoice\"," +
		"                \"required\": true" +
		"            }," +
		"            {" +
		"                \"id\": \"multi-select-question\"," +
		"                \"answer_choices\": [" +
		"                    {" +
		"                        \"id\": \"multi-select-answer-0\"," +
		"                        \"value\": \"Speed\"" +
		"                    }," +
		"                    {" +
		"                        \"id\": \"multi-select-answer-1\"," +
		"                        \"value\": \"Easy to use\"" +
		"                    }," +
		"                    {" +
		"                        \"id\": \"multi-select-answer-2\"," +
		"                        \"value\": \"Reliability\"" +
		"                    }," +
		"                    {" +
		"                        \"id\": \"multi-select-answer-3\"," +
		"                        \"value\": \"Works offline\"" +
		"                    }" +
		"                ]," +
		"                \"instructions\": \"select up to 2\"," +
		"                \"min_selections\": 0," +
		"                \"max_selections\": 2," +
		"                \"value\": \"Which two qualities for an app are the most important to you?\"," +
		"                \"type\": \"multiselect\"," +
		"                \"required\": false" +
		"            }," +
		"            {" +
		"                \"id\": \"single-line-question\"," +
		"                \"multiline\": false," +
		"                \"value\": \"Is there anything you'd like to add?\"," +
		"                \"type\": \"singleline\"," +
		"                \"required\": false" +
		"            }" +
		"        ]," +
		"        \"name\": \"What should we build?\"," +
		"        \"show_success_message\": true," +
		"        \"success_message\": \"Thank you for your input.\"," +
		"        \"description\": \"Please help us figure this out!\"," +
		"        \"app_id\": \"517884df584ef064fc00000e\"," +
		"        \"active\": true," +
		"        \"date\": \"2014-01-19T04:51:14Z\"," +
		"        \"device_attrs\": {" +
		"            \"os_name\": \"Android\"," +
		"            \"os_version\": \"4.4.2\"" +
		"        }" +
		"    }" +
		"}";

	public void interaction(@SuppressWarnings("unused") View view) {
		Spinner interactionsSpinner = (Spinner) findViewById(R.id.interaction_spinner);
		String interactionName = (String) interactionsSpinner.getSelectedItem();
		Log.e("Testing engage(%s)", interactionName);
		long start = System.currentTimeMillis();
		try {
			Interaction interaction = null;
			if (interactionName.equals("UpgradeMessage exercise with branding")) {
				interaction = new UpgradeMessageInteraction(UPGRADE_MESSAGE_BRANDING_INTERACTION);
			} else if (interactionName.equals("UpgradeMessage exercise no branding")) {
				interaction = new UpgradeMessageInteraction(UPGRADE_MESSAGE_NO_BRANDING_INTERACTION);
			} else if (interactionName.equals("UpgradeMessage real example")) {
				interaction = new UpgradeMessageInteraction(UPGRADE_MESSAGE_REAL_EXAMPLE);
			} else if (interactionName.equals("Enjoyment Dialog")) {
				interaction = new EnjoymentDialogInteraction(ENJOYMENT_DIALOG_INTERACTION);
			} else if (interactionName.equals("Rating Dialog")) {
				interaction = new EnjoymentDialogInteraction(RATING_DIALOG_INTERACTION);
			} else if (interactionName.equals("App Store Rating")) {
				interaction = new AppStoreRatingInteraction(APP_STORE_RATING_INTERACTION);
			} else if (interactionName.equals("Feedback Dialog")) {
				interaction = new FeedbackDialogInteraction(FEEDBACK_DIALOG_INTERACTION);
			} else if (interactionName.equals("Survey")) {
				interaction = new SurveyInteraction(SURVEY_INTERACTION);
			} else if (interactionName.equals("Working Rating Flow Default Text")) {
				String json = FileUtil.loadTextAssetAsString(this, "ratingFlowInteractionsDefaultText.json");
				// Overwrites any existing interactions.
				InteractionManager.storeInteractions(this, json);
				Apptentive.engage(this, "init");
			} else if (interactionName.equals("Working Rating Flow Modified Text")) {
				String json = FileUtil.loadTextAssetAsString(this, "ratingFlowInteractionsModifiedText.json");
				// Overwrites any existing interactions.
				InteractionManager.storeInteractions(this, json);
				Apptentive.engage(this, "init");
			}
			if (interaction != null) {
				EngagementModule.launchInteraction((Activity) view.getContext(), interaction);
			}
		} catch (JSONException e) {
			Log.e("Error loading test Interaction.", e);
		}
		long end = System.currentTimeMillis();
		Log.e("Interaction storage took %d millis", end - start);
		Log.e(CodePointStore.toString(getApplicationContext()));
	}

	public void fetchInteractions(View view) {
		InteractionManager.asyncFetchAndStoreInteractions(view.getContext());
	}

	public void forceRatingsPrompt(View view) {
		String eventName = ((EditText) findViewById(R.id.force_ratings_prompt_event_name)).getText().toString();
		boolean shown = forceShowRatingsPromptInteraction(this, eventName);
		Log.e("Force showed Ratings Prompt? %b", shown);
	}

	public void checkForInteraction(View view) {
		String eventName = ((EditText) findViewById(R.id.force_ratings_prompt_event_name)).getText().toString();
		boolean available = isInteractionAvailable(this, eventName);
		Toast.makeText(this, "Ratins Prompt Available? " + available, Toast.LENGTH_SHORT).show();
		Log.e("Ratings Prompt Available? %b", available);
	}

	public static boolean forceShowRatingsPromptInteraction(Activity activity, String eventName) {
		if (eventName == null) {
			Log.w("Event name is null. Can't force show Ratings Prompt.");
			return false;
		}
		try {
			String eventLabel = EngagementModule.generateEventLabel("local", "app", eventName);
			Log.d("Force Showing Ratings Prompt at: ", eventLabel);

			Interaction interaction = getRatingsPromptInteraction(activity, eventLabel);

			if (interaction != null) {
				CodePointStore.storeCodePointForCurrentAppVersion(activity.getApplicationContext(), eventLabel);
				EventManager.sendEvent(activity.getApplicationContext(), new Event(eventLabel, (JSONObject) null));

				CodePointStore.storeInteractionForCurrentAppVersion(activity, interaction.getId());
				EngagementModule.launchInteraction(activity, interaction);
				return true;
			}
		} catch (Exception e) {
			MetricModule.sendError(activity.getApplicationContext(), e, null, null);
			Log.e("Error:", e);
		}
		return false;
	}

	public static boolean isInteractionAvailable(Context context, String eventName) {
		if (eventName == null) {
			Log.w("Event name is null. Can't check for Ratings Prompt.");
			return false;
		}

		String eventLabel = EngagementModule.generateEventLabel("local", "app", eventName);
		Interaction interaction = getRatingsPromptInteraction(context, eventLabel);
		return interaction != null;
	}

	public static Interaction getRatingsPromptInteraction(Context context, String eventLabel) {
		Interactions interactions = InteractionManager.loadInteractions(context);
		List<Interaction> interactionList = interactions.getInteractionList(eventLabel);

		for (Interaction interaction : interactionList) {
			switch (interaction.getType()) {
				case EnjoymentDialog:
					return interaction;
			}
		}
		return null;
	}
}
