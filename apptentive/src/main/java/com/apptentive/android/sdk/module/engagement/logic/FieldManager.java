/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.debug.Assert;
import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.CustomData;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.Util;

import java.math.BigDecimal;

import static com.apptentive.android.sdk.ApptentiveLogTag.INTERACTIONS;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class FieldManager {
	Context context;
	VersionHistory versionHistory;
	EventData eventData;
	Person person;
	Device device;
	AppRelease appRelease;
	private final RandomPercentProvider randomPercentProvider;

	public FieldManager(Context context, VersionHistory versionHistory, EventData eventData, Person person, Device device, AppRelease appRelease, RandomPercentProvider randomPercentProvider) {
		Assert.notNull(context);
		Assert.notNull(versionHistory);
		Assert.notNull(eventData);
		Assert.notNull(person);
		Assert.notNull(device);
		Assert.notNull(randomPercentProvider);
		this.context = context;
		this.versionHistory = versionHistory;
		this.eventData = eventData;
		this.person = person;
		this.device = device;
		this.appRelease = appRelease;
		this.randomPercentProvider = randomPercentProvider;
	}

	public boolean exists(String query) {
		return getValue(query) != null;
	}

	public Comparable getValue(String query) {
		Object rawValue = doGetValue(query);
		return (Comparable) ClauseParser.parseValue(rawValue);
	}

	private Object doGetValue(String query) {
		query = query.trim();
		String[] tokens = query.split("/");
		QueryPart topLevelQuery = QueryPart.parse(tokens[0]);

		switch (topLevelQuery) {
			case application: {
				QueryPart applicationQuery = QueryPart.parse(tokens[1]);
				switch (applicationQuery) {
					case version_code: {
						return appRelease.getVersionCode();
					}
					case version_name: {
						Apptentive.Version ret = new Apptentive.Version();
						ret.setVersion(appRelease.getVersionName());
						return ret;
					}
					case debug: {
						return appRelease.isDebug();
					}
				}
				return null; // Default value
			}
			case sdk: {
				QueryPart sdkQuery = QueryPart.parse(tokens[1]);
				switch (sdkQuery) {
					case version:
						Apptentive.Version ret = new Apptentive.Version();
						ret.setVersion(Constants.getApptentiveSdkVersion());
						return ret;
				}
			}
			case current_time:
				return new Apptentive.DateTime(Util.currentTimeSeconds());
			case is_update: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				switch (subQuery) {
					case version_code:
						return versionHistory.isUpdateForVersionCode();
					case version_name:
						return versionHistory.isUpdateForVersionName();
					default:
						break;
				}
				return false;
			}
			case time_at_install: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				switch (subQuery) {
					case total:
						return versionHistory.getTimeAtInstallTotal();
					case version_code:
						return versionHistory.getTimeAtInstallForVersionCode(RuntimeUtils.getAppVersionCode(context));
					case version_name:
						return versionHistory.getTimeAtInstallForVersionName(RuntimeUtils.getAppVersionName(context));
				}
				return new Apptentive.DateTime(Util.currentTimeSeconds());
			}
			case interactions: {
				String interactionId = tokens[1];
				QueryPart queryPart1 = QueryPart.parse(tokens[2]);
				switch (queryPart1) {
					case invokes:
						QueryPart queryPart2 = QueryPart.parse(tokens[3]);
						switch (queryPart2) {
							case total: // Get total for all versions of the app.
								return new BigDecimal(eventData.getInteractionCountTotal(interactionId));
							case version_code:
								Integer appVersionCode = RuntimeUtils.getAppVersionCode(context);
								return new BigDecimal(eventData.getInteractionCountForVersionCode(interactionId, appVersionCode));
							case version_name:
								String appVersionName = RuntimeUtils.getAppVersionName(context);
								return new BigDecimal(eventData.getInteractionCountForVersionName(interactionId, appVersionName));
							default:
								break;
						}
						break;
					case last_invoked_at:
						QueryPart queryPart3 = QueryPart.parse(tokens[3]);
						switch (queryPart3) {
							case total:
								Double lastInvoke = eventData.getTimeOfLastInteractionInvocation(interactionId);
								if (lastInvoke != null) {
									return new Apptentive.DateTime(lastInvoke);
								}
							default:
								break;
						}
					default:
						break;
				}
				break;
			}
			case code_point: {
				String eventLabel = tokens[1];
				QueryPart queryPart1 = QueryPart.parse(tokens[2]);
				switch (queryPart1) {
					case invokes:
						QueryPart queryPart2 = QueryPart.parse(tokens[3]);
						switch (queryPart2) {
							case total: // Get total for all versions of the app.
								return new BigDecimal(eventData.getEventCountTotal(eventLabel));
							case version_code:
								Integer appVersionCode = RuntimeUtils.getAppVersionCode(context);
								return new BigDecimal(eventData.getEventCountForVersionCode(eventLabel, appVersionCode));
							case version_name:
								String appVersionName = RuntimeUtils.getAppVersionName(context);
								return new BigDecimal(eventData.getEventCountForVersionName(eventLabel, appVersionName));
							default:
								break;
						}
						break;
					case last_invoked_at:
						QueryPart queryPart3 = QueryPart.parse(tokens[3]);
						switch (queryPart3) {
							case total:
								Double lastInvoke = eventData.getTimeOfLastEventInvocation(eventLabel);
								if (lastInvoke != null) {
									return new Apptentive.DateTime(lastInvoke);
								}
							default:
								break;
						}
					default:
						break;
				}
				return null; // Default Value
			}
			case person: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				if (person == null) {
					return null;
				}
				switch (subQuery) {
					case custom_data:
						String customDataKey = tokens[2].trim();
						CustomData customData = person.getCustomData();
						if (customData != null) {
							// We didn't trim the keys when they were added, so we need to iterate over them, trim them, then compare in order to get values.
							for (String key : customData.keySet()) {
								if (key.trim().equals(customDataKey)) {
									return customData.get(key);
								}
							}
						}
						break;
					case name:
						return person.getName();
					case email:
						return person.getEmail();
					case other:
						return null;
				}
				break;
			}
			case device: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				if (device == null) {
					return null;
				}
				switch (subQuery) {
					case custom_data:
						String customDataKey = tokens[2].trim();
						CustomData customData = device.getCustomData();
						if (customData != null) {
							// We didn't trim the keys when they were added, so we need to iterate over them, trim them, then compare in order to get values.
							for (String key : customData.keySet()) {
								if (key.trim().equals(customDataKey)) {
									return customData.get(key);
								}
							}
						}
						break;
					case os_version:
						String osVersion = device.getOsVersion();
						if (osVersion == null) {
							osVersion = "0";
						}
						Apptentive.Version ret = new Apptentive.Version();
						ret.setVersion(osVersion);
						return ret;
					case os_api_level:
						return device.getOsApiLevel();
					case board:
						return device.getBoard();
					case bootloader_version:
						return device.getBootloaderVersion();
					case brand:
						return device.getBrand();
					case build_id:
						return device.getBuildId();
					case build_type:
						return device.getBuildType();
					case carrier:
						return device.getCarrier();
					case cpu:
						return device.getCpu();
					case current_carrier:
						return device.getCurrentCarrier();
					case device:
						return device.getDevice();
					case hardware:
						return null; // What is this key?
					case locale_country_code:
						return device.getLocaleCountryCode();
					case locale_language_code:
						return device.getLocaleLanguageCode();
					case locale_raw:
						return device.getLocaleRaw();
					case manufacturer:
						return device.getManufacturer();
					case model:
						return device.getModel();
					case network_type:
						return device.getNetworkType();
					case os_name:
						return device.getOsName();
					case os_build:
						return device.getOsBuild();
					case product:
						return device.getProduct();
					case radio_version:
						return device.getRadioVersion();
					case uuid:
						return device.getUuid();
					case other:
						return null;
				}
				break;
			}
			case random: {
				if (tokens.length == 3) { // random/<key>/percent
					final String randomNumberKey = tokens[1];
					QueryPart subQuery = QueryPart.valueOf(tokens[2]);
					switch (subQuery) {
						case percent:
							return randomPercentProvider.getPercent(randomNumberKey);
					}
				} else if (tokens.length == 2) { // random/percent
					QueryPart subQuery = QueryPart.valueOf(tokens[1]);
					switch (subQuery) {
						case percent:
							return randomPercentProvider.getPercent(null);
					}
				}
			}
			default:
				break;
		}
		return null;
	}

	public String getDescription(String query) {

		query = query.trim();
		String[] tokens = query.split("/");
		QueryPart topLevelQuery = QueryPart.parse(tokens[0]);

		switch (topLevelQuery) {
			case application: {
				QueryPart applicationQuery = QueryPart.parse(tokens[1]);
				switch (applicationQuery) {
					case version_code: {
						return "app version code";
					}
					case version_name: {
						return "app version name";
					}
					case debug: {
						return "app debuggable";
					}
				}
				return null; // Default value
			}
			case sdk: {
				QueryPart sdkQuery = QueryPart.parse(tokens[1]);
				switch (sdkQuery) {
					case version:
						return "SDK version";
				}
			}
			case current_time:
				return "current time";
			case is_update: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				switch (subQuery) {
					case version_code:
						return "app version code changed";
					case version_name:
						return "app version name changed";
					default:
						break;
				}
				return null;
			}
			case time_at_install: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				switch (subQuery) {
					case total:
						return "time at install";
					case version_code:
						return StringUtils.format("time at install for version code '%d'", RuntimeUtils.getAppVersionCode(context));
					case version_name:
						return StringUtils.format("time at install for version name '%s'", RuntimeUtils.getAppVersionName(context));
				}
				return null;
			}
			case interactions: {
				String interactionId = tokens[1];
				QueryPart queryPart1 = QueryPart.parse(tokens[2]);
				switch (queryPart1) {
					case invokes:
						QueryPart queryPart2 = QueryPart.parse(tokens[3]);
						switch (queryPart2) {
							case total: // Get total for all versions of the app.
								return StringUtils.format("number of invokes for interaction '%s'", interactionId);
							case version_code:
								int appVersionCode = RuntimeUtils.getAppVersionCode(context);
								return StringUtils.format("number of invokes for interaction '%s' for version code '%d'", interactionId, appVersionCode);
							case version_name:
								String appVersionName = RuntimeUtils.getAppVersionName(context);
								return StringUtils.format("number of invokes for interaction '%s' for version name '%s'", interactionId, appVersionName);
							default:
								break;
						}
						break;
					case last_invoked_at:
						QueryPart queryPart3 = QueryPart.parse(tokens[3]);
						switch (queryPart3) {
							case total:
								return StringUtils.format("last time interaction '%s' was invoked", interactionId);
							default:
								break;
						}
					default:
						break;
				}
				break;
			}
			case code_point: {
				String eventLabel = tokens[1];
				QueryPart queryPart1 = QueryPart.parse(tokens[2]);
				switch (queryPart1) {
					case invokes:
						QueryPart queryPart2 = QueryPart.parse(tokens[3]);
						switch (queryPart2) {
							case total: // Get total for all versions of the app.
								return StringUtils.format("number of invokes for event '%s'", eventLabel);
							case version_code:
								int appVersionCode = RuntimeUtils.getAppVersionCode(context);
								return StringUtils.format("number of invokes for event '%s' for version code '%d'", eventLabel, appVersionCode);
							case version_name:
								String appVersionName = RuntimeUtils.getAppVersionName(context);
								return StringUtils.format("number of invokes for event '%s' for version name '%s'", eventLabel, appVersionName);
							default:
								break;
						}
						break;
					case last_invoked_at:
						QueryPart queryPart3 = QueryPart.parse(tokens[3]);
						switch (queryPart3) {
							case total:
								return StringUtils.format("last time event '%s' was invoked", eventLabel);
							default:
								break;
						}
					default:
						break;
				}
				return null; // Default Value
			}
			case person: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				if (person == null) {
					return null;
				}
				switch (subQuery) {
					case custom_data:
						String customDataKey = tokens[2].trim();
						return StringUtils.format("person_data['%s']", customDataKey);
					case name:
						return "person name";
					case email:
						return "person email";
					case other:
						return null;
				}
			}
			case device: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				if (device == null) {
					return null;
				}
				switch (subQuery) {
					case custom_data:
						String customDataKey = tokens[2].trim();
						return StringUtils.format("device_data['%s']", customDataKey);
					case os_version:
						return "device OS version";
					case os_api_level:
						return "device API level";
					case board:
						return "device board";
					case bootloader_version:
						return "device bootloader version";
					case brand:
						return "device brand";
					case build_id:
						return "device build id";
					case build_type:
						return "device build type";
					case carrier:
						return "device carrier";
					case cpu:
						return "device CPU";
					case current_carrier:
						return "device current carrier";
					case device:
						return "device";
					case hardware:
						return "device hardware";
					case locale_country_code:
						return "device country";
					case locale_language_code:
						return "device language";
					case locale_raw:
						return "device locale";
					case manufacturer:
						return "device manufacturer";
					case model:
						return "device model";
					case network_type:
						return "device network type";
					case os_name:
						return "device OS name";
					case os_build:
						return "device OS build";
					case product:
						return "device product";
					case radio_version:
						return "device radio version";
					case uuid:
						return "UUID";
					case other:
						return null;
				}
			}
			case random: {
				if (tokens.length == 3) { // random/<key>/percent
					final String randomNumberKey = tokens[1];
					QueryPart subQuery = QueryPart.valueOf(tokens[2]);
					switch (subQuery) {
						case percent:
							return StringUtils.format("random percent for key '%s'", randomNumberKey);
					}
				} else if (tokens.length == 2) { // random/percent
					QueryPart subQuery = QueryPart.valueOf(tokens[1]);
					switch (subQuery) {
						case percent:
							return StringUtils.format("random percent");
					}
				}
			}
			default:
				break;
		}
		return null;
	}

	private enum QueryPart {
		application,
		current_time,
		is_update,
		time_at_install,
		code_point,
		interactions,
		person,
		device,
		sdk,

		custom_data,
		name,
		email,

		board,
		bootloader_version,
		brand,
		build_id,
		build_type,
		carrier,
		cpu,
		current_carrier,
		hardware,
		locale_country_code,
		locale_language_code,
		locale_raw,
		manufacturer,
		model,
		network_type,
		os_name,
		os_version,
		os_build,
		os_api_level,
		product,
		radio_version,
		uuid,


		last_invoked_at,
		invokes,
		total,
		version,
		version_code,
		version_name,
		debug,
		build,
		time_ago,

		random,
		percent,

		other;

		public static QueryPart parse(String name) {
			if (name != null) {
				name = name.trim();
				try {
					return QueryPart.valueOf(name);
				} catch (IllegalArgumentException e) {
					ApptentiveLog.e(INTERACTIONS, "Unrecognized QueryPart: \"%s\". Defaulting to \"unknown\"", name);
					logException(e);
				}
			}
			return other;
		}
	}
}
