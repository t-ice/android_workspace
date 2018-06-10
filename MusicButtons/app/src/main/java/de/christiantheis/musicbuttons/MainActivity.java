package de.christiantheis.musicbuttons;

import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    public static final String SERVICECMD = "com.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    /**
     * Called when the user taps the Send button
     */
    public void onNextTrackBottonClick(View view) {


        // next
        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDNEXT);
        sendBroadcast(i);


    }

    /**
     * Called when the user taps the Send button
     */
    public void onPreviousTrackBottonClick(View view) {

        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDPREVIOUS);
        sendBroadcast(i);
    }

    /**
     * Called when the user taps the Send button
     */
    public void onPlayPauseBottonClick(View view) {

        Intent i = new Intent(SERVICECMD);
        i.putExtra(CMDNAME, CMDTOGGLEPAUSE);
        sendBroadcast(i);
    }
}
