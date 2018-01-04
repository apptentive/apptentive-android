package com.apptentive.android.sdk;

public enum ApptentiveLogTag {
	NETWORK(true),
	APP_CONFIGURATION(true),
	CONVERSATION(true),
	NOTIFICATIONS(true),
	MESSAGES(true),
	DATABASE(true),
	PAYLOADS(true),
	TESTER_COMMANDS(true);

	ApptentiveLogTag(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean enabled;
}
