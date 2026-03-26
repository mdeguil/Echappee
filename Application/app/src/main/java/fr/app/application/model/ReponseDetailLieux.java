package fr.app.application.model;

import com.google.gson.annotations.SerializedName;

public class ReponseDetailLieux {
    @SerializedName("data")
    private DetailLieux data;

    public DetailLieux getData() { return data; }
}
