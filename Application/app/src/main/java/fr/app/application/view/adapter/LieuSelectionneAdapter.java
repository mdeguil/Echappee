package fr.app.application.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.app.application.R;
import fr.app.application.model.Lieu;

public class LieuSelectionneAdapter extends RecyclerView.Adapter<LieuSelectionneAdapter.ViewHolder> {

    public interface OnRetirerListener {
        void onRetirer(Lieu lieu);
    }

    private final Context           contexte;
    private final List<Lieu>        lieux;
    private final OnRetirerListener onRetirer;

    public LieuSelectionneAdapter(Context contexte, List<Lieu> lieux, OnRetirerListener onRetirer) {
        this.contexte   = contexte;
        this.lieux      = lieux;
        this.onRetirer  = onRetirer;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(contexte)
                .inflate(R.layout.item_lieu_selectionne, parent, false);
        return new ViewHolder(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Lieu lieu = lieux.get(position);

        holder.tvOrdre.setText(String.valueOf(position + 1));
        holder.tvNom.setText(lieu.getNom());
        holder.tvCategorie.setText(lieu.getCategorie() != null ? lieu.getCategorie() : "");

        holder.btnRetirer.setOnClickListener(v -> onRetirer.onRetirer(lieu));
    }

    @Override
    public int getItemCount() {
        return lieux.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvOrdre;
        TextView    tvNom;
        TextView    tvCategorie;
        ImageButton btnRetirer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrdre    = itemView.findViewById(R.id.tvOrdre);
            tvNom      = itemView.findViewById(R.id.tvNomLieuSelectionne);
            tvCategorie= itemView.findViewById(R.id.tvCategorieLieuSelectionne);
            btnRetirer = itemView.findViewById(R.id.btnRetirerLieu);
        }
    }
}
