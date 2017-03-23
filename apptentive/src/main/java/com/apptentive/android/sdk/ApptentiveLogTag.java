package com.apptentive.android.sdk;

public enum ApptentiveLogTag {
	NETWORK(true),
	CONVERSATION(true),
	NOTIFICATIONS(true),
	MESSAGES(true),
	DATABASE(true);

	ApptentiveLogTag(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean enabled;
}
