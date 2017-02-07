package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchQueueType;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.debug.Assert.assertTrue;

/**
 * Class for asynchronous HTTP requests handling
 */
public class HttpRequestManager {
	/**
	 * List of active requests (started but not yet finished)
	 */
	private List<HttpRequest> activeRequests;

	/**
	 * Dispatch queue for blocking network operations
	 */
	private final DispatchQueue networkQueue;

	/**
	 * Dispatch queue for listener callbacks
	 */
	private final DispatchQueue callbackQueue;

	private Listener listener;

	/**
	 * Creates a request manager with a default concurrent "network" queue. All listener callbacks are
	 * dispatched on the main queue.
	 */
	public HttpRequestManager() {
		this(DispatchQueue.createBackgroundQueue("Apptentive Network Queue", DispatchQueueType.Concurrent), DispatchQueue.mainQueue());
	}

	/**
	 * Creates a request manager with custom "network" and "callback" queues
	 *
	 * @param networkQueue  - dispatch queue for blocking network operations
	 * @param callbackQueue - dispatch queue for listener callbacks
	 * @throws IllegalArgumentException if any of specified queues is null
	 */
	public HttpRequestManager(DispatchQueue networkQueue, DispatchQueue callbackQueue) {
		if (networkQueue == null) {
			throw new IllegalArgumentException("Network queue is null");
		}
		if (callbackQueue == null) {
			throw new IllegalArgumentException("Callback queue is null");
		}

		this.networkQueue = networkQueue;
		this.callbackQueue = callbackQueue;
		this.activeRequests = new ArrayList<>();
	}

	//region Requests

	/**
	 * Starts network request on the network queue (method returns immediately)
	 */
	public synchronized void startRequest(final HttpRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request is null");
		}

		registerRequest(request);

		networkQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				try {
					request.dispatchSync();
				} finally {
					unregisterRequest(request);
				}
			}
		});

		notifyRequestStarted(request);
	}

	/**
	 * Cancel all active requests
	 */
	public synchronized void cancelAll() {
		if (activeRequests.size() > 0) {
			List<HttpRequest> temp = new ArrayList<>(activeRequests);
			for (HttpRequest request : temp) {
				request.cancel();
			}
			activeRequests.clear();
			notifyCancelledAllRequests();
		}
	}

	/**
	 * Register active request
	 */
	synchronized void registerRequest(HttpRequest request) {
		activeRequests.add(request);
	}

	/**
	 * Unregisters active request
	 */
	synchronized void unregisterRequest(HttpRequest request) {
		boolean removed = activeRequests.remove(request);
		assertTrue(removed, "Attempted to unregister missing request: %s", request);

		if (removed) {
			notifyRequestFinished(request);
		}
	}

	//endregion

	//region Listener callbacks

	private void notifyRequestStarted(final HttpRequest request) {
		callbackQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				if (listener != null) {
					listener.onRequestStart(HttpRequestManager.this, request);
				}
			}
		});
	}

	private void notifyRequestFinished(final HttpRequest request) {
		callbackQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				if (listener != null) {
					listener.onRequestFinish(HttpRequestManager.this, request);
				}
			}
		});
	}

	private void notifyCancelledAllRequests() {
		callbackQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				if (listener != null) {
					listener.onRequestsCancel(HttpRequestManager.this);
				}
			}
		});
	}

	//endregion

	//region Getters/Setters

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	//endregion

	//region Listener

	public interface Listener {
		void onRequestStart(HttpRequestManager manager, HttpRequest request);

		void onRequestFinish(HttpRequestManager manager, HttpRequest request);

		void onRequestsCancel(HttpRequestManager manager);
	}

	//endregion
}
