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

    // ── Getters ───────────────────────────────────────────────────────────

    public int getId()            { return id; }
    public String getDescription()        { return description; }
    public String getHoraires()      { return horaires; }
    public Integer getTarif() { return tarif; }
    public String getAccessibilite()   { return accessibilite; }
    public String getPhotos()  { return photos; }
}
