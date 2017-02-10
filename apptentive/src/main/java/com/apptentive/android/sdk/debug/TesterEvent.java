package com.apptentive.android.sdk.debug;

public class TesterEvent {
	public static final String EVT_FETCH_CONVERSATION_TOKEN = "fetch_conversation_token";
	public static final String EVT_INSTANCE_CREATED = "instance_created"; // { successful:boolean }
	public static final String EVT_EXCEPTION = "exception"; // { class:String, message:String, stackTrace:String }
}
