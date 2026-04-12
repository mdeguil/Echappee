package fr.app.application.controller;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.List;

import fr.app.application.model.Lieu;
import fr.app.application.model.reponse.ReponseLieux;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.VolleyUtils;

public class LieuController {

    private static final String ENDPOINT_LIEUX = "/api/lieus";

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
     * Récupère la liste complète des lieux sans aucun filtre.
     *
     * @param callback L'interface pour traiter la réponse ou l'erreur.
     */
    public void recupererLieux(CallbackLieux callback) {
        recupererLieuxAvecFiltres(null, null, callback);
    }

    /**
     * Récupère les lieux en appliquant des filtres de catégorie et de recherche textuelle.
     *
     * @param categorie Nom de la catégorie à filtrer (peut être null).
     * @param recherche Terme de recherche pour le nom du lieu (peut être null).
     * @param callback  L'interface pour traiter la réponse ou l'erreur.
     */
    public void recupererLieuxAvecFiltres(String categorie, String recherche, CallbackLieux callback) {
        // L'URL de base est toujours lue depuis le singleton
        String urlBase = ApiConfig.getInstance(contexte).getUrl(ENDPOINT_LIEUX);

        StringBuilder urlBuilder = new StringBuilder(urlBase);
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
