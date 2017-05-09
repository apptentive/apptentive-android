/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.storage.CustomData;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.IntegrationConfig;
import com.apptentive.android.sdk.storage.IntegrationConfigItem;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.util.Constants;

import java.util.Iterator;

public class Migrator {

	private Conversation conversation;
	private SharedPreferences prefs;
	private Context context;

	public Migrator(Context context, SharedPreferences prefs, Conversation conversation) {
		this.context = context;
		this.prefs = prefs;
		this.conversation = conversation;
	}

	public void migrate() {
		// Miscellaneous
		conversation.setLastSeenSdkVersion(prefs.getString(Constants.PREF_KEY_LAST_SEEN_SDK_VERSION, null));

		migrateDevice();
		migrateSdk();
	}

	private static final String INTEGRATION_APPTENTIVE_PUSH = "apptentive_push";
	private static final String INTEGRATION_PARSE = "parse";
	private static final String INTEGRATION_URBAN_AIRSHIP = "urban_airship";
	private static final String INTEGRATION_AWS_SNS = "aws_sns";

	private static final String INTEGRATION_PUSH_TOKEN = "token";

	private void migrateDevice() {
		// Device, Device Custom Data, Integration Config
		Device device = DeviceManager.generateNewDevice(context);
		String deviceDataString = prefs.getString(Constants.PREF_KEY_DEVICE_DATA, null);
		if (deviceDataString != null) {
			try {
				com.apptentive.android.sdk.model.CustomData customDataOld = new com.apptentive.android.sdk.model.CustomData(deviceDataString);
				CustomData customData = CustomData.fromJson(customDataOld);
				device.setCustomData(customData);
			} catch (Exception e) {
				ApptentiveLog.e("Error migrating Device Custom Data.", e);
			}
		}

		String integrationConfigString = prefs.getString(Constants.PREF_KEY_DEVICE_INTEGRATION_CONFIG, null);
		if (integrationConfigString != null) {
			try {
				com.apptentive.android.sdk.model.CustomData integrationConfigOld = new com.apptentive.android.sdk.model.CustomData(integrationConfigString);
				IntegrationConfig integrationConfig = new IntegrationConfig();
				Iterator it = integrationConfigOld.keys();
				while (it.hasNext()) {
					String key = (String) it.next();
					IntegrationConfigItem item = new IntegrationConfigItem();
					switch (key ) {
						case INTEGRATION_APPTENTIVE_PUSH:
							item.put(INTEGRATION_PUSH_TOKEN, integrationConfigOld.get(key));
							integrationConfig.setApptentive(item);
							break;
						case INTEGRATION_PARSE:
							item.put(INTEGRATION_PUSH_TOKEN, integrationConfigOld.get(key));
							integrationConfig.setParse(item);
							break;
						case INTEGRATION_URBAN_AIRSHIP:
							item.put(INTEGRATION_PUSH_TOKEN, integrationConfigOld.get(key));
							integrationConfig.setUrbanAirship(item);
							break;
						case INTEGRATION_AWS_SNS:
							item.put(INTEGRATION_PUSH_TOKEN, integrationConfigOld.get(key));
							integrationConfig.setAmazonAwsSns(item);
							break;
					}
				}
				device.setIntegrationConfig(integrationConfig);
			} catch (Exception e) {
				ApptentiveLog.e("Error migrating Device Integration Config.", e);
			}
		}

		conversation.setDevice(device);
	}

	private void migrateSdk() {
		String sdkString = prefs.getString(Constants.PREF_KEY_SDK, null);
		if (sdkString != null) {
			try {
				com.apptentive.android.sdk.migration.v4_0_0.Sdk sdkOld = new com.apptentive.android.sdk.migration.v4_0_0.Sdk(sdkString);
				Sdk sdk = new Sdk();
				sdk.setVersion(sdkOld.getVersion());
				sdk.setDistribution(sdkOld.getDistribution());
				sdk.setDistributionVersion(sdkOld.getDistributionVersion());
				sdk.setPlatform(sdkOld.getPlatform());
				sdk.setProgrammingLanguage(sdkOld.getProgrammingLanguage());
				sdk.setAuthorName(sdkOld.getAuthorName());
				sdk.setAuthorEmail(sdkOld.getAuthorEmail());
				conversation.setSdk(sdk);
			}catch (Exception  e) {
				ApptentiveLog.e("Error migrating Sdk.", e);
			}
		}
	}
}
