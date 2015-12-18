/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;

import com.apptentive.android.sdk.Apptentive;
import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.model.CodePointStore;
import com.apptentive.android.sdk.model.CustomData;
import com.apptentive.android.sdk.model.Device;
import com.apptentive.android.sdk.model.Person;
import com.apptentive.android.sdk.storage.DeviceManager;
import com.apptentive.android.sdk.storage.PersonManager;
import com.apptentive.android.sdk.storage.VersionHistoryStore;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import java.math.BigDecimal;

/**
 * @author Sky Kelsey
 */
public class FieldManager {

	public static boolean exists(Context context, String query) {
		return getValue(context, query) != null;
	}

	public static Comparable getValue(Context context, String query) {
		Object rawValue = doGetValue(context, query);
		return (Comparable) ClauseParser.parseValue(rawValue);
	}

	public static Object doGetValue(Context context, String query) {
		query = query.trim();
		String[] tokens = query.split("/");
		QueryPart topLevelQuery = QueryPart.parse(tokens[0]);

		switch (topLevelQuery) {
			case application: {
				QueryPart applicationQuery = QueryPart.parse(tokens[1]);
				switch (applicationQuery) {
					case version:
						int version = Util.getAppVersionCode(context);
						if (version == -1) {
							version = 0; // Default
						}
						return new Apptentive.Version(version);
				}
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
						return VersionHistoryStore.isUpdate(context, VersionHistoryStore.Selector.build);
					default:
						break;
				}
				return false;
			}
			case time_at_install: {
				QueryPart subQuery = QueryPart.parse(tokens[1]);
				switch (subQuery) {
					case total:
						return VersionHistoryStore.getTimeAtInstall(context, VersionHistoryStore.Selector.total);
					case version:
						return VersionHistoryStore.getTimeAtInstall(context, VersionHistoryStore.Selector.build);
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
								return new BigDecimal(CodePointStore.getTotalInvokes(context, isInteraction, name));
							case version:
								String appVersion = String.valueOf(Util.getAppVersionCode(context));
								return new BigDecimal(CodePointStore.getBuildInvokes(context, isInteraction, name, appVersion));
							default:
								break;
						}
					case last_invoked_at:
						QueryPart queryPart3 = QueryPart.parse(tokens[3]);
						switch (queryPart3) {
							case total:
								Double lastInvoke = CodePointStore.getLastInvoke(context, isInteraction, name);
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
				Person person = PersonManager.getStoredPerson(context);
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
				Device device = DeviceManager.getStoredDevice(context);
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
					case os_version:
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
		product,
		radio_version,
		uuid,


		last_invoked_at,
		invokes,
		total,
		version,
		build,
		time_ago,
		other;

		public static QueryPart parse(String name) {
			if (name != null) {
				name = name.trim();
				try {
					return QueryPart.valueOf(name);
				} catch (IllegalArgumentException e) {
					Log.d(String.format("Unrecognized QueryPart: \"%s\". Defaulting to \"unknown\"", name), e);
				}
			}
			return other;
		}
	}
}
