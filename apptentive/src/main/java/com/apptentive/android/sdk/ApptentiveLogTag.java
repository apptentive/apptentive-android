package com.apptentive.android.sdk;

public enum ApptentiveLogTag {
	NETWORK(true),
	CONVERSATION(true),
	NOTIFICATIONS(true),
	TESTER_COMMANDS(false);

	ApptentiveLogTag(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean enabled;
}
