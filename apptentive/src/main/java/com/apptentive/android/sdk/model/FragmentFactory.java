package com.apptentive.android.sdk.model;

import android.os.Bundle;

import com.apptentive.android.sdk.Log;
import com.apptentive.android.sdk.R;
import com.apptentive.android.sdk.module.ActivityContent;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.ApptentiveBaseFragment;
import com.apptentive.android.sdk.module.engagement.interaction.fragment.MessageCenterFragment;
import com.apptentive.android.sdk.module.engagement.interaction.model.AppStoreRatingInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.EnjoymentDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.Interaction;
import com.apptentive.android.sdk.module.engagement.interaction.model.MessageCenterInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.NavigateToLinkInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.RatingDialogInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.SurveyInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.TextModalInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.model.UpgradeMessageInteraction;
import com.apptentive.android.sdk.module.engagement.interaction.view.AppStoreRatingInteractionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.EnjoymentDialogInteractionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.NavigateToLinkInteractionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.RatingDialogInteractionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.TextModalInteractionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.UpgradeMessageInteractionView;
import com.apptentive.android.sdk.module.engagement.interaction.view.survey.SurveyInteractionView;
import com.apptentive.android.sdk.module.messagecenter.view.MessageCenterActivityContent;

import org.json.JSONException;

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
							break;
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