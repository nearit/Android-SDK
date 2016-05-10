package it.near.sdk.Reactions.PollNotification;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import it.near.sdk.Reactions.Action;
import it.near.sdk.Utils.NearUtils;

/**
 * Action for submitting a poll answer.
 *
 * @author cattaneostefano
 */
public class PollAction extends Action{
    public static final String PLUGIN_NAME = "PollAction";
    private static final String ATTRIBUTE_ANSWER = "answer";
    private static final String ATTRIBUTE_ID = "notification_id";
    private static final String RES_TYPE = "answers";
    String id;
    int answer;

    /**
     * Default constructor
     * @param id poll identifier
     * @param answer answer number, can be either 1 or 2
     */
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
    public String getPlugin() {
        return PLUGIN_NAME;
    }

    public String toJsonAPI() throws JSONException {
        HashMap<String, Object> attributeMap = new HashMap<>();
        attributeMap.put(ATTRIBUTE_ANSWER, this.answer);
        attributeMap.put(ATTRIBUTE_ID, this.id);
        return NearUtils.toJsonAPI(RES_TYPE, attributeMap);
    }
}
