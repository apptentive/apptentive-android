/*
 * Copyright (c) 2011, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

public final class AccountManagerDelegate {
	public static String getAccountEmail(Context ctx) {
		AccountManager accountManager = AccountManager.get(ctx);
		Account account = getAccount(accountManager);
		if(account == null){
			return null;
		}else{
			return account.name;
		}
	}
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
