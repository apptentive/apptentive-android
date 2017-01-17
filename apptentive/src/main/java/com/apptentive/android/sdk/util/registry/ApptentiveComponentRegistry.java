package com.apptentive.android.sdk.util.registry;

import com.apptentive.android.sdk.ApptentiveLog;

import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.debug.Assert.*;
import static com.apptentive.android.sdk.util.ObjectUtils.*;

public class ApptentiveComponentRegistry {
	/**
	 * List of currently registered components
	 */
	private final List<ApptentiveComponent> components; // TODO: weak references?

	public ApptentiveComponentRegistry() {
		components = new ArrayList<>();
	}

	//region Object registration

	public void register(ApptentiveComponent object) {
		assertNotNull(object, "Attempted to register a null object");
		if (object != null) {
			assertNotContains(components, object, "Object already registered: %s", object);
			if (!components.contains(object)) {
				components.add(object);
			}
		}
	}

	public void unregister(ApptentiveComponent object) {
		assertNotNull(object, "Attempted to unregister a null object");
		if (object != null) {
			assertContains(components, object, "Object is not registered: %s", object);
			components.remove(object);
		}
	}

	//endregion

	//region Notifications

	public <T extends ApptentiveComponent> void notify(Notifier<T> notifier) {

		List<T> qualifiedObjects = new ArrayList<>(components.size());

		// we put all the qualified components into a separate list to avoid ConcurrentModificationException
		Class<T> type = notifier.getType();
		for (ApptentiveComponent object : components) {
			T instance = as(object, type);
			if (instance != null) {
				qualifiedObjects.add(instance);
			}
		}

		// notify each object safely
		for (T object : qualifiedObjects) {
			try {
				notifier.notify(object);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while notifying object: %s", object);
			}
		}
	}

	//endregion

	//region Notifier helper class

	public static abstract class Notifier<T> {
		private final Class<T> type;

		public Notifier(Class<T> type) {
			this.type = type;
		}

		public abstract void notify(T object);

		public Class<T> getType() {
			return type;
		}
	}

	//endregion
}