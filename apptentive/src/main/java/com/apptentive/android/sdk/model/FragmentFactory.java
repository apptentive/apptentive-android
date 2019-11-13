/*
 * Copyright (c) 2016, Apptentive, Inc. All Rights Reserved.
 * Please refer to the LICENSE file for the terms and conditions
 * under which redistribution and use of this file is permitted.
 */

package com.apptentive.android.sdk.model;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.apptentive.android.sdk.module.engagement.interaction.fragment.AboutFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.AppStoreRatingFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.EnjoymentDialogFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterErrorFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.NavigateToLinkFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.NoteFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.RatingDialogFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.SurveyFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.UpgradeMessageFragment;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.util.Constants;
import com.apptentive.android.sdk.util.Constants.FragmentConfigKeys;


public class FragmentFactory {
	public static ApptentiveBaseFragment createFragmentInstance(@NonNull Bundle bundle) {

		int fragmentType = bundle.getInt(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.UNKNOWN);
		Interaction interaction;
		if (fragmentType != Constants.FragmentTypes.UNKNOWN) {
			if (fragmentType == Constants.FragmentTypes.INTERACTION) {
				String interactionString;

				interactionString = bundle.getString(Interaction.KEY_NAME);

				interaction = Interaction.Factory.parseInteraction(interactionString);
				if (interaction != null) {
					switch (interaction.getType()) {
						case UpgradeMessage:
							return UpgradeMessageFragment.newInstance(bundle);
						case EnjoymentDialog:
							return EnjoymentDialogFragment.newInstance(bundle);
						case RatingDialog:
							return RatingDialogFragment.newInstance(bundle);
						case AppStoreRating:
							return AppStoreRatingFragment.newInstance(bundle);
						case Survey:
							return SurveyFragment.newInstance(bundle);
						case MessageCenter:
							return MessageCenterFragment.newInstance(bundle);
						case TextModal:
							return NoteFragment.newInstance(bundle);
						case NavigateToLink:
							return NavigateToLinkFragment.newInstance(bundle);
						default:
							break;
					}
				}
			} else if (fragmentType == Constants.FragmentTypes.MESSAGE_CENTER_ERROR) {
				return MessageCenterErrorFragment.newInstance(bundle);
			} else if (fragmentType == Constants.FragmentTypes.ABOUT) {
				return AboutFragment.newInstance(bundle);
			}
		}
		return null;
	}

	public static Bundle addDisplayModeToFragmentBundle(@NonNull Bundle bundle) {
		// If bundle to launch the interaction already contains the mode value, honor it and return
		if (bundle.containsKey(FragmentConfigKeys.MODAL)) {
			return bundle;
		}
		// Otherwise, make survey and message center default to full screen, and others are modal dialog.
		int fragmentType = bundle.getInt(Constants.FragmentConfigKeys.TYPE, Constants.FragmentTypes.UNKNOWN);
		Interaction interaction;
		boolean defaultVal = true;

		if (fragmentType == Constants.FragmentTypes.INTERACTION) {
			String interactionString;
			interactionString = bundle.getString(Interaction.KEY_NAME);
			interaction = Interaction.Factory.parseInteraction(interactionString);
			if (interaction != null) {
				switch (interaction.getType()) {
					case Survey:
						defaultVal = false;
						break;
					case MessageCenter:
						defaultVal = false;
						break;
					default:
						break;
				}
			}
		}
		bundle.putBoolean(FragmentConfigKeys.MODAL, defaultVal);
		return bundle;
	}
}