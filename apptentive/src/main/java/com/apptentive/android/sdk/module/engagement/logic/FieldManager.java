/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.ApptentiveInternal;
import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.BuildConfig;
import com.apptentive.android.sdk.model.CustomData;
import com.apptentive.android.sdk.model.Device;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.math.BigDecimal;

public class FieldManager {

	public static boolean exists(String query) {
		return getValue(query) != null;
	}

	public static Comparable getValue(String query) {
		Object rawValue = doGetValue(query);
		return (Comparable) ClauseParser.parseValue(rawValue);
	}

	public static Object doGetValue(String query) {
		query = query.trim();
		String[] tokens = query.split("/");
		QueryPart topLevelQuery = QueryPart.parse(tokens[0]);

		switch (topLevelQuery) {
			case application: {
				QueryPart applicationQuery = QueryPart.parse(tokens[1]);
				switch (applicationQuery) {
					case version: {
						int version = Util.getAppVersionCode(ApptentiveInternal.getInstance().getApplicationContext());
						if (version == -1) {
							version = 0; // Default
						}
						return new Apptentive.Version(version);
					}
					case version_code: {
						int version = Util.getAppVersionCode(ApptentiveInternal.getInstance().getApplicationContext());
						if (version == -1) {
							version = 0; // Default
						}
						return version;
					}
					case version_name: {
						String version = Util.getAppVersionName(ApptentiveInternal.getInstance().getApplicationContext());
						if (version == null) {
							version = "0"; // Default
						}
						Apptentive.Version ret = new Apptentive.Version();
						ret.setVersion(version);
						return ret;
					}
					case debug: {
						return BuildConfig.DEBUG;
					}
				}
				return null; // Default value
			}
			case sdk: {
				QueryPart sdkQuery = QueryPart.parse(tokens[1]);
				switch (sdkQuery) {
					case version:
						Apptentive.Version ret = new Apptentive.Version();
						ret.setVersion(Constants.APPTENTIVE_SDK_VERSION);
						return ret;
				}
			}
			case current_time:
				return new Apptentive.DateTime(Util.currentTimeSeconds());
			case is_update: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				switch (subQuery) {
					case version:
					case version_code:
						return VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_code);
					case version_name:
						return VersionHistoryStore.isUpdate(VersionHistoryStore.Selector.version_name);
					default:
						break;
				}
				return false;
			}
			case time_at_install: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				switch (subQuery) {
					case total:
						return VersionHistoryStore.getTimeAtInstall(VersionHistoryStore.Selector.total);
					case version:
					case version_code:
						return VersionHistoryStore.getTimeAtInstall(VersionHistoryStore.Selector.version_code);
					case version_name:
						return VersionHistoryStore.getTimeAtInstall(VersionHistoryStore.Selector.version_name);
				}
				return new Apptentive.DateTime(Util.currentTimeSeconds());
			}
			case interactions:
			case code_point: {
				boolean isInteraction = topLevelQuery.equals(QueryPart.interactions);
				String name = tokens[1];
				QueryPart queryPart1 = QueryPart.parse(tokens[2]);

				switch (queryPart1) {
					case invokes:
						QueryPart queryPart2 = QueryPart.parse(tokens[3]);
						switch (queryPart2) {
							case total: // Get total for all versions of the app.
								return new BigDecimal(ApptentiveInternal.getInstance().getCodePointStore().getTotalInvokes(isInteraction, name));
							case version:
								String appVersion = String.valueOf(Util.getAppVersionCode(ApptentiveInternal.getInstance().getApplicationContext()));
								return new BigDecimal(ApptentiveInternal.getInstance().getCodePointStore().getVersionCodeInvokes(isInteraction, name, appVersion));
							default:
								break;
						}
					case last_invoked_at:
						QueryPart queryPart3 = QueryPart.parse(tokens[3]);
						switch (queryPart3) {
							case total:
								Double lastInvoke = ApptentiveInternal.getInstance().getCodePointStore().getLastInvoke(isInteraction, name);
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
				Person person = PersonManager.getStoredPerson();
				if (person == null) {
					return null;
				}
				switch (subQuery) {
					case custom_data:
						String customDataKey = tokens[2].trim();
						CustomData customData = person.getCustomData();
						if (customData != null) {
							return customData.opt(customDataKey);
						}
						break;
					case name:
						return person.getEmail();
					case email:
						return person.getEmail();
					case other:
						String key = tokens[1];
						return person.opt(key);
				}
			}
			case device: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				Device device = DeviceManager.getStoredDevice();
				if (device == null) {
					return null;
				}
				switch (subQuery) {
					case custom_data:
						String customDataKey = tokens[2].trim();
						CustomData customData = device.getCustomData();
						if (customData != null) {
							return customData.opt(customDataKey);
						}
						break;
					case os_version:
						String osVersion = device.optString(subQuery.name(), "0");
						Apptentive.Version ret = new Apptentive.Version();
						ret.setVersion(osVersion);
						return ret;
					case os_api_level:
						return device.optInt(subQuery.name(), 0);
					case board:
					case bootloader_version:
					case brand:
					case build_id:
					case build_type:
					case carrier:
					case cpu:
					case current_carrier:
					case device:
					case hardware:
					case locale_country_code:
					case locale_language_code:
					case locale_raw:
					case manufacturer:
					case model:
					case network_type:
					case os_name:
					case os_build:
					case product:
					case radio_version:
					case uuid:
					case other:
						return device.opt(subQuery.name());
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
		other;

		public static QueryPart parse(String name) {
			if (name != null) {
				name = name.trim();
				try {
					return QueryPart.valueOf(name);
				} catch (IllegalArgumentException e) {
					ApptentiveLog.d(String.format("Unrecognized QueryPart: \"%s\". Defaulting to \"unknown\"", name), e);
				}
			}
			return other;
		}
	}
}
