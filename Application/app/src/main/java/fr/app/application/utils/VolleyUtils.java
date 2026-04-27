package fr.app.application.utils;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Classe utilitaire pour la gestion de la file d'attente des requêtes Volley.
 */
public class VolleyUtils {
    private static VolleyUtils instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private VolleyUtils(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized VolleyUtils getInstance(Context context) {
        if (instance == null) {
            instance = new VolleyUtils(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    /**
     * Retourne l'en-tête Authorization Bearer depuis la session active.
     */
    public static java.util.Map<String, String> getAuthHeaders(Context context) {
        SessionManager session = new SessionManager(context);
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        String token = session.getToken();
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }
}
