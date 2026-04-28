package fr.app.application.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

import fr.app.application.model.Itineraire;
import fr.app.application.model.Lieu;
import fr.app.application.model.reponse.ReponseItineraires;
import fr.app.application.model.reponse.ReponseLieux;
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


    private final Context     appContext;
    private final Gson        gson = new Gson();

    private SyncManager(Context appContext) {
        this.appContext = appContext;
    }

    public void start() {
        NetworkMonitor.getInstance(appContext).ajouterObserver(new NetworkMonitor.Observer() {
            @Override
            public void onConnexionRetablie() {
                Log.d(TAG, "Connexion rétablie → synchronisation globale en cours…");
                syncLieux();
                syncItineraires();
            }
        });
    }

    private void syncLieux() {
        String url = ApiConfig.getInstance(appContext).getUrl("/api/lieus");

        StringRequest requete = new StringRequest(
                Request.Method.GET, url,
                reponse -> {
                    try {
                        ReponseLieux rep = gson.fromJson(reponse, ReponseLieux.class);
                        if (rep != null && rep.getData() != null) {
                            List<Lieu> lieux = rep.getData();
                            AppDatabase db = AppDatabase.getDatabase(appContext);
                            new Thread(() -> {
                                db.myDao().insertLieux(lieux);
                                Log.d(TAG, "Lieux synchronisés en base : " + lieux.size());
                            }).start();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur sync lieux", e);
                    }
                },
                erreur -> Log.w(TAG, "Sync lieux échouée (réseau) : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(appContext).addToRequestQueue(requete);
    }

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
                                Log.d(TAG, "Itinéraires synchronisés en base : " + finalListe.size());
                            }).start();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur sync itinéraires", e);
                    }
                },
                erreur -> Log.w(TAG, "Sync itinéraires échouée (réseau) : " + erreur.getMessage())
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                return VolleyUtils.getAuthHeaders(appContext);
            }
        };

        VolleyUtils.getInstance(appContext).addToRequestQueue(requete);
    }
}