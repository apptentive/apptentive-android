/*
 * Copyright (c) 2012, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk;

import android.test.AndroidTestCase;
import android.text.format.DateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sky Kelsey
 */
public class RatingsLogicTest extends AndroidTestCase {

	private static List<TestInput> inputs = new ArrayList<TestInput>();

	private static final long START_DATE = 1382300000;
	private static final String DEFAULT_AND = "{\"and\": [\"uses\",\"days\",\"events\"]}";
	private static final String DEFAULT_OR = "{\"or\": [\"uses\",\"days\",\"events\"]}";
	private static final String COMPLEX = "{\"and\": [{\"or\": [\"uses\",\"days\"]},\"events\"]}";
	private static final String CORRUPT = "{\"and\" [{\"or\": [\"uses\",\"days\"]},\"events\"]}";

	static {
		// Variations on default logic.
		inputs.add(new TestInput( 0, RatingModule.RatingState.START, DEFAULT_AND, 30, START_DATE, START_DATE, 5, 0, 10, 0, 5, false));
		inputs.add(new TestInput( 1, RatingModule.RatingState.START, DEFAULT_AND, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 5, 5, 10, 10, 5, false));
		inputs.add(new TestInput( 2, RatingModule.RatingState.START, DEFAULT_AND, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30 + 1, 5, 4, 10, 10, 5, false));
		inputs.add(new TestInput( 3, RatingModule.RatingState.START, DEFAULT_AND, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30 + 1, 5, 5, 10, 9, 5, false));
		inputs.add(new TestInput( 4, RatingModule.RatingState.START, DEFAULT_AND, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30 + 1, 5, 5, 10, 10, 5, true));
		inputs.add(new TestInput( 5, RatingModule.RatingState.START, DEFAULT_AND, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 5, 5, 10, 10, 5, true));
		inputs.add(new TestInput( 6, RatingModule.RatingState.START, DEFAULT_AND, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30 + 1, 0, 5, 10, 10, 5, true));
		inputs.add(new TestInput( 7, RatingModule.RatingState.START, DEFAULT_AND, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30 + 1, 5, 5, 0, 10, 5, true));

		// Variations on default logic switched to OR.
		inputs.add(new TestInput( 8, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE, 5, 0, 10, 0, 5, false));
		inputs.add(new TestInput( 9, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE, 5, 4, 10, 9, 5, false));
		inputs.add(new TestInput(10, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30 + 1, 5, 4, 10, 9, 5, true));
		inputs.add(new TestInput(11, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 5, 5, 10, 9, 5, true));
		inputs.add(new TestInput(12, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 5, 4, 10, 10, 5, true));
		inputs.add(new TestInput(13, RatingModule.RatingState.START, DEFAULT_OR, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 5, 4, 10, 9, 5, false));
		inputs.add(new TestInput(14, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 0, 0, 10, 9, 5, false));
		inputs.add(new TestInput(15, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 5, 4, 0, 0, 5, false));
		inputs.add(new TestInput(16, RatingModule.RatingState.START, DEFAULT_OR, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 5, 5, 10, 9, 5, true));
		inputs.add(new TestInput(17, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30, 0, 0, 10, 10, 5, true));
		inputs.add(new TestInput(18, RatingModule.RatingState.START, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 30+1, 5, 4, 0, 0, 5, true));

		// Variations with more complex logic
		inputs.add(new TestInput(19, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 0, 10, 0, 5, false));
		inputs.add(new TestInput(20, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 0, 10, 10, 5, false));
		inputs.add(new TestInput(21, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 5, 10, 10, 5, true));
		inputs.add(new TestInput(22, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS + 1, 5, 0, 10, 10, 5, true));
		inputs.add(new TestInput(23, RatingModule.RatingState.START, COMPLEX, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 0, 10, 10, 5, false));
		inputs.add(new TestInput(23, RatingModule.RatingState.START, COMPLEX, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 5, 10, 10, 5, true));
		inputs.add(new TestInput(24, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 0, 0, 10, 10, 5, false));
		inputs.add(new TestInput(24, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS + 1, 0, 0, 10, 10, 5, true));
		inputs.add(new TestInput(25, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 5, 10, 0, 5, false));
		inputs.add(new TestInput(26, RatingModule.RatingState.START, COMPLEX, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS + 1, 5, 0, 10, 10, 5, true));

		// Test fallback with corrupt logic
		inputs.add(new TestInput(27, RatingModule.RatingState.START, CORRUPT, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS + 1, 5, 5, 10, 10, 5, true));
		inputs.add(new TestInput(28, RatingModule.RatingState.START, CORRUPT, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS + 1, 5, 5, 10, 0, 5, true));
		inputs.add(new TestInput(29, RatingModule.RatingState.START, CORRUPT, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS + 1, 5, 0, 10, 10, 5, true));
		inputs.add(new TestInput(30, RatingModule.RatingState.START, CORRUPT, 1, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 5, 10, 10, 5, false));
		inputs.add(new TestInput(31, RatingModule.RatingState.START, CORRUPT, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 5, 10, 10, 5, true));
		inputs.add(new TestInput(32, RatingModule.RatingState.START, CORRUPT, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 5, 10, 0, 5, true));
		inputs.add(new TestInput(33, RatingModule.RatingState.START, CORRUPT, 0, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 0, 0, 10, 10, 5, true));

		// Test reprompt
		inputs.add(new TestInput(34, RatingModule.RatingState.REMIND, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS, 5, 5, 10, 10, 5, false));
		inputs.add(new TestInput(35, RatingModule.RatingState.REMIND, DEFAULT_OR, 30, START_DATE, START_DATE + DateUtils.DAY_IN_MILLIS * 5 + 1, 5, 5, 10, 10, 5, true));
	}

	public void testRatingLogic() {
		Log.i("testRatingLogic()");
		for (TestInput input : inputs) {
			boolean result = runLogicTest(input);
			Log.i("Ran testRatingLogicTest with input #%d, and result %b", input.index, result);
			assertTrue(result == input.expectedResult);
		}

	}

	private static boolean runLogicTest(TestInput input) {
		RatingModule.Logic logic = new RatingModule.Logic();
		logic.setRatingState(input.state);
		logic.setLogicString(input.logic);
		logic.setDaysBeforePrompt(input.daysBeforePrompt);
		logic.setStartOfRatingPeriod(input.startOfRatingPeriod);
		logic.setUsesBeforePrompt(input.usesBeforePrompt);
		logic.setUsesElapsed(input.usesElapsed);
		logic.setSignificantEventsBeforePrompt(input.significantEventsBeforePrompt);
		logic.setSignificantEventsElapsed(input.significantEventsElapsed);
		logic.setDaysBetweenPrompts(input.daysBetweenPrompts);
		logic.setCurrentTime(input.currentTime);
		// logic.logRatingFlowState();
		return logic.evaluate();
	}


	private static final class TestInput {
		public int index;
		public RatingModule.RatingState state;
		public String logic;
		public int daysBeforePrompt;
		public long startOfRatingPeriod;
		public long currentTime;
		public int usesBeforePrompt;
		public int usesElapsed;
		public int significantEventsBeforePrompt;
		public int significantEventsElapsed;
		public int daysBetweenPrompts;

		public boolean expectedResult;

		private TestInput(int index, RatingModule.RatingState state, String logic, int daysBeforePrompt, long startOfRatingPeriod, long currentTime, int usesBeforePrompt, int usesElapsed, int significantEventsBeforePrompt, int significantEventsElapsed, int daysBetweenPrompts, boolean expectedResult) {
			this.index = index;
			this.state = state;
			this.logic = logic;
			this.daysBeforePrompt = daysBeforePrompt;
			this.startOfRatingPeriod = startOfRatingPeriod;
			this.currentTime = currentTime;
			this.usesBeforePrompt = usesBeforePrompt;
			this.usesElapsed = usesElapsed;
			this.significantEventsBeforePrompt = significantEventsBeforePrompt;
			this.significantEventsElapsed = significantEventsElapsed;
			this.daysBetweenPrompts = daysBetweenPrompts;
			this.expectedResult = expectedResult;
		}
	}
}
