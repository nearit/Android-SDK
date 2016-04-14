package it.near.sdk.Reactions.PollNotification;

import it.near.sdk.MorpheusNear.Annotations.SerializeName;
import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 14/04/16.
 */
public class PollNotification extends Resource {
    @SerializeName("text")
    String text;
    @SerializeName("question")
    String question;
    @SerializeName("choice_1")
    String choice_1;
    @SerializeName("choice_2")
    String choice_2;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getChoice_1() {
        return choice_1;
    }

    public void setChoice_1(String choice_1) {
        this.choice_1 = choice_1;
    }

    public String getChoice_2() {
        return choice_2;
    }

    public void setChoice_2(String choice_2) {
        this.choice_2 = choice_2;
    }
}
