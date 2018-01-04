/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

public class Constants {

	public static final int  API_VERSION = 9;
	public static final String APPTENTIVE_SDK_VERSION = "4.0.3";

	public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 45000;
	public static final int DEFAULT_READ_TIMEOUT_MILLIS = 45000;

	public static final int REQUEST_CODE_PHOTO_FROM_SYSTEM_PICKER = 10;
	public static final int REQUEST_CODE_CLOSE_COMPOSING_CONFIRMATION = 20;

	public final static int REQUEST_READ_STORAGE_PERMISSION = 1;

	public static final String PREF_NAME = "APPTENTIVE";

	// Globals
	public static final String PREF_KEY_SERVER_URL = "serverUrl";
	public static final String PREF_KEY_APP_CONFIG_PREFIX = "appConfiguration.";
	public static final String PREF_KEY_APP_CONFIG_JSON = PREF_KEY_APP_CONFIG_PREFIX+"json";
	public static final String PREF_KEY_PUSH_PROVIDER = "pushProvider";
	public static final String PREF_KEY_PUSH_TOKEN = "pushToken";

	// Just in case a customer copies the example text verbatim.
	public static final String EXAMPLE_APPTENTIVE_KEY_VALUE = "YOUR_APPTENTIVE_KEY";
	public static final String EXAMPLE_APPTENTIVE_SIGNATURE_VALUE = "YOUR_APPTENTIVE_SIGNATURE";



	// Engagement
	// Used to turn off Interaction polling so that contrived payloads can be manually tested.
	// TODO: Migrate into global data.
	public static final String PREF_KEY_MESSAGE_CENTER_SERVER_ERROR_LAST_ATTEMPT = "messageCenterServerErrorLastAttempt";
	public static final String PREF_KEY_POLL_FOR_INTERACTIONS = "pollForInteractions";

	// Config Defaults
	public static final String CONFIG_DEFAULT_SERVER_URL = "https://api.apptentive.com";

	//region Default Values
	public static final int CONFIG_DEFAULT_INTERACTION_CACHE_EXPIRATION_DURATION_SECONDS = 28800; // 8 hours
	public static final int CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_MILLIS = 0;
	public static final int CONFIG_DEFAULT_APP_CONFIG_EXPIRATION_DURATION_SECONDS = 86400; // 24 hours
	public static final int CONFIG_DEFAULT_MESSAGE_CENTER_FG_POLL_SECONDS = 15;
	public static final int CONFIG_DEFAULT_MESSAGE_CENTER_BG_POLL_SECONDS = 60;
	public static final boolean CONFIG_DEFAULT_MESSAGE_CENTER_ENABLED = false;
	public static final boolean CONFIG_DEFAULT_MESSAGE_CENTER_NOTIFICATION_POPUP_ENABLED = false;
	public static final boolean CONFIG_DEFAULT_HIDE_BRANDING = false;
	//endregion

	// region Android Manifest Keys
	public static final String MANIFEST_KEY_APPTENTIVE_LOG_LEVEL = "apptentive_log_level";
	public static final String MANIFEST_KEY_APPTENTIVE_KEY = "apptentive_key";
	public static final String MANIFEST_KEY_APPTENTIVE_SIGNATURE = "apptentive_signature";
	public static final String MANIFEST_KEY_SDK_DISTRIBUTION = "apptentive_sdk_distribution";
	public static final String MANIFEST_KEY_SDK_DISTRIBUTION_VERSION = "apptentive_sdk_distribution_version";
	public static final String MANIFEST_KEY_INITIALLY_HIDE_BRANDING = "apptentive_initially_hide_branding";
	public static final String MANIFEST_KEY_APPTENTIVE_DEBUG = "apptentive_debug";
	//endregion

	//region Database and File Storage
	public static final String CONVERSATIONS_DIR = "apptentive/conversations";
	public static final String PAYLOAD_DATA_DIR = "payloads";
	public static final String PAYLOAD_DATA_FILE_SUFFIX = ".data";
	//endregion

	// region Keys used to access old data for migration
	public static final String PREF_KEY_CONVERSATION_TOKEN = "conversationToken";
	public static final String PREF_KEY_CONVERSATION_ID = "conversationId";
	public static final String PREF_KEY_PERSON_ID = "personId";
	public static final String PREF_KEY_DEVICE = "device";
	public static final String PREF_KEY_SDK = "sdk";
	public static final String PREF_KEY_APP_RELEASE = "app_release";
	public static final String PREF_KEY_PERSON = "person";
	public static final String PREF_KEY_LAST_SEEN_SDK_VERSION = "lastSeenSdkVersion";
	public static final String PREF_KEY_MESSAGE_CENTER_FEATURE_USED = "messageCenterFeatureUsed";
	public static final String PREF_KEY_CODE_POINT_STORE = "codePointStore";
	public static final String PREF_KEY_VERSION_HISTORY = "versionHistory";
	public static final String PREF_KEY_VERSION_HISTORY_V2 = "versionHistoryV2";
	public static final String PREF_KEY_VERSION_HISTORY_V2_MIGRATED = "versionHistoryV2Migrated";
	public static final String PREF_KEY_MESSAGE_CENTER_WHO_CARD_DISPLAYED_BEFORE = "messageCenterWhoCardSet";
	public static final String PREF_KEY_INTERACTIONS_PAYLOAD_CACHE_EXPIRATION = "interactionsCacheExpiration";
	//endregion


	//region Old keys no longer used
	public static final String PREF_KEY_APP_ACTIVITY_STATE_QUEUE = "appActivityStateQueue";
	public static final String PREF_KEY_PERSON_EMAIL = "personEmail";
	public static final String PREF_KEY_PERSON_NAME = "personName";
	public static final String PREF_KEY_DEVICE_DATA = "deviceData";
	public static final String PREF_KEY_DEVICE_INTEGRATION_CONFIG = "integrationConfig";
	public static final String PREF_KEY_PERSON_DATA = "personData";
	public static final String PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_MESSAGE = "messageCenterPendingComposingMessage";
	public static final String PREF_KEY_MESSAGE_CENTER_PENDING_COMPOSING_ATTACHMENTS = "messageCenterPendingComposingAttachments";
	public static final String PREF_KEY_INTERACTIONS = "interactions";
	public static final String PREF_KEY_TARGETS = "targets";
	public static final String MANIFEST_KEY_MESSAGE_CENTER_ENABLED = "apptentive_message_center_enabled";
	public static final String MANIFEST_KEY_EMAIL_REQUIRED = "apptentive_email_required";
	public static final String PREF_KEY_APP_CONFIG_EXPIRATION = PREF_KEY_APP_CONFIG_PREFIX+"cache-expiration";
	public static final String PREF_KEY_SURVEYS = "surveys";
	public static final String PREF_KEY_SURVEYS_CACHE_EXPIRATION = "surveyCacheExpiration";
	public static final String PREF_KEY_SURVEYS_HISTORY = "surveyHistory";
	public static final String PREF_KEY_USER_ENTERED_EMAIL = "userEnteredEmail";
	public static final String PREF_KEY_APP_VERSION_CODE = "app_version_code";
	public static final String PREF_KEY_APP_VERSION_NAME = "app_version_name";
	public static final String PREF_KEY_DEVICE_DATA_SENT = "deviceDataSent"; // Keeps track of whether we have ever sent device data.
	public static final String PREF_KEY_START_OF_RATING_PERIOD = "startOfRatingPeriod";
	public static final String PREF_KEY_RATING_STATE = "ratingState";
	public static final String PREF_KEY_RATING_EVENTS = "events";
	public static final String PREF_KEY_RATING_USES = "uses";
	public static final String PREF_KEY_PERSON_INITIAL_EMAIL = "personInitialEmail";
	public static final String PREF_KEY_PERSON_INITIAL_USER_NAME = "personInitialUserName";
	public static final String PREF_KEY_API_KEY = "apiKey";
	public static final String PREF_KEY_APP_MAIN_ACTIVITY_NAME = "mainActivityName";
	public static final String PREF_KEY_AUTO_MESSAGE_SHOWN_AUTO_MESSAGE = "autoMessageShownAutoMessage";
	public static final String PREF_KEY_AUTO_MESSAGE_SHOWN_NO_LOVE = "autoMessageShownNoLove";
	public static final String PREF_KEY_AUTO_MESSAGE_SHOWN_MANUAL = "autoMessageShownManual";
	public static final String PREF_KEY_MESSAGE_CENTER_SHOULD_SHOW_INTRO_DIALOG = "messageCenterShouldShowIntroDialog";
	public static final String MANIFEST_KEY_USE_STAGING_SERVER = "apptentive_use_staging_server";
	public static final String PREF_KEY_PENDING_PUSH_NOTIFICATION = "pendingPushNotification";
	//region

	public interface FragmentConfigKeys {

		String TOOLBAR_ID = "toolbarLayoutId";
		String MODAL = "showAsModal";
		String TYPE = "fragmentType";
		String EXTRA = "fragmentExtraData";
	}

	public interface FragmentTypes {
		int UNKNOWN = 0;
		int ABOUT = 1;
		int MESSAGE_CENTER_ERROR = 2;
		int INTERACTION = 3;
		int ENGAGE_INTERNAL_EVENT = 4;
	}


	/**
	 * A list of mobile carrier network types as Strings.
	 * From {@link android.telephony.TelephonyManager TelephonyManager}
	 * @see android.telephony.TelephonyManager
	 */
	private static final String[] networkTypeLookup = {
		"UNKNOWN", //  0
		"GPRS",    //  1
		"EDGE",    //  2
		"UMTS",    //  3
		"CDMA",    //  4
		"EVDO_0",  //  5
		"EVDO_A",  //  6
		"1xRTT",   //  7
		"HSDPA",   //  8
		"HSUPA",   //  9
		"HSPA",    // 10
		"IDEN",    // 11
		"EVDO_B",  // 12
		"LTE",     // 13
		"EHRPD",   // 14
		"HSPAP",   // 15
		"GSM",     // 16
		"TD_SCDMA",// 17
		"IWLAN"    // 18
	};

	public static String networkTypeAsString(int networkTypeAsInt) {
		try {
			return networkTypeLookup[networkTypeAsInt];
		} catch (ArrayIndexOutOfBoundsException e) {
			return networkTypeLookup[0];
		}
	}
}
