package fr.app.application.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.List;

import fr.app.application.model.Itineraire;
import fr.app.application.model.ReponseItineraires;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.VolleyUtils;

/**
 * Controller qui récupère la liste des itinéraires depuis l'API.
 *
 * Endpoint : GET /api/itiniraires
 */
public class ItineraireController {

    private static final String ENDPOINT_ITINERAIRES = "/api/itiniraires";

    private final Context contexte;
    private final Gson    gson;

    public interface CallbackItineraires {
        void onSucces(List<Itineraire> itineraires);
        void onErreur(String messageErreur);
    }

    public ItineraireController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    /**
     * Récupère tous les itinéraires depuis l'API.
     */
    public void recupererItineraires(CallbackItineraires callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_ITINERAIRES);

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        ReponseItineraires reponse2 = gson.fromJson(reponse, ReponseItineraires.class);
                        if (reponse2 != null && reponse2.getData() != null) {
                            callback.onSucces(reponse2.getData());
                        } else {
                            // API Platform retourne parfois directement un tableau JSON
                            // sans wrapper "data" — on tente le parsing direct
                            Itineraire[] tableau = gson.fromJson(reponse, Itineraire[].class);
                            if (tableau != null) {
                                callback.onSucces(java.util.Arrays.asList(tableau));
                            } else {
                                callback.onErreur("Réponse vide ou invalide");
                            }
                        }
                    } catch (Exception e) {
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> callback.onErreur("Erreur réseau : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }
}
