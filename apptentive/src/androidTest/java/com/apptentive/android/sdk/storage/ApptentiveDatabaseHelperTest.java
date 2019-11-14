/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import com.apptentive.android.sdk.model.*;
import com.apptentive.android.sdk.module.messagecenter.model.MessageFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

@RunWith(AndroidJUnit4.class)
public class ApptentiveDatabaseHelperTest {

	@After
	public void tearDown() throws Exception {
		deleteDbFile(InstrumentationRegistry.getContext());
	}

	@Test
	public void testMigration() throws Exception {
		final Context context = InstrumentationRegistry.getContext();
		replaceDbFile(context, "apptentive-v2");

		Payload[] expectedPayloads = {
			new AppReleasePayload("{\"type\":\"android\",\"version_name\":\"3.4.1\",\"identifier\":\"com.apptentive.dev\",\"version_code\":51,\"target_sdk_version\":\"24\",\"inheriting_styles\":true,\"overriding_styles\":true,\"debug\":true}"),
			new SdkPayload("{\"version\":\"3.4.1\",\"platform\":\"Android\"}"),
			new EventPayload("{\"nonce\":\"b9a91f27-87b4-4bd9-b9a0-5c605891824a\",\"client_created_at\":1.492737199856E9,\"client_created_at_utc_offset\":-25200,\"label\":\"com.apptentive#app#launch\"}"),
			new DevicePayload("{\"device\":\"bullhead\",\"integration_config\":{},\"locale_country_code\":\"US\",\"carrier\":\"\",\"uuid\":\"6c0b74d07c064421\",\"build_type\":\"user\",\"cpu\":\"arm64-v8a\",\"os_build\":\"3687331\",\"manufacturer\":\"LGE\",\"radio_version\":\"M8994F-2.6.36.2.20\",\"os_name\":\"Android\",\"build_id\":\"N4F26T\",\"utc_offset\":\"-28800\",\"bootloader_version\":\"BHZ11h\",\"board\":\"bullhead\",\"os_api_level\":\"25\",\"current_carrier\":\"AT&T\",\"network_type\":\"LTE\",\"locale_raw\":\"en_US\",\"brand\":\"google\",\"os_version\":\"7.1.1\",\"product\":\"bullhead\",\"model\":\"Nexus 5X\",\"locale_language_code\":\"en\",\"custom_data\":{}}"),
			new PersonPayload("{\"custom_data\":{}}"),
			MessageFactory.fromJson("{\"nonce\":\"a68d606c-083a-4496-a5e0-f07bcdff52a4\",\"client_created_at\":1.492737257565E9,\"client_created_at_utc_offset\":-25200,\"type\":\"CompoundMessage\",\"body\":\"Test message\",\"text_only\":false}")
		};
	}

	private static void replaceDbFile(Context context, String filename) throws IOException {
		InputStream input = context.getAssets().open(filename);
		try {
			OutputStream output = new FileOutputStream(getDatabaseFile(context));
			try {
				byte[] buffer = new byte[1024];
				int read;
				while ((read = input.read(buffer)) != -1) {
					output.write(buffer, 0, read);
				}
			} finally {
				output.close();
			}
		} finally {
			input.close();
		}
	}

	private static void deleteDbFile(Context context) throws IOException {
		getDatabaseFile(context).delete();
	}

	private static File getDatabaseFile(Context context) {
		return context.getDatabasePath("apptentive");
	}

}