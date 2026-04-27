package fr.app.application.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

/**
 * Modèle représentant un lieu touristique.
 * Les champs correspondent exactement à la réponse de GET /api/lieux.
 */
@Entity(tableName = "lieu")
public class Lieu {
    @PrimaryKey
    @SerializedName("id")
    private int id;

    @SerializedName("nom")
    private String nom;

    @SerializedName("photo")
    private String photo;

    @SerializedName("noteMoyen")
    private Integer noteMoyen;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("categorie")
    private String categorie;

    @SerializedName("commentaire")
    private String commentaire;

    // ── Getters ───────────────────────────────────────────────────────────

    public int getId()            { return id; }
    public String getNom()        { return nom; }
    public String getPhoto()      { return photo; }
    public Integer getNoteMoyen() { return noteMoyen; }
    public Double getLatitude()   { return latitude; }
    public Double getLongitude()  { return longitude; }
    public String getCategorie()  { return categorie; }
    public String getCommentaire(){ return commentaire; }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setNoteMoyen(Integer noteMoyen) {
        this.noteMoyen = noteMoyen;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}