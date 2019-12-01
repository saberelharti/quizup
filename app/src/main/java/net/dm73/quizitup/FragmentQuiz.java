package net.dm73.quizitup;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.dm73.quizitup.model.Answer;
import net.dm73.quizitup.model.Question;
import net.dm73.quizitup.util.DataApplication;

import java.util.List;


public class FragmentQuiz extends Fragment {

    private Button firstResponse;
    private Button secondResponse;
    private Button thirdResponse;
    private Button fourthResponse;

    private List<Answer> answers;
    private DataApplication data;

    private int idFragment;
    public static int FRAGMENT_COUNT = 0;

    public FragmentQuiz() {
        idFragment = FRAGMENT_COUNT;
        ++FRAGMENT_COUNT;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        data = (DataApplication) getActivity().getApplication();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View quizView = inflater.inflate(R.layout.fragment_quiz, container, false);

        //font for text
        Typeface adventProExl = Typeface.createFromAsset(getContext().getAssets(), "fonts/AdventPro-ExtraLight.ttf");

        //get and modify the Question
        Question question = getQuestion();
        final TextView textQuestion = (TextView) quizView.findViewById(R.id.text_question);
        textQuestion.setText(question.getQuestion());
        textQuestion.setTypeface(adventProExl);
        answers = question.getAnswers();

        int nbrOfAnswers = answers.size();

        //Get and modify the button quiz
        answers = question.getAnswers();
        firstResponse = (Button) quizView.findViewById(R.id.response_1);
        secondResponse = (Button) quizView.findViewById(R.id.response_2);
        thirdResponse = (Button) quizView.findViewById(R.id.response_3);
        fourthResponse = (Button) quizView.findViewById(R.id.response_4);


        switch (nbrOfAnswers) {
            case 2:
                firstResponse.setText(answers.get(0).getAnswer());
                firstResponse.setTypeface(adventProExl);
                secondResponse.setText(answers.get(1).getAnswer());
                secondResponse.setTypeface(adventProExl);
                thirdResponse.setVisibility(View.GONE);
                fourthResponse.setVisibility(View.GONE);
                break;
            case 3:
                firstResponse.setText(answers.get(0).getAnswer());
                firstResponse.setTypeface(adventProExl);
                secondResponse.setText(answers.get(1).getAnswer());
                secondResponse.setTypeface(adventProExl);
                thirdResponse.setText(answers.get(2).getAnswer());
                thirdResponse.setTypeface(adventProExl);
                fourthResponse.setVisibility(View.GONE);
                break;
            case 4:
                firstResponse.setText(answers.get(0).getAnswer());
                firstResponse.setTypeface(adventProExl);
                secondResponse.setText(answers.get(1).getAnswer());
                secondResponse.setTypeface(adventProExl);
                thirdResponse.setText(answers.get(2).getAnswer());
                thirdResponse.setTypeface(adventProExl);
                fourthResponse.setText(answers.get(3).getAnswer());
                fourthResponse.setTypeface(adventProExl);
                break;
        }


        //Restore the previews answer
        if (data.itemListAnswersNotEmpty(idFragment, 0)) {
            restoreTheLastAnswer();
        }

        return quizView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final int nbrOfAnswers = answers.size();

        //Add listner for the fisrt response
        firstResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firstAnswerActive(nbrOfAnswers);
                startButtonAnimation();
                saveTheResponse(1, answers.get(0));

            }
        });

        //Add listner for the second response
        secondResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SecondAnswerActive(nbrOfAnswers);
                startButtonAnimation();
                saveTheResponse(2, answers.get(1));
            }
        });


        //Add listner for the third response
        thirdResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThirdAnswerActive(nbrOfAnswers);
                startButtonAnimation();
                saveTheResponse(3, answers.get(2));
            }
        });

        //Add listner for the fourth response
        fourthResponse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FourthAnswerActive();
                startButtonAnimation();
                saveTheResponse(4, answers.get(3));
            }
        });

    }

    private void firstAnswerActive(int totalAnswers) {
        switch (totalAnswers) {
            case 2:
                firstResponse.setBackgroundResource(R.drawable.response_button_active);
                secondResponse.setBackgroundResource(R.drawable.response_button);
                break;
            case 3:
                firstResponse.setBackgroundResource(R.drawable.response_button_active);
                secondResponse.setBackgroundResource(R.drawable.response_button);
                thirdResponse.setBackgroundResource(R.drawable.response_button);
                break;
            case 4:
                firstResponse.setBackgroundResource(R.drawable.response_button_active);
                secondResponse.setBackgroundResource(R.drawable.response_button);
                thirdResponse.setBackgroundResource(R.drawable.response_button);
                fourthResponse.setBackgroundResource(R.drawable.response_button);
                break;
        }
    }

    private void SecondAnswerActive(int totalAnswers) {
        switch (totalAnswers) {
            case 2:
                firstResponse.setBackgroundResource(R.drawable.response_button);
                secondResponse.setBackgroundResource(R.drawable.response_button_active);
                break;
            case 3:
                firstResponse.setBackgroundResource(R.drawable.response_button);
                secondResponse.setBackgroundResource(R.drawable.response_button_active);
                thirdResponse.setBackgroundResource(R.drawable.response_button);
                break;
            case 4:
                firstResponse.setBackgroundResource(R.drawable.response_button);
                secondResponse.setBackgroundResource(R.drawable.response_button_active);
                thirdResponse.setBackgroundResource(R.drawable.response_button);
                fourthResponse.setBackgroundResource(R.drawable.response_button);
                break;
        }
    }

    private void ThirdAnswerActive(int totalAnswers) {
        switch (totalAnswers) {
            case 3:
                firstResponse.setBackgroundResource(R.drawable.response_button);
                secondResponse.setBackgroundResource(R.drawable.response_button);
                thirdResponse.setBackgroundResource(R.drawable.response_button_active);
                break;
            case 4:
                firstResponse.setBackgroundResource(R.drawable.response_button);
                secondResponse.setBackgroundResource(R.drawable.response_button);
                thirdResponse.setBackgroundResource(R.drawable.response_button_active);
                fourthResponse.setBackgroundResource(R.drawable.response_button);
                break;
        }
    }

    private void FourthAnswerActive() {
        firstResponse.setBackgroundResource(R.drawable.response_button);
        secondResponse.setBackgroundResource(R.drawable.response_button);
        thirdResponse.setBackgroundResource(R.drawable.response_button);
        fourthResponse.setBackgroundResource(R.drawable.response_button_active);
    }

    private void saveTheResponse(int idResponse, Answer answer) {
        if (data.listAnswersLength() != 0) {
            data.saveAnswer(idFragment, idResponse + "", answer.getAnswerId(), answer.getAnswerValue());
        }
    }

    private void restoreTheLastAnswer() {
        switch (data.getItemListAnswer(idFragment, 0)) {
            case "1":
                firstResponse.setBackgroundResource(R.drawable.response_button_active);
                break;
            case "2":
                secondResponse.setBackgroundResource(R.drawable.response_button_active);
                break;
            case "3":
                thirdResponse.setBackgroundResource(R.drawable.response_button_active);
                break;
            case "4":
                fourthResponse.setBackgroundResource(R.drawable.response_button_active);
                break;
        }
    }

    private void startButtonAnimation() {

        //Change the background of the button and start the animation
        if (data.listAnswersLength() != 0 && data.itemListAnswersEmpty(idFragment, 0)) {
            QuizActivity.startAnimation(getContext());
        }
    }


    public static FragmentQuiz newInstance(Question question) {

        FragmentQuiz f = new FragmentQuiz();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("question", question);
        f.setArguments(args);

        return f;
    }

    public Question getQuestion() {
        return getArguments().getParcelable("question");
    }

}
