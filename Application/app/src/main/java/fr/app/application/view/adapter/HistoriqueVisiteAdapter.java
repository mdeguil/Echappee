package fr.app.application.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import fr.app.application.R;
import fr.app.application.model.Visite;

public class HistoriqueVisiteAdapter extends RecyclerView.Adapter<HistoriqueVisiteAdapter.ViewHolder> {

    public interface OnSupprimerListener {
        void onSupprimer(Visite visite, int position);
    }

    private final List<Visite>         visites;
    private final OnSupprimerListener  onSupprimer;

    public HistoriqueVisiteAdapter(List<Visite> visites, OnSupprimerListener onSupprimer) {
        this.visites     = visites;
        this.onSupprimer = onSupprimer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_visite, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Visite visite = visites.get(position);

        holder.tvDate.setText("📅 " + visite.getDateFormatee());
        holder.tvNomLieu.setText("📍 " + (visite.getNomLieu() != null ? visite.getNomLieu() : "Lieu inconnu"));
        holder.ratingBar.setRating(visite.getNote());
        holder.tvCommentaire.setText("💬 " + (visite.getMessage() != null ? visite.getMessage() : "Aucun commentaire"));


        holder.btnSupprimer.setOnClickListener(v ->
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Supprimer la visite")
                        .setMessage("Voulez-vous vraiment supprimer cette visite ?")
                        .setPositiveButton("Supprimer", (dialog, which) ->
                                onSupprimer.onSupprimer(visite, holder.getAdapterPosition()))
                        .setNegativeButton("Annuler", null)
                        .show()
        );
    }

    @Override
    public int getItemCount() { return visites != null ? visites.size() : 0; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvNomLieu;
        TextView       tvDate;
        RatingBar      ratingBar;
        TextView       tvCommentaire;
        MaterialButton btnSupprimer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomLieu     = itemView.findViewById(R.id.tvVisiteNomLieu);
            tvDate        = itemView.findViewById(R.id.tvVisiteDate);
            ratingBar     = itemView.findViewById(R.id.ratingBarVisite);
            tvCommentaire = itemView.findViewById(R.id.tvVisiteCommentaire);
            btnSupprimer  = itemView.findViewById(R.id.btnSupprimerVisite);
        }
    }
}