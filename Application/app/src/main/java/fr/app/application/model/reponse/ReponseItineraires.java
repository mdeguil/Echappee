package fr.app.application.model.reponse;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import fr.app.application.model.Itiniraire;

/**
 * Wrapper de la réponse API pour la liste des itinéraires.
 * Correspond à la structure : { "data": [...] }
 */
public class ReponseItineraires {

    @SerializedName("data")
    private List<Itiniraire> data;

    public List<Itiniraire> getData() { return data; }
}
