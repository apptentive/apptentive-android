package com.apptentive.android.sdk.module.engagement.interaction.model;

public class TermsAndConditions {


    public TermsAndConditions(String bodyText, String linkText, String linkURL) {
        this.bodyText = bodyText;
        this.linkText = linkText;
        this.linkURL = linkURL;
    }

    private String bodyText;
    private String linkText;
    private String linkURL;

    public String getLinkURL() {
        return linkURL;
    }

    public String getLinkText() {
        return linkText;
    }

    public String getBodyText() {
        return bodyText;
    }
}

