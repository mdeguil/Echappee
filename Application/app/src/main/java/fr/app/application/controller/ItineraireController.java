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
import fr.app.application.model.reponse.ReponseItineraires;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.VolleyUtils;

public class ItineraireController {

    private static final String ENDPOINT_ITINERAIRES = "/api/itiniraires";

    private final Context contexte;
    private final Gson    gson;

    public interface CallbackItineraires {
        void onSucces(List<Itineraire> itineraires);
        void onErreur(String messageErreur);
    }

    public interface CallbackCreerItineraire {
        void onSucces(Itineraire itineraire);
        void onErreur(String messageErreur);
    }

    public interface CallbackSupprimer {
        void onSucces();
        void onErreur(String messageErreur);
    }

    public ItineraireController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    /**
     * Récupère la liste complète des itineraires.
     * Endpoint : GET /api/itiniraires
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

    /**
     * Crée un itinéraire avec une durée estimée et une liste d'identifiants de lieux.
     * Endpoint : POST /api/itiniraires
     */
    public void creerItineraire(int dureTotal, List<Integer> idLieux,
                                CallbackCreerItineraire callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_ITINERAIRES);

        try {
            JSONObject body = new JSONObject();
            body.put("dureTotal", dureTotal);

            JSONArray lieuxArray = new JSONArray();
            for (int id : idLieux) lieuxArray.put(id);
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


    /**
     * Supprime un itinéraire par son ID.
     * Endpoint : DELETE /api/itiniraires/{id}
     */
    public void supprimerItineraire(int id, CallbackSupprimer callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_ITINERAIRES + "/" + id);

        StringRequest requete = new StringRequest(
                Request.Method.DELETE,
                url,
                reponse -> callback.onSucces(),
                erreur -> {
                    // HTTP 204 No Content est considéré comme une erreur par Volley
                    // mais c'est en réalité un succès pour un DELETE
                    if (erreur.networkResponse != null
                            && erreur.networkResponse.statusCode == 204) {
                        callback.onSucces();
                    } else {
                        callback.onErreur("Erreur réseau : " + erreur.getMessage());
                    }
                }
        );

        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }
}