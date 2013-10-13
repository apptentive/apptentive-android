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

	public JsonObjectBindingTest() {
		super();
	}

	public void testParsingTextMessageResponse() {
		String exampleResponse = "{\"items\":[{\"id\":\"520a86f24712c76b7000000d\",\"nonce\":\"520a74bf4712c76b700000044de48b8266880000010000071376421617273\",\"type\":\"TextMessage\",\"created_at\":1376421618.004,\"sender\":{\"name\":\"Sky Kelsey\",\"id\":\"4de48b826688000001000007\",\"profile_photo\":\"https://secure.gravatar.com/avatar/d515bb535e73afffe3b45c997c145848.png?d=mm&r=PG\"},\"body\":\"Test reply via website.\"},{\"id\":\"520a84fe4712c71b65000005\",\"nonce\":\"33b042735704283c0407f22e6d6f8c6fcb0d2f073bf964f76d36ef176ec26472\",\"type\":\"TextMessage\",\"client_created_at\":1376421112.0,\"client_created_at_utc_offset\":-25200,\"created_at\":1376421118.499,\"sender\":{\"name\":\"Sky Kelsey\",\"id\":\"4de48b826688000001000007\",\"profile_photo\":\"https://secure.gravatar.com/avatar/d515bb535e73afffe3b45c997c145848.png?d=mm&r=PG\"},\"body\":\"Test reply via email.\"},{\"id\":\"520a75ac4712c7b622000009\",\"nonce\":\"8f2271b4-db04-48a8-b99c-404048f5f7a6\",\"type\":\"FileMessage\",\"client_created_at\":1376417178.828,\"client_created_at_utc_offset\":-25200,\"created_at\":1376417196.878,\"sender\":{\"name\":null,\"id\":\"520a74bf4712c76b70000005\",\"profile_photo\":\"https://secure.gravatar.com/avatar/d515bb535e73afffe3b45c997c145848.png?d=mm&r=PG\"},\"url\":\"https://s3.amazonaws.com/apptentive_staging/520a75ac4712c7b622000009?AWSAccessKeyId=1FG0SNS81VAX9NRE5M02&Expires=1376425813&Signature=6iHjtR0sJ8z%2BVmW1Z3pmyQKBm5w%3D\",\"content_type\":\"image/jpeg\",\"icon_url\":\"https://s3.amazonaws.com/apptentive_staging/520a75ac4712c7b622000009?AWSAccessKeyId=1FG0SNS81VAX9NRE5M02&Expires=1376425813&Signature=6iHjtR0sJ8z%2BVmW1Z3pmyQKBm5w%3D\"},{\"id\":\"520a75794712c7b622000003\",\"nonce\":\"6758696e-e474-431e-a8e4-ae56f3ccc855\",\"type\":\"TextMessage\",\"client_created_at\":1376417136.348,\"client_created_at_utc_offset\":-25200,\"created_at\":1376417145.687,\"sender\":{\"name\":null,\"id\":\"520a74bf4712c76b70000005\",\"profile_photo\":\"https://secure.gravatar.com/avatar/d515bb535e73afffe3b45c997c145848.png?d=mm&r=PG\"},\"body\":\"Test message.\"},{\"id\":\"520a756e4712c7afb7000003\",\"nonce\":\"9afbeec2-786e-435e-9eb2-d3c431ccd23b\",\"type\":\"AutomatedMessage\",\"client_created_at\":1376417122.487,\"client_created_at_utc_offset\":-25200,\"created_at\":1376417134.838,\"sender\":{\"name\":null,\"id\":\"520a74bf4712c76b70000005\",\"profile_photo\":\"https://secure.gravatar.com/avatar/d515bb535e73afffe3b45c997c145848.png?d=mm&r=PG\"},\"body\":\"What can we do to ensure that you love our app? We appreciate your constructive feedback.\",\"title\":\"We're Sorry!\"}],\"has_more\":false}";
		List<Message> messages = null;
		try {
			messages = MessageManager.parseMessagesString(exampleResponse);
		} catch (JSONException e) {
		}
		assertNotNull(messages);
		assertEquals(messages.size(), 5);
	}

	public void testTextMessageRoundTrip() {
		String exampleMessage = "{\n" +
				"            \"id\": \"520a84fe4712c71b65000005\",\n" +
				"            \"nonce\": \"33b042735704283c0407f22e6d6f8c6fcb0d2f073bf964f76d36ef176ec26472\",\n" +
				"            \"type\": \"TextMessage\",\n" +
				"            \"client_created_at\": 1376421112,\n" +
				"            \"client_created_at_utc_offset\": -25200,\n" +
				"            \"created_at\": 1376421118.499,\n" +
				"            \"sender\": {\n" +
				"                \"name\": \"Sky Kelsey\",\n" +
				"                \"id\": \"4de48b826688000001000007\",\n" +
				"                \"profile_photo\": \"https://secure.gravatar.com/avatar/d515bb535e73afffe3b45c997c145848.png?d=mm&r=PG\"\n" +
				"            },\n" +
				"            \"body\": \"Test reply via email.\"\n" +
				"        }";
		TextMessage message = null;
		try {
			message = new TextMessage(exampleMessage);
		} catch (JSONException e) {

		}
		assertNotNull(message);
		String recoveredMessage = message.toString();
		assertEquals(true, recoveredMessage.contains("\"id\":\"520a84fe4712c71b65000005\""));
		assertEquals(true, recoveredMessage.contains("\"nonce\":\"33b042735704283c0407f22e6d6f8c6fcb0d2f073bf964f76d36ef176ec26472\""));
		assertEquals(true, recoveredMessage.contains("\"created_at\":1.376421118499E9"));
		assertEquals(true, recoveredMessage.contains("\"client_created_at\":1376421112"));
		assertEquals(true, recoveredMessage.contains("\"client_created_at_utc_offset\":-25200"));
		assertEquals(true, recoveredMessage.contains("\"type\":\"TextMessage\""));
		assertEquals(true, recoveredMessage.contains("\"name\":\"Sky Kelsey\""));
		assertEquals(true, recoveredMessage.contains("\"id\":\"4de48b826688000001000007\""));
		assertEquals(true, recoveredMessage.contains("\"body\":\"Test reply via email.\""));
	}


/*
	public void testParseConfiguration() {
		String exampleConfig = "{\"_id\":\"4f7f453f1a387e00070000cd\",\"metrics_enabled\":true,\"ratings_clear_on_upgrade\":true,\"ratings_days_before_prompt\":10,\"ratings_days_between_prompts\":3,\"ratings_enabled\":true,\"ratings_events_before_prompt\":2,\"ratings_prompt_logic\":{\"or\":[\"uses\",\"days\",\"events\"]},\"ratings_uses_before_prompt\":5,\"cache-expiration\":\"2012-10-17T02:02:44+00:00\"}";
		// TODO: Make an object for this, and then test parsing.
	}
*/
}
