/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.CustomData;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.IntegrationConfig;
import com.apptentive.android.sdk.storage.IntegrationConfigItem;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONObject;

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
		migrateAppRelease();
		migratePerson();
	}

	private static final String INTEGRATION_APPTENTIVE_PUSH = "apptentive_push";
	private static final String INTEGRATION_PARSE = "parse";
	private static final String INTEGRATION_URBAN_AIRSHIP = "urban_airship";
	private static final String INTEGRATION_AWS_SNS = "aws_sns";

	private static final String INTEGRATION_PUSH_TOKEN = "token";

	private void migrateDevice() {
		try {
			String deviceString = prefs.getString(Constants.PREF_KEY_DEVICE, null);
			if (deviceString != null) {
				com.apptentive.android.sdk.migration.v4_0_0.Device deviceOld = new com.apptentive.android.sdk.migration.v4_0_0.Device(deviceString);
				Device device = new Device();

				device.setUuid(deviceOld.getUuid());
				device.setOsName(deviceOld.getOsName());
				device.setOsVersion(deviceOld.getOsVersion());
				device.setOsBuild(deviceOld.getOsBuild());
				String osApiLevel = deviceOld.getOsApiLevel();
				if (!StringUtils.isNullOrEmpty(osApiLevel)) {
					device.setOsApiLevel(Integer.parseInt(osApiLevel));
				}
				device.setManufacturer(deviceOld.getManufacturer());
				device.setModel(deviceOld.getModel());
				device.setBoard(deviceOld.getBoard());
				device.setProduct(deviceOld.getProduct());
				device.setBrand(deviceOld.getBrand());
				device.setCpu(deviceOld.getCpu());
				device.setDevice(deviceOld.getDevice());
				device.setCarrier(deviceOld.getCarrier());
				device.setCurrentCarrier(deviceOld.getCurrentCarrier());
				device.setNetworkType(deviceOld.getNetworkType());
				device.setBuildType(deviceOld.getBuildType());
				device.setBuildId(deviceOld.getBuildId());
				device.setBootloaderVersion(deviceOld.getBootloaderVersion());
				device.setRadioVersion(deviceOld.getRadioVersion());
				device.setLocaleCountryCode(deviceOld.getLocaleCountryCode());
				device.setLocaleLanguageCode(deviceOld.getLocaleLanguageCode());
				device.setLocaleRaw(deviceOld.getLocaleRaw());
				device.setUtcOffset(deviceOld.getUtcOffset());

				JSONObject customDataOld = deviceOld.getCustomData();
				if (customDataOld != null) {
					CustomData customData = new CustomData();
					Iterator it = customDataOld.keys();
					while (it.hasNext()) {
						String key = (String) it.next();
						customData.put(key, customDataOld.get(key));
					}
					device.setCustomData(customData);
				}

				JSONObject integrationConfigOld = deviceOld.getIntegrationConfig();
				if (integrationConfigOld != null) {
					IntegrationConfig integrationConfig = new IntegrationConfig();
					Iterator it = integrationConfigOld.keys();
					while (it.hasNext()) {
						String key = (String) it.next();
						IntegrationConfigItem item = new IntegrationConfigItem();
						switch (key) {
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
				}
				conversation.setDevice(device);
			}
		} catch (Exception e) {
			ApptentiveLog.e("Error migrating Device.", e);
		}
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
			} catch (Exception e) {
				ApptentiveLog.e("Error migrating Sdk.", e);
			}
		}
	}

	private void migrateAppRelease() {
		String appReleaseString = prefs.getString(Constants.PREF_KEY_APP_RELEASE, null);
		if (appReleaseString != null) {
			try {
				com.apptentive.android.sdk.migration.v4_0_0.AppRelease appReleaseOld = new com.apptentive.android.sdk.migration.v4_0_0.AppRelease(appReleaseString);
				AppRelease appRelease = new AppRelease();
				appRelease.setAppStore(appReleaseOld.getAppStore());
				appRelease.setDebug(appReleaseOld.getDebug());
				appRelease.setIdentifier(appReleaseOld.getIdentifier());
				appRelease.setInheritStyle(appReleaseOld.getInheritStyle());
				appRelease.setOverrideStyle(appReleaseOld.getOverrideStyle());
				appRelease.setTargetSdkVersion(appReleaseOld.getTargetSdkVersion());
				appRelease.setType(appReleaseOld.getType());
				appRelease.setVersionCode(appReleaseOld.getVersionCode());
				appRelease.setVersionName(appReleaseOld.getVersionName());
				conversation.setAppRelease(appRelease);
			} catch (Exception e) {
				ApptentiveLog.e("Error migrating AppRelease.", e);
			}
		}
	}

	private void migratePerson() {
		String personString = prefs.getString(Constants.PREF_KEY_PERSON, null);

		if (personString != null) {
			try {
				Person person = new Person();
				com.apptentive.android.sdk.migration.v4_0_0.Person personOld = new com.apptentive.android.sdk.migration.v4_0_0.Person(personString);
				person.setEmail(personOld.getEmail());
				person.setBirthday(personOld.getBirthday());
				person.setCity(personOld.getCity());
				person.setCountry(personOld.getCountry());
				person.setFacebookId(personOld.getFacebookId());
				person.setId(personOld.getId());
				person.setName(personOld.getName());
				person.setPhoneNumber(personOld.getPhoneNumber());
				person.setStreet(personOld.getStreet());
				person.setZip(personOld.getZip());

				JSONObject customDataOld = personOld.getCustomData();
				if (customDataOld != null) {
					CustomData customData = new CustomData();
					Iterator it = customDataOld.keys();
					while (it.hasNext()) {
						String key = (String) it.next();
						customData.put(key, customDataOld.get(key));
					}
					person.setCustomData(customData);
				}
				conversation.setPerson(person);
			} catch (Exception e) {
				ApptentiveLog.e("Error migrating Person.", e);
			}
		}
	}
}
