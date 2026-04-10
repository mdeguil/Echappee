package fr.app.application.controller;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import fr.app.application.model.DetailLieux;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.VolleyUtils;

/**
 * Controller qui récupère le détail d'un lieu depuis l'API.
 *
 * Endpoint : GET /api/detail_lieus/{id}
 *
 * L'URL de base est lue depuis ApiConfig (singleton) — plus besoin
 * de toucher ce fichier pour changer l'adresse du serveur.
 */
public class DetailLieuController {

    private static final String TAG = "DetailLieuController";
    private static final String ENDPOINT_DETAIL = "/api/detail_lieus/";

    private final Context contexte;
    private final Gson    gson;

    public interface CallbackDetail {
        void onSucces(DetailLieux detail);
        void onErreur(String messageErreur);
    }

    public DetailLieuController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    /**
     * Récupère les détails d'un lieu par son identifiant.
     *
     * @param id       identifiant du lieu (provient de Lieu.getId())
     * @param callback résultat ou erreur
     */
    public void recupererDetail(int id, CallbackDetail callback) {
        // L'URL de base est toujours lue depuis le singleton
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_DETAIL) + id;

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        DetailLieux detail = gson.fromJson(reponse, DetailLieux.class);
                        if (detail != null && detail.getId() != 0) {
                            callback.onSucces(detail);
                        } else {
                            callback.onErreur("Détail introuvable pour l'id " + id);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur de parsing", e);
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> callback.onErreur("Erreur réseau : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(requete);
    }
}
