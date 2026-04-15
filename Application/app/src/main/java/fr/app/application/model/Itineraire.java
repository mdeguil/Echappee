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

    @SerializedName("lieux")
    private List<LieuRef> lieux;

    @SerializedName("nbLieux")
    private int nbLieux;

    // ── Getters ───────────────────────────────────────────────────────────

    public int getId()               { return id; }
    public Integer getDureTotal()    { return dureTotal; }
    public List<LieuRef> getLieux()  { return lieux; }

    public int getNombreDeLieux() {
        if (nbLieux > 0) return nbLieux;
        return (lieux != null) ? lieux.size() : 0;
    }

    public static class LieuRef {

        @SerializedName("id")
        private int id;

        @SerializedName("nom")
        private String nom;

        @SerializedName("lat")
        private Double lat;

        @SerializedName("lng")
        private Double lng;

        public int    getId()  { return id; }
        public String getNom() { return nom; }
        public Double getLat() { return lat; }
        public Double getLng() { return lng; }
    }
}