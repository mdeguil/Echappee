package fr.app.application.controller;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import fr.app.application.model.DetailLieux;
import fr.app.application.model.ReponseDetailLieux;
import fr.app.application.utils.VolleyUtils;

/**
 * Controller qui récupère le détail d'un lieu depuis l'API.
 *
 * Endpoint attendu : GET /api/detail_lieus/{id}
 * Réponse attendue : { "data": { "id": 1, "description": "...", "horaires": "...",
 *                                "tarif": 12, "accessibilite": "...", "photos": "url" } }
 */
public class DetailLieuController {

    // Même base URL que LieuController
    private static final String URL_BASE  = "http://10.241.249.102:8000";
    private static final String URL_DETAILLIEUX = URL_BASE + "/api/detail_lieus/";

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
        String url = URL_DETAILLIEUX + id;

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
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> callback.onErreur("Erreur réseau : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).getFileRequetes().add(requete);
    }

}