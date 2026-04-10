package fr.app.application.view;

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

/**
 * Adapter pour afficher la liste des itinéraires dans un RecyclerView.
 */
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

        // Numéro et durée
        holder.tvTitre.setText("Itinéraire #" + itineraire.getId());

        if (itineraire.getDureTotal() != null) {
            int heures  = itineraire.getDureTotal() / 60;
            int minutes = itineraire.getDureTotal() % 60;
            if (heures > 0) {
                holder.tvDuree.setText("Durée : " + heures + "h" + (minutes > 0 ? minutes + "min" : ""));
            } else {
                holder.tvDuree.setText("Durée : " + minutes + " min");
            }
        } else {
            holder.tvDuree.setText("Durée non définie");
        }

        // Nombre de lieux
        int nbLieux = itineraire.getListeLieux() != null ? itineraire.getListeLieux().size() : 0;
        holder.tvNbLieux.setText(nbLieux + " lieu" + (nbLieux > 1 ? "x" : ""));

        // Noms des lieux sous forme de liste
        if (itineraire.getListeLieux() != null && !itineraire.getListeLieux().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Itineraire.ListeLieuItineraire ll : itineraire.getListeLieux()) {
                if (ll.getIdLieu() != null) {
                    if (sb.length() > 0) sb.append(" → ");
                    sb.append(ll.getIdLieu().getNom());
                }
            }
            holder.tvLieux.setText(sb.toString());
            holder.tvLieux.setVisibility(View.VISIBLE);
        } else {
            holder.tvLieux.setVisibility(View.GONE);
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
