package fr.app.application.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.List;

import fr.app.application.model.Lieu;
import fr.app.application.model.ReponseLieux;
import fr.app.application.utils.VolleyUtils;

/**
 * Controller qui gère les appels à l'API pour récupérer les lieux.
 * Utilise Volley pour le réseau et Gson pour le parsing JSON.
 */
public class LieuController {

    // À remplacer par votre IP locale (ex: 192.168.1.X) si test sur téléphone physique
    // ou "10.0.2.2" si émulateur Android Studio
    private static final String URL_BASE    = "http://10.98.175.102:8000";
    private static final String URL_LIEUX   = URL_BASE + "/api/lieus";

    private final Context contexte;
    private final Gson    gson;

    public interface CallbackLieux {
        void onSucces(List<Lieu> lieux);
        void onErreur(String messageErreur);
    }

    public LieuController(Context contexte) {
        this.contexte = contexte;
        this.gson     = new Gson();
    }

    /**
     * Récupère tous les lieux depuis l'API.
     */
    public void recupererLieux(CallbackLieux callback) {
        recupererLieuxAvecFiltres(null, null, callback);
    }

    /**
     * Récupère les lieux filtrés par catégorie et/ou nom.
     *
     * @param categorie  ex: "Musée" (null pour ignorer le filtre)
     * @param recherche  ex: "château" (null pour ignorer le filtre)
     */
    public void recupererLieuxAvecFiltres(String categorie, String recherche, CallbackLieux callback) {
        StringBuilder urlBuilder = new StringBuilder(URL_LIEUX);
        boolean premierParametre = true;

        if (categorie != null && !categorie.isEmpty()) {
            urlBuilder.append("?categorie.nom=").append(categorie);
            premierParametre = false;
        }

        if (recherche != null && !recherche.isEmpty()) {
            urlBuilder.append(premierParametre ? "?" : "&").append("nom=").append(recherche);
        }

        String url = urlBuilder.toString();

        StringRequest requete = new StringRequest(
                Request.Method.GET,
                url,
                reponse -> {
                    // Parsing Gson de la réponse { "data": [...] }
                    ReponseLieux reponseLieux = gson.fromJson(reponse, ReponseLieux.class);

                    if (reponseLieux != null && reponseLieux.getData() != null) {
                        callback.onSucces(reponseLieux.getData());
                    } else {
                        callback.onErreur("Réponse vide ou invalide");
                    }
                },
                erreur -> callback.onErreur("Erreur réseau : " + erreur.getMessage())
        );

        VolleyUtils.getInstance(contexte).getRequestQueue().add(requete);
    }
}