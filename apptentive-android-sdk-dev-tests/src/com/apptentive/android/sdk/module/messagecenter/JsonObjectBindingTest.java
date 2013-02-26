package com.apptentive.android.sdk.module.messagecenter;

import com.apptentive.android.sdk.model.Message;
import com.apptentive.android.sdk.model.TextMessage;
import junit.framework.TestCase;
import org.json.JSONException;

import java.util.List;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.apptentive.android.dev.DevActivityTest \
 * com.apptentive.android.dev.tests/android.test.InstrumentationTestRunner
 */
public class JsonObjectBindingTest extends TestCase {

	private final static String TAG = "JsonObjectBindingTest";

	public JsonObjectBindingTest() {
		super();
	}

	public void testParsingTextMessageResponse() {
		String exampleResponse = "{\"messages\":[{\"id\":\"507c6c270aa2420005000006\",\"created_at\":1350331427.0,\"type\":\"text_message\",\"body\":\"test\",\"display\":\"message_center\",\"sender_id\":\"507c6c0c0aa242000e000001\",\"app_id\":\"4de48c13668800000100000c\"},{\"id\":\"507c70650aa2420005000007\",\"created_at\":1350332511.0,\"type\":\"text_message\",\"body\":\"more\",\"display\":\"message_center\",\"sender_id\":\"507c6c0c0aa242000e000001\",\"app_id\":\"4de48c13668800000100000c\"},{\"id\":\"507c70670aa242000b000001\",\"created_at\":1350332516.0,\"type\":\"text_message\",\"body\":\"blah\",\"display\":\"message_center\",\"sender_id\":\"507c6c0c0aa242000e000001\",\"app_id\":\"4de48c13668800000100000c\"}],\"location\":null}";
		List<Message> messages = null;
		try {
			messages = MessageManager.parseMessagesString(exampleResponse);
		} catch (JSONException e) {
		}
		assertNotNull(messages);
		assertEquals(messages.size(), 3);
	}

	public void testTextMessageRoundTrip() {
		String exampleMessage = "{\"id\":\"507c6c270aa2420005000006\",\"created_at\":1350331427.0,\"type\":\"text_message\",\"body\":\"test\",\"display\":\"message_center\",\"sender_id\":\"507c6c0c0aa242000e000001\",\"app_id\":\"4de48c13668800000100000c\"}";
		TextMessage message = null;
		try {
			message = new TextMessage(exampleMessage);
		} catch (JSONException e) {

		}
		String recoveredMessage = message.toString();
		assertNotNull(message);
		assertEquals(true, recoveredMessage.contains("\"id\":\"507c6c270aa2420005000006\""));
		assertEquals(true, recoveredMessage.contains("\"created_at\":1350331427")); // Will come back without .0
		assertEquals(true, recoveredMessage.contains("\"type\":\"text_message\""));
		assertEquals(true, recoveredMessage.contains("\"body\":\"test\""));
		assertEquals(true, recoveredMessage.contains("\"display\":\"message_center\""));
		assertEquals(true, recoveredMessage.contains("\"sender_id\":\"507c6c0c0aa242000e000001\""));
		assertEquals(true, recoveredMessage.contains("\"app_id\":\"4de48c13668800000100000c\""));
	}


	public void testParseConfiguration() {
		String exampleConfig = "{\"_id\":\"4f7f453f1a387e00070000cd\",\"metrics_enabled\":true,\"ratings_clear_on_upgrade\":true,\"ratings_days_before_prompt\":10,\"ratings_days_between_prompts\":3,\"ratings_enabled\":true,\"ratings_events_before_prompt\":2,\"ratings_prompt_logic\":{\"or\":[\"uses\",\"days\",\"events\"]},\"ratings_uses_before_prompt\":5,\"cache-expiration\":\"2012-10-17T02:02:44+00:00\"}";
		// TODO: Make an object for this, and then test parsing.
	}
}
