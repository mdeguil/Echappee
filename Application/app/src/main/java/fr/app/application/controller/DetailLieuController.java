package fr.app.application.controller;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import fr.app.application.model.DetailLieux;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.BDD.AppDatabase;
import fr.app.application.utils.NetworkMonitor;
import fr.app.application.utils.VolleyUtils;

public class DetailLieuController {

    private static final String TAG             = "DetailLieuController";
    private static final String ENDPOINT_DETAIL = "/api/detail_lieus/";

    private final Context     contexte;
    private final Gson        gson;
    private final AppDatabase db;

    public interface CallbackDetail {
        void onSucces(DetailLieux detail);
        void onErreur(String messageErreur);
    }

    public DetailLieuController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
        this.db       = AppDatabase.getDatabase(contexte);
    }

    /**
     * Récupère les détails d'un lieu par son identifiant.
     *
     * Stratégie offline-first :
     *  1. Si le réseau est disponible → appel API → sauvegarde Room → callback
     *  2. Si hors connexion           → lit Room directement → callback
     *  3. Si API échoue              → repli sur Room → callback
     *  4. Si rien nulle part         → onErreur()
     */
    public void recupererDetail(int id, CallbackDetail callback) {

        boolean connecte = NetworkMonitor.getInstance(contexte).estConnecte();

        if (!connecte) {
            // Mode hors connexion : lecture immédiate en base, sans tenter le réseau
            lireDepuisRoom(id, callback, "Hors connexion");
            return;
        }

        // Mode en ligne : appel API avec repli Room en cas d'échec
        String url = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_DETAIL) + id;

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        DetailLieux detail = gson.fromJson(reponse, DetailLieux.class);
                        if (detail != null && detail.getId() != 0) {
                            // Sauvegarde en arrière-plan pour le prochain accès offline
                            new Thread(() -> db.myDao().insertDetailLieu(detail)).start();
                            callback.onSucces(detail);
                        } else {
                            callback.onErreur("Détail introuvable pour l'id " + id);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur de parsing", e);
                        // Parsing raté → on essaie quand même le cache local
                        lireDepuisRoom(id, callback, "Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> {
                    Log.w(TAG, "Erreur réseau, repli sur Room pour l'id " + id);
                    lireDepuisRoom(id, callback, "Erreur réseau");
                }
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(requete);
    }

    /**
     * Lecture depuis Room sur un thread secondaire, post du résultat sur le thread UI.
     *
     * @param raisonRepli Message de contexte pour le log (non affiché à l'utilisateur).
     */
    private void lireDepuisRoom(int id, CallbackDetail callback, String raisonRepli) {
        new Thread(() -> {
            DetailLieux local = db.myDao().getDetailLieu(id);
            Handler handler = new Handler(Looper.getMainLooper());

            if (local != null) {
                Log.d(TAG, "Données locales utilisées (" + raisonRepli + ") pour l'id " + id);
                handler.post(() -> callback.onSucces(local));
            } else {
                Log.w(TAG, "Aucune donnée locale disponible pour l'id " + id);
                handler.post(() -> callback.onErreur(
                        "Détails indisponibles hors connexion"));
            }
        }).start();
    }
}