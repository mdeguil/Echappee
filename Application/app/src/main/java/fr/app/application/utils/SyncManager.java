package fr.app.application.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import fr.app.application.controller.LieuController;
import fr.app.application.model.Itineraire;
import fr.app.application.model.reponse.ReponseItineraires;
import fr.app.application.utils.BDD.AppDatabase;

public class SyncManager {

    private static final String TAG = "SyncManager";

    private static volatile SyncManager instance;

    public static SyncManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SyncManager.class) {
                if (instance == null) {
                    instance = new SyncManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final Context appContext;
    private final Gson    gson = new Gson();

    private SyncManager(Context appContext) {
        this.appContext = appContext;
    }

    /**
     * Lance l'écoute réseau.
     * Au retour de la connexion, synchronise lieux + leurs détails + itinéraires.
     *
     * Le préchargement des détails est délégué à LieuController.recupererLieux()
     * qui appelle prechargerTousLesDetails() après chaque chargement réussi.
     */
    public void start() {
        NetworkMonitor.getInstance(appContext).ajouterObserver(new NetworkMonitor.Observer() {
            @Override
            public void onConnexionRetablie() {
                Log.d(TAG, "Connexion rétablie → synchronisation globale en cours…");
                syncLieuxEtDetails();
                syncItineraires();
            }
        });
    }

    // ── Lieux + détails ───────────────────────────────────────────────────

    /**
     * Recharge la liste des lieux via LieuController.
     * Le préchargement de tous les détails est automatiquement déclenché
     * à l'intérieur de recupererLieux() dès que l'API répond.
     */
    private void syncLieuxEtDetails() {
        LieuController lieuController = new LieuController(appContext);
        lieuController.recupererLieux(new LieuController.CallbackLieux() {
            @Override
            public void onSucces(List<fr.app.application.model.Lieu> lieux) {
                Log.d(TAG, "Lieux + détails synchronisés : " + lieux.size() + " lieu(x).");
            }

            @Override
            public void onErreur(String messageErreur) {
                Log.w(TAG, "Sync lieux échouée : " + messageErreur);
            }
        });
    }

    // ── Itinéraires ───────────────────────────────────────────────────────

    private void syncItineraires() {
        String url = ApiConfig.getInstance(appContext).getUrl("/api/itiniraires");
        SessionManager session = new SessionManager(appContext);

        StringRequest requete = new StringRequest(
                Request.Method.GET, url,
                reponse -> {
                    try {
                        List<Itineraire> liste = null;
                        ReponseItineraires rep = gson.fromJson(reponse, ReponseItineraires.class);
                        if (rep != null && rep.getData() != null) {
                            liste = rep.getData();
                        } else {
                            Itineraire[] tableau = gson.fromJson(reponse, Itineraire[].class);
                            if (tableau != null) liste = Arrays.asList(tableau);
                        }

                        if (liste != null) {
                            final List<Itineraire> finalListe = liste;
                            int userId = session.getUserId();
                            for (Itineraire it : finalListe) it.setUserId(userId);

                            AppDatabase db = AppDatabase.getDatabase(appContext);
                            new Thread(() -> {
                                db.myDao().insertItineraires(finalListe);
                                Log.d(TAG, "Itinéraires synchronisés : " + finalListe.size());
                            }).start();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur sync itinéraires", e);
                    }
                },
                erreur -> Log.w(TAG, "Sync itinéraires échouée : " + erreur.getMessage())
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return VolleyUtils.getAuthHeaders(appContext);
            }
        };

        VolleyUtils.getInstance(appContext).addToRequestQueue(requete);
    }
}