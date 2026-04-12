package fr.app.application.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.app.application.R;
import fr.app.application.model.Lieu;
import fr.app.application.view.detailsLieux.DetailLieuActivity;

/**
 * Adapter RecyclerView pour afficher la liste des lieux touristiques.
 * Chaque item affiche : photo, nom, catégorie, note moyenne et un bouton "Détails".
 */
public class LieuAdapter extends RecyclerView.Adapter<LieuAdapter.LieuViewHolder> {

    private final Context contexte;
    private List<Lieu> listeLieux;
    private final OnLieuClique ecouteurClic;

    public interface OnLieuClique {
        void onClic(Lieu lieu);
    }

    public LieuAdapter(Context contexte, List<Lieu> listeLieux, OnLieuClique ecouteurClic) {
        this.contexte       = contexte;
        this.listeLieux     = listeLieux;
        this.ecouteurClic   = ecouteurClic;
    }

    @NonNull
    @Override
    public LieuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vue = LayoutInflater.from(contexte).inflate(R.layout.item_lieu, parent, false);
        return new LieuViewHolder(vue);
    }

    @Override
    public void onBindViewHolder(@NonNull LieuViewHolder titulaire, int position) {
        Lieu lieu = listeLieux.get(position);

        // Nom du lieu
        titulaire.nomLieu.setText(lieu.getNom());

        // Catégorie
        titulaire.categorieLabel.setText(lieu.getCategorie() != null ? lieu.getCategorie() : "Non classé");

        // Note moyenne
        if (lieu.getNoteMoyen() != null) {
            titulaire.noteMoyenne.setText(lieu.getNoteMoyen() + " / 5");
            titulaire.noteMoyenne.setVisibility(View.VISIBLE);
        } else {
            titulaire.noteMoyenne.setVisibility(View.GONE);
        }

        // Photo avec Glide (placeholder si pas de photo)
        if (lieu.getPhoto() != null && !lieu.getPhoto().isEmpty()) {
            Glide.with(contexte)
                    .load(lieu.getPhoto())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(titulaire.photoLieu);
        } else {
            titulaire.photoLieu.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Clic sur l'item → centrer la carte (comportement existant)
        titulaire.itemView.setOnClickListener(v -> ecouteurClic.onClic(lieu));

        // Clic sur le bouton "Détails" → ouvrir DetailLieuActivity
        titulaire.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(contexte, DetailLieuActivity.class);
            intent.putExtra(DetailLieuActivity.EXTRA_ID,        lieu.getId());
            contexte.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return listeLieux != null ? listeLieux.size() : 0; }

    /**
     * Met à jour la liste et rafraîchit l'affichage.
     */
    public void mettreAJourListe(List<Lieu> nouveauxLieux) {
        this.listeLieux = nouveauxLieux;
        notifyDataSetChanged();
    }

    // ── ViewHolder ────────────────────────────────────────────────────────

    static class LieuViewHolder extends RecyclerView.ViewHolder {
        ImageView photoLieu;
        TextView  nomLieu;
        TextView  categorieLabel;
        TextView  noteMoyenne;
        Button    btnDetails;

        LieuViewHolder(@NonNull View itemView) {
            super(itemView);
            photoLieu      = itemView.findViewById(R.id.imageLieu);
            nomLieu        = itemView.findViewById(R.id.textNomLieu);
            categorieLabel = itemView.findViewById(R.id.textCategorie);
            noteMoyenne    = itemView.findViewById(R.id.textNoteMoyenne);
            btnDetails     = itemView.findViewById(R.id.btnDetails);
        }
    }
}
