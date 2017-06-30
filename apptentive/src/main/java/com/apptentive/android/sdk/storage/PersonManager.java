/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.PersonPayload;

public class PersonManager {

	public static PersonPayload getDiffPayload(com.apptentive.android.sdk.storage.Person oldPerson, com.apptentive.android.sdk.storage.Person newPerson) {
		if (newPerson == null) {
			return null;
		}

		PersonPayload ret = new PersonPayload();

		if (oldPerson == null || !oldPerson.getId().equals(newPerson.getId())) {
			ret.setId(newPerson.getId());
		}

		if (oldPerson == null || !oldPerson.getEmail().equals(newPerson.getEmail())) {
			ret.setEmail(newPerson.getEmail());
		}

		if (oldPerson == null || !oldPerson.getName().equals(newPerson.getName())) {
			ret.setName(newPerson.getName());
		}

		if (oldPerson == null || !oldPerson.getFacebookId().equals(newPerson.getFacebookId())) {
			ret.setFacebookId(newPerson.getFacebookId());
		}

		if (oldPerson == null || !oldPerson.getPhoneNumber().equals(newPerson.getPhoneNumber())) {
			ret.setPhoneNumber(newPerson.getPhoneNumber());
		}

		if (oldPerson == null || !oldPerson.getStreet().equals(newPerson.getStreet())) {
			ret.setStreet(newPerson.getStreet());
		}

		if (oldPerson == null || !oldPerson.getCity().equals(newPerson.getCity())) {
			ret.setCity(newPerson.getCity());
		}

		if (oldPerson == null || !oldPerson.getZip().equals(newPerson.getZip())) {
			ret.setZip(newPerson.getZip());
		}

		if (oldPerson == null || !oldPerson.getCountry().equals(newPerson.getCountry())) {
			ret.setCountry(newPerson.getCountry());
		}

		if (oldPerson == null || !oldPerson.getBirthday().equals(newPerson.getBirthday())) {
			ret.setBirthday(newPerson.getBirthday());
		}

		if (oldPerson == null || !oldPerson.getCustomData().equals(newPerson.getCustomData())) {
			CustomData customData = newPerson.getCustomData();
			ret.setCustomData(customData != null ? customData.toJson() : null);
		}

		return ret;
	}
}
