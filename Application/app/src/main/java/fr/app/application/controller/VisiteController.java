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
import fr.app.application.utils.VolleyUtils;

public class VisiteController {

    private static final String ENDPOINT_VISITES      = "/api/visites";
    private static final String ENDPOINT_COMMENTAIRES = "/api/commentaires";

    private final Context contexte;
    private final Gson    gson;

    // ── Callbacks ─────────────────────────────────────────────────────────

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

    // ── Constructeur ──────────────────────────────────────────────────────

    public VisiteController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    // ── GET /api/visites ──────────────────────────────────────────────────

    public void recupererVisites(CallbackVisites callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_VISITES);

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        Visite[] tableau = gson.fromJson(reponse, Visite[].class);
                        if (tableau != null) {
                            callback.onSucces(Arrays.asList(tableau));
                        } else {
                            callback.onErreur("Réponse vide ou invalide");
                        }
                    } catch (Exception e) {
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> callback.onErreur("Erreur réseau : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }

    // ── Création en 2 étapes ──────────────────────────────────────────────

    public void creerVisite(String date,
                            int note,
                            String message,
                            int lieuId,
                            CallbackCreerVisite callback) {
        etape1CreerCommentaire(date, note, message, lieuId, callback);
    }

    private void etape1CreerCommentaire(String date,
                                        int note,
                                        String message,
                                        int lieuId,
                                        CallbackCreerVisite callback) {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_COMMENTAIRES);

        try {
            JSONObject body = new JSONObject();
            body.put("note",    note);
            body.put("message", message);
            body.put("lieu",    "/api/lieus/" + lieuId);

            JsonObjectRequest requete = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    reponse -> {
                        try {
                            int commentaireId;

                            if (reponse.has("id")) {
                                // JSON simple
                                commentaireId = reponse.getInt("id");
                            } else if (reponse.has("@id")) {
                                // JSON-LD : "/api/commentaires/12"
                                String iri = reponse.getString("@id");
                                commentaireId = Integer.parseInt(
                                        iri.substring(iri.lastIndexOf("/") + 1)
                                );
                            } else {
                                callback.onErreur("Impossible de récupérer l'id du commentaire");
                                return;
                            }

                            etape2CreerVisite(date, commentaireId, callback);

                        } catch (Exception e) {
                            callback.onErreur("Réponse commentaire invalide : " + e.getMessage());
                        }
                    },
                    erreur -> callback.onErreur("Erreur création commentaire : " + erreur.getMessage())
            );

            VolleyUtils.getInstance(contexte).addToRequestQueue(requete);

        } catch (Exception e) {
            callback.onErreur("Erreur construction requête commentaire : " + e.getMessage());
        }
    }

    private void etape2CreerVisite(String date,
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
            );

            VolleyUtils.getInstance(contexte).addToRequestQueue(requete);

        } catch (Exception e) {
            callback.onErreur("Erreur construction requête visite : " + e.getMessage());
        }
    }

    // ── DELETE /api/visites/{id} ──────────────────────────────────────────

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