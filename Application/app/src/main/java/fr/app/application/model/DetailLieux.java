package fr.app.application.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "detail_lieu")
public class DetailLieux {
    @PrimaryKey
    @SerializedName("id")
    private int id;

    @SerializedName("description")
    private String description;

    @SerializedName("horaires")
    private String horaires;

    @SerializedName("tarif")
    private Integer tarif;

    @SerializedName("accessibilite")
    private String accessibilite;

    @SerializedName("photos")
    private String photos;

    public int getId()               { return id; }
    public void setId(int id)        { this.id = id; }
    public String getDescription()   { return description; }
    public void setDescription(String d){ this.description = d; }
    public String getHoraires()      { return horaires; }
    public void setHoraires(String h){ this.horaires = h; }
    public Integer getTarif()        { return tarif; }
    public void setTarif(Integer t)  { this.tarif = t; }
    public String getAccessibilite() { return accessibilite; }
    public void setAccessibilite(String a){ this.accessibilite = a; }
    public String getPhotos()        { return photos; }
    public void setPhotos(String p)  { this.photos = p; }
}
