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

		final ComponentA c1 = new ComponentA("ComponentA-1");
		final ComponentA c2 = new ComponentA("ComponentA-2");
		final ComponentA c3 = new ComponentA("ComponentA-3");

		registry.register(c1);
		registry.register(c2);
		registry.register(c3);

		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("ListenerA-ComponentA-1", "ListenerA-ComponentA-2", "ListenerA-ComponentA-3");

		registry.unregister(c2);
		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("ListenerA-ComponentA-1", "ListenerA-ComponentA-3");

		registry.unregister(c1);
		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("ListenerA-ComponentA-3");

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
		registry.register(new ComponentA("ComponentA-1"));
		registry.register(new ComponentA("ComponentA-2"));
		registry.register(new ComponentAB("ComponentAB-1"));
		registry.register(new ComponentAB("ComponentAB-2"));
		registry.register(new ComponentC("ComponentC-1"));
		registry.register(new ComponentC("ComponentC-2"));

		registry.notifyComponents(new ComponentNotifier<ListenerA>(ListenerA.class) {
			@Override
			public void onComponentNotify(ListenerA listener) {
				listener.OnTestEventA();
			}
		});
		assertResult("ListenerA-ComponentA-1", "ListenerA-ComponentA-2", "ListenerA-ComponentAB-1", "ListenerA-ComponentAB-2");

		registry.notifyComponents(new ComponentNotifier<ListenerB>(ListenerB.class) {
			@Override
			public void onComponentNotify(ListenerB listener) {
				listener.OnTestEventB();
			}
		});
		assertResult("ListenerB-ComponentAB-1", "ListenerB-ComponentAB-2");

		registry.notifyComponents(new ComponentNotifier<ListenerC>(ListenerC.class) {
			@Override
			public void onComponentNotify(ListenerC listener) {
				listener.OnTestEventC();
			}
		});
		assertResult("ListenerC-ComponentC-1", "ListenerC-ComponentC-2");
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

	class ComponentA extends BaseComponent implements ListenerA {

		ComponentA(String name) {
			super(name);
		}

		@Override
		public void OnTestEventA() {
			addResult("ListenerA-" + name);
		}
	}

	class ComponentAB extends BaseComponent implements ListenerA, ListenerB {

		ComponentAB(String name) {
			super(name);
		}

		@Override
		public void OnTestEventA() {
			addResult("ListenerA-" + name);
		}

		@Override
		public void OnTestEventB() {
			addResult("ListenerB-" + name);
		}
	}

	class ComponentC extends BaseComponent implements ListenerC {

		ComponentC(String name) {
			super(name);
		}

		@Override
		public void OnTestEventC() {
			addResult("ListenerC-" + name);
		}
	}

	//endregion
}