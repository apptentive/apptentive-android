/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.migration;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;
import com.apptentive.android.sdk.conversation.Conversation;
import com.apptentive.android.sdk.debug.ErrorMetrics;
import com.apptentive.android.sdk.migration.v4_0_0.CodePointStore;
import com.apptentive.android.sdk.migration.v4_0_0.VersionHistoryEntry;
import com.apptentive.android.sdk.migration.v4_0_0.VersionHistoryStore;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.CustomData;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.EventRecord;
import com.apptentive.android.sdk.storage.IntegrationConfig;
import com.apptentive.android.sdk.storage.IntegrationConfigItem;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.Sdk;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import static com.apptentive.android.sdk.ApptentiveLogTag.CONVERSATION;

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
		migrateMessageCenter();
		migrateVersionHistory();
		migrateEventData();
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
						Object value = customDataOld.get(key);
						if (value instanceof JSONObject) {
							customData.put(key, jsonObjectToSerializableType((JSONObject) value));
						} else {
							customData.put(key, (Serializable) value);
						}
					}
					device.setCustomData(customData);
				}

				JSONObject integrationConfigOld = deviceOld.getIntegrationConfig();
				if (integrationConfigOld != null) {
					IntegrationConfig integrationConfig = new IntegrationConfig();
					Iterator it = integrationConfigOld.keys();
					while (it.hasNext()) {
						String key = (String) it.next();
						IntegrationConfigItem item = new IntegrationConfigItem(integrationConfigOld);
						switch (key) {
							case INTEGRATION_APPTENTIVE_PUSH:
								integrationConfig.setApptentive(item);
								break;
							case INTEGRATION_PARSE:
								integrationConfig.setParse(item);
								break;
							case INTEGRATION_URBAN_AIRSHIP:
								integrationConfig.setUrbanAirship(item);
								break;
							case INTEGRATION_AWS_SNS:
								integrationConfig.setAmazonAwsSns(item);
								break;
						}
					}
					device.setIntegrationConfig(integrationConfig);
				}
				conversation.setDevice(device);
			}
		} catch (Exception e) {
			ApptentiveLog.e(CONVERSATION, e, "Error migrating Device.");
			logException(e);
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
				ApptentiveLog.e(CONVERSATION, e, "Error migrating Sdk.");
				logException(e);
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
				ApptentiveLog.e(CONVERSATION, e, "Error migrating AppRelease.");
				logException(e);
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
						Object value = customDataOld.get(key);
						if (value instanceof JSONObject) {
							customData.put(key, jsonObjectToSerializableType((JSONObject) value));
						} else {
							customData.put(key, (Serializable) value);
						}
					}
					person.setCustomData(customData);
				}
				conversation.setPerson(person);
			} catch (Exception e) {
				ApptentiveLog.e(CONVERSATION, e, "Error migrating Person.");
				logException(e);
			}
		}
	}

	private void migrateMessageCenter() {
		conversation.setMessageCenterFeatureUsed(prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_FEATURE_USED, false));
		conversation.setMessageCenterWhoCardPreviouslyDisplayed(prefs.getBoolean(Constants.PREF_KEY_MESSAGE_CENTER_WHO_CARD_DISPLAYED_BEFORE, false));
	}

	private void migrateVersionHistory() {
		// An existing static initializer will trigger the V1 to V2 migration of VersionHistory when VersionHistoryStore is loaded below.

		// Then migrate to V3, which is stored in the Conversation object.
		JSONArray versionHistoryOld = VersionHistoryStore.getBaseArray();
		try {
			if (versionHistoryOld != null && versionHistoryOld.length() > 0) {
				VersionHistory versionHistory = conversation.getVersionHistory();
				for (int i = 0; i < versionHistoryOld.length(); i++) {
					VersionHistoryEntry versionHistoryEntryOld = new VersionHistoryEntry((JSONObject) versionHistoryOld.get(i));
					versionHistory.updateVersionHistory(versionHistoryEntryOld.getTimestamp(), versionHistoryEntryOld.getVersionCode(), versionHistoryEntryOld.getVersionName());
				}
			}
		} catch (Exception e) {
			ApptentiveLog.w(CONVERSATION, e, "Error migrating VersionHistory entries V2 to V3.");
			logException(e);
		}
	}

	private void migrateEventData() {
		EventData eventData = conversation.getEventData();
		String codePointString = prefs.getString(Constants.PREF_KEY_CODE_POINT_STORE, null);
		try {
			CodePointStore codePointStore = new CodePointStore(codePointString);
			Map<String, EventRecord> migratedEvents = codePointStore.migrateCodePoints();
			Map<String, EventRecord> migratedInteractions = codePointStore.migrateInteractions();
			if (migratedEvents != null) {
				eventData.setEvents(migratedEvents);
			}
			if (migratedInteractions != null) {
				eventData.setInteractions(migratedInteractions);
			}
		} catch (Exception e) {
			ApptentiveLog.w(CONVERSATION, e, "Error migrating Event Data.");
			logException(e);
		}
	}

	/**
	 * Takes a legacy Apptentive Custom Data object base on JSON, and returns the modern serializable version
	 */
	private static Serializable jsonObjectToSerializableType(JSONObject input) {
		String type = input.optString(Apptentive.Version.KEY_TYPE, null);
		try {
			if (type != null) {
				if (type.equals(Apptentive.Version.TYPE)) {
					return new Apptentive.Version(input);
				} else if (type.equals(Apptentive.DateTime.TYPE)) {
					return new Apptentive.DateTime(input);
				}
			}
		} catch (JSONException e) {
			ApptentiveLog.e(CONVERSATION, e, "Error migrating JSONObject.");
			logException(e);
		}
		return null;
	}

	private static void logException(Exception e) {
		ErrorMetrics.logException(e); // TODO: add more context
	}
}
