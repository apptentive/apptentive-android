/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.storage;

import com.apptentive.android.sdk.model.PersonPayload;

public class PersonManager {

	public static PersonPayload getDiffPayload(Person oldPerson, Person newPerson) {
		if (newPerson == null) {
			return null;
		}

		PersonPayload ret = new PersonPayload();
		boolean changed = false;

		if (oldPerson == null || !equal(oldPerson.getId(), newPerson.getId())) {
			ret.setId(newPerson.getId());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getEmail(), newPerson.getEmail())) {
			ret.setEmail(newPerson.getEmail());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getName(), newPerson.getName())) {
			ret.setName(newPerson.getName());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getFacebookId(), newPerson.getFacebookId())) {
			ret.setFacebookId(newPerson.getFacebookId());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getPhoneNumber(), newPerson.getPhoneNumber())) {
			ret.setPhoneNumber(newPerson.getPhoneNumber());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getStreet(), newPerson.getStreet())) {
			ret.setStreet(newPerson.getStreet());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getCity(), newPerson.getCity())) {
			ret.setCity(newPerson.getCity());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getZip(), newPerson.getZip())) {
			ret.setZip(newPerson.getZip());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getCountry(), newPerson.getCountry())) {
			ret.setCountry(newPerson.getCountry());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getBirthday(), newPerson.getBirthday())) {
			ret.setBirthday(newPerson.getBirthday());
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getCustomData(), newPerson.getCustomData())) {
			CustomData customData = newPerson.getCustomData();
			ret.setCustomData(customData != null ? customData.toJson() : null);
			changed = true;
		}

		if (oldPerson == null || !equal(oldPerson.getMParticleId(), newPerson.getMParticleId())) {
			ret.setMParticleId(newPerson.getMParticleId());
			changed = true;
		}

		return changed ? ret : null;
	}

	private static boolean equal(Object a, Object b) {
		return a == null && b == null || a != null && b != null && a.equals(b);
	}
}
