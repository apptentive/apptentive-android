package com.apptentive.android.sdk.partners.apptimize;

/**
 * Data container class that mimics `ApptimizeTestInfo`.
 * https://sdk.apptimize.com/ios/appledocs/appledoc-3.0.1/Protocols/ApptimizeTestInfo.html
 */
public class ApptentiveApptimizeTestInfo {
    private final String testName;
    private final String enrolledVariantName;
    private final boolean participated;

    public ApptentiveApptimizeTestInfo(String testName, String enrolledVariantName, boolean participated) {
        this.testName = testName;
        this.enrolledVariantName = enrolledVariantName;
        this.participated = participated;
    }

    public String getTestName() {
        return this.testName;
    }

    public String getEnrolledVariantName() {
        return this.enrolledVariantName;
    }

    public boolean userHasParticipated() {
        return this.participated;
    }
}
