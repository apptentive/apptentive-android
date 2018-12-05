/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.util.StringUtils;

import org.json.JSONException;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class DevicePayload extends JsonPayload {

	public static final String KEY = "device";

	private static final String KEY_UUID = "uuid";
	private static final String KEY_OS_NAME = "os_name";
	private static final String KEY_OS_VERSION = "os_version";
	private static final String KEY_OS_BUILD = "os_build";
	private static final String KEY_OS_API_LEVEL = "os_api_level";
	private static final String KEY_MANUFACTURER = "manufacturer";
	private static final String KEY_MODEL = "model";
	private static final String KEY_BOARD = "board";
	private static final String KEY_PRODUCT = "product";
	private static final String KEY_BRAND = "brand";
	private static final String KEY_CPU = "cpu";
	private static final String KEY_DEVICE = "device"; //
	private static final String KEY_CARRIER = "carrier";
	private static final String KEY_CURRENT_CARRIER = "current_carrier";
	private static final String KEY_NETWORK_TYPE = "network_type";
	private static final String KEY_BUILD_TYPE = "build_type";
	private static final String KEY_BUILD_ID = "build_id";
	private static final String KEY_BOOTLOADER_VERSION = "bootloader_version";
	private static final String KEY_RADIO_VERSION = "radio_version";
	@SensitiveDataKey private static final String KEY_CUSTOM_DATA = "custom_data";
	private static final String KEY_LOCALE_COUNTRY_CODE = "locale_country_code";
	private static final String KEY_LOCALE_LANGUAGE_CODE = "locale_language_code";
	private static final String KEY_LOCALE_RAW = "locale_raw";
	private static final String KEY_UTC_OFFSET = "utc_offset";
	@SensitiveDataKey private static final String KEY_ADVERTISER_ID = "advertiser_id";
	private static final String KEY_INTEGRATION_CONFIG = "integration_config";

	static {
		registerSensitiveKeys(DevicePayload.class);
	}

	public DevicePayload() {
		super(PayloadType.device);
	}

	public DevicePayload(String json) throws JSONException {
		super(PayloadType.device, json);
	}

	//region Http-request

	@Override
	public String getHttpEndPoint(String conversationId) {
		return StringUtils.format("/conversations/%s/device", conversationId);
	}

	//endregion

	public void setUuid(String uuid) {
		put(KEY_UUID, uuid);
	}

	public void setOsName(String osName) {
		put(KEY_OS_NAME, osName);
	}

	public void setOsVersion(String osVersion) {
		put(KEY_OS_VERSION, osVersion);
	}

	public void setOsBuild(String osBuild) {
		put(KEY_OS_BUILD, osBuild);
	}

	public void setOsApiLevel(String osApiLevel) {
		put(KEY_OS_API_LEVEL, osApiLevel);
	}

	public void setManufacturer(String manufacturer) {
		put(KEY_MANUFACTURER, manufacturer);
	}

	public String getModel() {
		return optString(KEY_MODEL, null);
	}

	public void setModel(String model) {
		put(KEY_MODEL, model);
	}

	public void setBoard(String board) {
		put(KEY_BOARD, board);
	}

	public void setProduct(String product) {
		put(KEY_PRODUCT, product);
	}

	public void setBrand(String brand) {
		put(KEY_BRAND, brand);
	}

	public void setCpu(String cpu) {
		put(KEY_CPU, cpu);
	}

	public String getDevice() {
		return optString(KEY_DEVICE, null);
	}

	public void setDevice(String device) {
		put(KEY_DEVICE, device);
	}

	public void setCarrier(String carrier) {
		put(KEY_CARRIER, carrier);
	}

	public void setCurrentCarrier(String currentCarrier) {
		put(KEY_CURRENT_CARRIER, currentCarrier);
	}

	public void setNetworkType(String networkType) {
		put(KEY_NETWORK_TYPE, networkType);
	}

	public void setBuildType(String buildType) {
		put(KEY_BUILD_TYPE, buildType);
	}

	public void setBuildId(String buildId) {
		put(KEY_BUILD_ID, buildId);
	}

	public void setBootloaderVersion(String bootloaderVersion) {
		put(KEY_BOOTLOADER_VERSION, bootloaderVersion);
	}

	public void setRadioVersion(String radioVersion) {
		put(KEY_RADIO_VERSION, radioVersion);
	}

	@SuppressWarnings("unchecked") // We check it coming in.
	public CustomData getCustomData() {
		if (!isNull(KEY_CUSTOM_DATA)) {
			try {
				return new CustomData(getJSONObject(KEY_CUSTOM_DATA));
			} catch (JSONException e) {
				logException(e);
			}
		}
		return null;
	}

	public void setCustomData(CustomData customData) {
		put(KEY_CUSTOM_DATA, customData);
	}

	public void setIntegrationConfig(CustomData integrationConfig) {
		put(KEY_INTEGRATION_CONFIG, integrationConfig);
	}

	public void setLocaleCountryCode(String localeCountryCode) {
		put(KEY_LOCALE_COUNTRY_CODE, localeCountryCode);
	}

	public void setLocaleLanguageCode(String localeLanguageCode) {
		put(KEY_LOCALE_LANGUAGE_CODE, localeLanguageCode);
	}

	public void setLocaleRaw(String localeRaw) {
		put(KEY_LOCALE_RAW, localeRaw);
	}

	public void setUtcOffset(String utcOffset) {
		put(KEY_UTC_OFFSET, utcOffset);
	}

	public void setAdvertiserId(String advertiserId) {
		put(KEY_ADVERTISER_ID, advertiserId);
	}

	@Override
	protected String getJsonContainer() {
		return "device";
	}
}
