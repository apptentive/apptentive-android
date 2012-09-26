package com.apptentive.android.dev;

/**
 * @author Sky Kelsey
 */
public class LifecycleTestActivityTwo extends BaseLifecycleTestActivity {
	@Override
	Class getChildClassToLaunch() {
		return LifecycleTestActivityThree.class;
	}
}
