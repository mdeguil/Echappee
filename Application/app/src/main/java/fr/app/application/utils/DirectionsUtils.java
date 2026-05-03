package fr.app.application.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import fr.app.application.model.Lieu;

public class DirectionsUtils {

    private static final String TAG      = "DirectionsUtils";
    private static final String BASE_URL = "https://router.project-osrm.org/route/v1/foot/";

    public interface CallbackDuree {
        void onSucces(int dureeEnMinutes);
        void onErreur(String messageErreur);
    }

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

        String urlStr = BASE_URL + coordsBuilder + "?overview=false";
        Log.d(TAG, "Requête OSRM : " + urlStr);

        Handler mainHandler = new Handler(Looper.getMainLooper());

        // HttpURLConnection dans un thread séparé — aucun intercepteur Volley
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(urlStr);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15_000);
                connection.setReadTimeout(15_000);
                // Aucun header Authorization — uniquement Accept
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                int statusCode = connection.getResponseCode();
                Log.d(TAG, "Status code OSRM : " + statusCode);

                if (statusCode != 200) {
                    mainHandler.post(() -> callback.onErreur("OSRM HTTP " + statusCode));
                    return;
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                String reponseStr = sb.toString();
                Log.d(TAG, "Réponse OSRM : " + reponseStr);

                JSONObject reponse = new JSONObject(reponseStr);
                String code = reponse.getString("code");

                if (!"Ok".equals(code)) {
                    mainHandler.post(() -> callback.onErreur("OSRM erreur : " + code));
                    return;
                }

                JSONArray routes = reponse.getJSONArray("routes");
                JSONObject route = routes.getJSONObject(0);

                double dureeSecondesBrute = route.getDouble("duration");
                double dureeMinutesBrute  = dureeSecondesBrute / 60.0;
                double facteurAjuste      = dureeMinutesBrute < 6.0 ? 4.0 : 5.7;
                int dureeFinalMinutes     = (int) Math.ceil(dureeMinutesBrute * facteurAjuste);

                Log.d(TAG, "Durée brute OSRM : "     + dureeMinutesBrute + " min");
                Log.d(TAG, "Facteur appliqué : "      + facteurAjuste);
                Log.d(TAG, "Durée finale affichée : " + dureeFinalMinutes + " min");

                mainHandler.post(() -> callback.onSucces(dureeFinalMinutes));

            } catch (Exception e) {
                Log.e(TAG, "Erreur : " + e.getMessage());
                mainHandler.post(() -> callback.onErreur("Erreur OSRM : " + e.getMessage()));
            } finally {
                if (connection != null) connection.disconnect();
            }
        }).start();
    }
}