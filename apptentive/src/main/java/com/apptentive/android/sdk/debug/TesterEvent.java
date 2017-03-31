package com.apptentive.android.sdk.debug;

public class TesterEvent {

	/** An active conversation loading finished (might be either successful or failed) */
	public static final String EVT_CONVERSATION_LOAD = "conversation_load"; // { successful:boolean, conversation_state:String, conversation_identifier:String }

	/** An active conversation state changed */
	public static final String EVT_CONVERSATION_STATE_CHANGE = "conversation_state_change"; // { conversation_state:String, conversation_identifier:String }

	/** Conversation metadata loading finished (might be either successful or failed) */
	public static final String EVT_CONVERSATION_METADATA_LOAD = "conversation_metadata_load"; // { successful:boolean }

	/** Conversation token fetch request started */
	public static final String EVT_CONVERSATION_WILL_FETCH_TOKEN = "conversation_will_fetch_token";

	/** Conversation token fetch request finished (might be either successful or failed) */
	public static final String EVT_CONVERSATION_DID_FETCH_TOKEN = "conversation_did_fetch_token"; // { successful:boolean }

	/** Conversation interactions fetch request finished (might be either successful or failed) */
	public static final String EVT_CONVERSATION_FETCH_INTERACTIONS = "conversation_fetch_interactions"; // { successful:boolean }

	/** There was an unexpected runtime exception */
	public static final String EVT_EXCEPTION = "exception"; // { class:String, message:String, stackTrace:String }

	/** Apptentive event was sent */
	public static final String EVT_APPTENTIVE_EVENT = "apptentive_event"; // { eventLabel:String }
	public static final String EVT_APPTENTIVE_EVENT_KEY_EVENT_LABEL = "eventLabel";

	// Common event keys
	public static final String EVT_KEY_SUCCESSFUL = "successful";
}
