package fr.app.application.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.List;

import fr.app.application.model.DetailLieux;
import fr.app.application.model.Lieu;
import fr.app.application.model.reponse.ReponseLieux;
import fr.app.application.model.reponse.ReponseDetailLieux;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.BDD.AppDatabase;
import fr.app.application.utils.VolleyUtils;

public class LieuController {

    private static final String TAG             = "LieuController";
    private static final String ENDPOINT_LIEUX  = "/api/lieus";
    private static final String ENDPOINT_DETAILS = "/api/detail_lieus";

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

    /**
     * Récupère la liste des lieux selon les filtres.
     * Dès que la liste est obtenue depuis l'API, déclenche en parallèle
     * le préchargement de TOUS les détails en 1 seul appel réseau.
     */
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

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                urlBuilder.toString(),
                reponse -> {
                    try {
                        ReponseLieux reponseLieux = gson.fromJson(reponse, ReponseLieux.class);
                        if (reponseLieux != null && reponseLieux.getData() != null) {
                            List<Lieu> lieux = reponseLieux.getData();

                            new Thread(() -> db.myDao().insertLieux(lieux)).start();

                            prechargerTousLesDetails();

                            callback.onSucces(lieux);
                        } else {
                            callback.onErreur("Réponse vide ou invalide");
                        }
                    } catch (Exception e) {
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> {
                    new Thread(() -> {
                        List<Lieu> lieuxLocaux = db.myDao().getAllLieux();
                        Handler h = new Handler(Looper.getMainLooper());
                        if (lieuxLocaux != null && !lieuxLocaux.isEmpty()) {
                            h.post(() -> callback.onSucces(lieuxLocaux));
                        } else {
                            h.post(() -> callback.onErreur("Erreur réseau et aucune donnée locale disponible"));
                        }
                    }).start();
                }
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(requete);
    }

    private void prechargerTousLesDetails() {
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_DETAILS);

        StringRequest req = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        ReponseDetailLieux reponseDetail = gson.fromJson(reponse, ReponseDetailLieux.class);
                        if (reponseDetail != null && reponseDetail.getData() != null) {
                            List<DetailLieux> details = reponseDetail.getData();
                            new Thread(() -> {
                                db.myDao().insertAllDetailLieux(details);
                                Log.d(TAG, details.size() + " détails mis en cache Room.");
                            }).start();
                        } else {
                            Log.w(TAG, "Réponse détails vide ou invalide.");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur parsing détails : " + e.getMessage());
                    }
                },
                erreur -> Log.w(TAG, "Préchargement détails échoué : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(req);
    }
}