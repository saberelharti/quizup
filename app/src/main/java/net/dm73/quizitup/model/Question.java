package net.dm73.quizitup.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;


public class Question implements Parcelable {

    private String question;
    private String questionId;
    private List<Answer> answers;

    public Question(String question, String questionId, List<Answer> answers) {
        this.question = question;
        this.questionId = questionId;
        this.answers = answers;
    }

    //getters and setters
    public String getQuestion() {
        return question;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    protected Question(Parcel in) {
        question = in.readString();
        questionId = in.readString();
        if (in.readByte() == 0x01) {
            answers = new ArrayList<Answer>();
            in.readList(answers, Answer.class.getClassLoader());
        } else {
            answers = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(question);
        dest.writeString(questionId);
        if (answers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(answers);
        }
    }

    @SuppressWarnings("unused")
    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };
}