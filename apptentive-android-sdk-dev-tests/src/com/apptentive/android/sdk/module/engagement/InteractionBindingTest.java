package com.apptentive.android.sdk.module.engagement;

import android.test.AndroidTestCase;
import com.apptentive.android.sdk.model.Interaction;
import com.apptentive.android.sdk.model.Interactions;
import org.json.JSONException;

import java.util.List;

/**
 * @author Sky Kelsey
 */
public class InteractionBindingTest extends AndroidTestCase {

	private static final String INTERACTIONS_WITH_SINGLE_UPGRADEMESSAGE =
			"{\n" +
			"    \"interactions\": {\n" +
			"        \"app.launch\": [\n" +
			"            {\n" +
			"                \"id\": \"528d14854712c7bfd7000002\",\n" +
			"                \"priority\": 1,\n" +
			"                \"criteria\": {\n" +
			"                    \"code_point/app.launch/invokes/version\": 1,\n" +
			"                    \"application_version\": \"2\"\n" +
			"                },\n" +
			"                \"type\": \"UpgradeMessage\",\n" +
			"                \"version\": null,\n" +
			"                \"active\": true,\n" +
			"                \"configuration\": {\n" +
			"                    \"active\": true,\n" +
			"                    \"app_version\": \"2\",\n" +
			"                    \"show_app_icon\": true,\n" +
			"                    \"show_powered_by\": true,\n" +
			"                    \"body\": \"<html><head><style>\\nbody {\\n\\tfont-family: \\\"Helvetica Neue\\\", Helvetica;\\n\\tcolor: #4d4d4d;\\n\\tfont-size: .875em;\\n\\tline-height: 1.36em;\\n\\t-webkit-text-size-adjust:none;\\n}\\n\\nh1, h2, h3, h4, h5, h6 {\\n\\tcolor: #000000;\\n\\tline-height: 1.25em;\\n\\ttext-align: center;\\n}\\n\\nh1 {font-size: 22px;}\\nh2 {font-size: 18px;}\\nh3 {font-size: 16px;}\\nh4 {font-size: 14px;}\\nh5, h6 {font-size: 12px;}\\nh6 {font-weight: normal;}\\n\\nblockquote {\\n\\tmargin: 1em 1.75em;\\n\\tfont-style: italic;\\n}\\n\\nul, ol {\\n\\tpadding-left: 1.75em;\\n}\\n\\ntable {\\n\\tborder-collapse: collapse;\\n\\tborder-spacing: 0;\\n\\tempty-cells: show;\\n}\\n\\ntable caption {\\n\\tpadding: 1em 0;\\n\\ttext-align: center;\\n}\\n\\ntable td,\\ntable th {\\n\\tborder-left: 1px solid #cbcbcb;\\n\\tborder-width: 0 0 0 1px;\\n\\tfont-size: inherit;\\n\\tmargin: 0;\\n\\tpadding: .25em .5em;\\n\\n}\\ntable td:first-child,\\ntable th:first-child {\\n\\tborder-left-width: 0;\\n}\\ntable th:first-child {\\n\\tborder-radius: 4px 0 0 4px;\\n}\\ntable th:last-child {\\n\\tborder-radius: 0 4px 4px 0;\\n}\\n\\ntable thead {\\n\\tbackground: #E5E5E5;\\n\\tcolor: #000;\\n\\ttext-align: left;\\n\\tvertical-align: bottom;\\n}\\n\\ntable td {\\n\\tbackground-color: transparent;\\n\\tborder-bottom: 1px solid #E5E5E5;\\n}\\n</style></head><body><p>Testing upgrade messaging.</p></body></html>\"\n" +
			"                }\n" +
			"            }\n" +
			"        ]\n" +
			"    }\n" +
			"}";

	public void testInteractionCreation() {
		Interactions interactionsList = null;
		try {
			interactionsList = new Interactions(INTERACTIONS_WITH_SINGLE_UPGRADEMESSAGE);
			List<Interaction> interactionsForCodePoint = interactionsList.getInteractions("app.launch");
			assertNotNull("Failed to parse Interactions with single UpgradeMessage.", interactionsList);
			for (Interaction interaction : interactionsForCodePoint) {
				String id = interaction.getId();
				assertEquals(id, "528d14854712c7bfd7000002");
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}
