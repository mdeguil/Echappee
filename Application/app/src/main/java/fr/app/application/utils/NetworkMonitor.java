package fr.app.application.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.CopyOnWriteArrayList;


public class NetworkMonitor {

    // ── Interface Observer ────────────────────────────────────────────────

    public interface Observer {
        void onConnexionRetablie();

        default void onConnexionPerdue() {}
    }
    private static volatile NetworkMonitor instance;

    public static NetworkMonitor getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (NetworkMonitor.class) {
                if (instance == null) {
                    instance = new NetworkMonitor(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final ConnectivityManager connectivityManager;
    private final Handler             mainHandler  = new Handler(Looper.getMainLooper());

    private final CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<>();

    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean estHorsLigne = false;

    private NetworkMonitor(@NonNull Context appContext) {
        this.connectivityManager =
                (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public void start() {
        if (connectivityManager == null || networkCallback != null) return;

        estHorsLigne = !estConnecte();

        NetworkRequest requete = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                if (estHorsLigne) {
                    estHorsLigne = false;
                    mainHandler.post(() -> {
                        for (Observer o : observers) o.onConnexionRetablie();
                    });
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                if (!estConnecte()) {
                    estHorsLigne = true;
                    mainHandler.post(() -> {
                        for (Observer o : observers) o.onConnexionPerdue();
                    });
                }
            }
        };

        connectivityManager.registerNetworkCallback(requete, networkCallback);
    }

    public void ajouterObserver(@NonNull Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    public void retirerObserver(@NonNull Observer observer) {
        observers.remove(observer);
    }

    public boolean estConnecte() {
        if (connectivityManager == null) return false;
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) return false;
        NetworkCapabilities caps = connectivityManager.getNetworkCapabilities(network);
        return caps != null
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}