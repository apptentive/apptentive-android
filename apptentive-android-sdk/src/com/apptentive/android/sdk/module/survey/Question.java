/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.survey;

/**
 * @author Sky Kelsey.
 */
public interface Question {
	public static final int QUESTION_TYPE_SINGLELINE  = 1;
	public static final int QUESTION_TYPE_MULTICHOICE = 2;
	public static final int QUESTION_TYPE_MULTISELECT = 3;
	public static final int QUESTION_TYPE_STACKRANK   = 4;

	public int getType();

	public String getId();
	public String getValue();
	public boolean isRequired();
	public boolean isAnswered();

	public String[] getAnswers();
}
