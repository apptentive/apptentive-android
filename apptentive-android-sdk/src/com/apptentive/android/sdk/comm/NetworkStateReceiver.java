package com.apptentive.android.sdk.comm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

	public static void clearListeners() {
		listeners.clear();
	}
	public static void addListener(NetworkStateListener listener) {
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
