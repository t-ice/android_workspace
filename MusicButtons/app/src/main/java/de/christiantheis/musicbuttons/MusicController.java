package de.christiantheis.musicbuttons;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class MusicController {

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";
    public static final String COM_GOOGLE_ANDROID_MUSIC = "com.google.android.music";

    private static MusicController instance = null;
    private Context androidContext;

    public static MusicController getInstance(Context context) {
        if (instance == null) {
            instance = new MusicController(context);
        }
        return instance;
    }

    private MusicController() {
    }

    private MusicController(Context context) {
        this.androidContext = context;

        startGoogleMusic();

    }

    private void startGoogleMusic() {
        Intent launchIntent = androidContext.getPackageManager().getLaunchIntentForPackage(COM_GOOGLE_ANDROID_MUSIC);
        androidContext.startActivity(launchIntent);
    }


    public void togglePause() {
        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDTOGGLEPAUSE);
        androidContext.sendBroadcast(i);
    }


    public void previousTrack() {
        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDPREVIOUS);
        androidContext.sendBroadcast(i);
    }


    public void nextTrack() {
        // next
        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDNEXT);
        androidContext.sendBroadcast(i);
    }


    public void log(String text) {
        if (text != null && !text.isEmpty())
            Log.d(Constants.LOG_TAG, text);
    }

}
