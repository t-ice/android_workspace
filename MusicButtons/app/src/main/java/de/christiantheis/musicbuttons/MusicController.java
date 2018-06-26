package de.christiantheis.musicbuttons;

import android.content.Intent;
import android.content.Context;

public class MusicController {

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";

    private Context androidContext;

    private MusicController (){
    }

    public MusicController (Context context){
        this.androidContext = context;
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


    public void next() {
        // next
        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDNEXT);
        androidContext.sendBroadcast(i);
    }















}
