package net.dm73.quizitup.util;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;


public class MusicService extends Service {

    public static boolean SERVICERUNNING = false;

    public MusicService() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SERVICERUNNING = true;
        SharedPreferences settings = getSharedPreferences("settings", 0);
        MusicPlayer.SoundPlayer(this, "intro", settings.getInt("volume_level", 50));
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        SERVICERUNNING = false;
        MusicPlayer.stopMusic();
        super.onDestroy();
    }

}
