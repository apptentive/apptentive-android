/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import org.json.JSONException;


public class IntegrationConfig implements Saveable {

	private static final long serialVersionUID = 1L;

	private static final String INTEGRATION_APPTENTIVE_PUSH = "apptentive_push";
	private static final String INTEGRATION_AWS_SNS = "aws_sns";
	private static final String INTEGRATION_URBAN_AIRSHIP = "urban_airship";
	private static final String INTEGRATION_PARSE = "parse";

	private IntegrationConfigItem apptentive;
	private IntegrationConfigItem amazonAwsSns;
	private IntegrationConfigItem urbanAirship;
	private IntegrationConfigItem parse;

	private DataChangedListener listener;


	//region Listeners

	@Override
	public void setDataChangedListener(DataChangedListener listener) {
		this.listener = listener;
	}

	@Override
	public void notifyDataChanged() {
		if (listener != null) {
			listener.onDataChanged();
		}
	}

	@Override
	public void onDeserialize() {
	}

	//endregion

	//region Getters & Setters
	public IntegrationConfigItem getApptentive() {
		return apptentive;
	}

	public void setApptentive(IntegrationConfigItem apptentive) {
		this.apptentive = apptentive;
		notifyDataChanged();
	}

	public IntegrationConfigItem getAmazonAwsSns() {
		return amazonAwsSns;
	}

	public void setAmazonAwsSns(IntegrationConfigItem amazonAwsSns) {
		this.amazonAwsSns = amazonAwsSns;
		notifyDataChanged();
	}

	public IntegrationConfigItem getUrbanAirship() {
		return urbanAirship;
	}

	public void setUrbanAirship(IntegrationConfigItem urbanAirship) {
		this.urbanAirship = urbanAirship;
		notifyDataChanged();
	}

	public IntegrationConfigItem getParse() {
		return parse;
	}

	public void setParse(IntegrationConfigItem parse) {
		this.parse = parse;
		notifyDataChanged();
	}
	//endregion

	public com.apptentive.android.sdk.model.CustomData toJson() {
		try {
			com.apptentive.android.sdk.model.CustomData ret = new com.apptentive.android.sdk.model.CustomData();
			if (apptentive != null) {
				ret.put(INTEGRATION_APPTENTIVE_PUSH, apptentive.toJson());
			}
			if (amazonAwsSns != null) {
				ret.put(INTEGRATION_AWS_SNS, amazonAwsSns.toJson());
			}
			if (urbanAirship != null) {
				ret.put(INTEGRATION_URBAN_AIRSHIP, urbanAirship.toJson());
			}
			if (parse != null) {
				ret.put(INTEGRATION_PARSE, parse.toJson());
			}
		} catch (JSONException e) {
			// This can't happen.
		}
		return null;
	}

}
