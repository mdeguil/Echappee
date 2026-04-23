package fr.app.application.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import fr.app.application.model.Itiniraire;
import fr.app.application.model.reponse.ReponseItineraires;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.BDD.AppDatabase;
import fr.app.application.utils.SessionManager;
import fr.app.application.utils.VolleyUtils;

public class ItineraireController {

    private static final String ENDPOINT_ITINERAIRES = "/api/itiniraires";

    private final Context        contexte;
    private final Gson           gson;
    private final AppDatabase    db;
    private final SessionManager session;

    public interface CallbackItineraires {
        void onSucces(List<Itiniraire> itineraires);
        void onErreur(String messageErreur);
    }

    public interface CallbackCreerItineraire {
        void onSucces(Itiniraire itineraire);
        void onErreur(String messageErreur);
    }

    public interface CallbackSupprimer {
        void onSucces();
        void onErreur(String messageErreur);
    }

    public ItineraireController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
        this.db       = AppDatabase.getDatabase(contexte);
        this.session  = new SessionManager(contexte);
    }

    /**
     * Récupère les itinéraires depuis l'API et les sauvegarde en BDD.
     * En cas d'erreur réseau, retourne les itinéraires locaux de l'utilisateur courant.
     */
    public void recupererItineraires(CallbackItineraires callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_ITINERAIRES);

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        List<Itiniraire> liste = null;
                        ReponseItineraires rep = gson.fromJson(reponse, ReponseItineraires.class);
                        if (rep != null && rep.getData() != null) {
                            liste = rep.getData();
                        } else {
                            Itiniraire[] tableau = gson.fromJson(reponse, Itiniraire[].class);
                            if (tableau != null) liste = Arrays.asList(tableau);
                        }

                        if (liste != null) {
                            final List<Itiniraire> finalListe = liste;
                            int userId = session.getUserId();
                            // Associer l'userId avant la sauvegarde
                            for (Itiniraire it : finalListe) it.setUserId(userId);

                            new Thread(() -> db.myDao().insertItineraires(finalListe)).start();
                            callback.onSucces(finalListe);
                        } else {
                            callback.onErreur("Réponse vide ou invalide");
                        }
                    } catch (Exception e) {
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> {
                    // Fallback : itinéraires locaux de l'utilisateur connecté
                    int userId = session.getUserId();
                    new Thread(() -> {
                        List<Itiniraire> locaux = db.myDao().getItinerairesByUser(userId);
                        Handler h = new Handler(Looper.getMainLooper());
                        if (locaux != null && !locaux.isEmpty()) {
                            h.post(() -> callback.onSucces(locaux));
                        } else {
                            h.post(() -> callback.onErreur(
                                    "Erreur réseau et aucun itinéraire local disponible"));
                        }
                    }).start();
                }
        );

        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }

    /**
     * Crée un itinéraire via l'API et le sauvegarde localement.
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
                    Request.Method.POST, url, body,
                    reponse -> {
                        try {
                            Itiniraire itineraire = gson.fromJson(reponse.toString(), Itiniraire.class);
                            // Sauvegarder localement
                            int userId = session.getUserId();
                            itineraire.setUserId(userId);
                            new Thread(() -> db.myDao().insertItineraire(itineraire)).start();
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
     * Supprime un itinéraire via l'API et en local.
     */
    public void supprimerItineraire(int id, CallbackSupprimer callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_ITINERAIRES + "/" + id);

        StringRequest requete = new StringRequest(
                Request.Method.DELETE, url,
                reponse -> {
                    new Thread(() -> db.myDao().deleteItineraire(id)).start();
                    callback.onSucces();
                },
                erreur -> {
                    if (erreur.networkResponse != null
                            && erreur.networkResponse.statusCode == 204) {
                        new Thread(() -> db.myDao().deleteItineraire(id)).start();
                        callback.onSucces();
                    } else {
                        callback.onErreur("Erreur réseau : " + erreur.getMessage());
                    }
                }
        );

        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }
}
