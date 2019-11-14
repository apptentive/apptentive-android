package com.apptentive.android.sdk.partners.apptimize;

import androidx.annotation.Nullable;

import com.apptentive.android.sdk.ApptentiveLog;
import com.apptentive.android.sdk.ApptentiveLogTag;
import com.apptentive.android.sdk.util.Invocation;
import com.apptentive.android.sdk.util.RuntimeUtils;
import com.apptentive.android.sdk.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static com.apptentive.android.sdk.debug.ErrorMetrics.logException;

public class ApptentiveApptimize {
    private static final String CLASS_APPTIMIZE = "com.apptimize.Apptimize";
    private static final String METHOD_GET_TEST_INFO = "getTestInfo";

    /**
     * @return true if Apptimize SDK is integrated with the host app.
     */
    public static boolean isApptimizeSDKAvailable() {
        return RuntimeUtils.classExists(CLASS_APPTIMIZE);
    }

    /**
     * @return the version number of the Apptimize library as a string formatted as *major.minor.build* (e.g., 3.0.1).
     */
    public static @Nullable String getLibraryVersion() {
        return "3.0.0"; // FIXME: figure out how to resolve Apptimize SDK version at runtime.
    }

    /**
     * @return true is Apptimize SDK library version is supported.
     */
    public static boolean isSupportedLibraryVersion() {
        String libraryVersion = getLibraryVersion();
        if (libraryVersion == null) {
            return false;
        }

        String[] tokens = libraryVersion.split("\\.");
        if (tokens.length != 3) {
            return false;
        }

        int major = StringUtils.parseInt(tokens[0], 0);
        return major >= 3;
    }

    @SuppressWarnings("unchecked")
    public static @Nullable Map<String, ApptentiveApptimizeTestInfo> getTestInfo() {
        try {
            Invocation apptimize = Invocation.fromClass(CLASS_APPTIMIZE);
            Map<String, Object> testInfoDict = apptimize.invokeMethod(METHOD_GET_TEST_INFO, Map.class);
            if (testInfoDict == null) {
                return null;
            }

            Map<String, ApptentiveApptimizeTestInfo> experiments = new HashMap<>();
            for (Map.Entry<String, Object> e : testInfoDict.entrySet()) {
                String experimentName = e.getKey();
                Object experimentObject = e.getValue();

                Invocation experiment = Invocation.fromObject(experimentObject);
                String testName = experiment.invokeStringMethod("getTestName");
                String enrolledVariantName = experiment.invokeStringMethod("getEnrolledVariantName");
                boolean participated = experiment.invokeBooleanMethod("userHasParticipated");
                ApptentiveApptimizeTestInfo testInfo = new ApptentiveApptimizeTestInfo(
                        testName, enrolledVariantName, participated
                );
                experiments.put(experimentName, testInfo);
            }

            return experiments;
        } catch (Exception e) {
            ApptentiveLog.e(ApptentiveLogTag.PARTNERS, "Error while getting Apptimize experiment info: %s", e.getMessage());
            logException(e); // TODO: add more context info
            return null;
        }

    }
}
