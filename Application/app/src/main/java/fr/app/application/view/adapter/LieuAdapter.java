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

        titulaire.nomLieu.setText(lieu.getNom());

        titulaire.categorieLabel.setText(lieu.getCategorie() != null ? lieu.getCategorie() : "Non classé");

        if (lieu.getNoteMoyen() != null) {
            titulaire.noteMoyenne.setText(lieu.getNoteMoyen() + " / 5");
            titulaire.noteMoyenne.setVisibility(View.VISIBLE);
        } else {
            titulaire.noteMoyenne.setVisibility(View.GONE);
        }

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

        titulaire.itemView.setOnClickListener(v -> ecouteurClic.onClic(lieu));

        // Clic sur le bouton "Détails" → ouvrir DetailLieuActivity
        titulaire.btnDetails.setOnClickListener(v -> {
            contexte.startActivity(DetailLieuActivity.creerIntent(contexte, lieu));
        });
    }

    @Override
    public int getItemCount() { return listeLieux != null ? listeLieux.size() : 0; }

    public void mettreAJourListe(List<Lieu> nouveauxLieux) {
        this.listeLieux.clear();
        this.listeLieux.addAll(nouveauxLieux);
        notifyDataSetChanged();
    }
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
