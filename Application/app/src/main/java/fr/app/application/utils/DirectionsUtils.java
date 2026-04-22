package fr.app.application.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import fr.app.application.model.Lieu;

public class DirectionsUtils {

    private static final String TAG = "DirectionsUtils";
    private static final String BASE_URL = "https://router.project-osrm.org/route/v1/foot/";

    public interface CallbackDuree {
        void onSucces(int dureeEnMinutes);
        void onErreur(String messageErreur);
    }

    /**
     * Calcule la durée totale estimée d'un itinéraire à pied en interrogeant l'API OSRM.
     */
    public static void calculerDureeAPied(
            Context contexte,
            List<Lieu> lieux,
            CallbackDuree callback) {

        if (lieux == null || lieux.size() < 2) {
            callback.onSucces(0);
            return;
        }

        for (Lieu lieu : lieux) {
            if (lieu.getLatitude() == null || lieu.getLongitude() == null) {
                callback.onErreur("Un lieu n'a pas de coordonnées GPS");
                return;
            }
        }

        StringBuilder coordsBuilder = new StringBuilder();
        for (Lieu lieu : lieux) {
            if (coordsBuilder.length() > 0) coordsBuilder.append(";");
            coordsBuilder
                    .append(lieu.getLongitude())
                    .append(",")
                    .append(lieu.getLatitude());
        }

        String url = BASE_URL + coordsBuilder + "?overview=false";
        Log.d(TAG, "Requête OSRM : " + url);

        JsonObjectRequest requete = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                reponse -> {
                    try {
                        String code = reponse.getString("code");

                        if (!"Ok".equals(code)) {
                            callback.onErreur("OSRM erreur : " + code);
                            return;
                        }

                        JSONArray routes = reponse.getJSONArray("routes");
                        JSONObject route = routes.getJSONObject(0);

                        double dureeSecondesBrute = route.getDouble("duration");
                        double dureeMinutesBrute = dureeSecondesBrute / 60.0;

                        double facteurAjuste;

                        if (dureeMinutesBrute < 6.0) {
                            facteurAjuste = 4.0;
                        } else {
                            facteurAjuste = 5.7;
                        }

                        int dureeFinalMinutes = (int) Math.ceil(dureeMinutesBrute * facteurAjuste);

                        Log.d(TAG, "Durée brute OSRM : " + dureeMinutesBrute + " min");
                        Log.d(TAG, "Facteur appliqué : " + facteurAjuste);
                        Log.d(TAG, "Durée finale affichée : " + dureeFinalMinutes + " min");

                        callback.onSucces(dureeFinalMinutes);

                    } catch (Exception e) {
                        callback.onErreur("Erreur parsing OSRM : " + e.getMessage());
                    }
                },
                erreur -> callback.onErreur("Erreur réseau OSRM : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).addToRequestQueue(requete);
    }
}