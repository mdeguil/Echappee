package fr.app.application.utils;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton qui gère la file de requêtes Volley.
 * À utiliser depuis n'importe quelle classe via VolleyUtils.getInstance(context).
 */
public class VolleyUtils {

    private static VolleyUtils instance;
    private final RequestQueue fileRequetes;

    private VolleyUtils(Context contexte) {
        fileRequetes = Volley.newRequestQueue(contexte.getApplicationContext());
    }

    public static synchronized VolleyUtils getInstance(Context contexte) {
        if (instance == null) {
            instance = new VolleyUtils(contexte);
        }
        return instance;
    }

    public RequestQueue getFileRequetes() {
        return fileRequetes;
    }
}