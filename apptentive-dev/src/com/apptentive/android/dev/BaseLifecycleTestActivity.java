package com.apptentive.android.dev;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Sky Kelsey
 */
public abstract class BaseLifecycleTestActivity extends Activity {
	private final String LOG_TAG = "LIFECYCLE_TEST";

	int spawnDepth;
	String method;
	boolean startSameActivity;
	int pause;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e(LOG_TAG, "onCreate(): " + this);

		final Intent intent = getIntent();
		spawnDepth = intent.getIntExtra("spawn_depth", 3) - 1;
		method = intent.getStringExtra("method");
		startSameActivity = intent.getBooleanExtra("start_same_activity", true);
		pause = intent.getIntExtra("pause", 0);

		super.onCreate(savedInstanceState);

		if (method.equals("onCreate")) {
			startActivity();
		} else if (method.equals("thread")) {
			AsyncTask task = new AsyncTask() {
				@Override
				protected Object doInBackground(Object... objects) {
					Log.e(LOG_TAG, "Starting new Activity now.");
					startActivity();
					return null;
				}
			};
			task.execute();
		}
	}

	@Override
	protected void onStart() {
		Log.e(LOG_TAG, "onStart(): " + this);
		super.onStart();
		if (method.equals("onStart")) {
			startActivity();
		}
	}

	@Override
	protected void onResume() {
		Log.e(LOG_TAG, "onResume(): " + this);
		super.onResume();
		if (method.equals("onResume")) {
			startActivity();
		}
	}

	@Override
	protected void onPause() {
		Log.e(LOG_TAG, "onPause(): " + this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.e(LOG_TAG, "onStop(): " + this);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.e(LOG_TAG, "onDestroy(): " + this);
		super.onDestroy();
	}

	private void startActivity() {
		if ((spawnDepth) <= 0) {
			Log.e("LOG_TAG", "Depth reached. Done launching Activities.");
			return;
		}

		if (pause > 0) {
			try {
				Thread.sleep(pause);
			} catch (Exception e) {
			}
		}

		Intent intent = new Intent();
		intent.setClass(this, getChildClassToLaunch());
		intent.putExtra("spawn_depth", spawnDepth);
		intent.putExtra("method", method);
		intent.putExtra("start_same_activity", startSameActivity);
		intent.putExtra("pause", pause);
		startActivity(intent);
	}

	abstract Class getChildClassToLaunch();
}
