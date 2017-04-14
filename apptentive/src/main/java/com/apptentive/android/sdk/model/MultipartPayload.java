/*
 * Copyright (c) 2017, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import java.util.List;

/**
 * Interface for payloads which should be send with an http-multipart request
 */
public interface MultipartPayload {
	List<StoredFile> getAssociatedFiles();
}
