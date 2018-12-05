/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import org.json.JSONException;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;


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

	private transient DataChangedListener listener;


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
			return ret;
		} catch (JSONException e) {
			logException(e);
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		IntegrationConfig that = (IntegrationConfig) o;

		if (apptentive != null ? !apptentive.equals(that.apptentive) : that.apptentive != null)
			return false;
		if (amazonAwsSns != null ? !amazonAwsSns.equals(that.amazonAwsSns) : that.amazonAwsSns != null)
			return false;
		if (urbanAirship != null ? !urbanAirship.equals(that.urbanAirship) : that.urbanAirship != null)
			return false;
		return parse != null ? parse.equals(that.parse) : that.parse == null;

	}

	@Override
	public int hashCode() {
		int result = apptentive != null ? apptentive.hashCode() : 0;
		result = 31 * result + (amazonAwsSns != null ? amazonAwsSns.hashCode() : 0);
		result = 31 * result + (urbanAirship != null ? urbanAirship.hashCode() : 0);
		result = 31 * result + (parse != null ? parse.hashCode() : 0);
		return result;
	}

	// TODO: unit tests
	public IntegrationConfig clone() {
		IntegrationConfig clone = new IntegrationConfig();
		clone.apptentive = apptentive != null ? apptentive.clone() : null;
		clone.amazonAwsSns = amazonAwsSns != null ? amazonAwsSns.clone() : null;
		clone.urbanAirship = urbanAirship != null ? urbanAirship.clone() : null;
		clone.parse = parse != null ? parse.clone() : null;
		clone.listener = listener;
		return clone;
	}

}
