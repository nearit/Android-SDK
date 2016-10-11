package it.near.sdk.Reactions.Feedback;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import it.near.sdk.MorpheusNear.Resource;

/**
 * Created by cattaneostefano on 11/10/2016.
 */

public class Feedback extends Resource implements Parcelable{

    @SerializedName("question")
    String question;

    public Feedback() {
    }

    protected Feedback(Parcel in) {
        question = in.readString();
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

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(getId());
    }
}
