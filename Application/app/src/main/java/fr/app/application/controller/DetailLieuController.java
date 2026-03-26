package fr.app.application.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import fr.app.application.model.DetailLieux;
import fr.app.application.model.ReponseDetailLieux;
import fr.app.application.utils.VolleyUtils;

/**
 * Controller qui récupère le détail d'un lieu depuis l'API.
 *
 * Endpoint attendu : GET /api/lieux/{id}
 * Réponse attendue : { "data": { "id": 1, "description": "...", "horaires": "...",
 *                                "tarif": 12, "accessibilite": "...", "photos": "url" } }
 */
public class DetailLieuController {

    // Même base URL que LieuController
    private static final String URL_BASE  = "http://172.20.10.2:8000";
    private static final String URL_LIEUX = URL_BASE + "/api/lieux/";

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
        String url = URL_LIEUX + id;

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        ReponseDetailLieux reponse2 = gson.fromJson(reponse, ReponseDetailLieux.class);
                        if (reponse2 != null && reponse2.getData() != null) {
                            callback.onSucces(reponse2.getData());
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