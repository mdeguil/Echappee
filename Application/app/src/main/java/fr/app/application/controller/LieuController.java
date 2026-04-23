package fr.app.application.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.List;

import fr.app.application.model.Lieu;
import fr.app.application.model.reponse.ReponseLieux;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.BDD.AppDatabase;
import fr.app.application.utils.VolleyUtils;

public class LieuController {

    private static final String ENDPOINT_LIEUX = "/api/lieus";

    private final Context     contexte;
    private final Gson        gson;
    private final AppDatabase db;

    public interface CallbackLieux {
        void onSucces(List<Lieu> lieux);
        void onErreur(String messageErreur);
    }

    public LieuController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
        this.db       = AppDatabase.getDatabase(contexte);
    }

    public void recupererLieux(CallbackLieux callback) {
        recupererLieuxAvecFiltres(null, null, callback);
    }

    public void recupererLieuxAvecFiltres(String categorie, String recherche, CallbackLieux callback) {
        String urlBase = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_LIEUX);
        StringBuilder urlBuilder = new StringBuilder(urlBase);
        boolean premier = true;

        if (categorie != null && !categorie.isEmpty()) {
            urlBuilder.append("?categorie.nom=").append(categorie);
            premier = false;
        }
        if (recherche != null && !recherche.isEmpty()) {
            urlBuilder.append(premier ? "?" : "&").append("nom=").append(recherche);
        }

        String url = urlBuilder.toString();

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        ReponseLieux reponseLieux = gson.fromJson(reponse, ReponseLieux.class);
                        if (reponseLieux != null && reponseLieux.getData() != null) {
                            List<Lieu> lieux = reponseLieux.getData();

                            // Sauvegarder en BDD pour l'accès offline
                            new Thread(() -> db.myDao().insertLieux(lieux)).start();

                            callback.onSucces(lieux);
                        } else {
                            callback.onErreur("Réponse vide ou invalide");
                        }
                    } catch (Exception e) {
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> {
                    // Pas de réseau → lire depuis la BDD locale
                    new Thread(() -> {
                        List<Lieu> lieuxLocaux = db.myDao().getAllLieux();
                        android.os.Handler mainHandler =
                                new android.os.Handler(android.os.Looper.getMainLooper());
                        if (lieuxLocaux != null && !lieuxLocaux.isEmpty()) {
                            mainHandler.post(() -> callback.onSucces(lieuxLocaux));
                        } else {
                            mainHandler.post(() ->
                                    callback.onErreur("Erreur réseau et aucune donnée locale disponible"));
                        }
                    }).start();
                }
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(requete);
    }
}
