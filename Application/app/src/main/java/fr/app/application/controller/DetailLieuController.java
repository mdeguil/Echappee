package fr.app.application.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import fr.app.application.model.DetailLieux;
import fr.app.application.model.ReponseDetailLieux;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.BDD.AppDatabase;
import fr.app.application.utils.VolleyUtils;

/**
 * Controller qui récupère le détail d'un lieu depuis l'API,
 * le sauvegarde en BDD locale, et offre un fallback offline.
 */
public class DetailLieuController {

    private static final String TAG             = "DetailLieuController";
    private static final String ENDPOINT_DETAIL = "/api/detail_lieus/";

    private final Context     contexte;
    private final Gson        gson;
    private final AppDatabase db;

    public interface CallbackDetail {
        void onSucces(DetailLieux detail);
        void onErreur(String messageErreur);
    }

    public DetailLieuController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
        this.db       = AppDatabase.getDatabase(contexte);
    }

    /**
     * Récupère les détails d'un lieu par son identifiant.
     * Sauvegarde le résultat en BDD pour l'accès offline.
     * En cas d'erreur réseau, retourne les données locales si disponibles.
     */
    public void recupererDetail(int id, CallbackDetail callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_DETAIL) + id;

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        DetailLieux detail = gson.fromJson(reponse, DetailLieux.class);
                        if (detail != null && detail.getId() != 0) {
                            // Sauvegarder en BDD pour l'accès offline
                            new Thread(() -> db.myDao().insertDetailLieu(detail)).start();
                            callback.onSucces(detail);
                        } else {
                            callback.onErreur("Détail introuvable pour l'id " + id);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur de parsing", e);
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> {
                    // Fallback : lire depuis la BDD locale
                    new Thread(() -> {
                        DetailLieux local = db.myDao().getDetailLieu(id);
                        Handler h = new Handler(Looper.getMainLooper());
                        if (local != null) {
                            h.post(() -> callback.onSucces(local));
                        } else {
                            h.post(() -> callback.onErreur(
                                    "Erreur réseau et aucun détail local disponible"));
                        }
                    }).start();
                }
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(requete);
    }
}
