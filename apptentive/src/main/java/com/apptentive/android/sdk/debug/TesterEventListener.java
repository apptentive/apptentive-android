package com.apptentive.android.sdk.debug;

import java.util.Map;

public interface TesterEventListener
{
    void onDebugEvent(String name, Map<String, Object> userInfo);
}
