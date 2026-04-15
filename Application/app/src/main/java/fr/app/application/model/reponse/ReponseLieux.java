package fr.app.application.model.reponse;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import fr.app.application.model.Lieu;

/**
 * Représente la réponse complète de GET /api/lieux
 * Format : { "data": [ {...}, {...} ] }
 */
public class ReponseLieux {

    @SerializedName("data")
    private List<Lieu> data;

    public List<Lieu> getData() { return data; }
}