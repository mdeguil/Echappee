package fr.app.application.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import fr.app.application.model.Itineraire;
import fr.app.application.model.ReponseItineraires;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.VolleyUtils;

/**
 * Controller qui gère les appels à l'API pour les itinéraires.
 *
 * GET  /api/itiniraires — récupère la liste
 * POST /api/itiniraires — crée un nouvel itinéraire avec ses lieux
 */
public class ItineraireController {

    private static final String ENDPOINT_ITINERAIRES = "/api/itiniraires";

    private final Context contexte;
    private final Gson    gson;

    // ── Interfaces callback ───────────────────────────────────────────────

    public interface CallbackItineraires {
        void onSucces(List<Itineraire> itineraires);
        void onErreur(String messageErreur);
    }

    public interface CallbackCreerItineraire {
        void onSucces(Itineraire itineraire);
        void onErreur(String messageErreur);
    }

    // ── Constructeur ─────────────────────────────────────────────────────

    public ItineraireController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    // ── GET : récupère tous les itinéraires ───────────────────────────────

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
                                callback.onSucces(Arrays.asList(tableau));
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

    // ── POST : crée un itinéraire avec ses lieux ──────────────────────────

    /**
     * Crée un nouvel itinéraire.
     *
     * @param dureTotal durée totale en minutes
     * @param idLieux   liste des IDs des lieux à inclure
     * @param callback  résultat ou erreur
     */
    public void creerItineraire(int dureTotal, List<Integer> idLieux, CallbackCreerItineraire callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_ITINERAIRES);

        try {
            JSONObject body = new JSONObject();
            body.put("dureTotal", dureTotal);

            JSONArray lieuxArray = new JSONArray();
            for (int id : idLieux) {
                lieuxArray.put(id);
            }
            body.put("listeLieux", lieuxArray);

            JsonObjectRequest requete = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    reponse -> {
                        try {
                            Itineraire itineraire = gson.fromJson(reponse.toString(), Itineraire.class);
                            callback.onSucces(itineraire);
                        } catch (Exception e) {
                            callback.onErreur("Erreur de parsing : " + e.getMessage());
                        }
                    },
                    erreur -> callback.onErreur("Erreur réseau : " + erreur.getMessage())
            );

            VolleyUtils.getInstance(contexte).addToRequestQueue(requete);

        } catch (Exception e) {
            callback.onErreur("Erreur construction requête : " + e.getMessage());
        }
    }
}