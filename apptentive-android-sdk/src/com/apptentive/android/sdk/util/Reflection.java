/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.os.Build;
import com.apptentive.android.sdk.Log;

/**
 * This class is used to allow access of fields, methods, and classes that are not available in the targeted API level.
 *
 * @author Sky Kelsey.
 */
public class Reflection {

	/**
	 * <p>Returns the Build.BOOTLOADER version String. This field is introduced in API level 8</p>
	 * <p>This field appears to be implemented sporadically.
	 *   <ul>
	 *     <li>Galaxy S Captivate on 2.1: BOOTLOADER = null (Expected, as this has only API 7)</li>
	 *     <li>Nexus S on 2.3.6: BOOTLOADER = I9020XXKA3</li>
	 *     <li>Atrix on 2.2.2: BOOTLOADER = unknown</li>
	 *     <li>Galaxy Tab 10.1 on 3.1: BOOTLOADER = P7500XXKG8</li>
	 *   </ul>
	 * </p>
	 * @return String The Build.BOOTLOADER version String.
	 */
	public static String getBootloaderVersion(){
		try{
			return (String)Build.class.getField("BOOTLOADER").get(null);
		} catch(Exception e) {
			return null;
		}
	}

	/**
	 * <p>Returns the Build.BOOTLOADER version String. This field is introduced in API level 8, but the
	 * getRadioVersion() method was only implemented with API 14 (4.0), and this is the preferred method, since the
	 * RADIO field may not have been initialized when Build is loaded.</p>
	 * <p>This field appears to be implemented sporadically.
	 *   <ul>
	 *     <li>Galaxy S Captivate on 2.1: RADIO = null (Expected, as this has only API 7)</li>
	 *     <li>Nexus S on 2.3.6: RADIO = unknown</li>
	 *     <li>Atrix on 2.2.2: RADIO = unknown</li>
	 *     <li>Galaxy Tab 10.1 on 3.1: RADIO = unknown</li>
	 *     <li>Phones with 4.0 should see a valid radio version.</li>
	 *   </ul>
	 * </p>
	 * @return String The Build.BOOTLOADER version String.
	 */
	public static String getRadioVersion(){
		try{
			return Build.class.getMethod("getRadioVersion", String.class).invoke(null).toString();
		} catch(Exception e) {
			try{
				return (String)Build.class.getField("RADIO").get(null);
			} catch(Exception f) {
				return null;
			}
		}
	}
}
