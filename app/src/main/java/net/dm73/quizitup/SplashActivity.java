package net.dm73.quizitup;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.style.ThreeBounce;

import net.dm73.quizitup.dao.DaoCategory;
import net.dm73.quizitup.dao.DataHelper;
import net.dm73.quizitup.model.Category;
import net.dm73.quizitup.util.ActivityConfig;
import net.dm73.quizitup.util.Constants;
import net.dm73.quizitup.util.urlCrypter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SplashActivity extends AppCompatActivity {

    private static SpinKitView spinView;
    private static DialogFragment alertDialog;
    private ThreeBounce threeBounce;
    public static List<Category> categoryData = new ArrayList<>();
    private static boolean volleyRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //fixe perfermance overdrawing
        getWindow().setBackgroundDrawable(null);
        ActivityConfig.setStatusBarTranslucent((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP), getWindow());

        //getting the view
        spinView = (SpinKitView) findViewById(R.id.spinView);

        if (savedInstanceState != null)
            volleyRunning = savedInstanceState.getBoolean("volleyRunning");
        else volleyRunning = false;

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkAvailable(getApplicationContext())) {

            spinView.setVisibility(View.GONE);

            alertDialog = new AlertDialog();
            alertDialog.show(getSupportFragmentManager(), "Error");

        } else {

            threeBounce = new ThreeBounce();
            spinView.setIndeterminateDrawable(threeBounce);

            //start the animation
            new AnimationThread().start();

            if (!volleyRunning) {
                //getting the categorie data from the server
                RequestVolley(this, getSupportFragmentManager());
            }

        }
    }

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public static void RequestVolley(final Context context, final FragmentManager fragmentManager) {

        final RequestQueue queue = Volley.newRequestQueue(context);
        final String url = Constants.API_CATEGORIES;
        String URL = Constants.API_TIME_STAMP;
        final String[] TimeStamp = {""};
        volleyRunning = true;

        spinView.setVisibility(View.VISIBLE);

        if (alertDialog != null && alertDialog.isVisible())
            alertDialog.dismiss();


        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    TimeStamp[0] = response.getString("data");

                    // Request a string response from the provided URL.
                    JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, urlCrypter.decodeURL(url, TimeStamp[0]), null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            JSONArray jsonObjectData = null;
                            try {

                                jsonObjectData = response.getJSONArray("data");

                                for (int i = 0; i < jsonObjectData.length(); i++) {
                                    JSONObject jsonObjectCategory = jsonObjectData.getJSONObject(i);
                                    categoryData.add(new Category(jsonObjectCategory.getInt("id"), jsonObjectCategory.getString("name"), jsonObjectCategory.getString("image")));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            //stock the categories inside db
                            DataHelper dataHelper = new DataHelper(context);
                            DaoCategory daoCategory = new DaoCategory(dataHelper);
                            if (daoCategory.countCategories() == categoryData.size()) {
                                daoCategory.replacecategories(categoryData);
                            } else {
                                daoCategory.deleteAllCategories();
                                daoCategory.replacecategories(categoryData);
                            }

                            Intent intent = new Intent();
                            intent.setClass(context, CategoryActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            volleyRunning = false;
                            spinView.setVisibility(View.GONE);
                            alertDialog = new AlertDialog();
                            alertDialog.show(fragmentManager, "Error");
                            VolleyLog.e("Error request", error.getMessage());
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

                    queue.add(stringRequest);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyRunning = false;
                spinView.setVisibility(View.GONE);
                alertDialog = new AlertDialog();
                alertDialog.show(fragmentManager, "Error");
                VolleyLog.e("error", error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        queue.add(JsonRequest);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("volleyRunning", volleyRunning);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    class MyBounceInterpolator implements android.view.animation.Interpolator {
        double mAmplitude = 1;
        double mFrequency = 10;

        MyBounceInterpolator(double amplitude, double frequency) {
            mAmplitude = amplitude;
            mFrequency = frequency;
        }

        public float getInterpolation(float time) {
            return (float) (-1 * Math.pow(Math.E, -time / mAmplitude) * Math.cos(mFrequency * time) + 1);
        }
    }

    private class AnimationThread extends Thread {

        ImageView logo = (ImageView) findViewById(R.id.logoIntro);
        ImageView NameQuiz = (ImageView) findViewById(R.id.quizupName);
        ImageView SubNameQuiz = (ImageView) findViewById(R.id.quizupSubName);

        @Override
        public void run() {
            final Animation flatAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_intro);
            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
            flatAnim.setInterpolator(interpolator);
            final Animation ApearAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.app_name_appear);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SubNameQuiz.setVisibility(View.VISIBLE);
                    spinView.setVisibility(View.VISIBLE);
                    logo.startAnimation(flatAnim);
                    NameQuiz.startAnimation(ApearAnim);
                    SubNameQuiz.startAnimation(ApearAnim);
                    spinView.startAnimation(ApearAnim);
                }
            });


        }

    }
}
