package fr.app.application.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;
import java.util.List;

@Entity(tableName = "Itiniraire")
public class Itiniraire {
    @PrimaryKey
    @SerializedName("id")
    private int id;

    @SerializedName("dureTotal")
    private Integer dureTotal;

    @SerializedName("lieux")
    private List<LieuRef> lieux;

    @SerializedName("nbLieux")
    private int nbLieux;

    public int getId()               { return id; }
    public Integer getDureTotal()    { return dureTotal; }
    public List<LieuRef> getLieux()  { return lieux; }

    public int getNombreDeLieux() {
        if (nbLieux > 0) return nbLieux;
        return (lieux != null) ? lieux.size() : 0;
    }

    /**
     * Cette classe interne fait office de DTO simplifié.
     */
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