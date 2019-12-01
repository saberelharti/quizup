package net.dm73.quizitup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import net.dm73.quizitup.dao.DaoCategory;
import net.dm73.quizitup.dao.DataHelper;
import net.dm73.quizitup.model.Category;
import net.dm73.quizitup.util.ActivityConfig;
import net.dm73.quizitup.util.DataApplication;
import net.dm73.quizitup.util.MusicPlayer;
import net.dm73.quizitup.util.MusicService;
import net.dm73.quizitup.util.RateQuizApp;

import java.util.List;

import static net.dm73.quizitup.util.Constants.ENCODED_PUBLIC_KEY;

public class CategoryActivity extends AppCompatActivity {

    private GridView gridView;
    private List<Category> listCategories;
    private DataApplication dataApplication;
    private boolean stopMusic;
    private BillingProcessor bp;
    private String SKU_REMOVE_ADS = "remove_ads";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        getWindow().setBackgroundDrawable(null);
        ActivityConfig.setStatusBarTranslucent((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP), getWindow());

        dataApplication = (DataApplication) getApplication();
        prefs = getSharedPreferences("settings", 0);

        gridView = (GridView) findViewById(R.id.simpleGridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                stopMusic = false;
                Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                intent.putExtra("categorieName", listCategories.get(position).getName());
                intent.putExtra("position", listCategories.get(position).getId() + "");
                startActivity(intent);

            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                RateQuizApp.app_launched(CategoryActivity.this, getSupportFragmentManager());
            }
        }, 1000);

    }

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                //getting the list of categories
                DataHelper dataHelper = new DataHelper(getApplicationContext());
                listCategories = (new DaoCategory(dataHelper)).getAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageAdapter adapter = new ImageAdapter(getApplicationContext(), listCategories);
                        gridView.setAdapter(adapter);
                    }
                });

            }
        }).start();

        stopMusic = true;

        if (!MusicService.SERVICERUNNING) {
            dataApplication.setIntent(new Intent("net.dm73.quizitup.MUSIC_SERVICE").setClass(this, MusicService.class).putExtra("trackId", "Intro"));
            startService(dataApplication.getIntent());
        } else if (MusicPlayer.musicPaused) {
            if (MusicPlayer.trackid == "intro") MusicPlayer.startMusic();
            else
                MusicPlayer.changeTrack(getApplicationContext(), "intro", getSharedPreferences("settings", 0).getInt("volume_level", 50));
        }


        /*
        * Buckup the status of the user
        */
        if (prefs.getBoolean("first_open", true)) {

            if (!BillingProcessor.isIabServiceAvailable(this)) {
                showToast("Le service de facturation dans l'application n'est pas disponible, mettez à niveau Android Play en version >= 3.9.16");
            }

            bp = new BillingProcessor(this, ENCODED_PUBLIC_KEY, SKU_REMOVE_ADS, new BillingProcessor.IBillingHandler() {

                @Override
                public void onProductPurchased(String productId, TransactionDetails details) {
                }

                @Override
                public void onBillingError(int errorCode, Throwable error) {
                }

                @Override
                public void onBillingInitialized() {

                    editor = prefs.edit();
                    if (prefs.getBoolean("adsremoved", false) != bp.isPurchased(SKU_REMOVE_ADS)) {
                        editor.putBoolean("adsremoved", bp.isPurchased(SKU_REMOVE_ADS));
                    }
                    editor.putBoolean("first_open", false);
                    editor.commit();
                }

                @Override
                public void onPurchaseHistoryRestored() {
                }

            });

        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {
        if (MusicService.SERVICERUNNING && stopMusic) {
            MusicPlayer.pauseMusic();
        }

        super.onPause();
    }


}
