/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.EnjoymentDialogFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Constants.FragmentConfigKeys;


public class FragmentFactory {
	public static ApptentiveBaseFragment createFragmentInstance(@NonNull Bundle bundle) {

		ActivityContent.Type activeContentType;
		String activityContentTypeString = bundle.getString(ActivityContent.KEY);
		Interaction interaction;
		if (activityContentTypeString != null) {
			activeContentType = ActivityContent.Type.parse(activityContentTypeString);

			if (activeContentType == ActivityContent.Type.INTERACTION) {
				String interactionString;

				interactionString = bundle.getCharSequence(Interaction.KEY_NAME).toString();

				interaction = Interaction.Factory.parseInteraction(interactionString);
				if (interaction != null) {
					switch (interaction.getType()) {
						case UpgradeMessage:
							if (!bundle.containsKey(FragmentConfigKeys.MODAL)) {
								bundle.putBoolean(FragmentConfigKeys.MODAL, true);
							}
							break;
						case EnjoymentDialog:
							if (!bundle.containsKey(FragmentConfigKeys.MODAL)) {
								bundle.putBoolean(FragmentConfigKeys.MODAL, true);
							}
							return EnjoymentDialogFragment.newInstance(bundle);
						case RatingDialog:
							if (!bundle.containsKey(FragmentConfigKeys.MODAL)) {
								bundle.putBoolean(FragmentConfigKeys.MODAL, true);
							}
							break;
						case AppStoreRating:
							if (!bundle.containsKey(FragmentConfigKeys.MODAL)) {
								bundle.putBoolean(FragmentConfigKeys.MODAL, true);
							}
							break;
						case Survey:
							break;
						case MessageCenter:
							return MessageCenterFragment.newInstance(bundle);
						case TextModal:
							if (!bundle.containsKey(FragmentConfigKeys.MODAL)) {
								bundle.putBoolean(FragmentConfigKeys.MODAL, true);
							}
							break;
						case NavigateToLink:
							break;
						default:
							break;
					}
				}
			}
		}
		return null;
	}
}