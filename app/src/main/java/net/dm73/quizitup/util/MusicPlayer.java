package net.dm73.quizitup.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import net.dm73.quizitup.R;

import java.util.HashMap;

public class MusicPlayer {
    public static MediaPlayer player;
    public static boolean musicPaused = false;
    public static String trackid;
    private static HashMap<String, Integer> trackIdList = initializeHashMap();

    public static void SoundPlayer(Context ctx, String raw_id, int vol){
        trackid = raw_id;
        player = MediaPlayer.create(ctx, trackIdList.get(raw_id));
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setLooping(true);
        setVolume(vol);
        //player.release();
        player.start();
    }

    public static void changeTrack(Context ctx, String raw_id, final int vol) {
        player.stop();
        player.release();
        trackid = raw_id;
        player = MediaPlayer.create(ctx, trackIdList.get(raw_id));
        player.setLooping(true);
        setVolume(vol);
        //player.release();
        player.start();
    }


    public static void setVolume(int level) {
        float log1 = (float) (Math.log(100 - level) / Math.log(100));
        player.setVolume(1 - log1, 1 - log1);
    }

    public static void startMusic() {
        musicPaused = false;
        player.start();
    }

    public static void pauseMusic() {
        musicPaused = true;
        player.pause();
    }

    public static void stopMusic() {
        player.stop();
    }

    private static HashMap<String, Integer> initializeHashMap() {

        HashMap<String, Integer> trackList = new HashMap<>();

        trackList.put("intro", R.raw.acousticbreeze);
        trackList.put("love", R.raw.thejazzpiano);
        trackList.put("culture", R.raw.india);
        trackList.put("pro", R.raw.sunny);
        trackList.put("psycho", R.raw.sadday);
        trackList.put("santé", R.raw.tenderness);

        return trackList;
    }

}



