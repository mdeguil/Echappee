package fr.app.application.model.reponse;

import com.google.gson.annotations.SerializedName;

import fr.app.application.model.DetailLieux;


public class ReponseDetailLieux {
    @SerializedName("data")
    private DetailLieux data;

    public DetailLieux getData() { return data; }
}
