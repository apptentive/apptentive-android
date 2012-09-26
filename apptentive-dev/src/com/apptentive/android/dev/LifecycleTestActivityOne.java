package com.apptentive.android.dev;

/**
 * @author Sky Kelsey
 */
public class LifecycleTestActivityOne extends BaseLifecycleTestActivity {

	@Override
	Class getChildClassToLaunch() {
		if (startSameActivity) {
			return LifecycleTestActivityOne.class;
		} else {
			return LifecycleTestActivityTwo.class;
		}
	}
}
