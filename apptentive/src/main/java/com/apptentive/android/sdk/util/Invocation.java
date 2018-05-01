/*
 * Copyright (c) 2018, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util;

import java.lang.reflect.Method;

public class Invocation {
	private static final Class<?>[] EMPTY_PARAMETER_TYPES = new Class<?>[0];
	private static final Object[] EMPTY_ARGS = new Object[0];

	private final Class<?> cls;
	private final Object target;

	public static Invocation fromClass(String name) throws InvocationException {
		if (name == null) {
			throw new IllegalArgumentException("Class name is null");
		}

		Class<?> cls = RuntimeUtils.classForName(name);
		if (cls == null) {
			throw new InvocationException("Unable to find class '%s'", name);
		}

		return fromClass(cls);
	}

	public static Invocation fromClass(Class<?> cls) {
		if (cls == null) {
			throw new IllegalArgumentException("Class is null");
		}

		return new Invocation(cls, null);
	}

	public static Invocation fromObject(Object target) {
		if (target == null) {
			throw new IllegalArgumentException("Target is null");
		}

		return new Invocation(target.getClass(), target);
	}

	private Invocation(Class<?> cls, Object target) {
		this.cls = cls;
		this.target = target;
	}

	public boolean invokeBooleanMethod(String name) throws InvocationException {
		return invokeBooleanMethod(name, EMPTY_PARAMETER_TYPES, EMPTY_ARGS);
	}

	public boolean invokeBooleanMethod(String name, Class<?>[] parameterTypes, Object[] args) throws InvocationException {
		Object result = invokeMethod(name, parameterTypes, args);
		if (result == null) {
			throw new InvocationException("Unable to invoke method '%s' on class '%s': null returned", name, cls);
		}

		if (!(result instanceof Boolean)) {
			throw new InvocationException("Unable to invoke method '%s' on class '%s': mismatch return type '%s'", name, cls, result.getClass());
		}

		return (Boolean) result;
	}

	public String invokeStringMethod(String name) throws InvocationException {
		return invokeStringMethod(name, EMPTY_PARAMETER_TYPES, EMPTY_ARGS);
	}

	public String invokeStringMethod(String name, Class<?>[] parameterTypes, Object[] args) throws InvocationException {
		Object result = invokeMethod(name, parameterTypes, args);
		if (result != null && !(result instanceof String)) {
			throw new InvocationException("Unable to invoke method '%s' on class '%s': mismatch return type '%s'", name, cls, result.getClass());

		}

		return (String) result;
	}

	public Object invokeMethod(String name, Class<?>[] parameterTypes, Object[] args) throws InvocationException {
		try {
			Method method = cls.getDeclaredMethod(name, parameterTypes);
			if (method == null) {
				throw new InvocationException("Unable to invoke method '%s' on class '%s': method not found", name, cls);
			}

			return method.invoke(target, args);
		} catch (InvocationException e) {
			throw e;
		} catch (Exception e) {
			throw new InvocationException(e, "Unable to invoke method '%s' on class '%s'", name, cls);
		}
	}
}
