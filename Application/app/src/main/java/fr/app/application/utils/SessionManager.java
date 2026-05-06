package fr.app.application.utils;

import android.content.Context;
import android.content.SharedPreferences;

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
        return prefs.getInt(KEY_USER_ID, 0);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * Retourne un userId stable et unique dérivé de l'email.
     * Utilisé pour isoler les itinéraires par utilisateur en mode hors-ligne.
     */
    public int getUserIdFromEmail() {
        String email = getEmail();
        if (email == null || email.isEmpty()) return 0;
        return Math.abs(email.hashCode());
    }

    /**
     * L'utilisateur est considéré connecté dès qu'un token JWT est présent.
     */
    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}