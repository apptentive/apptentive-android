/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommerceExtendedData extends ExtendedData {

	private static final String KEY_ID = "id";
	private static final String KEY_AFFILIATION = "affiliation";
	private static final String KEY_REVENUE = "revenue";
	private static final String KEY_SHIPPING = "shipping";
	private static final String KEY_TAX = "tax";
	private static final String KEY_CURRENCY = "currency";
	private static final String KEY_ITEMS = "items";

	private static final int VERSION = 1;

	private Object id;
	private String affiliation;
	private double revenue;
	private double shipping;
	private double tax;
	private String currency;
	private List<Item> items;

	@Override
	protected void init() {
		items = new ArrayList<>();
		setType(Type.commerce);
		setVersion(VERSION);
	}

	public CommerceExtendedData() {
		super();
	}

	public CommerceExtendedData(Object id, String affiliation, double revenue, double shipping, double tax, String currency) throws JSONException {
		setId(id);
		setAffiliation(affiliation);
		setRevenue(revenue);
		setShipping(shipping);
		setTax(tax);
		setCurrency(currency);
	}

	public CommerceExtendedData(String json) throws JSONException {
		super(json);
		JSONObject jsonObject = new JSONObject(json);
		setId(jsonObject.opt(KEY_ID));
		setAffiliation(jsonObject.optString(KEY_AFFILIATION, null));
		setRevenue(jsonObject.optDouble(KEY_REVENUE, 0));
		setShipping(jsonObject.optDouble(KEY_SHIPPING, 0));
		setTax(jsonObject.optDouble(KEY_TAX, 0));
		setCurrency(jsonObject.optString(KEY_CURRENCY, null));
		setItems(jsonObject.optJSONArray(KEY_ITEMS));
	}

	public CommerceExtendedData setId(Object id) throws JSONException {
		this.id = id;
		return this;
	}

	public CommerceExtendedData setAffiliation(String affiliation) throws JSONException {
		this.affiliation = affiliation;
		return this;
	}

	public CommerceExtendedData setRevenue(double revenue) throws JSONException {
		this.revenue = revenue;
		return this;
	}

	public CommerceExtendedData setShipping(double shipping) throws JSONException {
		this.shipping = shipping;
		return this;
	}

	public CommerceExtendedData setTax(double tax) throws JSONException {
		this.tax = tax;
		return this;
	}

	public CommerceExtendedData setCurrency(String currency) throws JSONException {
		this.currency = currency;
		return this;
	}

	/**
	 * Add information about a purchased item to this record. Calls to this method can be chained.
	 *
	 * @param item A {@link CommerceExtendedData.Item}
	 * @return This object.
	 * @throws JSONException if item can't be added.
	 */
	public CommerceExtendedData addItem(Item item) throws JSONException {
		if (this.items == null) {
			this.items = new ArrayList<>();
		}
		items.add(item);
		return this;
	}

	public CommerceExtendedData setItems(JSONArray items) throws JSONException {
		if (items != null) {
			for (int i = 0; i < items.length(); i++) {
				addItem(new Item((items.getJSONObject(i)).toString()));
			}
		}
		return this;
	}

	@Override
	public JSONObject toJsonObject() throws JSONException {
		JSONObject ret = super.toJsonObject();
		ret.put(KEY_ID, id);
		ret.put(KEY_AFFILIATION, affiliation);
		ret.put(KEY_REVENUE, revenue);
		ret.put(KEY_SHIPPING, shipping);
		ret.put(KEY_TAX, tax);
		ret.put(KEY_CURRENCY, currency);
		JSONArray itemsArray = new JSONArray();
		for (Item item : items) {
			itemsArray.put(item.toJsonObject());
		}
		ret.put(KEY_ITEMS, itemsArray);
		return ret;
	}

	public static class Item implements Serializable {

		private static final String KEY_ID = "id";
		private static final String KEY_NAME = "name";
		private static final String KEY_CATEGORY = "category";
		private static final String KEY_PRICE = "price";
		private static final String KEY_QUANTITY = "quantity";
		private static final String KEY_CURRENCY = "currency";

		private Object id;
		private String name;
		private String category;
		private double price;
		private double quantity;
		private String currency;

		public Item() {
		}

		public Item(String json) throws JSONException {
			JSONObject jsonObject = new JSONObject(json);
			this.id = jsonObject.opt(KEY_ID);
			this.name = jsonObject.optString(KEY_NAME, null);
			this.category = jsonObject.optString(KEY_CATEGORY, null);
			this.price = jsonObject.optDouble(KEY_PRICE, 0);
			this.quantity = jsonObject.optDouble(KEY_QUANTITY, 0);
			this.currency = jsonObject.optString(KEY_CURRENCY, null);
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
		 * @throws JSONException if values cannot be set.
		 */
		public Item(Object id, String name, String category, double price, double quantity, String currency) throws JSONException {
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
			setPrice(price);
			setQuantity(quantity);
			if (currency != null) {
				setCurrency(currency);
			}
		}

		public Item setId(Object id) throws JSONException {
			this.id = id;
			return this;
		}

		public Item setName(String name) throws JSONException {
			this.name = name;
			return this;
		}

		public Item setCategory(String category) throws JSONException {
			this.category = category;
			return this;
		}

		public Item setPrice(double price) throws JSONException {
			this.price = price;
			return this;
		}

		public Item setQuantity(double quantity) throws JSONException {
			this.quantity = quantity;
			return this;
		}

		public Item setCurrency(String currency) throws JSONException {
			this.currency = currency;
			return this;
		}

		public JSONObject toJsonObject() throws JSONException {
			JSONObject ret = new JSONObject();
			ret.put(KEY_ID, id);
			ret.put(KEY_NAME, name);
			ret.put(KEY_CATEGORY, category);
			ret.put(KEY_PRICE, price);
			ret.put(KEY_QUANTITY, quantity);
			ret.put(KEY_CURRENCY, currency);
			return ret;
		}
	}
}
