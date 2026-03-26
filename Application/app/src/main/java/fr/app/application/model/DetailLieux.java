package fr.app.application.model;

import com.google.gson.annotations.SerializedName;

public class DetailLieux {
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
    public Integer getFarif() { return tarif; }
    public String getAccessibilite()   { return accessibilite; }
    public String getPhotos()  { return photos; }
}
