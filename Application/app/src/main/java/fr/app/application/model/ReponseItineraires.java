package fr.app.application.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Wrapper de la réponse API pour la liste des itinéraires.
 * Correspond à la structure : { "data": [...] }
 */
public class ReponseItineraires {

    @SerializedName("data")
    private List<Itineraire> data;

    public List<Itineraire> getData() { return data; }
}
