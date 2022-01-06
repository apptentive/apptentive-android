package com.apptentive.android.sdk.tests.misc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.tests.ApptentiveTestCaseBase;
import com.apptentive.android.sdk.util.ThrottleUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class ThrottleUtilsTest extends ApptentiveTestCaseBase {
    private SharedPreferences sharedPreferences;
    private ThrottleUtils throttleUtils;
    Interaction.Type ratingInteraction = Interaction.Type.InAppRatingDialog;
    Interaction.Type noteInteractionOne = Interaction.Type.TextModal;
    Interaction.Type noteInteractionTwo = Interaction.Type.TextModal;
    Interaction.Type surveyInteraction = Interaction.Type.Survey;

    @Before
    public void setUp() {
        String APPTENTIVE_TEST_SHARED_PREF = "APPTENTIVE TEST SHARED PREF";
        sharedPreferences = targetContext.getSharedPreferences(APPTENTIVE_TEST_SHARED_PREF, Context.MODE_PRIVATE);
        throttleUtils = new ThrottleUtils(100L, sharedPreferences);
    }

    @After
    public void tearDown() {
        sharedPreferences = null;
    }

    @Test
    public final void shouldThrottleRatingInteractionTest() {
        try {
            // First call
            assertFalse(throttleUtils.shouldThrottleInteraction(ratingInteraction));

            // Call right after
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingInteraction));
            TimeUnit.MILLISECONDS.sleep(10L);

            // 10ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingInteraction));
            TimeUnit.MILLISECONDS.sleep(50L);

            // 60ms since first call
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingInteraction));
            TimeUnit.MILLISECONDS.sleep(60L);

            // 120ms since first call (should be able to call again)
            assertFalse(throttleUtils.shouldThrottleInteraction(ratingInteraction));
            TimeUnit.MILLISECONDS.sleep(50L);

            // 50ms since second call
            assertTrue(throttleUtils.shouldThrottleInteraction(ratingInteraction));
        } catch (Exception e) {

        }
    }

    @Test
    public final void shouldThrottleInteractionWithOtherInteractionsTest() {
        try {
            // Default throttle length is 1 second aka 1000 ms

            // First call interactionTwo
            assertFalse(throttleUtils.shouldThrottleInteraction(noteInteractionOne));
            TimeUnit.MILLISECONDS.sleep(300L);

            // 300ms since first call interactionTwo
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionOne));

            // Same Type as interactionTwo (so 300ms since last called this type)
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionTwo));

            // first call interactionFour
            assertFalse(throttleUtils.shouldThrottleInteraction(surveyInteraction));
            TimeUnit.MILLISECONDS.sleep(500L);

            // 800ms since second call interactionTwo
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionOne));
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionTwo));
            TimeUnit.MILLISECONDS.sleep(300L);

            // 1100ms since first call of interactionTwo (should be able to call)
            assertFalse(throttleUtils.shouldThrottleInteraction(noteInteractionTwo));

            // Same type as interactionThree that was just called (shouldn't be able to call)
            assertTrue(throttleUtils.shouldThrottleInteraction(noteInteractionOne));

            // 800ms since first call of interactionFour
            assertTrue(throttleUtils.shouldThrottleInteraction(surveyInteraction));
            TimeUnit.MILLISECONDS.sleep(300L);

            // 1100ms since first call of interactionFour
            assertFalse(throttleUtils.shouldThrottleInteraction(surveyInteraction));

            // Call right after same interaction should not call
            assertTrue(throttleUtils.shouldThrottleInteraction(surveyInteraction));
        } catch (Exception e) {

        }
    }
}
