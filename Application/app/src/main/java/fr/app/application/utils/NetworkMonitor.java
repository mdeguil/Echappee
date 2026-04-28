package fr.app.application.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

/**
 * Surveille les changements de connectivité réseau et notifie
 * quand la connexion est rétablie, afin de déclencher une resynchronisation.
 *
 * Usage type dans une Activity :
 * <pre>
 *   private NetworkMonitor networkMonitor;
 *
 *   {@literal @}Override protected void onResume() {
 *       super.onResume();
 *       networkMonitor = new NetworkMonitor(this, this::resynchroniser);
 *       networkMonitor.start();
 *   }
 *
 *   {@literal @}Override protected void onPause() {
 *       super.onPause();
 *       networkMonitor.stop();
 *   }
 * </pre>
 */
public class NetworkMonitor {

    /** Appelé sur le thread principal dès que la connexion est rétablie. */
    public interface OnConnexionRetablie {
        void onConnexionRetablie();
    }

    private final ConnectivityManager    connectivityManager;
    private final OnConnexionRetablie    listener;
    private final Handler                mainHandler = new Handler(Looper.getMainLooper());

    private ConnectivityManager.NetworkCallback networkCallback;
    private boolean etaitHorsLigne = false;

    public NetworkMonitor(@NonNull Context context,
                          @NonNull OnConnexionRetablie listener) {
        this.connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listener = listener;
    }

    /**
     * Démarre l'écoute des changements réseau.
     * À appeler dans onResume() ou onStart().
     */
    public void start() {
        if (connectivityManager == null) return;

        // Initialise l'état courant avant d'écouter
        etaitHorsLigne = !estConnecte();

        NetworkRequest requete = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                // Appelé quand un réseau valide devient disponible
                if (etaitHorsLigne) {
                    etaitHorsLigne = false;
                    mainHandler.post(listener::onConnexionRetablie);
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                // Connexion perdue : on note qu'on est hors ligne
                if (!estConnecte()) {
                    etaitHorsLigne = true;
                }
            }
        };

        connectivityManager.registerNetworkCallback(requete, networkCallback);
    }

    /**
     * Arrête l'écoute des changements réseau.
     * À appeler dans onPause() ou onStop().
     */
    public void stop() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (IllegalArgumentException ignored) {
                // Callback déjà désenregistré
            }
            networkCallback = null;
        }
    }

    /** Vérifie si une connexion Internet est actuellement disponible. */
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