package com.apptentive.android.sdk.debug;

public class TesterEvent {

	public static final String EVT_CONVERSATION_LOAD = "conversation_load"; // { successful:boolean, conversation_state:String, conversation_identifier:String }
	public static final String EVT_CONVERSATION_METADATA_LOAD = "conversation_metadata_load"; // { successful:boolean }
	public static final String EVT_CONVERSATION_FETCH_TOKEN = "conversation_fetch_token"; // { successful:boolean }
	public static final String EVT_INTERACTION_FETCH = "interaction_fetch"; // { successful:boolean }
	public static final String EVT_EXCEPTION = "exception"; // { class:String, message:String, stackTrace:String }

	public static final String EVT_APPTENTIVE_EVENT = "apptentive_event"; // { eventLabel:String }
	public static final String EVT_APPTENTIVE_EVENT_KEY_EVENT_LABEL = "eventLabel";

	public static final String EVT_KEY_SUCCESSFUL = "successful";
	public static final String EVT_KEY_STATE = "state";
}
