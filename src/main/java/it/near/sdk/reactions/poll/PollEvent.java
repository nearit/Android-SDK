package it.near.sdk.reactions.poll;

import android.content.Context;

import org.json.JSONException;
import java.util.HashMap;

import it.near.sdk.GlobalConfig;
import it.near.sdk.reactions.Event;
import it.near.sdk.utils.NearJsonAPIUtils;

/**
 * Action for submitting a poll answer.
 *
 * @author cattaneostefano
 */
public class PollEvent extends Event{
    public static final String PLUGIN_NAME = "PollEvent";
    private static final String RES_TYPE = "answers";
    private static final String ANSWER = "answer";
    private static final String PROFILE_ID = "profile_id";
    private static final String RECIPE_ID = "recipe_id";
    private static final String POLL_ID = "poll_id";
    String pollId;
    int answer = 0;
    String recipeId;

    /**
     * Default constructor.
     * @param poll poll to answer.
     * @param answer answer number, can be either 1 or 2.
     */
    public PollEvent(Poll poll, int answer) {
        this.pollId = poll.getId();
        this.answer = answer;
        this.recipeId = poll.getRecipeId();
    }

    /**
     * Constructor that doesn't need the original poll object.
     * @param pollId the poll id.
     * @param answer answer number, can be either 1 or 2.
     * @param recipeId the recipe id.
     */
    public PollEvent(String pollId, int answer, String recipeId){
        this.pollId = pollId;
        this.answer = answer;
        this.recipeId = recipeId;
    }

    public String getPollId() {
        return pollId;
    }

    public void setPollId(String pollId) {
        this.pollId = pollId;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    @Override
    public String getPlugin() {
        return PLUGIN_NAME;
    }

    public String toJsonAPI(Context context) throws JSONException {
        String profileId = GlobalConfig.getInstance(context).getProfileId();
        HashMap<String, Object> attributeMap = new HashMap<>();
        if (profileId == null ||
                answer == 0 ||
                recipeId == null ||
                pollId == null
                ) {
            throw new JSONException("missing data");
        }
        attributeMap.put(ANSWER, answer);
        attributeMap.put(PROFILE_ID, profileId);
        attributeMap.put(RECIPE_ID, recipeId);
        attributeMap.put(POLL_ID, pollId);
        return NearJsonAPIUtils.toJsonAPI(RES_TYPE, attributeMap);
    }
}
