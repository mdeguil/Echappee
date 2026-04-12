package fr.app.application.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.app.application.R;
import fr.app.application.model.Itineraire;

public class ItineraireAdapter extends RecyclerView.Adapter<ItineraireAdapter.ViewHolder> {

    private final Context           contexte;
    private final List<Itineraire>  itineraires;

    public ItineraireAdapter(Context contexte, List<Itineraire> itineraires) {
        this.contexte    = contexte;
        this.itineraires = itineraires;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(contexte)
                .inflate(R.layout.item_itineraire, parent, false);
        return new ViewHolder(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Itineraire itineraire = itineraires.get(position);

        holder.tvTitre.setText("Itinéraire #" + itineraire.getId());

        if (itineraire.getDureTotal() != null) {
            int heures  = itineraire.getDureTotal() / 60;
            int minutes = itineraire.getDureTotal() % 60;
            if (heures > 0) {
                holder.tvDuree.setText("Durée : " + heures + "h" + (minutes > 0 ? String.format("%02d", minutes) : ""));
            } else {
                holder.tvDuree.setText("Durée : " + minutes + " min");
            }
        } else {
            holder.tvDuree.setText("Durée non définie");
        }

        int nbLieux = (itineraire.getLieux() != null) ? itineraire.getLieux().size() : 0;
        holder.tvNbLieux.setText(nbLieux + " lieu" + (nbLieux > 1 ? "x" : ""));

        if (itineraire.getLieux() != null && !itineraire.getLieux().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Itineraire.LieuRef lieu : itineraire.getLieux()) {
                if (lieu.getNom() != null) {
                    if (sb.length() > 0) sb.append(" → ");
                    sb.append(lieu.getNom());
                }
            }
            holder.tvLieux.setText(sb.toString());
            holder.tvLieux.setVisibility(View.VISIBLE);
        } else {
            holder.tvLieux.setText("Aucun lieu associé");
            holder.tvLieux.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return itineraires.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitre;
        TextView tvDuree;
        TextView tvNbLieux;
        TextView tvLieux;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitre   = itemView.findViewById(R.id.tvItineraireTitre);
            tvDuree   = itemView.findViewById(R.id.tvItineraireDuree);
            tvNbLieux = itemView.findViewById(R.id.tvItineraireNbLieux);
            tvLieux   = itemView.findViewById(R.id.tvItineraireLieux);
        }
    }
}
