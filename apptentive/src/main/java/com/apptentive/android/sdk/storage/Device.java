/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.util.StringUtils;

public class Device implements Saveable, DataChangedListener {

	private static final long serialVersionUID = 1L;

	private String uuid;
	private String osName;
	private String osVersion;
	private String osBuild;
	private int osApiLevel;
	private String manufacturer;
	private String model;
	private String board;
	private String product;
	private String brand;
	private String cpu;
	private String device;
	private String carrier;
	private String currentCarrier;
	private String networkType;
	private String buildType;
	private String buildId;
	private String bootloaderVersion;
	private String radioVersion;
	private CustomData customData;
	private String localeCountryCode;
	private String localeLanguageCode;
	private String localeRaw;
	private String utcOffset;
	private String advertiserId;
	private IntegrationConfig integrationConfig;

	private transient DataChangedListener listener;
	private transient DeviceDataChangedListener deviceDataChangedListener;

	public Device() {
		customData = new CustomData();
		integrationConfig = new IntegrationConfig();
	}

	public void setDeviceDataChangedListener(DeviceDataChangedListener deviceDataChangedListener) {
		this.deviceDataChangedListener = deviceDataChangedListener;
	}

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
		customData.setDataChangedListener(this);
		integrationConfig.setDataChangedListener(this);
	}

	@Override
	public void notifyDataChanged() {
		if (deviceDataChangedListener != null) {
			deviceDataChangedListener.onDeviceDataChanged();
		}
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	@Override
	public void onDataChanged() {
		notifyDataChanged();
	}

	// TODO: unit tests
	public Device clone() {
		Device clone = new Device();
		clone.uuid = uuid;
		clone.osName = osName;
		clone.osVersion = osVersion;
		clone.osBuild = osBuild;
		clone.osApiLevel = osApiLevel;
		clone.manufacturer = manufacturer;
		clone.model = model;
		clone.board = board;
		clone.product = product;
		clone.brand = brand;
		clone.cpu = cpu;
		clone.device = device;
		clone.carrier = carrier;
		clone.currentCarrier = currentCarrier;
		clone.networkType = networkType;
		clone.buildType = buildType;
		clone.buildId = buildId;
		clone.bootloaderVersion = bootloaderVersion;
		clone.radioVersion = radioVersion;
		if (customData != null) {
			clone.customData.putAll(customData);
		}
		clone.localeCountryCode = localeCountryCode;
		clone.localeLanguageCode = localeLanguageCode;
		clone.localeRaw = localeRaw;
		clone.utcOffset = utcOffset;
		clone.advertiserId = advertiserId;
		if (integrationConfig != null) {
			clone.integrationConfig = integrationConfig.clone();
		}
		clone.listener = listener;
		clone.deviceDataChangedListener = deviceDataChangedListener;
		return clone;
	}

	//region Getters & Setters

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		if (!StringUtils.equal(this.uuid, uuid)) {
			this.uuid = uuid;
			notifyDataChanged();
		}
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		if (!StringUtils.equal(this.osName, osName)) {
			this.osName = osName;
			notifyDataChanged();
		}
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		if (!StringUtils.equal(this.osVersion, osVersion)) {
			this.osVersion = osVersion;
			notifyDataChanged();
		}
	}

	public String getOsBuild() {
		return osBuild;
	}

	public void setOsBuild(String osBuild) {
		if (!StringUtils.equal(this.osBuild, osBuild)) {
			this.osBuild = osBuild;
			notifyDataChanged();
		}
	}

	public int getOsApiLevel() {
		return osApiLevel;
	}

	public void setOsApiLevel(int osApiLevel) {
		if (this.osApiLevel != osApiLevel) {
			this.osApiLevel = osApiLevel;
			notifyDataChanged();
		}
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		if (!StringUtils.equal(this.manufacturer, manufacturer)) {
			this.manufacturer = manufacturer;
			notifyDataChanged();
		}
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		if (!StringUtils.equal(this.model, model)) {
			this.model = model;
			notifyDataChanged();
		}
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		if (!StringUtils.equal(this.board, board)) {
			this.board = board;
			notifyDataChanged();
		}
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		if (!StringUtils.equal(this.product, product)) {
			this.product = product;
			notifyDataChanged();
		}
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		if (!StringUtils.equal(this.brand, brand)) {
			this.brand = brand;
			notifyDataChanged();
		}
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		if (!StringUtils.equal(this.cpu, cpu)) {
			this.cpu = cpu;
			notifyDataChanged();
		}
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		if (!StringUtils.equal(this.device, device)) {
			this.device = device;
			notifyDataChanged();
		}
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		if (!StringUtils.equal(this.carrier, carrier)) {
			this.carrier = carrier;
			notifyDataChanged();
		}
	}

	public String getCurrentCarrier() {
		return currentCarrier;
	}

	public void setCurrentCarrier(String currentCarrier) {
		if (!StringUtils.equal(this.currentCarrier, currentCarrier)) {
			this.currentCarrier = currentCarrier;
			notifyDataChanged();
		}
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		if (!StringUtils.equal(this.networkType, networkType)) {
			this.networkType = networkType;
			notifyDataChanged();
		}
	}

	public String getBuildType() {
		return buildType;
	}

	public void setBuildType(String buildType) {
		if (!StringUtils.equal(this.buildType, buildType)) {
			this.buildType = buildType;
			notifyDataChanged();
		}
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		if (!StringUtils.equal(this.buildId, buildId)) {
			this.buildId = buildId;
			notifyDataChanged();
		}
	}

	public String getBootloaderVersion() {
		return bootloaderVersion;
	}

	public void setBootloaderVersion(String bootloaderVersion) {
		if (!StringUtils.equal(this.bootloaderVersion, bootloaderVersion)) {
			this.bootloaderVersion = bootloaderVersion;
			notifyDataChanged();
		}
	}

	public String getRadioVersion() {
		return radioVersion;
	}

	public void setRadioVersion(String radioVersion) {
		if (!StringUtils.equal(this.radioVersion, radioVersion)) {
			this.radioVersion = radioVersion;
			notifyDataChanged();
		}
	}

	public CustomData getCustomData() {
		return customData;
	}

	public void setCustomData(CustomData customData) {
		this.customData = customData;
		this.customData.setDataChangedListener(this);
		notifyDataChanged();
	}

	public String getLocaleCountryCode() {
		return localeCountryCode;
	}

	public void setLocaleCountryCode(String localeCountryCode) {
		if (!StringUtils.equal(this.localeCountryCode, localeCountryCode)) {
			this.localeCountryCode = localeCountryCode;
			notifyDataChanged();
		}
	}

	public String getLocaleLanguageCode() {
		return localeLanguageCode;
	}

	public void setLocaleLanguageCode(String localeLanguageCode) {
		if (!StringUtils.equal(this.localeLanguageCode, localeLanguageCode)) {
			this.localeLanguageCode = localeLanguageCode;
			notifyDataChanged();
		}
	}

	public String getLocaleRaw() {
		return localeRaw;
	}

	public void setLocaleRaw(String localeRaw) {
		if (!StringUtils.equal(this.localeRaw, localeRaw)) {
			this.localeRaw = localeRaw;
			notifyDataChanged();
		}
	}

	public String getUtcOffset() {
		return utcOffset;
	}

	public void setUtcOffset(String utcOffset) {
		if (!StringUtils.equal(this.utcOffset, utcOffset)) {
			this.utcOffset = utcOffset;
			notifyDataChanged();
		}
	}

	public String getAdvertiserId() {
		return advertiserId;
	}

	public void setAdvertiserId(String advertiserId) {
		if (!StringUtils.equal(this.advertiserId, advertiserId)) {
			this.advertiserId = advertiserId;
			notifyDataChanged();
		}
	}

	public IntegrationConfig getIntegrationConfig() {
		return integrationConfig;
	}

	public void setIntegrationConfig(IntegrationConfig integrationConfig) {
		if (integrationConfig == null) {
			throw new IllegalArgumentException("Integration config is null");
		}
		this.integrationConfig = integrationConfig;
		this.integrationConfig.setDataChangedListener(this);
		notifyDataChanged();
	}

	//endregion

}
