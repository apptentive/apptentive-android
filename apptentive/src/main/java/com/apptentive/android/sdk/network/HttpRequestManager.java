package com.apptentive.android.sdk.network;

import com.apptentive.android.sdk.util.StringUtils;
import com.apptentive.android.sdk.util.threading.DispatchQueue;
import com.apptentive.android.sdk.util.threading.DispatchQueueType;
import com.apptentive.android.sdk.util.threading.DispatchTask;

import java.util.ArrayList;
import java.util.List;

import static com.apptentive.android.sdk.debug.Assert.*;

/**
 * Class for asynchronous HTTP requests handling.
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

	private Listener listener;
	private HttpRequest.Injector requestInjector;

	/**
	 * Creates a request manager with custom network dispatch queue
	 *
	 * @param networkQueue - dispatch queue for blocking network operations
	 * @throws IllegalArgumentException if queue is null
	 */
	public HttpRequestManager(DispatchQueue networkQueue) {
		if (networkQueue == null) {
			throw new IllegalArgumentException("Network queue is null");
		}
		this.networkQueue = networkQueue;
		this.activeRequests = new ArrayList<>();
	}

	//region Requests

	/**
	 * Starts network request on the network queue (method returns immediately)
	 */
	synchronized HttpRequest startRequest(HttpRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request is null");
		}

		if (requestInjector != null) {
			request.setInjector(requestInjector);
		}

		registerRequest(request);
		dispatchRequest(request);
		notifyRequestStarted(request);

		return request;
	}

	/**
	 * Handles request synchronously
	 */
	void dispatchRequest(final HttpRequest request) {
		networkQueue.dispatchAsync(new DispatchTask() {
			@Override
			protected void execute() {
				request.dispatchSync(networkQueue);
			}
		});
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
		}
		notifyCancelledAllRequests();
	}

	/**
	 * Register active request
	 */
	synchronized void registerRequest(HttpRequest request) {
		assertTrue(this == request.requestManager);
		activeRequests.add(request);
	}

	/**
	 * Unregisters active request
	 */
	synchronized void unregisterRequest(HttpRequest request) {
		assertTrue(this == request.requestManager);
		boolean removed = activeRequests.remove(request);
		assertTrue(removed, "Attempted to unregister missing request: %s", request);

		if (removed) {
			notifyRequestFinished(request);
		}
	}

	/**
	 * Returns a request with a specified tag or <code>null</code> is not found
	 */
	public synchronized HttpRequest findRequest(String tag) {
		for (HttpRequest request : activeRequests) {
			if (StringUtils.equal(request.getTag(), tag)) {
				return request;
			}
		}
		return null;
	}

	//endregion

	//region Listener callbacks

	private void notifyRequestStarted(final HttpRequest request) {
		if (listener != null) {
			listener.onRequestStart(HttpRequestManager.this, request);
		}
	}

	private void notifyRequestFinished(final HttpRequest request) {
		if (listener != null) {
			listener.onRequestFinish(HttpRequestManager.this, request);
		}
	}

	private void notifyCancelledAllRequests() {
		if (listener != null) {
			listener.onRequestsCancel(HttpRequestManager.this);
		}
	}

	//endregion

	//region Getters/Setters

	public static HttpRequestManager sharedManager() {
		return Holder.INSTANCE;
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	public void setRequestInjector(HttpRequest.Injector requestInjector) {
		this.requestInjector = requestInjector;
	}

	//endregion

	//region Listener

	public interface Listener {
		void onRequestStart(HttpRequestManager manager, HttpRequest request);

		void onRequestFinish(HttpRequestManager manager, HttpRequest request);

		void onRequestsCancel(HttpRequestManager manager);
	}

	//endregion

	//region Holder

	private static class Holder {
		private static final HttpRequestManager INSTANCE = new HttpRequestManager(DispatchQueue.createBackgroundQueue("Apptentive Network Queue", DispatchQueueType.Concurrent));
	}

	//endregion
}
