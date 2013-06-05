This document tracks changes to the API between versions.


# 1.0

The following changes from the 0.6.x series were made.

We are moving over to a unified message center, which is an expansion of the feedback API. We have decided to take the opportunity to clean up the ratings flow API, and simplify how you interact with the SDK in general. Below are detailed changes that have been made to the API, but from a simple perspective, you'll want to:

General setup:

* Replace

`Apptentive.getFeedbackModule().addDataField("username", "Sky Kelsey");`

with

<pre><code>
Map<String, String> customData = new HashMap<String, String>();
customData.put("username", "Sky Kelsey");
Apptentive.setCustomData(customData);
</code></pre>

* Replace `Apptentive.getRatingModule().setRatingProvider(new AmazonAppstoreRatingProvider());` with `Apptentive.setRatingProvider(new AmazonAppstoreRatingProvider());`

To launch feedback:

* Replace `Apptentive.getFeedbackModule().forceShowFeedbackDialog(YourActivity.this);` with `Apptentive.showMessageCenter(YourActivity.this);`.

In ratings:

* Replace `Apptentive.getRatingModule().run(YourActivity.this);` with `Apptentive.showRatingFlowIfConditionsAreMet(YourActivity.this);`
* Replace `Apptentive.getRatingModule().logEvent();` with `Apptentive.logSignificantEvent();`.
