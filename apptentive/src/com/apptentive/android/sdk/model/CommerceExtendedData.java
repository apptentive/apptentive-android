/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import com.apptentive.android.sdk.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Sky Kelsey
 */
public class CommerceExtendedData extends ExtendedData {

	private static final String KEY_ID = "id";
	private static final String KEY_AFFILIATION = "affiliation";
	private static final String KEY_REVENUE = "revenue";
	private static final String KEY_SHIPPING = "shipping";
	private static final String KEY_TAX = "tax";
	private static final String KEY_CURRENCY = "currency";
	private static final String KEY_ITEMS = "items";
	private static final String KEY_ITEMS_ID = "id";
	private static final String KEY_ITEMS_NAME = "name";
	private static final String KEY_ITEMS_CATEGORY = "category";
	private static final String KEY_ITEMS_PRICE = "price";
	private static final String KEY_ITEMS_QUANTITY = "quantity";
	private static final String KEY_ITEMS_CURRENCY = "currency";

	private static final int VERSION = 1;


	@Override
	protected void init() {
		setType(Type.commerce);
		setVersion(VERSION);
	}

	public CommerceExtendedData(Object id) {
		super();
		setId(id);
	}

	public CommerceExtendedData(Object id, Object affiliation, Number revenue, Number shipping, Number tax, String currency) {
		this(id);
		setId(id);
		setAffiliation(affiliation);
		setRevenue(revenue);
		setShipping(shipping);
		setTax(tax);
		setCurrency(currency);
	}

	public CommerceExtendedData setId(Object id) {
		try {
			put(KEY_ID, id);
		} catch (JSONException e) {
			Log.w("Error adding %s to CommerceCustomData.", KEY_ID, e);
		}
		return this;
	}

	public CommerceExtendedData setAffiliation(Object affiliation) {
		try {
			put(KEY_AFFILIATION, affiliation);
		} catch (JSONException e) {
			Log.w("Error adding %s to CommerceCustomData.", KEY_AFFILIATION, e);
		}
		return this;
	}

	public CommerceExtendedData setRevenue(Number revenue) {
		try {
			put(KEY_REVENUE, revenue);
		} catch (JSONException e) {
			Log.w("Error adding %s to CommerceCustomData.", KEY_REVENUE, e);
		}
		return this;
	}

	public CommerceExtendedData setShipping(Number shipping) {
		try {
			put(KEY_SHIPPING, shipping);
		} catch (JSONException e) {
			Log.w("Error adding %s to CommerceCustomData.", KEY_SHIPPING, e);
		}
		return this;
	}

	public CommerceExtendedData setTax(Number tax) {
		try {
			put(KEY_TAX, tax);
		} catch (JSONException e) {
			Log.w("Error adding %s to CommerceCustomData.", KEY_TAX, e);
		}
		return this;
	}

	public CommerceExtendedData setCurrency(String currency) {
		try {
			put(KEY_CURRENCY, currency);
		} catch (JSONException e) {
			Log.w("Error adding %s to CommerceCustomData.", KEY_CURRENCY, e);
		}
		return this;
	}

	/**
	 * Add information about a purchased item to this record. Calls to this method can be chained.
	 *
	 * @param id       The ID of this item. May be a String or Number. Ignored if null.
	 * @param name     The name of this item. May be a String or Number. Ignored if null.
	 * @param category The item category. Ignored if null.
	 * @param price    The individual item price. Ignored if null.
	 * @param quantity The number of units purchased. Ignored if null.
	 * @param currency The currency code for the currency used in this transaction. Ignored if null.
	 * @return This object.
	 */
	public CommerceExtendedData addItem(Object id, Object name, String category, Number price, Number quantity, String currency) {
		try {
			JSONArray items;
			if (isNull(KEY_ITEMS)) {
				items = new JSONArray();
				put(KEY_ITEMS, items);
			} else {
				items = getJSONArray(KEY_ITEMS);
			}
			JSONObject item = new JSONObject();
			if (id != null) {
				item.put(KEY_ITEMS_ID, id);
			}
			if (name != null) {
				item.put(KEY_ITEMS_NAME, name);
			}
			if (category != null) {
				item.put(KEY_ITEMS_CATEGORY, category);
			}
			if (price != null) {
				item.put(KEY_ITEMS_PRICE, price);
			}
			if (quantity != null) {
				item.put(KEY_ITEMS_QUANTITY, quantity);
			}
			if (currency != null) {
				item.put(KEY_ITEMS_CURRENCY, currency);
			}
			items.put(item);
		} catch (JSONException e) {
			Log.w("Error adding %s to CommerceCustomData.", KEY_ITEMS, e);
		}
		return this;
	}
}
