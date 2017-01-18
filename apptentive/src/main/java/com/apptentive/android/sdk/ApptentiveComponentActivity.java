package com.apptentive.android.sdk;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.apptentive.android.sdk.util.registry.ApptentiveComponentRegistry;
import com.apptentive.android.sdk.util.registry.ApptentiveComponent;

import static com.apptentive.android.sdk.util.ObjectUtils.*;

/** A base class for any SDK activity */
public class ApptentiveComponentActivity extends AppCompatActivity implements ApptentiveComponent {

	//region Activity life cycle

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerComponent();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterComponent();
	}

	//endregion

	//region Object registry

	private void registerComponent() {
		ApptentiveComponentRegistry componentRegistry = getComponentRegistry();
		if (componentRegistry != null) {
			componentRegistry.register(this);
		}
	}

	private void unregisterComponent() {
		ApptentiveComponentRegistry componentRegistry = getComponentRegistry();
		if (componentRegistry != null) {
			componentRegistry.unregister(this);
		}
	}

	private ApptentiveComponentRegistry getComponentRegistry() {
		ApptentiveInternal instance = notNull(ApptentiveInternal.getInstance(), "ApptentiveInternal is not initialized");
		if (instance == null) {
			return null;
		}

		return notNull(instance.getComponentRegistry(), "ApptentiveComponentRegistry is not initialized");
	}

	//endregion
}
