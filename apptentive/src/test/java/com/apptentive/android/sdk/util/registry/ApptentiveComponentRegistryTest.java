/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.registry;

import com.apptentive.android.sdk.TestCaseBase;
import org.junit.Test;

import static com.apptentive.android.sdk.util.registry.ApptentiveComponentRegistry.ComponentNotifier;

public class ApptentiveComponentRegistryTest extends TestCaseBase {

	//region Testing

	@Test
	public void testComponentRegistration() {

		ApptentiveComponentRegistry registry = new ApptentiveComponentRegistry();
		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});

		final CompA c1 = new CompA("A-1");
		final CompA c2 = new CompA("A-2");
		final CompA c3 = new CompA("A-3");

		registry.register(c1);
		registry.register(c2);
		registry.register(c3);

		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("A-A-1", "A-A-2", "A-A-3");

		registry.unregister(c2);
		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("A-A-1", "A-A-3");

		registry.unregister(c1);
		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("A-A-3");

		registry.unregister(c3);
		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult();
	}

	@Test
	public void testComponentNotification() {

		ApptentiveComponentRegistry registry = new ApptentiveComponentRegistry();
		registry.register(new CompA("A-1"));
		registry.register(new CompA("A-2"));
		registry.register(new CompAB("AB-1"));
		registry.register(new CompAB("AB-2"));
		registry.register(new CompC("C-1"));
		registry.register(new CompC("C-2"));

		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("A-A-1", "A-A-2", "A-AB-1", "A-AB-2");

		registry.notifyComponents(new ComponentNotifier<ListenerB>(ListenerB.class) {
			@Override
			public void onComponentNotify(ListenerB listener) {
				listener.OnTestEventB();
			}
		});
		assertResult("B-AB-1", "B-AB-2");

		registry.notifyComponents(new ComponentNotifier<ListenerC>(ListenerC.class) {
			@Override
			public void onComponentNotify(ListenerC listener) {
				listener.OnTestEventC();
			}
		});
		assertResult("C-C-1", "C-C-2");
	}

	//endregion

	//region Helpers

	interface ListenerA {
		void OnTestEventA();
	}

	interface ListenerB {
		void OnTestEventB();
	}

	interface ListenerC {
		void OnTestEventC();
	}

	class BaseComponent implements ApptentiveComponent{

		protected final String name;

		BaseComponent(String name) {
			this.name = name;
		}
	}

	class CompA extends BaseComponent implements ListenerA {

		CompA(String name) {
			super(name);
		}

		@Override
		public void OnTestEventA() {
			addResult("A-" + name);
		}
	}

	class CompAB extends BaseComponent implements ListenerA, ListenerB {

		CompAB(String name) {
			super(name);
		}

		@Override
		public void OnTestEventA() {
			addResult("A-" + name);
		}

		@Override
		public void OnTestEventB() {
			addResult("B-" + name);
		}
	}

	class CompC extends BaseComponent implements ListenerC {

		CompC(String name) {
			super(name);
		}

		@Override
		public void OnTestEventC() {
			addResult("C-" + name);
		}
	}

	//endregion
}