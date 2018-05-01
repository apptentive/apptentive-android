package com.apptentive.android.sdk.util;

public class ApplicationInfo {
	static final ApplicationInfo NULL = new ApplicationInfo("0", -1, -1, false); // TODO: figure out constant values

	private final String versionName;
	private final int versionCode;
	private final int targetSdkVersion;
	private final boolean debuggable;

	ApplicationInfo(String versionName, int versionCode, int targetSdkVersion, boolean debuggable) {
		this.versionName = versionName;
		this.versionCode = versionCode;
		this.targetSdkVersion = targetSdkVersion;
		this.debuggable = debuggable;
	}

	public String getVersionName() {
		return versionName;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public int getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public boolean isDebuggable() {
		return debuggable;
	}

	@Override
	public String toString() {
		return StringUtils.format("%s: versionName=%s versionCode=%d targetSdkVersion=%s debuggable=%b", getClass().getSimpleName(), versionName, versionCode, targetSdkVersion, debuggable);
	}
}
