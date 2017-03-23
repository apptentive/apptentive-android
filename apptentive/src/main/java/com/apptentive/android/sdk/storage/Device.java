/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.text.TextUtils;

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
	private IntegrationConfig integrationConfig;

	private transient DataChangedListener listener;

	public Device() {
		customData = new CustomData();
		integrationConfig = new IntegrationConfig();
	}

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
		customData.setDataChangedListener(this);
		integrationConfig.setDataChangedListener(this);
	}

	@Override
	public void notifyDataChanged() {
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	@Override
	public void onDataChanged() {
		notifyDataChanged();
	}

	//region Getters & Setters

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		if (!TextUtils.equals(this.uuid, uuid)) {
			this.uuid = uuid;
			notifyDataChanged();
		}
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		if (!TextUtils.equals(this.osName, osName)) {
			this.osName = osName;
			notifyDataChanged();
		}
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		if (!TextUtils.equals(this.osVersion, osVersion)) {
			this.osVersion = osVersion;
			notifyDataChanged();
		}
	}

	public String getOsBuild() {
		return osBuild;
	}

	public void setOsBuild(String osBuild) {
		if (!TextUtils.equals(this.osBuild, osBuild)) {
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
		if (!TextUtils.equals(this.manufacturer, manufacturer)) {
			this.manufacturer = manufacturer;
			notifyDataChanged();
		}
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		if (!TextUtils.equals(this.model, model)) {
			this.model = model;
			notifyDataChanged();
		}
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		if (!TextUtils.equals(this.board, board)) {
			this.board = board;
			notifyDataChanged();
		}
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		if (!TextUtils.equals(this.product, product)) {
			this.product = product;
			notifyDataChanged();
		}
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		if (!TextUtils.equals(this.brand, brand)) {
			this.brand = brand;
			notifyDataChanged();
		}
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		if (!TextUtils.equals(this.cpu, cpu)) {
			this.cpu = cpu;
			notifyDataChanged();
		}
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		if (!TextUtils.equals(this.device, device)) {
			this.device = device;
			notifyDataChanged();
		}
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		if (!TextUtils.equals(this.carrier, carrier)) {
			this.carrier = carrier;
			notifyDataChanged();
		}
	}

	public String getCurrentCarrier() {
		return currentCarrier;
	}

	public void setCurrentCarrier(String currentCarrier) {
		if (!TextUtils.equals(this.currentCarrier, currentCarrier)) {
			this.currentCarrier = currentCarrier;
			notifyDataChanged();
		}
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		if (!TextUtils.equals(this.networkType, networkType)) {
			this.networkType = networkType;
			notifyDataChanged();
		}
	}

	public String getBuildType() {
		return buildType;
	}

	public void setBuildType(String buildType) {
		if (!TextUtils.equals(this.buildType, buildType)) {
			this.buildType = buildType;
			notifyDataChanged();
		}
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		if (!TextUtils.equals(this.buildId, buildId)) {
			this.buildId = buildId;
			notifyDataChanged();
		}
	}

	public String getBootloaderVersion() {
		return bootloaderVersion;
	}

	public void setBootloaderVersion(String bootloaderVersion) {
		if (!TextUtils.equals(this.bootloaderVersion, bootloaderVersion)) {
			this.bootloaderVersion = bootloaderVersion;
			notifyDataChanged();
		}
	}

	public String getRadioVersion() {
		return radioVersion;
	}

	public void setRadioVersion(String radioVersion) {
		if (!TextUtils.equals(this.radioVersion, radioVersion)) {
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
		if (!TextUtils.equals(this.localeCountryCode, localeCountryCode)) {
			this.localeCountryCode = localeCountryCode;
			notifyDataChanged();
		}
	}

	public String getLocaleLanguageCode() {
		return localeLanguageCode;
	}

	public void setLocaleLanguageCode(String localeLanguageCode) {
		if (!TextUtils.equals(this.localeLanguageCode, localeLanguageCode)) {
			this.localeLanguageCode = localeLanguageCode;
			notifyDataChanged();
		}
	}

	public String getLocaleRaw() {
		return localeRaw;
	}

	public void setLocaleRaw(String localeRaw) {
		if (!TextUtils.equals(this.localeRaw, localeRaw)) {
			this.localeRaw = localeRaw;
			notifyDataChanged();
		}
	}

	public String getUtcOffset() {
		return utcOffset;
	}

	public void setUtcOffset(String utcOffset) {
		if (!TextUtils.equals(this.utcOffset, utcOffset)) {
			this.utcOffset = utcOffset;
			notifyDataChanged();
		}
	}

	public IntegrationConfig getIntegrationConfig() {
		return integrationConfig;
	}

	public void setIntegrationConfig(IntegrationConfig integrationConfig) {
		this.integrationConfig = integrationConfig;
		this.integrationConfig.setDataChangedListener(this);
		notifyDataChanged();
	}

//endregion

}
