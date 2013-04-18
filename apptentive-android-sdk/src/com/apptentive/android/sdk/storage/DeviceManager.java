package com.apptentive.android.sdk.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import com.apptentive.android.sdk.GlobalInfo;
import com.apptentive.android.sdk.model.Device;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Reflection;

/**
 * A helper class with static methods for getting, storing, retrieving, and diffing information about the current device.
 * @author Sky Kelsey
 */
public class DeviceManager {

	/**
	 * If any device setting has changed, return only the changed fields in a new Device object. If a field's value was
	 * cleared, set that value to null in the Device. The first time this is called, all Device will be returned.
	 * @return
	 */
	public static Device storeDeviceAndReturnDiff(Context context) {

		Device original = getStoredDevice(context);
		Device current = generateCurrentDevice(context);
		Device diff = diffDevice(original, current);
		if(diff != null) {
			storeDevice(context, current);
			return diff;
		}
		return null;
	}

	private static Device generateCurrentDevice(Context context) {
		Device device = new Device();

		// First, get all the information we can load from static resources.
		device.setOsName("Android");
		device.setOsVersion(Build.VERSION.RELEASE);
		device.setOsBuild(Build.VERSION.INCREMENTAL);
		device.setManufacturer(Build.MANUFACTURER);
		device.setModel(Build.MODEL);
		device.setBoard(Build.BOARD);
		device.setProduct(Build.PRODUCT);
		device.setBrand(Build.BRAND);
		device.setCpu(Build.CPU_ABI);
		device.setDevice(Build.DEVICE);
		device.setUuid(GlobalInfo.androidId);
		device.setBuildType(Build.TYPE);
		device.setBuildId(Build.ID);

		// Second, set the stuff that requires querying system services.
		TelephonyManager tm = ((TelephonyManager) (context.getSystemService(Context.TELEPHONY_SERVICE)));
		device.setCarrier(tm.getSimOperatorName());
		device.setCurrentCarrier(tm.getNetworkOperatorName());
		device.setNetworkType(Constants.networkTypeAsString(tm.getNetworkType()));

		// Finally, use reflection to try loading from APIs that are not available on all Android versions.
		device.setBootloaderVersion(Reflection.getBootloaderVersion());
		device.setRadioVersion(Reflection.getRadioVersion());

		return device;
	}

	private static Device getStoredDevice(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		String deviceString = prefs.getString(Constants.PREF_KEY_DEVICE, null);
		try {
			return new Device(deviceString);
		} catch (Exception e) {
		}
		return null;
	}

	private static void storeDevice(Context context, Device device) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
		prefs.edit().putString(Constants.PREF_KEY_DEVICE, device.toString()).commit();
	}

	/**
	 * Creates a new Device object with the values from newer where they are different from older. If a value exists
	 * in older but not newer, an empty string is set for that key, which tells the server to clear the value. A null
	 * values for a key will not be written so that this method only returns a strict diff of older and newer.
	 * @param older
	 * @param newer
	 * @return A new Device object if there were any differences, else null.
	 */
	private static Device diffDevice(Device older, Device newer) {
		if(older == null) {
			return newer;
		}

		Device ret = new Device();
		int baseEntries = ret.length();

		String uuid = chooseLatest(older.getUuid(), newer.getUuid());
		if (uuid != null) {
			ret.setUuid(uuid);
		}

		String osName = chooseLatest(older.getOsName(), newer.getOsName());
		if (osName != null) {
			ret.setOsName(osName);
		}

		String osVersion = chooseLatest(older.getOsVersion(), newer.getOsVersion());
		if (osVersion != null) {
			ret.setOsVersion(osVersion);
		}

		String osBuild = chooseLatest(older.getOsBuild(), newer.getOsBuild());
		if (osBuild != null) {
			ret.setOsBuild(osBuild);
		}

		String manufacturer = chooseLatest(older.getManufacturer(), newer.getManufacturer());
		if (manufacturer != null) {
			ret.setManufacturer(manufacturer);
		}

		String model = chooseLatest(older.getModel(), newer.getModel());
		if (model != null) {
			ret.setModel(model);
		}

		String board = chooseLatest(older.getBoard(), newer.getBoard());
		if (board != null) {
			ret.setBoard(board);
		}

		String product = chooseLatest(older.getProduct(), newer.getProduct());
		if (product != null) {
			ret.setProduct(product);
		}

		String brand = chooseLatest(older.getBrand(), newer.getBrand());
		if (brand != null) {
			ret.setBrand(brand);
		}

		String cpu = chooseLatest(older.getCpu(), newer.getCpu());
		if (cpu != null) {
			ret.setCpu(cpu);
		}

		String device = chooseLatest(older.getDevice(), newer.getDevice());
		if (device != null) {
			ret.setDevice(device);
		}

		String carrier = chooseLatest(older.getCarrier(), newer.getCarrier());
		if (carrier != null) {
			ret.setCarrier(carrier);
		}

		String currentCarrier = chooseLatest(older.getCurrentCarrier(), newer.getCurrentCarrier());
		if (currentCarrier != null) {
			ret.setCurrentCarrier(currentCarrier);
		}

		String networkType = chooseLatest(older.getNetworkType(), newer.getNetworkType());
		if (networkType != null) {
			ret.setNetworkType(networkType);
		}

		String buildType = chooseLatest(older.getBuildType(), newer.getBuildType());
		if (buildType != null) {
			ret.setBuildType(buildType);
		}

		String buildId = chooseLatest(older.getBuildId(), newer.getBuildId());
		if (buildId != null) {
			ret.setBuildId(buildId);
		}

		String bootloaderVersion = chooseLatest(older.getBootloaderVersion(), newer.getBootloaderVersion());
		if (bootloaderVersion != null) {
			ret.setBootloaderVersion(bootloaderVersion);
		}

		String radioVersion = chooseLatest(older.getRadioVersion(), newer.getRadioVersion());
		if (radioVersion != null) {
			ret.setRadioVersion(radioVersion);
		}

		// If there were no differences, return null.
		if(ret.length() <= baseEntries) {
			return null;
		}
		return ret;
	}

	/**
	 * A convenience method.
	 * @param old
	 * @param newer
	 * @return newer - if it is different from old. <p/>empty string - if there was an old value, but not a newer value. This clears the old value.<p/> null - if there is no difference.
	 */
	private static String chooseLatest(String old, String newer) {
		if(old == null || old.equals("")) {
			old = null;
		}
		if(newer == null || newer.equals("")) {
			newer = null;
		}

		// New value.
		if(old != null && newer != null && !old.equals(newer)) {
		 	return newer;
		}

		// Clear existing value.
		if(old != null && newer == null) {
			return "";
		}

		// Do nothing.
		return null;
	}
}
