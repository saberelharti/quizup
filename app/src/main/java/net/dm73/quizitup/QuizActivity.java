package net.dm73.quizitup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.dm73.quizitup.model.Answer;
import net.dm73.quizitup.model.Question;
import net.dm73.quizitup.util.ActivityConfig;
import net.dm73.quizitup.util.Constants;
import net.dm73.quizitup.util.DataApplication;
import net.dm73.quizitup.util.MusicPlayer;
import net.dm73.quizitup.util.MusicService;
import net.dm73.quizitup.util.nonSwippableViewPager;
import net.dm73.quizitup.util.urlCrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {

    public static ImageButton moveRight;
    public static ImageButton moveLeft;

    private TextView totalQuestions;
    private TextView numbreQuestion;
    private TextView titreQuiz;

    private static Animation animation;
    private RelativeLayout cloudProbLayout;
    private ImageView imageCloud;
    private ProgressBar progressBar;
    private ImageView settings;

    private nonSwippableViewPager viewPager;
    private List<Fragment> listQuiz;
    private List<Question> listQuestions;
    private DataApplication data;

    private static AdView mAdView;
    public static InterstitialAd mInterstitialAd;
    private AdsInterstitial adsInterstitial;
    public static FirebaseAnalytics mFirebaseAnalytics;
    public static Bundle params;

    private boolean stopMusic;
    public boolean hideAds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        getWindow().setBackgroundDrawable(null);
        ActivityConfig.setStatusBarTranslucent((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP), getWindow());

        data = (DataApplication) getApplication();
        data.setListAnwers(new String[0][0]);
        listQuestions = new ArrayList<>();
        listQuiz = new ArrayList<>();


        Typeface adventProExl = Typeface.createFromAsset(getAssets(), "fonts/AdventPro-ExtraLight.ttf");
        totalQuestions = (TextView) findViewById(R.id.num_test);
        numbreQuestion = (TextView) findViewById(R.id.text_count);
        totalQuestions.setTypeface(adventProExl);
        numbreQuestion.setTypeface(adventProExl);

        cloudProbLayout = (RelativeLayout) findViewById(R.id.hors_connexion_layout);
        viewPager = (nonSwippableViewPager) findViewById(R.id.questionary);
        imageCloud = (ImageView) findViewById(R.id.problem_cloud);
        progressBar = (ProgressBar) findViewById(R.id.bar_progress_resultat);
        settings = (ImageButton) findViewById(R.id.settings);
        TextView titre = (TextView) findViewById(R.id.title_category);
        titre.setText(data.getQuiz().getCategorie());
        titre.setTypeface(adventProExl);

        titreQuiz = (TextView) findViewById(R.id.title_question);
        titreQuiz.setText(data.getQuiz().getTitle());
        titreQuiz.setTypeface(adventProExl);

        moveRight = (ImageButton) findViewById(R.id.move_right);
        moveLeft = (ImageButton) findViewById(R.id.move_left);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusic = false;
                startActivity(new Intent().setClass(QuizActivity.this, SettingsActivity.class).putExtra("activity", "QUIZ_ACTIVITY"));
            }
        });

        // get user status from sharedpreferences
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        hideAds = prefs.getBoolean("adsremoved", false);

        //geting the FirebaseAnalytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, data.getQuiz().getId());

        if (!hideAds) {
            MobileAds.initialize(getApplicationContext(), getString(R.string.app_id));
            mAdView = new AdView(this);
            mAdView.setAdSize(AdSize.SMART_BANNER);
            mAdView.setAdUnitId(getString(R.string.banner_home_footer));
            AdRequest mAdReaquest = new AdRequest.Builder().build();
            mAdView.loadAd(mAdReaquest);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            mAdView.setLayoutParams(layoutParams);
            RelativeLayout admobContainer = (RelativeLayout) findViewById(R.id.adMob);
            admobContainer.addView(mAdView);

            adsInterstitial = new AdsInterstitial();
            adsInterstitial.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isNetworkAvailable() && listQuiz.size() == 0) {
            progressBarVisible();
            getQuestions(data.getQuiz().getHash(), getSupportFragmentManager());
        } else if (listQuiz.size() != 0) {
            recycleViewVisible();
        } else {
            layoutCloudVisible();
        }


        moveRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Clear the animation
                moveRight.clearAnimation();

                //incrementation of the page
                if (viewPager != null && listQuiz != null) {

                    int nextPage = viewPager.getCurrentItem() + 1;
                    if (viewPager.getCurrentItem() < (listQuiz.size() - 1) && data.itemListAnswersNotEmpty(viewPager.getCurrentItem(), 0)) {
                        //Small vibration between transition
                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vibrator.vibrate(20);
                        viewPager.setCurrentItem(nextPage);
                        if (nextPage < (listQuiz.size() - 1))
                            numbreQuestion.setText((nextPage + 1) + "");
                    } else {
                        return;
                    }

                    if (viewPager.getCurrentItem() == 1) {
                        moveLeft.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_appear));
                        moveLeft.setVisibility(View.VISIBLE);
                    }

                    if (viewPager.getCurrentItem() == (listQuiz.size() - 1)) {
                        moveRight.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_disappear));
                        moveRight.setVisibility(View.GONE);
                        titreQuiz.setVisibility(View.INVISIBLE);

                        AnimationCounterlast animationCounter = new AnimationCounterlast();
                        animationCounter.start();
                    }
                }
            }
        });

        //swip on left the view pager
        moveLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (viewPager != null) {

                    int previeousPage = viewPager.getCurrentItem() - 1;

                    //decrementation of the page
                    if (0 < viewPager.getCurrentItem()) {

                        //Small vibration between transition
                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vibrator.vibrate(20);
                        viewPager.setCurrentItem(previeousPage);
                        numbreQuestion.setText((previeousPage + 1) + "");

                    } else {
                        return;
                    }

                    if (viewPager.getCurrentItem() == 0) {
                        moveLeft.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_disappear));
                        moveLeft.setVisibility(View.GONE);
                    }

                    if (animation != null) {
                        moveRight.clearAnimation();
                    }

                    if (viewPager.getCurrentItem() == (listQuiz.size() - 2)) {
                        titreQuiz.setVisibility(View.VISIBLE);

                        moveRight.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_appear));
                        moveRight.setVisibility(View.VISIBLE);
                        AnimationCounter animationCounter = new AnimationCounter();
                        animationCounter.start();
                    }
                }
            }
        });

        imageCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarVisible();
                getQuestions(data.getQuiz().getHash(), getSupportFragmentManager());
            }
        });

        if (!hideAds && mAdView != null) {
            mAdView.resume();
        }

        //starting the music service
        stopMusic = true;
        if (!MusicService.SERVICERUNNING) {
            data.setIntent(new Intent("net.dm73.quizitup.MUSIC_SERVICE").setClass(this, MusicService.class).putExtra("trackId", data.getQuiz().getCategorie().toLowerCase()));
            startService(data.getIntent());
        } else if (MusicPlayer.musicPaused) {
            MusicPlayer.startMusic();
        }
    }

    @Override
    protected void onPause() {
        if (MusicService.SERVICERUNNING && stopMusic) {
            MusicPlayer.pauseMusic();
        }

        if (!hideAds && mAdView != null) {
            mAdView.pause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (!hideAds && mAdView != null) {
            mAdView.destroy();
        }

        FragmentQuiz.FRAGMENT_COUNT = 0;
        super.onDestroy();
    }

    public static void removeAds() {
        mAdView.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onBackPressed() {
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "cancel quiz");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
        mFirebaseAnalytics.logEvent("cancel_quiz", params);
        super.onBackPressed();
    }

    static void startAnimation(Context context) {
        animation = AnimationUtils.loadAnimation(context, R.anim.animation_button_next);
        moveRight.startAnimation(animation);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void layoutCloudVisible() {
        cloudProbLayout.setVisibility(View.VISIBLE);
        titreQuiz.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void recycleViewVisible() {
        titreQuiz.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        cloudProbLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void progressBarVisible() {
        progressBar.setVisibility(View.VISIBLE);
        cloudProbLayout.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
    }

    private void getQuestions(final String hash, final FragmentManager fragmentManager) {

        final RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest timeServer = new JsonObjectRequest(Request.Method.GET, Constants.API_TIME_STAMP, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String timeStamp = "";

                try {
                    timeStamp = response.getString("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                    layoutCloudVisible();
                }

                JsonObjectRequest Questionsdata = new JsonObjectRequest(Request.Method.GET, urlCrypter.decodeURL(Constants.API_TEST + hash, timeStamp), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject jsonObjectData = response.getJSONObject("data");

                            JSONArray jsonArrayQuestions = jsonObjectData.getJSONArray("questions");
                            String[][] RESPONSE = new String[jsonArrayQuestions.length()][4];
                            for (int i = 0; i < jsonArrayQuestions.length(); i++) {
                                JSONObject jsonArrayQuestion = jsonArrayQuestions.getJSONObject(i);
                                JSONArray jsonArrayAnswers = jsonArrayQuestion.getJSONArray("answers");

                                List<Answer> listAnswers = new ArrayList<>();
                                for (int j = 0; j < jsonArrayAnswers.length(); j++) {
                                    JSONObject jsonObjectAnswer = jsonArrayAnswers.getJSONObject(j);
                                    listAnswers.add(new Answer(jsonObjectAnswer.getString("answer"), jsonObjectAnswer.getInt("value"), jsonObjectAnswer.getString("client_id"), jsonObjectAnswer.getString("value")));
                                }


                                listQuestions.add(new Question(jsonArrayQuestion.getString("question"), jsonArrayQuestion.getString("client_id"), listAnswers));
                                RESPONSE[i][0] = "0";
                                RESPONSE[i][1] = jsonArrayQuestion.getString("client_id");
                            }
                            data.setListAnwers(RESPONSE);

                            for (int i = 0; i < listQuestions.size(); i++) {
                                FragmentQuiz fragmentQuiz = FragmentQuiz.newInstance(listQuestions.get(i));
                                listQuiz.add(fragmentQuiz);
                            }

                            recycleViewVisible();

                            totalQuestions.setText(listQuestions.size() + "");

                            listQuiz.add(new FragmentQuizResultat());
                            FragmentViewPagerQuiz fragmentViewPagerQuiz = new FragmentViewPagerQuiz(fragmentManager, listQuiz);
                            viewPager.setAdapter(fragmentViewPagerQuiz);

                            AnimationCounter animationCounter = new AnimationCounter();
                            animationCounter.start();


                        } catch (JSONException e) {
                            e.printStackTrace();
                            layoutCloudVisible();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        layoutCloudVisible();
                    }


                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        String credentials = "demo:demodm73";
                        String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", auth);
                        return headers;
                    }
                };

                queue.add(Questionsdata);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layoutCloudVisible();
            }
        });

        queue.add(timeServer);

    }

    private class AnimationCounter extends Thread {

        @Override
        public void run() {
            super.run();

            final Animation slidAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_to_left);

            final Animation transAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.trans_lineare);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    numbreQuestion.startAnimation(slidAnim);
                    totalQuestions.startAnimation(transAnim);
                    numbreQuestion.setVisibility(View.VISIBLE);
                    totalQuestions.setVisibility(View.VISIBLE);
                }
            });

        }
    }

    private class AnimationCounterlast extends Thread {

        @Override
        public void run() {
            super.run();

            final Animation slidAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_to_right);
            final Animation transAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.anti_trans_lineare);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    totalQuestions.startAnimation(transAnim);
                    numbreQuestion.startAnimation(slidAnim);
                    numbreQuestion.setVisibility(View.GONE);
                    totalQuestions.setVisibility(View.GONE);
                }
            });

        }

    }

    private class AdsInterstitial extends Thread {
        @Override
        public void run() {
            final AdRequest adRequestInter = new AdRequest.Builder().build();
            mInterstitialAd = new InterstitialAd(getApplicationContext());

            // set the ad unit ID and listner
            mInterstitialAd.setAdUnitId(getString(R.string.interstitial_start_quiz));
            mInterstitialAd.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                    MusicPlayer.startMusic();
                    startActivity(new Intent().setClass(getApplicationContext(), ResultatActivity.class));
                    finish();
                }

            });


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mInterstitialAd.loadAd(adRequestInter);
                }
            });


        }
    }


}
