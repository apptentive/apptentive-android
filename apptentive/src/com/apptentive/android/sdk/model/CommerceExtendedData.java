/*
 * Copyright (c) 2014, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

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

	private static final int VERSION = 1;


	@Override
	protected void init() {
		setType(Type.commerce);
		setVersion(VERSION);
	}

	public CommerceExtendedData() {
		super();
	}

	public CommerceExtendedData(String json) throws JSONException {
		super(json);
	}

	public CommerceExtendedData(Object id, Object affiliation, Number revenue, Number shipping, Number tax, String currency) throws JSONException {
		setId(id);
		setAffiliation(affiliation);
		setRevenue(revenue);
		setShipping(shipping);
		setTax(tax);
		setCurrency(currency);
	}

	public CommerceExtendedData setId(Object id) throws JSONException {
		put(KEY_ID, id);
		return this;
	}

	public CommerceExtendedData setAffiliation(Object affiliation) throws JSONException {
		put(KEY_AFFILIATION, affiliation);
		return this;
	}

	public CommerceExtendedData setRevenue(Number revenue) throws JSONException {
		put(KEY_REVENUE, revenue);
		return this;
	}

	public CommerceExtendedData setShipping(Number shipping) throws JSONException {
		put(KEY_SHIPPING, shipping);
		return this;
	}

	public CommerceExtendedData setTax(Number tax) throws JSONException {
		put(KEY_TAX, tax);
		return this;
	}

	public CommerceExtendedData setCurrency(String currency) throws JSONException {
		put(KEY_CURRENCY, currency);
		return this;
	}

	/**
	 * Add information about a purchased item to this record. Calls to this method can be chained.
	 *
	 * @param item A {@link CommerceExtendedData.Item}
	 * @return This object.
	 */
	public CommerceExtendedData addItem(Item item) throws JSONException {
		JSONArray items;
		if (isNull(KEY_ITEMS)) {
			items = new JSONArray();
			put(KEY_ITEMS, items);
		} else {
			items = getJSONArray(KEY_ITEMS);
		}
		items.put(item);
		return this;
	}

	public static class Item extends JSONObject {

		private static final String KEY_ID = "id";
		private static final String KEY_NAME = "name";
		private static final String KEY_CATEGORY = "category";
		private static final String KEY_PRICE = "price";
		private static final String KEY_QUANTITY = "quantity";
		private static final String KEY_CURRENCY = "currency";

		public Item() {
			super();
		}

		public Item(String json) throws JSONException {
			super(json);
		}

		/**
		 * Create a record of an item that was purchased.
		 *
		 * @param id       The ID of this item. May be a String or Number. Ignored if null.
		 * @param name     The name of this item. May be a String or Number. Ignored if null.
		 * @param category The item category. Ignored if null.
		 * @param price    The individual item price. Ignored if null.
		 * @param quantity The number of units purchased. Ignored if null.
		 * @param currency The currency code for the currency used in this transaction. Ignored if null.
		 */
		public Item(Object id, Object name, String category, Number price, Number quantity, String currency) throws JSONException {
			super();
			if (id != null) {
				setId(id);
			}
			if (name != null) {
				setName(name);
			}
			if (category != null) {
				setCategory(category);
			}
			if (price != null) {
				setPrice(price);
			}
			if (quantity != null) {
				setQuantity(quantity);
			}
			if (currency != null) {
				setCurrency(currency);
			}
		}

		public Item setId(Object id) throws JSONException {
			put(KEY_ID, id);
			return this;
		}

		public Item setName(Object name) throws JSONException {
			put(KEY_NAME, name);
			return this;
		}

		public Item setCategory(String category) throws JSONException {
			put(KEY_CATEGORY, category);
			return this;
		}

		public Item setPrice(Number price) throws JSONException {
			put(KEY_PRICE, price);
			return this;
		}

		public Item setQuantity(Number quantity) throws JSONException {
			put(KEY_QUANTITY, quantity);
			return this;
		}

		public Item setCurrency(String currency) throws JSONException {
			put(KEY_CURRENCY, currency);
			return this;
		}
	}
}
