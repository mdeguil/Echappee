package fr.app.application.utils;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Classe utilitaire pour la gestion de la file d'attente des requêtes Volley.
 * * Centralise l'utilisation de la RequestQueue pour garantir que l'instance de Volley
 * persiste pendant toute la durée de vie de l'application, évitant ainsi de recréer
 * la file d'attente à chaque changement d'activité.
 */
public class VolleyUtils {
    private static VolleyUtils instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private VolleyUtils(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    /**
     * Fournit l'instance unique de VolleyUtils.
     *
     * @param context Le contexte de l'application ou de l'activité.
     * @return L'instance Singleton de VolleyUtils.
     */
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
}