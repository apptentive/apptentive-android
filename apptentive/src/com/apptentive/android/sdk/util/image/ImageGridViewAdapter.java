package com.apptentive.android.sdk.util.image;

/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.apptentive.android.sdk.util.Util;
import com.apptentive.android.sdk.view.ApptentiveMaterialDeterminateProgressBar;


public class ImageGridViewAdapter extends BaseAdapter {

	private static final int TYPE_CAMERA = 0;
	private static final int TYPE_IMAGE = 1;
	public static final int GONE = 0x00000008;

	private LayoutInflater inflater;
	private boolean showCamera = true;
	private boolean showImageIndicator = true;
	private int defaultImageIndicator;
	private Callback localCallback;

	private List<ImageItem> images = new ArrayList<ImageItem>();
	private List<ImageItem> selectedImages = new ArrayList<ImageItem>();

	// Items that are being downloaded
	private List<String> downloadItems = new ArrayList<String>();

	private int itemWidth;
	private int itemHeight;

	private GridView.LayoutParams itemLayoutParams;

	private boolean bHasWritePermission;

	public ImageGridViewAdapter(Context context, boolean showCamera) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.showCamera = showCamera;
		itemLayoutParams = new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT, GridView.LayoutParams.MATCH_PARENT);
		bHasWritePermission = (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable())
				&& Util.hasPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE");
	}

	/**
	 * selection indicator
	 *
	 * @param bval if true, overlay an indicator on the upper-right corner
	 */
	public void showImageIndicator(boolean bval) {
		showImageIndicator = bval;
	}

	/**
	 * selection indicator
	 *
	 * @param rid if true, overlay an indicator on the upper-right corner
	 */
	public void setImageIndicator(int rid) {
		defaultImageIndicator = rid;
	}

	public void setShowCamera(boolean bval) {
		if (showCamera == bval) {
			return;
		}

		showCamera = bval;
		notifyDataSetChanged();
	}

	public boolean isShowCamera() {
		return showCamera;
	}

	public void setIndicatorCallback(Callback localCallback) {
		this.localCallback = localCallback;
	}

	/**
	 * Click an image
	 *
	 * @param index
	 * @return True if handled here; False, let the caller handle it, i.e. launch 3rd party app to open attachment
	 */
	public boolean clickOn(int index) {
		ImageItem item = getItem(index);
		if (item == null || TextUtils.isEmpty(item.mimeType)) {
			return false;
		}
		// For non-image item, fisrt time tap will start download
		if (!Util.isMimeTypeImage(item.mimeType)) {
			// It is being downloaded, do nothing (prevent dobue tap, etc)
			if (downloadItems.contains(item.originalPath)) {
				return true;
			} else {
				// If no write permission, do not try to download. Instead let caller handles it by launching browser
				if (!bHasWritePermission) {
					return false;
				}
				File localFile = new File(item.localCachePath);
				if (localFile.exists() && ApptentiveAttachmentLoader.getInstance().isFileCompletelyDownloaded(item.localCachePath)) {
					// If have right permission, and already downloaded, let caller open 3rd app to view it
					return false;
				} else {
					// First tap detected and never download before, start download
					downloadItems.add(item.originalPath);
					notifyDataSetChanged();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Select an image
	 *
	 * @param image
	 */
	public void select(ImageItem image) {
		if (selectedImages.contains(image)) {
			selectedImages.remove(image);
		} else {
			selectedImages.add(image);
		}
		notifyDataSetChanged();
	}

	/**
	 * Re-Select image from selected list result
	 *
	 * @param resultList
	 */
	public void setDefaultSelected(ArrayList<String> resultList) {
		for (String uri : resultList) {
			ImageItem image = getImageByUri(uri);
			if (image != null) {
				selectedImages.add(image);
			}
		}
		if (selectedImages.size() > 0) {
			notifyDataSetChanged();
		}
	}

	private ImageItem getImageByUri(String uri) {
		if (images != null && images.size() > 0) {
			for (ImageItem image : images) {
				if (image.originalPath.equalsIgnoreCase(uri)) {
					return image;
				}
			}
		}
		return null;
	}

	/**
	 * Re-select image from selected image set
	 *
	 * @param images
	 */
	public void setData(List<ImageItem> images) {
		selectedImages.clear();

		if (images != null && images.size() > 0) {
			this.images = images;
		} else {
			this.images.clear();
		}
		notifyDataSetChanged();
	}

	/**
	 * Reset colum size
	 *
	 * @param columnWidth
	 */
	public void setItemSize(int columnWidth, int columnHeight) {

		if (itemWidth == columnWidth) {
			return;
		}

		itemWidth = columnWidth;
		itemHeight = columnHeight;

		itemLayoutParams = new GridView.LayoutParams(itemWidth, itemHeight);

		notifyDataSetChanged();
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (showCamera) {
			return position == 0 ? TYPE_CAMERA : TYPE_IMAGE;
		}
		return TYPE_IMAGE;
	}

	@Override
	public int getCount() {
		return showCamera ? images.size() + 1 : images.size();
	}

	@Override
	public ImageItem getItem(int i) {
		if (showCamera) {
			if (i == 0) {
				return null;
			}
			return images.get(i - 1);
		} else {
			return images.get(i);
		}
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {

		int type = getItemViewType(i);
		if (type == TYPE_CAMERA) {
			//view = inflater.inflate(R.layout.apptentive_image_picker_item_camera, viewGroup, false);
			view.setTag(null);
		} else if (type == TYPE_IMAGE) {
			ViewHolder holder;
			if (view == null) {
				view = inflater.inflate(R.layout.apptentive_image_grid_view_item, viewGroup, false);
				holder = new ViewHolder(view, i);
			} else {
				holder = (ViewHolder) view.getTag();
				if (holder == null) {
					view = inflater.inflate(R.layout.apptentive_image_grid_view_item, viewGroup, false);
					holder = new ViewHolder(view, i);
				}
			}
			if (holder != null) {
				holder.bindData(i);
			}
		}

		/** Fixed View Size */
		GridView.LayoutParams lp = (GridView.LayoutParams) view.getLayoutParams();
		if (lp.height != itemHeight) {
			view.setLayoutParams(itemLayoutParams);
		}

		return view;
	}

	class ViewHolder {
		ImageView image;
		ImageView indicator;
		TextView attachmentExtension;
		// a circular indeterminate progress bar showing local file loading progress
		ProgressBar progressBarLoading;
		// a horizontal determinate progress bar showing download progress
		ApptentiveMaterialDeterminateProgressBar progressBarDownload;
		int pos;

		View mask;
		boolean bLoadThumbnail;

		ViewHolder(View view, int index) {
			image = (ImageView) view.findViewById(R.id.image);
			indicator = (ImageView) view.findViewById(R.id.indicator);
			mask = view.findViewById(R.id.mask);
			attachmentExtension = (TextView) view.findViewById(R.id.image_file_extension);
			progressBarLoading = (ProgressBar) view.findViewById(R.id.thumbnail_progress);
			progressBarDownload = (ApptentiveMaterialDeterminateProgressBar) view.findViewById(R.id.thumbnail_progress_determinate);
			pos = index;
			view.setTag(this);
		}

		void bindData(final int index) {
			final ImageItem data = getItem(index);
			if (data == null) {
				return;
			}

			final int placeholderResId;
			// Image indicators are overlay controls, such as close button, check box
			if (showImageIndicator) {
				indicator.setVisibility(View.VISIBLE);
				image.setVisibility(View.VISIBLE);
				if (selectedImages.contains(data)) {
					// set as selected
					indicator.setImageResource(R.drawable.apptentive_ic_image_picker_selected);
					// Of item selection is enabled, show a translucant mask overlay for selected item
					mask.setVisibility(View.VISIBLE);
				} else {
					// set default indicator
					if (data.originalPath == null) {
						indicator.setVisibility(View.GONE);
						image.setVisibility(View.GONE);
					} else {
						indicator.setImageResource(defaultImageIndicator);
						indicator.setOnClickListener(new View.OnClickListener() {
							public void onClick(View v) {
								if (localCallback != null) {
									localCallback.onImageSelected(index);
								}
							}
						});
					}
					mask.setVisibility(View.GONE);
				}
			} else {
				indicator.setVisibility(View.GONE);
				if (data.originalPath == null) {
					image.setVisibility(View.GONE);
				} else {
					image.setVisibility(View.VISIBLE);
				}
			}

			if (Util.isMimeTypeImage(data.mimeType)) {
				if (TextUtils.isEmpty(data.originalPath)) {
					image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
					placeholderResId = R.drawable.apptentive_ic_add;
					indicator.setVisibility(View.GONE);
					bLoadThumbnail = false;
					progressBarLoading.setVisibility(View.GONE);
					attachmentExtension.setVisibility(View.GONE);
					progressBarDownload.setVisibility(View.GONE);
				} else {
					image.setScaleType(ImageView.ScaleType.FIT_CENTER);
					attachmentExtension.setVisibility(View.GONE);
					bLoadThumbnail = true;
					// show the circular progress bar while we load content...
					progressBarLoading.setVisibility(View.VISIBLE);
					placeholderResId = R.drawable.apptentive_ic_image_default_item;
				}
			} else {
				image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
				bLoadThumbnail = false;
				progressBarLoading.setVisibility(View.GONE);
				attachmentExtension.setVisibility(View.VISIBLE);
				attachmentExtension.setText("." + MimeTypeMap.getSingleton().getExtensionFromMimeType(data.mimeType));

				if (downloadItems.contains(data.originalPath)) {
					placeholderResId = R.drawable.apptentive_generic_file_thumbnail_download;
				} else {
					File localFile = new File(data.localCachePath);
					if (localFile.exists() && ApptentiveAttachmentLoader.getInstance().isFileCompletelyDownloaded(data.localCachePath)) {
						placeholderResId = R.drawable.apptentive_generic_file_thumbnail;
					} else {
						placeholderResId = R.drawable.apptentive_generic_file_thumbnail_download;
					}
				}
			}

			// Hide the progress bar till download starts
			if (progressBarDownload != null) {
				progressBarDownload.setVisibility(View.GONE);
			}

			image.setImageBitmap(null);
			image.setImageResource(placeholderResId);

			if (itemWidth > 0) {
				if (bLoadThumbnail) {
					ApptentiveAttachmentLoader.getInstance().load(data.originalPath, data.localCachePath, pos, image, itemWidth, itemHeight, true,
							new ApptentiveAttachmentLoader.LoaderCallback() {
								@Override
								public void onLoaded(ImageView view, int i, Bitmap d) {
									if (progressBarLoading != null) {
										progressBarLoading.setVisibility(View.GONE);
									}
									if (progressBarDownload != null) {
										progressBarDownload.setVisibility(View.GONE);
									}
									if (i == pos && image == view) {
										image.setImageBitmap(d);
									}
								}

								@Override
								public void onLoadTerminated() {
									if (progressBarLoading != null) {
										progressBarLoading.setVisibility(View.GONE);
									}
									if (progressBarDownload != null) {
										progressBarDownload.setVisibility(View.GONE);
									}
								}

								@Override
								public void onDownloadStart() {
									if (progressBarLoading != null) {
										progressBarLoading.setVisibility(View.GONE);
									}
									if (progressBarDownload != null) {
										progressBarDownload.setVisibility(View.VISIBLE);
										progressBarDownload.setProgress(0);
									}
								}

								@Override
								public void onDownloadProgress(int progress) {
									if (progressBarDownload != null) {
										// progress is -1 when download fails
										if (progress == 100 || progress == -1) {
											progressBarDownload.setVisibility(View.GONE);
											if (progressBarLoading != null) {
												if (progress == 100) {
													progressBarLoading.setVisibility(View.VISIBLE);
												} else {
													progressBarLoading.setVisibility(View.GONE);
												}
											}
										}
										if (progress >= 0) {
											progressBarDownload.setVisibility(View.VISIBLE);
											progressBarDownload.setProgress(progress);
										}
									}
								}
							});
				} else if (!TextUtils.isEmpty(data.originalPath) && downloadItems.contains(data.originalPath)) {
					ApptentiveAttachmentLoader.getInstance().load(data.originalPath, data.localCachePath, index, image, 0, 0, false,
							new ApptentiveAttachmentLoader.LoaderCallback() {
								@Override
								public void onLoaded(ImageView view, int pos, Bitmap d) {
									if (progressBarDownload != null) {
										progressBarDownload.setVisibility(View.GONE);
									}
									image.setImageResource(R.drawable.apptentive_generic_file_thumbnail);
									if (downloadItems.contains(data.originalPath)) {
										Log.d("ApptentiveAttachmentLoader onLoaded callback");
										downloadItems.remove(data.originalPath);
										Util.openFileAttachment(view.getContext(), data.originalPath, data.localCachePath, data.mimeType);
									}
								}

								@Override
								public void onLoadTerminated() {
									downloadItems.remove(data.originalPath);
									if (progressBarLoading != null) {
										progressBarLoading.setVisibility(View.GONE);
									}
									if (progressBarDownload != null) {
										progressBarDownload.setVisibility(View.GONE);
									}
								}

								@Override
								public void onDownloadStart() {
									if (progressBarDownload != null) {
										progressBarDownload.setVisibility(View.VISIBLE);
									}
									progressBarDownload.setProgress(0);
								}

								@Override
								public void onDownloadProgress(int progress) {
									if (progressBarDownload != null) {
										if (progress == -1) {
											downloadItems.remove(data.originalPath);
											progressBarDownload.setVisibility(View.GONE);
										} else if (progress >= 0) {
											progressBarDownload.setVisibility(View.VISIBLE);
											progressBarDownload.setProgress(progress);
											Log.d("ApptentiveAttachmentLoader progress callback: " + progress);
										}
									}
								}
							});
				} else {
					ApptentiveAttachmentLoader.getInstance().load(null, null, index, image, 0, 0, false, new ApptentiveAttachmentLoader.LoaderCallback() {
						@Override
						public void onLoaded(ImageView view, int pos, Bitmap d) {
							if (progressBarDownload != null) {
								progressBarDownload.setVisibility(View.GONE);
							}
							image.setImageBitmap(null);
							image.setImageResource(placeholderResId);
						}

						@Override
						public void onLoadTerminated() {
						}

						@Override
						public void onDownloadStart() {
						}

						@Override
						public void onDownloadProgress(int progress) {
						}
					});
				}
			}
		}
	}

	/**
	 * Callback Interface
	 */
	public interface Callback {

		void onImageSelected(int index);

		void onImageUnselected(String path);

		void onCameraShot(File imageFile);
	}

}