/*
 * EmailUtil.java
 *
 * Created by Sky Kelsey on 2011-09-23.
 * Copyright 2011 Apptentive, Inc. All rights reserved.
 */

package com.apptentive.android.sdk.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public class EmailUtil{

	public static String getEmail(Context context){
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(accountManager);
		if(account == null){
			return null;
		}else{
			return account.name;
		}
	}
	// TODO: Change this method to use a delegate of some kind,
	// and Apptentive will run in Android 1.6
	private static Account getAccount(AccountManager accountManager){
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Account account;
		if (accounts.length > 0){
			account = accounts[0];
		}else{
			account = null;
		}
		return account;
	}
}
