/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.messagecenter;

import android.widget.EditText;

import com.apptentive.android.sdk.module.messagecenter.view.holder.MessageComposerHolder;
import com.apptentive.android.sdk.util.image.ApptentiveImageGridView;
import com.apptentive.android.sdk.util.image.ImageItem;

public interface OnListviewItemActionListener {
	void onComposingViewCreated(MessageComposerHolder composer, EditText composerEditText, ApptentiveImageGridView attachments);

	void beforeComposingTextChanged(CharSequence str);

	void onComposingTextChanged(CharSequence str);

	void afterComposingTextChanged(String str);

	void onCancelComposing();

	void onFinishComposing();

	void onWhoCardViewCreated(EditText nameEditText, EditText emailEditText);

	void onSubmitWhoCard(String buttonLabel);

	void onCloseWhoCard(String buttonLabel);

	void onAttachImage();

	void onClickAttachment(int position, ImageItem image);
}