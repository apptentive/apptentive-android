/*
 * Copyright (c) 2015, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.module.engagement.interaction.model.survey;


import android.os.Parcel;
import android.os.Parcelable;

import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;

import java.util.*;

/**
 * @author Sky Kelsey
 */
public class SurveyState implements Parcelable {

	private Map<String, Set<String>> questionToAnswersMap;
	private Map<String, Integer> questionToMinAnswersMap;
	private Map<String, Integer> questionToMaxAnswersMap;
	private Map<String, Boolean> questionToMetricSentMap;

	public SurveyState(SurveyInteraction surveyInteraction) {
		questionToAnswersMap = new HashMap<String, Set<String>>();
		questionToMinAnswersMap = new HashMap<String, Integer>();
		questionToMaxAnswersMap = new HashMap<String, Integer>();
		questionToMetricSentMap = new HashMap<String, Boolean>();

		for (Question question : surveyInteraction.getQuestions()) {
			String questionId = question.getId();
			questionToAnswersMap.put(questionId, new HashSet<String>());
			questionToMinAnswersMap.put(questionId, question.getMinSelections());
			questionToMaxAnswersMap.put(questionId, question.getMaxSelections());
			questionToMetricSentMap.put(questionId, false);
		}
	}

	public void addAnswer(String questionId, String answer) {
		Set<String> answers = questionToAnswersMap.get(questionId);
		if (answers == null) {
			answers = new HashSet<String>();
			questionToAnswersMap.put(questionId, answers);
		}
		answers.add(answer);
	}

	public Set<String> getAnswers(String questionId) {
		return questionToAnswersMap.get(questionId);
	}

	public void setAnswers(String questionId, Set<String> answers) {
		questionToAnswersMap.put(questionId, answers);
	}

	public void clearAnswers(String questionId) {
		questionToAnswersMap.put(questionId, new HashSet<String>());
	}

	public boolean isQuestionValid(Question question) {
		String questionId = question.getId();
		int min = questionToMinAnswersMap.get(questionId);
		int max = questionToMaxAnswersMap.get(questionId);
		int size = questionToAnswersMap.get(questionId).size();
		return (!question.isRequired() && size == 0) || (size >= min) && (size <= max);
	}

	public boolean isMetricSent(String questionId) {
		Boolean ret = questionToMetricSentMap.get(questionId);
		return ret == null ? false : ret;
	}

	public void markMetricSent(String questionId) {
		questionToMetricSentMap.put(questionId, true);
	}

	public static final Parcelable.Creator<SurveyState> CREATOR
			= new Parcelable.Creator<SurveyState>() {
		public SurveyState createFromParcel(Parcel in) {
			return new SurveyState(in);
		}

		public SurveyState[] newArray(int size) {
			return new SurveyState[size];
		}
	};

	/**
	 * recreate maps from parcel
	 */
	private SurveyState(Parcel in) {
		// recreate questionToAnswersMap
		int mapSize = in.readInt();

		for (int i = 0; i < mapSize; i++) {
			final String key = in.readString();
			final int setLength = in.readInt();

			final Set<String> set = new HashSet<String>(setLength);
			for (int j = 0; j < setLength; j++) {
				final String value = in.readString();
				set.add(value);
			}
			questionToAnswersMap.put(key, set);
		}

		// recreate questionToMinAnswersMap
		mapSize = in.readInt();

		for (int i = 0; i < mapSize; i++) {
			final String key = in.readString();
			final int value = in.readInt();

			questionToMinAnswersMap.put(key, value);
		}

		// recreate questionToMaxAnswersMap
		mapSize = in.readInt();

		for (int i = 0; i < mapSize; i++) {
			final String key = in.readString();
			final int value = in.readInt();

			questionToMaxAnswersMap.put(key, value);
		}

		// recreate questionToMetricSentMap
		mapSize = in.readInt();

		for (int i = 0; i < mapSize; i++) {
			final String key = in.readString();
			final boolean value = (in.readByte() != 0);

			questionToMetricSentMap.put(key, value);
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * save maps to parcel
	 */
	@Override
	public void writeToParcel(Parcel out, int flags) {
		// save questionToAnswersMap
		out.writeInt(questionToAnswersMap.size());

		for (Map.Entry<String, Set<String>> entry : questionToAnswersMap.entrySet()) {
			out.writeString(entry.getKey());

			final Set<String> set = entry.getValue();
			final int listLength = set.size();

			out.writeInt(listLength);

			for (String item : set) {
				out.writeString(item);
			}
		}

		// save questionToMinAnswersMap
		out.writeInt(questionToMinAnswersMap.size());

		for (Map.Entry<String, Integer> entry : questionToMinAnswersMap.entrySet()) {
			out.writeString(entry.getKey());
			out.writeInt(entry.getValue());
		}

		// save questionToMaxAnswersMap
		out.writeInt(questionToMaxAnswersMap.size());

		for (Map.Entry<String, Integer> entry : questionToMaxAnswersMap.entrySet()) {
			out.writeString(entry.getKey());
			out.writeInt(entry.getValue());
		}

		// save questionToMetricSentMap
		out.writeInt(questionToMetricSentMap.size());

		for (Map.Entry<String, Boolean> entry : questionToMetricSentMap.entrySet()) {
			out.writeString(entry.getKey());
			out.writeByte((byte) (entry.getValue() ? 1 : 0));
		}
	}
}
