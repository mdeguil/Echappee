package fr.app.application.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modèle représentant un itinéraire.
 * Correspond à l'entité Itiniraire du backend Symfony.
 *
 * Endpoint : GET /api/itiniraires
 */
public class Itineraire {

    @SerializedName("id")
    private int id;

    @SerializedName("dureTotal")
    private Integer dureTotal;

    // Ici, on utilise "lieux" pour correspondre au Provider Symfony
    @SerializedName("lieux")
    private List<LieuRef> lieux;

    // On peut aussi mapper "nbLieux" si vous l'avez ajouté dans le PHP
    @SerializedName("nbLieux")
    private int nbLieux;

    public int getId() { return id; }
    public Integer getDureTotal() { return dureTotal; }
    public List<LieuRef> getLieux() { return lieux; }

    // Methode pratique pour obtenir le compte
    public int getNombreDeLieux() {
        if (nbLieux > 0) return nbLieux; // Si l'API l'envoie directement
        return (lieux != null) ? lieux.size() : 0;
    }

    // Gardez votre classe LieuRef comme avant
    public static class LieuRef {
        @SerializedName("id") private int id;
        @SerializedName("nom") private String nom;
        // ... reste de vos champs
        public String getNom() { return nom; }
    }
}
