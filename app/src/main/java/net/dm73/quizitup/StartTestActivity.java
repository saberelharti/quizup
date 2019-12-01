package net.dm73.quizitup;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.dm73.quizitup.util.ActivityConfig;
import net.dm73.quizitup.util.DataApplication;
import net.dm73.quizitup.util.MusicPlayer;
import net.dm73.quizitup.util.MusicService;


public class StartTestActivity extends Activity {

    private Button startButton;
    private RelativeLayout StartButtonBackground;
    private ImageButton settings;
    private DataApplication data;
    private static AdView mAdView;

    private InterstitialAd mInterstitialAd;
    private AdsInterstitial adsInterstitial;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Bundle params;

    private SharedPreferences prefs;
    private boolean hideAds;
    private boolean startMusic = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_test);

        getWindow().setBackgroundDrawable(null);
        ActivityConfig.setStatusBarTranslucent((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP), getWindow());


        data = (DataApplication) getApplication();

        TextView titre = (TextView) findViewById(R.id.title_category);
        TextView description = (TextView) findViewById(R.id.start_text);
        startButton = (Button) findViewById(R.id.start);
        StartButtonBackground = (RelativeLayout) findViewById(R.id.startTestBg);
        settings = (ImageButton) findViewById(R.id.settings);

        // getuser status from sharedpreferences
        prefs = getSharedPreferences("settings", 0);
        hideAds = prefs.getBoolean("adsremoved", false);


        Typeface adventProExl = Typeface.createFromAsset(getAssets(), "fonts/AdventPro-ExtraLight.ttf");
        Typeface caviarDreams = Typeface.createFromAsset(getAssets(), "fonts/CaviarDreams.ttf");

        description.setTypeface(adventProExl);
        titre.setTypeface(adventProExl);

        titre.setText(data.getQuiz().getCategorie());
        description.setText(data.getQuiz().getDescrition());

        startButton.setTypeface(caviarDreams);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(StartTestActivity.this, SettingsActivity.class).putExtra("activity", "START_TEST_ACTIVITY"));
            }
        });

        //get FirebaseAnalytics instance
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, data.getQuiz().getId());
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "start quiz");

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

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation_button);
                final Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.animation_button);
                MyBounceInterpolator interpolator = new MyBounceInterpolator(0.16, 18);
                animation.setInterpolator(interpolator);

                MyBounceInterpolator interpolator2 = new MyBounceInterpolator(0.2, 10);
                animation2.setInterpolator(interpolator2);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StartButtonBackground.startAnimation(animation2);
                    }
                });
            }
        }).start();

        hideAds = prefs.getBoolean("adsremoved", false);

        //renitialase the data
        data.setListAnwers(new String[0][0]);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //logEvent for start quiz
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params);
                mFirebaseAnalytics.logEvent("start_quiz", params);

                if (!hideAds && mInterstitialAd.isLoaded()) {
                    MusicPlayer.pauseMusic();
                    mInterstitialAd.show();
                } else {
                    startActivity(new Intent().setClass(getApplicationContext(), QuizActivity.class));
                    finish();
                }
            }
        });

        buttonEffect(startButton);

        if (!hideAds && mAdView != null) {
            mAdView.resume();
        }


        //starting the music service
        if (!MusicService.SERVICERUNNING) {
            data.setIntent(new Intent("net.dm73.quizitup.MUSIC_SERVICE").setClass(this, MusicService.class).putExtra("trackId", data.getQuiz().getCategorie().toLowerCase()));
            startService(data.getIntent());
        } else {
            if (startMusic) {
                MusicPlayer.startMusic();
            } else {
                MusicPlayer.changeTrack(this, data.getQuiz().getCategorie().toLowerCase(), getSharedPreferences("settings", 0).getInt("volume_level", 50));
            }
        }
    }

    @Override
    protected void onPause() {
        if (MusicService.SERVICERUNNING) {
//            stopService(data.getIntent());
            MusicPlayer.pauseMusic();
            startMusic = true;
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

        super.onDestroy();
    }

    public static void removeAds() {
        mAdView.setVisibility(View.INVISIBLE);
    }

    class MyBounceInterpolator implements android.view.animation.Interpolator {
        double mAmplitude = 1;
        double mFrequency = 10;

        MyBounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) *
                    Math.cos(mFrequency * time) + 1);
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
                    startActivity(new Intent().setClass(getApplicationContext(), QuizActivity.class));
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
