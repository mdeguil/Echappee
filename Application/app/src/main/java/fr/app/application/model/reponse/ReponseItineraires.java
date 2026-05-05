package fr.app.application.model.reponse;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import fr.app.application.model.Itineraire;

/**
 * Représente la réponse complète du GET
 * Format : { "data": [ {...}, {...} ] }
 */
public class ReponseItineraires {

    @SerializedName("data")
    private List<Itineraire> data;

    public List<Itineraire> getData() { return data; }
}
