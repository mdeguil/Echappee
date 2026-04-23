package fr.app.application.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestionnaire de session centralisé.
 * Stocke le JWT et l'ID utilisateur pour permettre l'accès offline.
 */
public class SessionManager {

    private static final String PREFS_NAME  = "auth";
    private static final String KEY_TOKEN   = "jwt_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL   = "user_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, int userId, String email) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, 0); // 0 = utilisateur par défaut si ID inconnu
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * L'utilisateur est considéré connecté dès qu'un token JWT est présent.
     * On ne bloque plus sur l'userId car certaines APIs ne l'incluent pas dans le JWT.
     */
    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}