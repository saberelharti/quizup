package net.dm73.quizitup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.style.Pulse;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.dm73.quizitup.util.MusicPlayer;

import static net.dm73.quizitup.QuizActivity.mFirebaseAnalytics;
import static net.dm73.quizitup.QuizActivity.params;

public class FragmentQuizResultat extends Fragment {

    private Button result;
    private boolean hideAds;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_quiz_resultat, container, false);

        result = (Button) rootView.findViewById(R.id.resultat);
        result.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/AdventPro-ExtraLight.ttf"));

        SpinKitView spinResult = (SpinKitView) rootView.findViewById(R.id.spinResult);
        Pulse pulse = new Pulse();
        spinResult.setIndeterminateDrawable(pulse);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        final Intent intent = new Intent();
        intent.setClass(getContext(), ResultatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //add data to logevent
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "result");

        // get user status from sharedpreferences
        SharedPreferences prefs = getContext().getSharedPreferences("settings", 0);
        hideAds = prefs.getBoolean("adsremoved", false);


        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
                mFirebaseAnalytics.logEvent("finish_quiz", params);

                if (!hideAds && QuizActivity.mInterstitialAd.isLoaded()) {
                    MusicPlayer.pauseMusic();
                    QuizActivity.mInterstitialAd.show();
                } else {
                    getActivity().getApplicationContext().startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        buttonEffect(result);
    }

    //adding the animation on button click
    private void buttonEffect(View button) {
        button.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            v.setBackgroundResource(R.drawable.ripple);
                            v.invalidate();
                            break;
                        }
                    }
                    case MotionEvent.ACTION_UP: {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            v.getBackground().clearColorFilter();
                            v.invalidate();
                            break;
                        }
                    }
                }
                return false;
            }
        });
    }


}
