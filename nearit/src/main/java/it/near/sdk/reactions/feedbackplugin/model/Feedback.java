package it.near.sdk.reactions.feedbackplugin.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.recipes.models.ReactionBundle;

/**
 * Created by cattaneostefano on 11/10/2016.
 */

public class Feedback extends ReactionBundle implements Parcelable {

    @SerializedName("question")
    public String question;

    private String recipeId;

    public Feedback() {
    }

    protected Feedback(Parcel in) {
        super(in);
        question = in.readString();
        setRecipeId(in.readString());
        setId(in.readString());
    }

    public static final Creator<Feedback> CREATOR = new Creator<Feedback>() {
        @Override
        public Feedback createFromParcel(Parcel in) {
            return new Feedback(in);
        }

        @Override
        public Feedback[] newArray(int size) {
            return new Feedback[size];
        }
    };

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(question);
        dest.writeString(getRecipeId());
        dest.writeString(getId());
    }
}
