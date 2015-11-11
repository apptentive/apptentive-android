/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.util.task;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


import android.os.AsyncTask;
import android.widget.ImageView;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.util.Util;

/**
 * @author Barry Li
 */
public class ApptentiveDownloaderTask extends AsyncTask<Object, Integer, Boolean> {
	private final FileDownloadListener listener;
	boolean download = false;


	public interface FileDownloadListener {
		public void onDownloadStart();

		public void onProgress(int progress);

		public void onDownloadComplete();

		public void onDownloadError();

		public void onDownloadCancel();
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
	protected Boolean doInBackground(Object... params) {
		Boolean finished = false;
		try {
			finished = downloadBitmap((String) params[0], (String) params[1]);
		} catch (Exception e) {
			Log.d("Error downloading bitmap", e);
		}
		return finished;
	}

	//for 2.2 where onCancelled(Object obj) is not implemented
	@Override
	protected void onCancelled() {
		onCancelled(false);
	}

	@Override
	protected void onCancelled(Boolean done) {
		Log.d("onCancelled(Boolean):  " + done);
		download = false;
		listener.onDownloadCancel();
	}

	@Override
	// Once the image is downloaded, associates it to the imageView
	protected void onPostExecute(Boolean done) {
		if (isCancelled()) {
			done = false;
		}
		Log.d("onPostExecute:  " + done);

		if (done) {
			listener.onDownloadComplete();
		} else {
			listener.onDownloadError();
		}
	}

	/**
	 * Updating progress bar
	 * */
	@Override
	protected void onProgressUpdate(Integer... progress) {

		super.onProgressUpdate(progress);
		listener.onProgress(progress[0]);
		// setting progress percentage
		// pDialog.setProgress(Integer.parseInt(progress[0]));
	}

	/**
	 * This function download the large file from the server
	 *
	 *
	 */
	private Boolean downloadBitmap(String urlstr, String destFilePath) {
		if (isCancelled()) {
			return false;
		}

		Boolean finished = true;
		int count;
		InputStream input = null;
		FileOutputStream output = null;
		try {
			URL url = new URL(urlstr);
			URLConnection conection = url.openConnection();
			conection.connect();
			// getting file length
			int lenghtOfFile = conection.getContentLength();

			// input stream to read file - with 8k buffer
			input = new BufferedInputStream(url.openStream(),
					8192);
			output = new FileOutputStream(destFilePath);

			byte data[] = new byte[1024];

			long total = 0;

			while ((count = input.read(data)) != -1) {
				if(this.download){
					total += count;
					// publishing the progress....
					publishProgress((int) ((total * 100) / lenghtOfFile));
					output.write(data, 0, count);
				}
			}
			// flushing output
			output.flush();
			if(!this.download){
				File delete = new File(destFilePath);
				delete.delete();
				publishProgress(-1);
			} else {
				publishProgress(100);
			}

		} catch (Exception e) {
			Log.e("Error: ", e.getMessage());
			publishProgress(-1);
		} finally {
			// closing streams
			Util.ensureClosed(output);
			Util.ensureClosed(input);
		}
		return finished;
	}

	/*private void setProgress(ProgressBar pr, int position) {
		ProgressBarSeek pbarSeek = new ProgressBarSeek();
		pbarSeek.setPosition(position);
		pbarSeek.setProgressValue(pr.getProgress());
		progreeSeekList.add(pbarSeek);
	}

	private void getProgress(ProgressBar pr, int position, Button cl, Button dl) {
		if (progreeSeekList.size() > 0) {
			for (int j = 0; j < progreeSeekList.size(); j++) {
				if (position == progreeSeekList.get(j).getPosition()) {
					pr.setProgress(progreeSeekList.get(j).getProgressValue());
					dl.setVisibility(View.GONE);
					cl.setVisibility(View.VISIBLE);
				}
			}
		}
	}*/

}