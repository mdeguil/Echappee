package fr.app.application.controller;

import static fr.app.application.BuildConfig.OPENWEATHER_API_KEY;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import fr.app.application.model.Meteo;
import fr.app.application.utils.VolleyUtils;

public class MeteoController {

    private static final String TAG = "MeteoController";

    private static final String URL_BASE =
            "https://api.openweathermap.org/data/2.5/weather";  // ← corrigé

    private final Context contexte;
    private final Gson    gson;

    public interface CallbackMeteo {
        void onSucces(Meteo meteo);
        void onErreur(String messageErreur);
    }

    public MeteoController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    public void recupererMeteo(double latitude, double longitude, CallbackMeteo callback) {

        String url = URL_BASE
                + "?lat="   + latitude
                + "&lon="   + longitude
                + "&appid=" + OPENWEATHER_API_KEY
                + "&units=metric"
                + "&lang=fr";  // ← plus de &dt=

        Log.d(TAG, "Appel météo : " + url);

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    try {
                        Meteo meteo = gson.fromJson(reponse, Meteo.class);
                        if (meteo != null && meteo.getPremierPoint() != null) {
                            callback.onSucces(meteo);
                        } else {
                            callback.onErreur("Données météo introuvables");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur parsing météo", e);
                        callback.onErreur("Erreur de parsing : " + e.getMessage());
                    }
                },
                erreur -> {
                    Log.e(TAG, "Erreur réseau météo", erreur);
                    callback.onErreur("Erreur réseau : " + erreur.getMessage());
                }
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(requete);
    }
}