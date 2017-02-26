package com.apptentive.android.sdk.debug;

public class TesterEvent {

	public static final String EVT_CONVERSATION_LOAD_ACTIVE = "conversation_load_active"; // { successful:boolean }
	public static final String EVT_CONVERSATION_CREATE = "conversation_create";  // { successful:boolean }
	public static final String EVT_CONVERSATION_METADATA_LOAD = "conversation_metadata_load"; // { successful:boolean }
	public static final String EVT_INTERACTION_FETCH = "interaction_fetch"; // { successful:boolean }
	public static final String EVT_INSTANCE_CREATED = "instance_created"; // { successful:boolean }
	public static final String EVT_EXCEPTION = "exception"; // { class:String, message:String, stackTrace:String }
	public static final String EVT_APPTENTIVE_EVENT = "apptentive_event"; // { eventLabel:String }
	public static final String EVT_CONVERSATION_BECAME_ACTIVE = "conversation_became_active"; // { }
}
