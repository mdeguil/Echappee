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

    @SerializedName("listeLieux")
    private List<ListeLieuItineraire> listeLieux;

    // ── Getters ───────────────────────────────────────────────────────────

    public int getId()                          { return id; }
    public Integer getDureTotal()               { return dureTotal; }
    public List<ListeLieuItineraire> getListeLieux() { return listeLieux; }

    // ── Classe interne : un lieu dans l'itinéraire ────────────────────────

    public static class ListeLieuItineraire {

        @SerializedName("id")
        private int id;

        @SerializedName("idLieu")
        private LieuRef idLieu;

        public int getId()         { return id; }
        public LieuRef getIdLieu() { return idLieu; }
    }

    public static class LieuRef {

        @SerializedName("id")
        private int id;

        @SerializedName("nom")
        private String nom;

        @SerializedName("photo")
        private String photo;

        @SerializedName("latitude")
        private Double latitude;

        @SerializedName("longitude")
        private Double longitude;

        public int getId()        { return id; }
        public String getNom()    { return nom; }
        public String getPhoto()  { return photo; }
        public Double getLatitude()  { return latitude; }
        public Double getLongitude() { return longitude; }
    }
}
