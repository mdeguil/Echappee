package fr.app.application.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Représente la réponse complète de GET /api/lieux
 * Format : { "data": [ {...}, {...} ] }
 */
public class ReponseLieux {

    @SerializedName("data")
    private List<Lieu> data;

    public List<Lieu> getData() { return data; }
}