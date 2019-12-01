package net.dm73.quizitup.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class RateQuizApp {

    public final static String APP_PNAME = "net.dm73.quizitup";//Package Name

    private static int DAYS_UNTIL_PROMPT = 3;//Min number of days
    private static int LAUNCHES_UNTIL_PROMPT = 3;//Min number of launches


    public static void app_launched(Context mContext, FragmentManager fragmentManager) {


        //creatting new ones
        SharedPreferences prefs = mContext.getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = prefs.edit();

        if (prefs.getBoolean("dontshowagain", false)) {

            if (System.currentTimeMillis() >= prefs.getLong("lastlaunch", System.currentTimeMillis()) + (prefs.getInt("days_until_prompt", DAYS_UNTIL_PROMPT) * 24 * 60 * 60 * 1000)) {
                editor.putBoolean("dontshowagain", false);
                editor.commit();
            } else {
                return;
            }

        }

        // Increment launch counter
        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);


        // Get date of first launch
        if (prefs.getLong("lastlaunch", 0) == 0) {
            editor.putInt("days_until_prompt", DAYS_UNTIL_PROMPT);
            editor.putLong("lastlaunch", System.currentTimeMillis());
        }

        editor.commit();

        // Wait at least 3 days before opening & 3 launtches
        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {

            if (System.currentTimeMillis() > (prefs.getLong("lastlaunch", System.currentTimeMillis()) + (prefs.getInt("days_until_prompt", DAYS_UNTIL_PROMPT) * 24 * 60 * 60 * 1000))) {
                DialogFragment dialog = new DialogRate();
                dialog.show(fragmentManager, "NoticeDialogFragment");
            }
        }
    }


    public static void ModifyPreferencesParametersLater(final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = context.getSharedPreferences("settings", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("days_until_prompt", 2);
                editor.putLong("lastlaunch", System.currentTimeMillis());
                editor.putBoolean("dontshowagain", true);
                editor.commit();
            }
        }).start();

    }

    public static void ModifyPreferencesParametersOtherLunch(Context context) {

        SharedPreferences prefs = context.getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("days_until_prompt", 20);
        editor.putLong("lastlaunch", System.currentTimeMillis());
        editor.putBoolean("dontshowagain", true);
        editor.commit();
    }

}
