/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.os.AsyncTask;
import android.widget.ImageView;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.comm.ApptentiveClient;
import com.apptentive.android.sdk.comm.ApptentiveHttpResponse;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Util;

import static com.apptentive.android.sdk.ApptentiveLogTag.UTIL;
import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class ApptentiveDownloaderTask extends AsyncTask<Object, Integer, ApptentiveHttpResponse> {

	private static boolean FILE_DOWNLOAD_REDIRECTION_ENABLED = false;

	private final FileDownloadListener listener;

	boolean download = false;

	public interface FileDownloadListener {
		void onDownloadStart();

		void onProgress(int progress);

		void onDownloadComplete();

		void onDownloadError();

		void onDownloadCancel();
	}

	public ApptentiveDownloaderTask(ImageView imageView, FileDownloadListener listener) {
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		download = true;
		listener.onDownloadStart();
	}

	@Override
	protected ApptentiveHttpResponse doInBackground(Object... params) {
		ApptentiveHttpResponse finished = new ApptentiveHttpResponse();
		try {
			finished = downloadBitmap((String) params[0], (String) params[1], (String) params[2]);
		} catch (Exception e) {
			ApptentiveLog.e(UTIL, e, "Error downloading bitmap");
			logException(e);
		}
		return finished;
	}

	//for 2.2 where onCancelled(Object obj) is not implemented
	@Override
	protected void onCancelled() {
		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
		onCancelled(ret);
	}

	@Override
	protected void onCancelled(ApptentiveHttpResponse response) {
		ApptentiveLog.v(UTIL, "ApptentiveDownloaderTask onCancelled, response code:  " + ((response != null) ? response.getCode() : ""));
		download = false;
		listener.onDownloadCancel();
	}

	@Override
	// Once the image is downloaded, associates it to the imageView
	protected void onPostExecute(ApptentiveHttpResponse response) {
		if (isCancelled()) {
			response.setCode(-1);
		}
		ApptentiveLog.v(UTIL, "ApptentiveDownloaderTask onPostExecute, response code:  " + response.getCode());

		if (response.isSuccessful()) {
			listener.onDownloadComplete();
		} else {
			listener.onDownloadError();
		}
	}

	/**
	 * Updating progress bar
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {
		super.onProgressUpdate(progress);
		listener.onProgress(progress[0]);
	}

	/**
	 * This function download the large file from the server
	 */
	private ApptentiveHttpResponse downloadBitmap(String urlString, String destFilePath, String conversationToken) {
		if (isCancelled()) {
			return null;
		}

		int count;

		boolean bRequestRedirectThroughApptentive = FILE_DOWNLOAD_REDIRECTION_ENABLED;

		HttpURLConnection connection = null;
		ApptentiveHttpResponse ret = new ApptentiveHttpResponse();
		String cookies = null;
		URL httpUrl;
		try {
			while (true) {
				httpUrl = new URL(urlString);
				connection = (HttpURLConnection) httpUrl.openConnection();
				if (bRequestRedirectThroughApptentive) {
					connection.setRequestProperty("User-Agent", ApptentiveClient.getUserAgentString());
					connection.setRequestProperty("Authorization", "OAuth " + conversationToken);
					connection.setRequestProperty("X-API-Version", String.valueOf(Constants.API_VERSION));
				} else if (cookies != null) {
					connection.setRequestProperty("Cookie", cookies);
				}

				connection.setConnectTimeout(Constants.DEFAULT_CONNECT_TIMEOUT_MILLIS);
				connection.setReadTimeout(Constants.DEFAULT_READ_TIMEOUT_MILLIS);
				connection.setRequestProperty("Accept-Encoding", "gzip");
				connection.setRequestProperty("Accept", "application/json");

				connection.setRequestMethod("GET");
				connection.setInstanceFollowRedirects(false);

				switch (connection.getResponseCode()) {
					case HttpURLConnection.HTTP_MOVED_PERM:
					case HttpURLConnection.HTTP_MOVED_TEMP:
					case HttpURLConnection.HTTP_SEE_OTHER: {
						bRequestRedirectThroughApptentive = false;
						String location = connection.getHeaderField("Location");
						URL base = new URL(urlString);
						URL next = new URL(base, location);  // Deal with relative URLs
						urlString = next.toExternalForm();
						// get the cookie if need, for login
						cookies = connection.getHeaderField("Set-Cookie");
						// Follow redirection
						continue;
					}
				}
				// End while loop
				break;
			}

			int responseCode = connection.getResponseCode();
			ret.setCode(responseCode);
			ret.setReason(connection.getResponseMessage());

			ApptentiveLog.v(UTIL, "Response Status Line: " + connection.getResponseMessage());

			// Get the Http response header values
			Map<String, String> headers = new HashMap<String, String>();
			Map<String, List<String>> map = connection.getHeaderFields();
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				headers.put(entry.getKey(), entry.getValue().toString());
			}

			ret.setHeaders(headers);

			if (ret.isSuccessful()) {
				// Read the normal content response
				InputStream input = null;
				FileOutputStream output = null;
				try {
					int fileLength = connection.getContentLength();

					// input stream to read file - with 8k buffer
					input = new BufferedInputStream(httpUrl.openStream(), 8192);
					output = new FileOutputStream(destFilePath);

					byte data[] = new byte[8192];

					long total = 0;

					while ((count = input.read(data)) != -1) {
						// allow canceling
						if (isCancelled()) {
							this.download = false;
							break;
						} else if (this.download) {
							total += count;
							// publishing the progress only if fileLength is known
							if (fileLength > 0) {
								publishProgress((int) ((total * 100) / fileLength));
							}
							output.write(data, 0, count);
						}
					}
					// flushing output
					output.flush();
					if (!this.download) {
						File fileToDelete = new File(destFilePath);
						fileToDelete.delete();
						publishProgress(-1);
					} else {
						publishProgress(100);
					}
				} finally {
					// closing streams
					Util.ensureClosed(output);
					Util.ensureClosed(input);
				}
			}
		} catch (IllegalArgumentException e) {
			ApptentiveLog.w(UTIL, e, "Error communicating with server.");
			logException(e);
		} catch (SocketTimeoutException e) {
			ApptentiveLog.w(UTIL, e, "Timeout communicating with server.");
			logException(e);
		} catch (final MalformedURLException e) {
			ApptentiveLog.w(UTIL, e, "ClientProtocolException");
			logException(e);
		} catch (final IOException e) {
			ApptentiveLog.w(UTIL, e, "ClientProtocolException");
			logException(e);
			try {
				ret.setContent(ApptentiveClient.getErrorResponse(connection, ret.isZipped()));
			} catch (IOException ex) {
				ApptentiveLog.w(UTIL, ex, "Can't read error stream.");
				logException(e);
			}
		}

		return ret;
	}

}