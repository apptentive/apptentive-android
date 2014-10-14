/*
 * Copyright (c) 2013, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.comm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Sky Kelsey.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

	private static Set<NetworkStateListener> listeners = new HashSet<NetworkStateListener>();

	public static void clearListeners(Context context) {
        ComponentName receiver = new ComponentName(context, NetworkStateReceiver.class);
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }

        listeners.clear();
	}
	public static void addListener(NetworkStateListener listener, Context context) {
        ComponentName receiver = new ComponentName(context, NetworkStateReceiver.class);
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        listeners.add(listener);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			NetworkInfo ni = (NetworkInfo) extras.get(ConnectivityManager.EXTRA_NETWORK_INFO);
			if (ni != null) {
				for (NetworkStateListener listener : listeners) {
					listener.stateChanged(ni);
				}
			}
		}
	}
}
