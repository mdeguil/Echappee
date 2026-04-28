package fr.app.application;

import android.app.Application;

import fr.app.application.utils.NetworkMonitor;
import fr.app.application.utils.SyncManager;


public class MonApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        NetworkMonitor.getInstance(this).start();
        SyncManager.getInstance(this).start();
    }
}