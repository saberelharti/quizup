package net.dm73.quizitup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adcolony.sdk.AdColony;
import com.adcolony.sdk.AdColonyInterstitial;
import com.adcolony.sdk.AdColonyInterstitialListener;
import com.adcolony.sdk.AdColonyZone;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.dm73.quizitup.dao.DaoQuiz;
import net.dm73.quizitup.util.ActivityConfig;
import net.dm73.quizitup.util.Constants;
import net.dm73.quizitup.util.DataApplication;
import net.dm73.quizitup.util.MusicPlayer;
import net.dm73.quizitup.util.MusicService;
import net.dm73.quizitup.util.urlCrypter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ResultatActivity extends AppCompatActivity {

    private Button redoQuiz;
    private Button newQuiz;
    private ImageView share;
    private TextView titleResultat;
    private TextView resultatText;
    private LinearLayout resultTitle;
    private RelativeLayout cloudProbLayout;
    private ArrayList<String> listResult;
    private ProgressBar progressBar;
    private ImageView imageCloud;
    private DataApplication data;
    private boolean hideAds;
    private boolean stopMusic;
    private boolean redoClicked = false;
    private String textShare;
    private TextView titreQuiz;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Bundle savedInstance;

    private String APP_ID = "app4ac6015feae9403191";
    private String ZONE_ID = "vzb1b2b561624d4021b5";
    private AdColonyInterstitial ad;
    private AdColonyInterstitialListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultat);

        getWindow().setBackgroundDrawable(null);
        ActivityConfig.setStatusBarTranslucent((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP), getWindow());

        data = (DataApplication) getApplicationContext();
        savedInstance = savedInstanceState;


        TextView titre = (TextView) findViewById(R.id.title_category);
        titreQuiz = (TextView) findViewById(R.id.title_question);
        titleResultat = (TextView) findViewById(R.id.title_resultat);
        resultatText = (TextView) findViewById(R.id.resultat_text);
        resultTitle = (LinearLayout) findViewById(R.id.result);
        cloudProbLayout = (RelativeLayout) findViewById(R.id.hors_connexion_layout);
        redoQuiz = (Button) findViewById(R.id.redo);
        newQuiz = (Button) findViewById(R.id.newtest);
        share = (ImageButton) findViewById(R.id.share);
        progressBar = (ProgressBar) findViewById(R.id.bar_progress_resultat);
        imageCloud = (ImageView) findViewById(R.id.problem_cloud);
        listResult = new ArrayList<>();

        Typeface adventProExl = Typeface.createFromAsset(getAssets(), "fonts/AdventPro-ExtraLight.ttf");

        titre.setText(data.getQuiz().getCategorie());
        titreQuiz.setText(data.getQuiz().getTitle());
        titreQuiz.setTypeface(adventProExl);
        titleResultat.setTypeface(adventProExl);
        titre.setTypeface(adventProExl);

        redoQuiz.setTypeface(adventProExl);
        newQuiz.setTypeface(adventProExl);


        // getuser status from sharedpreferences
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        hideAds = prefs.getBoolean("adsremoved", false);

        // get FirebaseAnalytics insatnce and pass data
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        final Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, data.getQuiz().getId());
        params.putString(FirebaseAnalytics.Param.CONTENT_TYPE, data.getQuiz().getCategorie());

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textShare != null) {
                    //sending logEvent data
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, params);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, textShare);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.shareTitle)));
                }
            }
        });

        if (!hideAds) {
            AdColony.configure(this, APP_ID, ZONE_ID);

            listener = new AdColonyInterstitialListener() {
                @Override
                public void onRequestFilled(AdColonyInterstitial adColonyInterstitial) {
                    ResultatActivity.this.ad = adColonyInterstitial;
                }

                @Override
                public void onRequestNotFilled(AdColonyZone zone) {
                    super.onRequestNotFilled(zone);
                }

                @Override
                public void onExpiring(AdColonyInterstitial ad) {
                    AdColony.requestInterstitial(ZONE_ID, this);
                }

                @Override
                public void onClosed(AdColonyInterstitial ad) {
                    super.onClosed(ad);
                    if (redoClicked) {
                        startActivity(new Intent().setClass(getApplicationContext(), QuizActivity.class));
                        finish();
                    } else {
                        finish();
                    }

                }
            };
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (savedInstance != null && savedInstance.getStringArrayList("listresult") != null) {
            listResult = savedInstance.getStringArrayList("listresult");
            titleResultat.setText(listResult.get(0));
            resultatText.setText(listResult.get(1));
            layoutResultVisible();
        } else {

            if (!hideAds && (ad == null || ad.isExpired())) {
                AdColony.requestInterstitial(ZONE_ID, listener);
            }

            if (isNetworkAvailable() && listResult.size() == 0) {
                progressBarVisible();
                getResult();
            } else if (listResult.size() != 0) {
                layoutResultVisible();
            } else {
                layoutCloudVisible();
            }
        }


        redoQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redoClicked = true;

                if (!hideAds && ad != null && !ad.isExpired()) {
                    ad.show();
                } else {
                    startActivity(new Intent().setClass(getApplicationContext(), QuizActivity.class));
                    finish();
                }
            }
        });

        //track the new test button
        final Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, data.getQuiz().getId());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "nouveau quiz");

        newQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                mFirebaseAnalytics.logEvent("nouveau_quiz", bundle);
                MusicPlayer.pauseMusic();
                if (!hideAds && ad != null && !ad.isExpired()) {
                    ad.show();
                } else {
                    finish();
                }
            }
        });

        imageCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarVisible();
                getResult();
            }
        });

        if (!MusicService.SERVICERUNNING) {
            data.setIntent(new Intent("net.dm73.quizitup.MUSIC_SERVICE").setClass(this, MusicService.class).putExtra("trackId", data.getQuiz().getCategorie().toLowerCase()));
            startService(data.getIntent());
        } else if (MusicPlayer.musicPaused) {
            MusicPlayer.startMusic();
        }

        stopMusic = true;
    }

    @Override
    protected void onPause() {
        if (MusicService.SERVICERUNNING && stopMusic) {
            MusicPlayer.pauseMusic();
        } else {
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //save the data for the user
        outState.putStringArrayList("listresult", listResult);

        super.onSaveInstanceState(outState);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void layoutCloudVisible() {
        cloudProbLayout.setVisibility(View.VISIBLE);
        titreQuiz.setVisibility(View.GONE);
        resultTitle.setVisibility(View.GONE);
        resultatText.setVisibility(View.GONE);
        titleResultat.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    private void layoutResultVisible() {
        titreQuiz.setVisibility(View.VISIBLE);
        resultTitle.setVisibility(View.VISIBLE);
        resultatText.setVisibility(View.VISIBLE);
        titleResultat.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        cloudProbLayout.setVisibility(View.GONE);
    }

    private void progressBarVisible() {
        progressBar.setVisibility(View.VISIBLE);
        resultTitle.setVisibility(View.GONE);
        resultatText.setVisibility(View.GONE);
        titleResultat.setVisibility(View.GONE);
        cloudProbLayout.setVisibility(View.GONE);
    }

    private void getResult() {
        final RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest timeStamp = new JsonObjectRequest(Request.Method.GET, Constants.API_TIME_STAMP, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String timeStamp = "";
                try {
                    timeStamp = response.getString("data");
                } catch (JSONException e) {
                    e.printStackTrace();
                    layoutCloudVisible();
                }

                final String finalTimeStamp = timeStamp;
                JsonObjectRequest jsonResult = new JsonObjectRequest(Request.Method.GET, urlCrypter.decodeResultURL(responcesApi(), timeStamp), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null) {

                            try {

                                JSONObject jsonObjectCorrection = response.getJSONObject("correction");
                                titleResultat.setText(jsonObjectCorrection.getString("title").replaceAll("\u0092", "'"));
                                resultatText.setText(jsonObjectCorrection.getString("description_no_tags").replaceAll("\u0092", "'"));
                                listResult.add(jsonObjectCorrection.getString("title").replaceAll("\u0092", "'"));
                                listResult.add(jsonObjectCorrection.getString("description_no_tags").replaceAll("\u0092", "'"));
                                listResult.add(response.getString("hash"));

                                InsertQuiz insertQuiz = new InsertQuiz();
                                insertQuiz.execute(data.getQuiz().getId());

                                textShare = listResult.get(0) + "\n\n" + listResult.get(1) + getString(R.string.shareURL);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                layoutCloudVisible();
                            }
                        } else {
                            layoutCloudVisible();
                            textShare = null;
                        }

                        layoutResultVisible();

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

                queue.add(jsonResult);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layoutCloudVisible();
            }
        });
        queue.add(timeStamp);
    }

    private class InsertQuiz extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            //memorise the id of the quiz
            DaoQuiz daoQuiz = new DaoQuiz(getApplicationContext());
            if (!daoQuiz.isPassed(params[0])) {
                daoQuiz.insertPassedQuiz(params[0]);
            }
            return null;
        }
    }

    private String responcesApi() {

        String responce = Constants.API_TEST + data.getQuiz().getHash() + "/result?";

        String[][] listAnswers = data.getListAnwers();

        for (int i = 0; i < listAnswers.length; i++) {
            responce += "responses%5B" + listAnswers[i][1] + "%5D=" + listAnswers[i][3] + "&";
        }

        for (int i = 0; i < listAnswers.length; i++) {
            if (i != (listAnswers.length - 1)) {
                responce += "value_responses%5B" + listAnswers[i][1] + "%5D=" + listAnswers[i][2] + "&";
            } else
                responce += "value_responses%5B" + listAnswers[i][1] + "%5D=" + listAnswers[i][2];
        }

        return responce;
    }

}
