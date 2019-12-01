package net.dm73.quizitup;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
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
import com.google.android.gms.ads.NativeExpressAdView;

import net.dm73.quizitup.model.Quiz;
import net.dm73.quizitup.util.ActivityConfig;
import net.dm73.quizitup.util.Constants;
import net.dm73.quizitup.util.DataApplication;
import net.dm73.quizitup.util.MusicPlayer;
import net.dm73.quizitup.util.MusicService;
import net.dm73.quizitup.util.urlCrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestActivity extends AppCompatActivity implements ItemClickListenner {


    private GridLayoutManager gridLayoutManager;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private List<Object> QuizsCategory;
    private RelativeLayout cloudProbLayout;
    private ImageView imageCloud;
    private DataApplication data;
    private ImageButton settings;
    private boolean stopMusic;
    private boolean hideAds;
    private int firstAds = 0;
    private String nativeAds;
    private int nativeAdsHeight;


    private static CustomAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        getWindow().setBackgroundDrawable(null);
        ActivityConfig.setStatusBarTranslucent((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP), getWindow());

        TextView titre = (TextView) findViewById(R.id.title_category);
        Typeface adventProExl = Typeface.createFromAsset(getAssets(), "fonts/AdventPro-ExtraLight.ttf");
        titre.setText(getIntent().getStringExtra("categorieName"));
        titre.setTypeface(adventProExl);
        progressBar = (ProgressBar) findViewById(R.id.bar_progress_resultat);
        cloudProbLayout = (RelativeLayout) findViewById(R.id.hors_connexion_layout);
        imageCloud = (ImageView) findViewById(R.id.problem_cloud);
        settings = (ImageButton) findViewById(R.id.settings);

        progressBar.getIndeterminateDrawable();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        QuizsCategory = new ArrayList<>();
        data = (DataApplication) getApplication();

        float scale = TestActivity.this.getResources().getDisplayMetrics().density;
        if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            nativeAds = getString(R.string.ad_unit_id_tab);
            nativeAdsHeight = (int) (120 * scale);

        } else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            nativeAds = getString(R.string.ad_unit_id_tab);
            nativeAdsHeight = (int) (110 * scale);
        } else {
            nativeAds = getString(R.string.ad_unit_id);
            nativeAdsHeight = 100;
        }


        // get user status from sharedpreferences
        SharedPreferences prefs = getSharedPreferences("settings", 0);
        hideAds = prefs.getBoolean("adsremoved", false);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopMusic = false;
                startActivity(new Intent().setClass(TestActivity.this, SettingsActivity.class).putExtra("activity", "TEST_ACTIVITY"));
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isNetworkAvailable() && QuizsCategory.isEmpty()) {
            progressBarVisible();
            loadRecyclerViewData(getIntent().getStringExtra("position"));
        } else if (!QuizsCategory.isEmpty()) {
            recyclerView.getAdapter().notifyDataSetChanged();
            recycleViewVisible();
        } else {
            layoutCloudVisible();
        }

        imageCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBarVisible();
                loadRecyclerViewData(getIntent().getStringExtra("position"));
            }
        });

        stopMusic = true;

        if (!MusicService.SERVICERUNNING) {
            data.setIntent(new Intent("net.dm73.quizitup.MUSIC_SERVICE").setClass(this, MusicService.class).putExtra("trackId", "intro"));
            startService(data.getIntent());
        } else if (MusicPlayer.musicPaused) {
            if (MusicPlayer.trackid == "intro") MusicPlayer.startMusic();
            else
                MusicPlayer.changeTrack(getApplicationContext(), "intro", getSharedPreferences("settings", 0).getInt("volume_level", 50));
        }

    }

    @Override
    protected void onPause() {
        if (MusicService.SERVICERUNNING && stopMusic) {
            MusicPlayer.pauseMusic();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        stopMusic = false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void layoutCloudVisible() {
        cloudProbLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void progressBarVisible() {
        progressBar.setVisibility(View.VISIBLE);
        cloudProbLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
    }

    private void recycleViewVisible() {
        recyclerView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        cloudProbLayout.setVisibility(View.GONE);
    }


    private void loadRecyclerViewData(final String idCategory) {

        final RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, Constants.API_TIME_STAMP, null, new Response.Listener<JSONObject>() {
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
                JsonObjectRequest JsonQuizs = new JsonObjectRequest(Request.Method.GET, urlCrypter.decodeQuizURL(Constants.API_QUIZS + idCategory, timeStamp), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject jsonObjectData = response.getJSONObject("data");
                            JSONArray jsonArrayProjects = jsonObjectData.getJSONArray("projects");

                            if (hideAds) {

                                for (int j = 0; j < jsonArrayProjects.length(); j++) {
                                    JSONObject jsonObjectQuiz = jsonArrayProjects.getJSONObject(j);

                                    if (jsonObjectQuiz.getString("type").equals("psycho")) {
                                        JSONArray jsonArrayHasSites = jsonObjectQuiz.getJSONArray("has_sites");
                                        JSONArray jsonArrayVersions = jsonObjectQuiz.getJSONArray("versions");

                                        QuizsCategory.add(new Quiz(jsonObjectQuiz.getString("id"), "psycho",
                                                jsonArrayHasSites.getJSONObject(0).getString("hash"),
                                                jsonArrayVersions.getJSONObject(0).getString("title"),
                                                jsonObjectData.getString("name"),
                                                jsonArrayVersions.getJSONObject(0).getString("image"),
                                                jsonArrayVersions.getJSONObject(0).getString("appreciation"),
                                                jsonObjectQuiz.getString("published_mobile_at")));
                                    }
                                }

                            } else {

                                int randomAds = (int) (Math.random() * 3 + 1);

                                for (int j = 0; j < jsonArrayProjects.length(); j++) {

                                    /**
                                     * Adds Native Express ads to the items list.
                                     */
                                    if (randomAds == 0) {
                                        NativeExpressAdView adView = new NativeExpressAdView(TestActivity.this);
                                        QuizsCategory.add(adView);
                                        randomAds = (int) (Math.random() * 3 + 1);
                                    } else {
                                        --randomAds;
                                    }

                                    JSONObject jsonObjectQuiz = jsonArrayProjects.getJSONObject(j);

                                    if (jsonObjectQuiz.getString("type").equals("psycho")) {
                                        JSONArray jsonArrayHasSites = jsonObjectQuiz.getJSONArray("has_sites");
                                        JSONArray jsonArrayVersions = jsonObjectQuiz.getJSONArray("versions");

                                        QuizsCategory.add(new Quiz(jsonObjectQuiz.getString("id"), "psycho",
                                                jsonArrayHasSites.getJSONObject(0).getString("hash"),
                                                jsonArrayVersions.getJSONObject(0).getString("title"),
                                                jsonObjectData.getString("name"),
                                                jsonArrayVersions.getJSONObject(0).getString("image"),
                                                jsonArrayVersions.getJSONObject(0).getString("appreciation"),
                                                jsonObjectQuiz.getString("published_mobile_at")));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            layoutCloudVisible();
                        }

                        recycleViewVisible();

                        adapter = new CustomAdapter(getApplicationContext(), QuizsCategory, finalTimeStamp);
                        gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);

                        recyclerView.setLayoutManager(gridLayoutManager);
                        adapter.setItemClickListenner(TestActivity.this);
                        recyclerView.setAdapter(adapter);

                        if (!hideAds) setUpAndLoadNativeExpressAds();

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

                queue.add(JsonQuizs);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                layoutCloudVisible();
            }
        });

        queue.add(JsonRequest);

    }

    public static void setAdapterNoAds() {
        if (adapter != null)
            adapter.deleteAdsNative();
    }


    private void loadNativeExpressAd(final int index) {

        if (index >= QuizsCategory.size()) {
            return;
        }

        Object item = QuizsCategory.get(index);
        if (!(item instanceof NativeExpressAdView)) {
            throw new ClassCastException("Expected item at index " + index + " to be a Native" + " Express ad.");
        }

        //getting the next indext


        final NativeExpressAdView adView = (NativeExpressAdView) item;

        // Set an AdListener on the NativeExpressAdView to wait for the previous Native Express ad
        // to finish loading before loading the next ad in the items list.
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // The previous Native Express ad loaded successfully, call this method again to
                // load the next ad in the items list.

                int nextIndex = index;
                while (++nextIndex < QuizsCategory.size()) {
                    if (QuizsCategory.get(nextIndex) instanceof NativeExpressAdView)
                        break;
                }
                loadNativeExpressAd(nextIndex);
                RelativeLayout cardView = (RelativeLayout) findViewById(R.id.cardNative);
                if (cardView != null) cardView.setVisibility(View.VISIBLE);
                adView.setVisibility(View.VISIBLE);
            }


            @Override
            public void onAdFailedToLoad(int errorCode) {
                // The previous Native Express ad failed to load. Call this method again to load
                // the next ad in the items list.
                int nextIndex = index;
                while (++nextIndex < QuizsCategory.size()) {
                    if (QuizsCategory.get(nextIndex) instanceof NativeExpressAdView)
                        break;
                }
                loadNativeExpressAd(nextIndex);


            }
        });

        // Load the Native Express ad.
        adView.loadAd(new AdRequest.Builder().build());
    }


    /**
     * Sets up and loads the Native Express ads.
     */
    private void setUpAndLoadNativeExpressAds() {
        // Use a Runnable to ensure that the RecyclerView has been laid out before setting the
        // ad size for the Native Express ad. This allows us to set the Native Express ad's
        // width to match the full width of the RecyclerView.
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                final float scale = TestActivity.this.getResources().getDisplayMetrics().density;

                boolean firstNotFound = true;
                // Set the ad size and ad unit ID for each Native Express ad in the items list.
                for (int i = 0; i < QuizsCategory.size(); i++) {
                    if (!(QuizsCategory.get(i) instanceof Quiz)) {
                        NativeExpressAdView adView = (NativeExpressAdView) QuizsCategory.get(i);
                        RelativeLayout cardView = (RelativeLayout) findViewById(R.id.cardNative);
                        AdSize adSize = new AdSize((cardView != null) ? (int) (cardView.getWidth() / scale) : 200, nativeAdsHeight);
                        adView.setAdSize(adSize);
                        adView.setAdUnitId(nativeAds);
                        if (cardView != null) cardView.setVisibility(View.GONE);
                        adView.setVisibility(View.GONE);

                        if (firstNotFound) {
                            firstNotFound = false;
                            firstAds = i;
                        }

                    }
                }

                // Load the first Native Express ad in the items list.
                loadNativeExpressAd(firstAds);
            }
        });
    }

    @Override
    public void onClick(View view, int position) {
        data.setQuiz((Quiz) QuizsCategory.get(position));
        Intent intent = new Intent(getApplicationContext(), StartTestActivity.class);
        startActivity(intent);

    }

}
