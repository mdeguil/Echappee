package fr.app.application.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singleton qui centralise la configuration de l'API.
 *
 * Utilisation :
 *   String url = ApiConfig.getInstance(context).getBaseUrl();
 *
 * Modification depuis la page Paramètres :
 *   ApiConfig.getInstance(context).setBaseUrl("http://192.168.1.50:8000");
 */
public class ApiConfig {

    private static final String PREFS_NAME  = "api_config";
    private static final String KEY_URL     = "base_url";

    // URL par défaut — changez-la ici une seule fois si besoin
    private static final String URL_PAR_DEFAUT = "http://192.168.0.70:8000";

    // ── Singleton ────────────────────────────────────────────────────────────

    private static ApiConfig instance;
    private final SharedPreferences prefs;

    private ApiConfig(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ApiConfig getInstance(Context context) {
        if (instance == null) {
            instance = new ApiConfig(context);
        }
        return instance;
    }

    // ── Lecture / Écriture ───────────────────────────────────────────────────

    /** Retourne l'URL de base courante (ex: "http://192.168.0.70:8000"). */
    public String getBaseUrl() {
        return prefs.getString(KEY_URL, URL_PAR_DEFAUT);
    }

    /**
     * Modifie l'URL de base et la persiste.
     * Appelez cette méthode depuis votre page Paramètres.
     *
     * @param nouvelleUrl ex: "http://192.168.1.50:8000"
     */
    public void setBaseUrl(String nouvelleUrl) {
        prefs.edit().putString(KEY_URL, nouvelleUrl).apply();
    }

    /** Remet l'URL à la valeur par défaut. */
    public void reinitialiserUrl() {
        prefs.edit().putString(KEY_URL, URL_PAR_DEFAUT).apply();
    }

    // ── Helpers pour construire les endpoints ────────────────────────────────

    /** Retourne l'URL complète d'un endpoint (ex: "/api/lieus"). */
    public String getUrl(String endpoint) {
        return getBaseUrl() + endpoint;
    }
}
