package fr.app.application.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import fr.app.application.utils.BDD.Converters;

@Entity(tableName = "itiniraire")
@TypeConverters(Converters.class)
public class Itineraire {
    @PrimaryKey
    @SerializedName("id")
    private int id;

    @SerializedName("dureTotal")
    private Integer dureTotal;

    @SerializedName("lieux")
    private List<LieuRef> lieux;

    @SerializedName("nbLieux")
    private int nbLieux;

    // Pour associer l'itinéraire à un utilisateur (mode offline)
    private int userId;

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }
    public Integer getDureTotal()    { return dureTotal; }
    public void setDureTotal(Integer d) { this.dureTotal = d; }
    public List<LieuRef> getLieux()  { return lieux; }
    public void setLieux(List<LieuRef> l) { this.lieux = l; }
    public int getUserId()           { return userId; }
    public void setUserId(int userId){ this.userId = userId; }

    public int getNbLieux() {
        return nbLieux;
    }

    public void setNbLieux(int nbLieux) {
        this.nbLieux = nbLieux;
    }

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
