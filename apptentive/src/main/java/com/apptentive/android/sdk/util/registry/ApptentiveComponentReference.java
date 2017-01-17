package com.apptentive.android.sdk.util.registry;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * A simple subclass of WeakReference to handle <code>{@link ApptentiveComponent}</code> objects
 */
class ApptentiveComponentReference extends WeakReference<ApptentiveComponent> {
	public ApptentiveComponentReference(ApptentiveComponent referent) {
		super(referent);
	}

	public ApptentiveComponentReference(ApptentiveComponent referent, ReferenceQueue<? super ApptentiveComponent> q) {
		super(referent, q);
	}
}
