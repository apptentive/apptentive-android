package com.apptentive.android.sdk.storage;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class DeviceManagerTest {
    @Test(expected = IllegalArgumentException.class)
    public void whenAndroidIdIsNull_thenExpectIllegalArgument() {
        new DeviceManager(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAndroidIdIsEmpty_thenExpectIllegalArgument() {
        new DeviceManager("");
    }

    @Test
    public void whenAndroidIdIsSet_thenExpectNoException() {
        new DeviceManager(UUID.randomUUID().toString());
    }

    @Test
    public void whenGeneratingDevice_thenExpectNoException() {
        String androidID = UUID.randomUUID().toString();

        DeviceManager deviceManager = new DeviceManager(androidID);
        assertNotNull(deviceManager);

        Device device = deviceManager.generateNewDevice(InstrumentationRegistry.getContext());
        assertNotNull(device);

        assertEquals("Android", device.getOsName());
        assertEquals(androidID, device.getUuid());
    }
}
