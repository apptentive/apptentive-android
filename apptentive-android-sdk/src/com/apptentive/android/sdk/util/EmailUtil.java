/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * This class requires Android API level 5 (2.0), as it depends on the android.accounts.* API.
 * @author Sky Kelsey
 */
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

	// TODO: Use reflection to load this so we can drop 2.1 API requirement.
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
