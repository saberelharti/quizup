package net.dm73.quizitup.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Answer implements Parcelable {

    private String Answer;
    private int value;
    private String answerId;
    private String answerValue;

    public Answer(String answer, int value, String answerId) {
        Answer = answer;
        this.value = value;
        this.answerId = answerId;
    }

    public Answer(String answer, int value, String answerId, String answerValue) {
        Answer = answer;
        this.value = value;
        this.answerId = answerId;
        this.answerValue = answerValue;
    }

    public Answer(String answer) {
        Answer = answer;
    }

    //getters and setters
    public String getAnswer() {
        return Answer;
    }

    public int getValue() {
        return value;
    }

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getAnswerValue() {
        return answerValue;
    }

    public void setAnswerValue(String answerValue) {
        this.answerValue = answerValue;
    }

    protected Answer(Parcel in) {
        Answer = in.readString();
        value = in.readInt();
        answerId = in.readString();
        answerValue = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(Answer);
        dest.writeInt(value);
        dest.writeString(answerId);
        dest.writeString(answerValue);
    }

    @SuppressWarnings("unused")
    public static final Creator<Answer> CREATOR = new Creator<Answer>() {
        @Override
        public Answer createFromParcel(Parcel in) {
            return new Answer(in);
        }

        @Override
        public Answer[] newArray(int size) {
            return new Answer[size];
        }
    };
}
