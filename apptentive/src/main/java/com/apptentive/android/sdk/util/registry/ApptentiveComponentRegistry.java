package com.apptentive.android.sdk.util.registry;

import com.apptentive.android.sdk.ApptentiveLog;

import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.debug.Assert.*;
import static com.apptentive.android.sdk.util.ObjectUtils.*;

public class ApptentiveComponentRegistry {
	/**
	 * List of references for currently registered components
	 */
	private final List<ApptentiveComponentReference> componentReferences;

	public ApptentiveComponentRegistry() {
		componentReferences = new ArrayList<>();
	}

	//region Object registration

	/** Register < */
	public void register(ApptentiveComponent component) {
		assertNotNull(component, "Attempted to register a null component");
		if (component != null) {
			boolean alreadyRegistered = isRegistered(component);
			assertFalse(alreadyRegistered, "Attempted to register component twice: %s", component);
			if (!alreadyRegistered) {
				componentReferences.add(new ApptentiveComponentReference(component));
			}
		}
	}

	public void unregister(ApptentiveComponent component) {
		assertNotNull(component, "Attempted to unregister a null component");
		if (component != null) {
			int index = indexOf(component);
			assertNotEquals(index, -1, "Attempted to unregister component twice: %s", component);
			if (index != -1) {
				componentReferences.remove(index);
			}
		}
	}

	/** Returns true if <code>component</code> is already registered */
	public boolean isRegistered(ApptentiveComponent component) {
		assertNotNull(component);
		return component != null && indexOf(component) != -1;
	}

	private int indexOf(ApptentiveComponent component) {
		int index = 0;
		for (ApptentiveComponentReference componentReference : componentReferences) {
			if (componentReference.get() == component) {
				return index;
			}
			++index;
		}

		return -1;
	}

	//endregion

	//region Notifications

	public <T> void notifyComponents(ComponentNotifier<T> notifier) {

		List<T> components = new ArrayList<>(componentReferences.size());
		boolean hasLostReferences = false;

		// we put all the qualified components into a separate list to avoid ConcurrentModificationException
		Class<T> targetType = notifier.getType();
		for (ApptentiveComponentReference reference : componentReferences) {
			ApptentiveComponent component = reference.get();
			if (component == null) {
				hasLostReferences = true;
				continue;
			}

			T target = as(component, targetType);
			if (target != null) {
				components.add(target);
			}
		}

		// notify each object safely
		for (T component : components) {
			try {
				notifier.onComponentNotify(component);
			} catch (Exception e) {
				ApptentiveLog.e(e, "Exception while notifying object: %s", component);
			}
		}

		// cleanup up lost references
		if (hasLostReferences) {
			for (int i = componentReferences.size() - 1; i >= 0; --i) {
				if (componentReferences.get(i).isReferenceLost()) {
					componentReferences.remove(i);
				}
			}
		}
	}

	//endregion

	//region Component notifier helper class

	public static abstract class ComponentNotifier<T> {
		private final Class<T> type;

		public ComponentNotifier(Class<T> type) {
			this.type = type;
		}

		public abstract void onComponentNotify(T object);

		public Class<T> getType() {
			return type;
		}
	}

	//endregion
}