package fr.app.application.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestionnaire de configuration centralisé pour les appels API.
 * * Cette classe utilise le pattern Singleton pour fournir un accès unique
 * aux paramètres de connexion (URL de base) stockés dans les SharedPreferences.
 */
public class ApiConfig {

    private static final String PREFS_NAME  = "api_config";
    private static final String KEY_URL     = "base_url";

    private static final String URL_PAR_DEFAUT = "http://192.168.0.70:8000";

    private static ApiConfig instance;
    private final SharedPreferences prefs;

    private ApiConfig(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Récupère l'instance unique de la configuration.
     *
     * @param context Le contexte Android pour accéder aux préférences.
     * @return L'instance Singleton de ApiConfig.
     */
    public static synchronized ApiConfig getInstance(Context context) {
        if (instance == null) {
            instance = new ApiConfig(context);
        }
        return instance;
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_URL, URL_PAR_DEFAUT);
    }

    public String getUrl(String endpoint) {
        return getBaseUrl() + endpoint;
    }
}
