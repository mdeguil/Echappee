package fr.app.application.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

import fr.app.application.model.Visite;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.SessionManager;
import fr.app.application.utils.VolleyUtils;

public class VisiteController {

    private static final String ENDPOINT_VISITES      = "/api/visites";
    private static final String ENDPOINT_COMMENTAIRES = "/api/commentaires";

    private final Context contexte;
    private final Gson    gson;

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

    public VisiteController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    /**
     * Recupère tout les visite de la BDD
     */
    public void recupererVisites(CallbackVisites callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_VISITES);

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    android.util.Log.d("VISITE_JSON", reponse);
                    try {
                        Visite[] tableau = gson.fromJson(reponse, Visite[].class);
                        if (tableau != null) {
                            callback.onSucces(Arrays.asList(tableau));
                        } else {
                            callback.onErreur("Réponse vide");
                        }
                    } catch (Exception e) {
                        try {
                            org.json.JSONObject json = new org.json.JSONObject(reponse);
                            if (json.has("hydra:member")) {
                                org.json.JSONArray members = json.getJSONArray("hydra:member");
                                Visite[] tableau = gson.fromJson(members.toString(), Visite[].class);
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


    public void creerVisite(String date,
                            int note,
                            String message,
                            int lieuId,
                            CallbackCreerVisite callback) {
        creationDuCommentaire(date, note, message, lieuId, callback);
    }

    private void creationDuCommentaire(String date,
                                        int note,
                                        String message,
                                        int lieuId,
                                        CallbackCreerVisite callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_COMMENTAIRES);

        SessionManager sessionManager = new SessionManager(contexte);
        int userId = sessionManager.getUserId();

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
                                commentaireId = Integer.parseInt(
                                        iri.substring(iri.lastIndexOf("/") + 1)
                                );
                            } else {
                                callback.onErreur("Impossible de récupérer l'id du commentaire");
                                return;
                            }

                            creationDeLaVisite(date, commentaireId, callback);

                        } catch (Exception e) {
                            callback.onErreur("Réponse commentaire invalide : " + e.getMessage());
                        }
                    },
                    erreur -> {
                        String detail = "Erreur";
                        if (erreur.networkResponse != null) {
                            String bodyError = new String(erreur.networkResponse.data);
                            android.util.Log.e("VISITE_ERROR", "Code " + erreur.networkResponse.statusCode + " : " + body);
                            detail += " : " + bodyError;
                        } else {
                            android.util.Log.e("VISITE_ERROR", "Pas de réponse réseau : " + erreur.getMessage());
                        }
                        callback.onErreur(detail);
                    }
            ){
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept",       "application/json");
                    return headers;
                }
            };

            VolleyUtils.getInstance(contexte).addToRequestQueue(requete);

        } catch (Exception e) {
            callback.onErreur("Erreur construction requête commentaire : " + e.getMessage());
        }
    }

    private void creationDeLaVisite(String date,
                                   int commentaireId,
                                   CallbackCreerVisite callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_VISITES);

        try {
            JSONObject body = new JSONObject();
            body.put("date",         date);
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
                                visite.setId(Integer.parseInt(
                                        iri.substring(iri.lastIndexOf("/") + 1)
                                ));
                            }

                            callback.onSucces(visite);

                        } catch (Exception e) {
                            // Visite bien créée en BDD, on considère comme un succès
                            callback.onSucces(new Visite());
                        }
                    },
                    erreur -> callback.onErreur("Erreur création visite : " + erreur.getMessage())
            ){
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Accept",       "application/json");
                    return headers;
                }
            };

            VolleyUtils.getInstance(contexte).addToRequestQueue(requete);

        } catch (Exception e) {
            callback.onErreur("Erreur construction requête visite : " + e.getMessage());
        }
    }

    public void supprimerVisite(int id, CallbackSupprimer callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_VISITES + "/" + id);

        StringRequest requete = new StringRequest(
                Request.Method.DELETE,
                url,
                reponse -> callback.onSucces(),
                erreur -> {
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