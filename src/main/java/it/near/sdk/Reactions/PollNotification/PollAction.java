package it.near.sdk.Reactions.PollNotification;

import org.json.JSONException;
import org.json.JSONObject;

import it.near.sdk.Reactions.Action;

/**
 * Created by cattaneostefano on 20/04/16.
 */
public class PollAction extends Action{
    public static final String INGREDIENT_NAME = "PollAction";
    private static final String ATTRIBUTE_ANSWER = "answer";
    private static final String ATTRIBUTE_ID = "notification_id";
    private static final String RES_TYPE = "answers";
    String id;
    int answer;

    public PollAction(String id, int answer) {
        this.id = id;
        this.answer = answer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getIngredient() {
        return INGREDIENT_NAME;
    }

    public String toJsonAPI() throws JSONException {
        JSONObject attributesObj = new JSONObject();

        attributesObj.put(ATTRIBUTE_ANSWER, this.answer);
        attributesObj.put(ATTRIBUTE_ID, this.id);

        JSONObject dataObject = new JSONObject();
        dataObject.put("type", RES_TYPE);
        dataObject.put("attributes", attributesObj);

        JSONObject outerObj = new JSONObject();
        outerObj.put("data", dataObject);

        return outerObj.toString();
    }
}
