package net.dm73.quizitup.util;

import android.app.Application;
import android.content.Intent;

import net.dm73.quizitup.model.Quiz;


public class DataApplication extends Application {

    public String[][] listAnwers;
    public Quiz quiz;
    public Intent intent;

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {

        return intent;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String[][] getListAnwers() {
        return listAnwers;
    }

    public void setListAnwers(String[][] listAnwers) {
        this.listAnwers = listAnwers;
    }

    public boolean itemListAnswersNotEmpty(int a, int b) {
        return listAnwers[a][b] != "0";
    }

    public boolean itemListAnswersEmpty(int a, int b) {
        return listAnwers[a][b] == "0";
    }

    public String getItemListAnswer(int a, int b) {
        return listAnwers[a][b];
    }

    public int listAnswersLength() {
        return listAnwers.length;
    }

    public void saveAnswer(int firstColumn, String FirstData, String secondData, String ThirdData) {

        listAnwers[firstColumn][0] = FirstData;
        listAnwers[firstColumn][2] = secondData;
        listAnwers[firstColumn][3] = ThirdData;

    }

}
