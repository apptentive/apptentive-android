/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import java.io.Serializable;

public class Device implements Serializable {

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

	public Device() {
		this.customData = new CustomData();
		this.integrationConfig = new IntegrationConfig();
	}

//region Getters & Setters

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	public String getOsBuild() {
		return osBuild;
	}

	public void setOsBuild(String osBuild) {
		this.osBuild = osBuild;
	}

	public int getOsApiLevel() {
		return osApiLevel;
	}

	public void setOsApiLevel(int osApiLevel) {
		this.osApiLevel = osApiLevel;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getCarrier() {
		return carrier;
	}

	public void setCarrier(String carrier) {
		this.carrier = carrier;
	}

	public String getCurrentCarrier() {
		return currentCarrier;
	}

	public void setCurrentCarrier(String currentCarrier) {
		this.currentCarrier = currentCarrier;
	}

	public String getNetworkType() {
		return networkType;
	}

	public void setNetworkType(String networkType) {
		this.networkType = networkType;
	}

	public String getBuildType() {
		return buildType;
	}

	public void setBuildType(String buildType) {
		this.buildType = buildType;
	}

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getBootloaderVersion() {
		return bootloaderVersion;
	}

	public void setBootloaderVersion(String bootloaderVersion) {
		this.bootloaderVersion = bootloaderVersion;
	}

	public String getRadioVersion() {
		return radioVersion;
	}

	public void setRadioVersion(String radioVersion) {
		this.radioVersion = radioVersion;
	}

	public CustomData getCustomData() {
		return customData;
	}

	public void setCustomData(CustomData customData) {
		this.customData = customData;
	}

	public String getLocaleCountryCode() {
		return localeCountryCode;
	}

	public void setLocaleCountryCode(String localeCountryCode) {
		this.localeCountryCode = localeCountryCode;
	}

	public String getLocaleLanguageCode() {
		return localeLanguageCode;
	}

	public void setLocaleLanguageCode(String localeLanguageCode) {
		this.localeLanguageCode = localeLanguageCode;
	}

	public String getLocaleRaw() {
		return localeRaw;
	}

	public void setLocaleRaw(String localeRaw) {
		this.localeRaw = localeRaw;
	}

	public String getUtcOffset() {
		return utcOffset;
	}

	public void setUtcOffset(String utcOffset) {
		this.utcOffset = utcOffset;
	}

	public IntegrationConfig getIntegrationConfig() {
		return integrationConfig;
	}

	public void setIntegrationConfig(IntegrationConfig integrationConfig) {
		this.integrationConfig = integrationConfig;
	}

//endregion

}
