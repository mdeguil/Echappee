package fr.app.application.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.List;

import fr.app.application.R;
import fr.app.application.model.Itiniraire;
import fr.app.application.view.itiniraires.DetailItineraireActivity;

public class ItineraireAdapter extends RecyclerView.Adapter<ItineraireAdapter.ViewHolder> {

    public interface OnSupprimerListener {
        void onSupprimer(Itiniraire itineraire, int position);
    }

    private final Context              contexte;
    private final List<Itiniraire>     itineraires;
    private final OnSupprimerListener  onSupprimer;

    public ItineraireAdapter(Context contexte,
                             List<Itiniraire> itineraires,
                             OnSupprimerListener onSupprimer) {
        this.contexte     = contexte;
        this.itineraires  = itineraires;
        this.onSupprimer  = onSupprimer;
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
        Itiniraire itineraire = itineraires.get(position);
        List<Itiniraire.LieuRef> lieux = itineraire.getLieux();

        if (lieux != null && !lieux.isEmpty()) {
            String nomDepart  = lieux.get(0).getNom();
            String nomArrivee = lieux.get(lieux.size() - 1).getNom();

            if (lieux.size() == 1) {
                holder.tvTitre.setText("📍 " + nomDepart);
            } else {
                holder.tvTitre.setText("📍 " + nomDepart + "  →  🏁 " + nomArrivee);
            }
        } else {
            holder.tvTitre.setText("Itinéraire #" + itineraire.getId());
        }

        // ── Nombre de lieux ───────────────────────────────────────────────
        int nbLieux = itineraire.getNombreDeLieux();
        holder.tvNbLieux.setText("📌 " + nbLieux + " lieu" + (nbLieux > 1 ? "x" : ""));

        // ── Durée estimée ─────────────────────────────────────────────────
        if (itineraire.getDureTotal() != null && itineraire.getDureTotal() > 0) {
            int heures  = itineraire.getDureTotal() / 60;
            int minutes = itineraire.getDureTotal() % 60;
            if (heures > 0) {
                holder.tvDuree.setText("🚶 " + heures + "h"
                        + (minutes > 0 ? String.format("%02d", minutes) : "") + " à pied");
            } else {
                holder.tvDuree.setText("🚶 " + minutes + " min à pied");
            }
        } else {
            holder.tvDuree.setText("🚶 Durée non définie");
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(contexte, DetailItineraireActivity.class);
            intent.putExtra(
                    DetailItineraireActivity.EXTRA_ITINERAIRE,
                    new Gson().toJson(itineraire)
            );
            contexte.startActivity(intent);
        });

        holder.btnSupprimer.setOnClickListener(v -> {
            new AlertDialog.Builder(contexte)
                    .setTitle("Supprimer l'itinéraire")
                    .setMessage("Voulez-vous vraiment supprimer cet itinéraire ?")
                    .setPositiveButton("Supprimer", (dialog, which) ->
                            onSupprimer.onSupprimer(itineraire, holder.getAdapterPosition())
                    )
                    .setNegativeButton("Annuler", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return itineraires.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvTitre;
        TextView       tvNbLieux;
        TextView       tvDuree;
        MaterialButton btnSupprimer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitre      = itemView.findViewById(R.id.tvItineraireTitre);
            tvNbLieux    = itemView.findViewById(R.id.tvItineraireNbLieux);
            tvDuree      = itemView.findViewById(R.id.tvItineraireDuree);
            btnSupprimer = itemView.findViewById(R.id.btnSupprimerItineraire);
        }
    }
}