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

        // ── Titre ────────────────────────────────────────────────────────
        holder.tvTitre.setText("Itinéraire #" + itineraire.getId());

        // ── Durée ─────────────────────────────────────────────────────────
        if (itineraire.getDureTotal() != null && itineraire.getDureTotal() > 0) {
            int heures  = itineraire.getDureTotal() / 60;
            int minutes = itineraire.getDureTotal() % 60;
            if (heures > 0) {
                holder.tvDuree.setText("🚶 " + heures + "h" + (minutes > 0 ? String.format("%02d", minutes) : ""));
            } else {
                holder.tvDuree.setText("🚶 " + minutes + " min");
            }
        } else {
            holder.tvDuree.setText("Durée non définie");
        }

        // ── Nombre de lieux ───────────────────────────────────────────────
        int nbLieux = (itineraire.getLieux() != null) ? itineraire.getLieux().size() : 0;
        holder.tvNbLieux.setText(nbLieux + " lieu" + (nbLieux > 1 ? "x" : ""));

        // ── Départ → Arrivée uniquement ───────────────────────────────────
        List<Itineraire.LieuRef> lieux = itineraire.getLieux();
        if (lieux != null && !lieux.isEmpty()) {
            String nomDepart  = lieux.get(0).getNom();
            String nomArrivee = lieux.get(lieux.size() - 1).getNom();

            if (lieux.size() == 1) {
                // Un seul lieu
                holder.tvLieux.setText("📍 " + nomDepart);
            } else {
                // Départ → Arrivée
                holder.tvLieux.setText("📍 " + nomDepart + "  →  🏁 " + nomArrivee);
            }
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