package com.apptentive.android.sdk.debug;

public interface TesterEventListener
{
    void onDebugEvent(String name, Object... params);
}
