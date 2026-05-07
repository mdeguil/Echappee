package fr.app.application.model.reponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import fr.app.application.model.DetailLieux;

/**
 * Réponse du GET /api/detail_lieus
 * Format : { "data": [ {...}, {...}, ... ] }
 */
public class ReponseDetailLieux {

    @SerializedName("data")
    private List<DetailLieux> data;

    public List<DetailLieux> getData() { return data; }
}