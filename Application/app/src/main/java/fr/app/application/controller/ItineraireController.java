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

    /**
     * Interface de rappel pour la récupération d'une liste d'itinéraires.
     */
    public interface CallbackItineraires {
        void onSucces(List<Itineraire> itineraires);
        void onErreur(String messageErreur);
    }

    /**
     * Interface de rappel pour la création d'un nouvel itinéraire.
     */
    public interface CallbackCreerItineraire {
        void onSucces(Itineraire itineraire);
        void onErreur(String messageErreur);
    }

    public ItineraireController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    /**
     * Récupère la liste de tous les itinéraires disponibles.
     *
     * @param callback L'interface pour traiter la liste reçue ou l'erreur.
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
     * Envoie une requête POST pour créer un nouvel itinéraire.
     *
     * @param dureTotal La durée estimée de l'itinéraire en minutes.
     * @param idLieux   La liste des identifiants (IDs) des lieux à associer.
     * @param callback  L'interface pour traiter l'itinéraire créé ou l'erreur.
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