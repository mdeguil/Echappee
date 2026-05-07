package fr.app.application.controller;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.app.application.model.Visite;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.SessionManager;
import fr.app.application.utils.VolleyUtils;

public class VisiteController {

    private static final String ENDPOINT_VISITES      = "/api/visites";
    private static final String ENDPOINT_COMMENTAIRES = "/api/commentaires";
    private static final String ENDPOINT_ME           = "/api/me";

    private final Context contexte;
    private final Gson    gson;

    // --- Interfaces de Callback ---

    public interface CallbackVisites {
        void onSucces(List<Visite> visites);
        void onErreur(String messageErreur);
    }

    public interface CallbackCreerVisite {
        void onSucces(Visite visite);
        void onErreur(String messageErreur);
    }

    public interface CallbackSupprimer {
        void onSucces();
        void onErreur(String messageErreur);
    }

    private interface OnIdRecupere {
        void onIdRecu(int userId);
        void onErreur(String message);
    }

    public VisiteController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    /**
     * Récupère toutes les visites de la BDD
     */
    public void recupererVisites(CallbackVisites callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_VISITES);

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    Log.d("VISITE_JSON", reponse);
                    try {
                        Visite[] tableau = gson.fromJson(reponse, Visite[].class);
                        if (tableau != null) {
                            callback.onSucces(Arrays.asList(tableau));
                        } else {
                            callback.onErreur("Réponse vide");
                        }
                    } catch (Exception e) {
                        try {
                            JSONObject json = new JSONObject(reponse);
                            if (json.has("hydra:member")) {
                                String members = json.getJSONArray("hydra:member").toString();
                                Visite[] tableau = gson.fromJson(members, Visite[].class);
                                callback.onSucces(Arrays.asList(tableau));
                            } else {
                                callback.onErreur("Format inattendu : " + e.getMessage());
                            }
                        } catch (Exception e2) {
                            callback.onErreur("Erreur parsing : " + e2.getMessage());
                        }
                    }
                },
                erreur -> callback.onErreur("Erreur réseau : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }

    /**
     * Processus de création :
     * 1. Récupère l'ID utilisateur (/api/me)
     * 2. Crée le commentaire avec cet ID
     * 3. Crée la visite avec l'ID du commentaire
     */
    public void creerVisite(String date, int note, String message, int lieuId, CallbackCreerVisite callback) {
        recupererMonId(new OnIdRecupere() {
            @Override
            public void onIdRecu(int userId) {
                creationDuCommentaire(date, note, message, lieuId, userId, callback);
            }

            @Override
            public void onErreur(String message) {
                callback.onErreur("Impossible d'identifier l'utilisateur : " + message);
            }
        });
    }

    /**
     * Appelle l'endpoint PHP MeController pour obtenir l'ID de l'utilisateur connecté
     */
    private void recupererMonId(final OnIdRecupere callbackId) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_ME);

        JsonObjectRequest requete = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                reponse -> {
                    try {
                        if (reponse.has("id")) {
                            callbackId.onIdRecu(reponse.getInt("id"));
                        } else {
                            callbackId.onErreur("ID absent de la réponse /api/me");
                        }
                    } catch (Exception e) {
                        callbackId.onErreur(e.getMessage());
                    }
                },
                erreur -> callbackId.onErreur(erreur.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");

                // Récupérer le token sauvegardé lors du login
                SessionManager sessionManager = new SessionManager(contexte);
                String token = sessionManager.getToken(); // Vérifie le nom de ta méthode dans SessionManager

                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }

                return headers;
            }
        };
        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }

    private void creationDuCommentaire(String date, int note, String message, int lieuId, int userId, CallbackCreerVisite callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_COMMENTAIRES);

        try {
            JSONObject body = new JSONObject();
            body.put("note",    note);
            body.put("message", message);
            body.put("lieu",    "/api/lieus/" + lieuId);
            body.put("utilisateur", "/api/utilisateurs/" + userId);

            JsonObjectRequest requete = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    reponse -> {
                        try {
                            int commentaireId;
                            if (reponse.has("id")) {
                                commentaireId = reponse.getInt("id");
                            } else if (reponse.has("@id")) {
                                String iri = reponse.getString("@id");
                                commentaireId = Integer.parseInt(iri.substring(iri.lastIndexOf("/") + 1));
                            } else {
                                callback.onErreur("Impossible de récupérer l'ID du commentaire");
                                return;
                            }
                            creationDeLaVisite(date, commentaireId, callback);
                        } catch (Exception e) {
                            callback.onErreur("Erreur réponse commentaire : " + e.getMessage());
                        }
                    },
                    erreur -> callback.onErreur("Erreur création commentaire")
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };
            VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
        } catch (Exception e) {
            callback.onErreur("Erreur construction JSON commentaire : " + e.getMessage());
        }
    }

    private void creationDeLaVisite(String date, int commentaireId, CallbackCreerVisite callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_VISITES);

        try {
            JSONObject body = new JSONObject();
            body.put("date", date);
            body.put("commentaires", "/api/commentaires/" + commentaireId);

            JsonObjectRequest requete = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    reponse -> {
                        try {
                            Visite visite = new Visite();
                            if (reponse.has("id")) {
                                visite.setId(reponse.getInt("id"));
                            } else if (reponse.has("@id")) {
                                String iri = reponse.getString("@id");
                                visite.setId(Integer.parseInt(iri.substring(iri.lastIndexOf("/") + 1)));
                            }
                            callback.onSucces(visite);
                        } catch (Exception e) {
                            callback.onSucces(new Visite());
                        }
                    },
                    erreur -> callback.onErreur("Erreur création visite : " + erreur.getMessage())
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept", "application/json");
                    return headers;
                }
            };
            VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
        } catch (Exception e) {
            callback.onErreur("Erreur construction JSON visite : " + e.getMessage());
        }
    }

    public void supprimerVisite(int id, CallbackSupprimer callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_VISITES + "/" + id);

        StringRequest requete = new StringRequest(
                Request.Method.DELETE,
                url,
                reponse -> callback.onSucces(),
                erreur -> {
                    if (erreur.networkResponse != null && erreur.networkResponse.statusCode == 204) {
                        callback.onSucces();
                    } else {
                        callback.onErreur("Erreur suppression : " + erreur.getMessage());
                    }
                }
        );
        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }
}