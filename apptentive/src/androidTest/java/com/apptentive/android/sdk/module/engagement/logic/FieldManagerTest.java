package com.apptentive.android.sdk.module.engagement.logic;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.apptentive.android.sdk.storage.AppRelease;
import com.apptentive.android.sdk.storage.Device;
import com.apptentive.android.sdk.storage.EventData;
import com.apptentive.android.sdk.storage.Person;
import com.apptentive.android.sdk.storage.VersionHistory;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class FieldManagerTest {
//    private final Context context = InstrumentationRegistry.getInstrumentation().getContext();
//
//    @Test
//    public void testRandomPercentWithKey() {
//        final FieldManager fieldManager = createFieldManager(50);
//        final Comparable expected = new BigDecimal(50);
//        final Comparable actual = fieldManager.getValue("random/abc123xyz/percent");
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    public void testRandomPercentWithoutKey() {
//        final FieldManager fieldManager = createFieldManager(50);
//        final Comparable expected = new BigDecimal(50);
//        final Comparable actual = fieldManager.getValue("random/percent");
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    public void testRandomPercentWithKeyDescription() {
//        final FieldManager fieldManager = createFieldManager(50);
//        final String expected = "random percent for key 'abc123xyz'";
//        final String actual = fieldManager.getDescription("random/abc123xyz/percent");
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    public void testRandomPercentWithoutKeyDescription() {
//        final FieldManager fieldManager = createFieldManager(50);
//        final String expected = "random percent";
//        final String actual = fieldManager.getDescription("random/percent");
//        assertEquals(expected, actual);
//    }
//
//    private FieldManager createFieldManager(double percent) {
//        return new FieldManager(context, new VersionHistory(), new EventData(), new Person(), new Device(), new AppRelease(), new MockRandomPercentProvider(percent));
//    }
//
//    private static class MockRandomPercentProvider implements RandomPercentProvider {
//        private final double percent;
//
//        private MockRandomPercentProvider(double percent) {
//            this.percent = percent;
//        }
//
//        @Override
//        public double getPercent(String key) {
//            return percent;
//        }
//    }
}