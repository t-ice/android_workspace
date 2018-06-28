package de.christiantheis.musicbuttons;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

/**
 * Sorgt dafuer, dass beim Erkennen eines IBeacon vom HM 10 Modul des BlueCan-Geraets die MainActivity gestartet wird.
 */
public class BlueCanApp extends Application implements BootstrapNotifier {
    public static final String I_BEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static final String I_BEACON_IDENTIFIER = "74278BDA-B644-4520-8F0C-720EAF059935";
    private BeaconManager beaconManager;
    private RegionBootstrap regionBootstrap;

    @Override
    public void onCreate() {
        super.onCreate();
        log("App started up");
        initializeRegionBootstrap();
    }

    @Override
    public void didEnterRegion(Region region) {
        log("Got a didEnterRegion call");

        // This call to disable will make it so the activity below only gets launched the first time a beacon is seen (until the next time the app is launched)
        // if you want the Activity to launch every single time beacons come into view, remove this call.
        regionBootstrap.disable();


        Intent intent = new Intent(this, MainActivity.class);
        // IMPORTANT: in the AndroidManifest.xml definition of this activity, you must set android:launchMode="singleInstance" or you will get two instances
        // created when a user launches the activity manually and it gets launched from here.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
    }

    @Override
    public void didExitRegion(Region region) {
        log("Got a didExitRegion call");
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    private void initializeRegionBootstrap() {
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(I_BEACON_LAYOUT));
        Region region = new Region(this.getClass().getCanonicalName() + ".boostrapRegion", Identifier.parse(I_BEACON_IDENTIFIER), null, null);
        regionBootstrap = new RegionBootstrap(this, region);
    }

    public void log(String text) {
        if (text != null && !text.isEmpty()) {
            Log.d(Constants.LOG_TAG, text);
        }

    }
}
