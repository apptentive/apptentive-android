package com.apptentive.android.sdk.model;

import android.os.Bundle;

import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.EnjoymentDialogFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;

/**
 * @author Sky Kelsey
 */
public class FragmentFactory {
	public static ApptentiveBaseFragment createFragmentInstance(Bundle bundle) {

		ActivityContent.Type activeContentType;
		String activityContentTypeString = bundle.getString(ActivityContent.KEY);
		Interaction interaction = null;
		if (activityContentTypeString != null) {
			activeContentType = ActivityContent.Type.parse(activityContentTypeString);

			if (activeContentType == ActivityContent.Type.INTERACTION) {
				String interactionString;

				interactionString = bundle.getCharSequence(Interaction.KEY_NAME).toString();

				interaction = Interaction.Factory.parseInteraction(interactionString);
				if (interaction != null) {
					switch (interaction.getType()) {
						case UpgradeMessage:
							break;
						case EnjoymentDialog:
							return EnjoymentDialogFragment.newInstance(bundle);
						case RatingDialog:
							break;
						case AppStoreRating:
							break;
						case Survey:
							break;
						case MessageCenter:
							return MessageCenterFragment.newInstance(bundle);
						case TextModal:
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