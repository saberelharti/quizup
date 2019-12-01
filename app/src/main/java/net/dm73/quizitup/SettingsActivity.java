package net.dm73.quizitup;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

import net.dm73.quizitup.util.MusicPlayer;

import static net.dm73.quizitup.TestActivity.setAdapterNoAds;
import static net.dm73.quizitup.util.Constants.ENCODED_PUBLIC_KEY;

public class SettingsActivity extends AppCompatActivity {

    private BillingProcessor bp;
    private String SKU_REMOVE_ADS = "remove_ads";
    private String activityFrom;

    private Button removeAds;
    private SeekBar soundBar;
    private ImageButton close;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setStatusBarTranslucent(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        //getting the ui elements
        close = (ImageButton) findViewById(R.id.close);
        removeAds = (Button) findViewById(R.id.removeAds);
        soundBar = (SeekBar) findViewById(R.id.soundBar);

        Intent intent = getIntent();
        activityFrom = intent.getStringExtra("activity");


        //changing the font of the removeAds button
        Typeface adventProEx = Typeface.createFromAsset(getAssets(), "fonts/AdventPro-ExtraLight.ttf");
        removeAds.setTypeface(adventProEx);

        //check the user status
        prefs = getSharedPreferences("settings", 0);
        if (prefs.getBoolean("adsremoved", false)) {
            disableRemoveAdsButton();
        }

        //changing the sound bar according to the settings
        soundBar.setProgress(getSharedPreferences("settings", 0).getInt("volume_level", 50));


        soundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                MusicPlayer.setVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                SharedPreferences prefs = getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("volume_level", seekBar.getProgress());
                editor.commit();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        /*
        * In App billing
        */
        if (!BillingProcessor.isIabServiceAvailable(this)) {
            showToast("Le service de facturation dans l'application n'est pas disponible, mettez à niveau Android Play en version >= 3.9.16");
        }

        bp = new BillingProcessor(this, ENCODED_PUBLIC_KEY, SKU_REMOVE_ADS, new BillingProcessor.IBillingHandler() {

            @Override
            public void onProductPurchased(String productId, TransactionDetails details) {

                //memorize the purchase
                editor = prefs.edit();
                editor.putBoolean("adsremoved", true);
                editor.commit();

                //remove the ads from the last opened activity
                if (removeAds != null) {
                    switch (activityFrom) {
                        case "START_TEST_ACTIVITY":
                            //remove the ads from startTestActivity
                            StartTestActivity.removeAds();
                            disableRemoveAdsButton();
                            break;
                        case "QUIZ_ACTIVITY":
                            //remove the ads from QuizActivity
                            QuizActivity.removeAds();
                            disableRemoveAdsButton();
                            break;
                        case "TEST_ACTIVITY":
                            //disable the button
                            setAdapterNoAds();
                            disableRemoveAdsButton();
                            break;
                        default:
                            break;
                    }
                }

                showToast("Le paiement a bien été effectué.");
            }

            @Override
            public void onBillingError(int errorCode, Throwable error) {
            }

            @Override
            public void onBillingInitialized() {

                if (prefs.getBoolean("adsremoved", false) != bp.isPurchased(SKU_REMOVE_ADS)) {
                    editor = prefs.edit();
                    editor.putBoolean("adsremoved", bp.isPurchased(SKU_REMOVE_ADS));
                    editor.commit();
                }
            }

            @Override
            public void onPurchaseHistoryRestored() {
            }

        });


        removeAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bp.isPurchased(SKU_REMOVE_ADS))
                    bp.purchase(SettingsActivity.this, SKU_REMOVE_ADS);
                else
                    disableRemoveAdsButton();

            }
        });

        //Button for close the activity
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void disableRemoveAdsButton() {
        if (prefs != null && prefs.getBoolean("adsremoved", false)) {
            removeAds.setTextColor(ContextCompat.getColor(this, R.color.textDisable));
            removeAds.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_noadsdisable), null, null, null);
            removeAds.setClickable(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setStatusBarTranslucent(boolean makeTranslucent) {
        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
