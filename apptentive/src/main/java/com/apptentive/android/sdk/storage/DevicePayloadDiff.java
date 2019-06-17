package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.DevicePayload;

/**
 * A helper class with static methods for and diffing information about the current device.
 */
public final class DevicePayloadDiff {

	public static DevicePayload getDiffPayload(Device oldDevice, Device newDevice) {
		if (newDevice == null) {
			return null;
		}

		DevicePayload ret = new DevicePayload();
		boolean changed = false;

		if (oldDevice == null || !equal(oldDevice.getUuid(), newDevice.getUuid())) {
			ret.setUuid(newDevice.getUuid());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getOsName(), newDevice.getOsName())) {
			ret.setOsName(newDevice.getOsName());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getOsVersion(), newDevice.getOsVersion())) {
			ret.setOsVersion(newDevice.getOsVersion());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getOsBuild(), newDevice.getOsBuild())) {
			ret.setOsBuild(newDevice.getOsBuild());
			changed = true;
		}

		if (oldDevice == null || oldDevice.getOsApiLevel() != newDevice.getOsApiLevel()) {
			ret.setOsApiLevel(String.valueOf(newDevice.getOsApiLevel()));
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getManufacturer(), newDevice.getManufacturer())) {
			ret.setManufacturer(newDevice.getManufacturer());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getModel(), newDevice.getModel())) {
			ret.setModel(newDevice.getModel());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBoard(), newDevice.getBoard())) {
			ret.setBoard(newDevice.getBoard());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getProduct(), newDevice.getProduct())) {
			ret.setProduct(newDevice.getProduct());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBrand(), newDevice.getBrand())) {
			ret.setBrand(newDevice.getBrand());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCpu(), newDevice.getCpu())) {
			ret.setCpu(newDevice.getCpu());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getDevice(), newDevice.getDevice())) {
			ret.setDevice(newDevice.getDevice());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCarrier(), newDevice.getCarrier())) {
			ret.setCarrier(newDevice.getCarrier());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCurrentCarrier(), newDevice.getCurrentCarrier())) {
			ret.setCurrentCarrier(newDevice.getCurrentCarrier());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getNetworkType(), newDevice.getNetworkType())) {
			ret.setNetworkType(newDevice.getNetworkType());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBuildType(), newDevice.getBuildType())) {
			ret.setBuildType(newDevice.getBuildType());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBuildId(), newDevice.getBuildId())) {
			ret.setBuildId(newDevice.getBuildId());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getBootloaderVersion(), newDevice.getBootloaderVersion())) {
			ret.setBootloaderVersion(newDevice.getBootloaderVersion());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getRadioVersion(), newDevice.getRadioVersion())) {
			ret.setRadioVersion(newDevice.getRadioVersion());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getCustomData(), newDevice.getCustomData())) {
			CustomData customData = newDevice.getCustomData();
			ret.setCustomData(customData != null ? customData.toJson() : null);
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getLocaleCountryCode(), newDevice.getLocaleCountryCode())) {
			ret.setLocaleCountryCode(newDevice.getLocaleCountryCode());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getLocaleLanguageCode(), newDevice.getLocaleLanguageCode())) {
			ret.setLocaleLanguageCode(newDevice.getLocaleLanguageCode());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getLocaleRaw(), newDevice.getLocaleRaw())) {
			ret.setLocaleRaw(newDevice.getLocaleRaw());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getUtcOffset(), newDevice.getUtcOffset())) {
			ret.setUtcOffset(newDevice.getUtcOffset());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getAdvertiserId(), newDevice.getAdvertiserId())) {
			ret.setAdvertiserId(newDevice.getAdvertiserId());
			changed = true;
		}

		if (oldDevice == null || !equal(oldDevice.getIntegrationConfig(), newDevice.getIntegrationConfig())) {
			IntegrationConfig integrationConfig = newDevice.getIntegrationConfig();
			ret.setIntegrationConfig(integrationConfig != null ? integrationConfig.toJson() : null);
			changed = true;
		}
		return changed ? ret : null;
	}

	private static boolean equal(Object a, Object b) {
		return a == null && b == null || a != null && b != null && a.equals(b);
	}
}
