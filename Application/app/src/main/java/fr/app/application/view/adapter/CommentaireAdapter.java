package fr.app.application.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.app.application.R;
import fr.app.application.model.Commentaire;
import fr.app.application.model.Visite;

public class CommentaireAdapter extends RecyclerView.Adapter<CommentaireAdapter.ViewHolder> {

    private final List<Commentaire> commentaires = new ArrayList<>();

    public void setCommentaires(List<Commentaire> nouveaux) {
        commentaires.clear();
        if (nouveaux != null) commentaires.addAll(nouveaux);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_commentaire, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Commentaire c = commentaires.get(position);

        h.ratingBar.setRating(c.getNote());
        h.tvNote.setText(c.getNote() + " / 5");

        String msg = c.getMessage();
        if (msg != null && !msg.trim().isEmpty()) {
            h.tvMessage.setText(msg.trim());
            h.tvMessage.setVisibility(View.VISIBLE);
        } else {
            h.tvMessage.setVisibility(View.GONE);
        }

        h.tvDate.setText("");
    }

    @Override
    public int getItemCount() { return commentaires.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final RatingBar ratingBar;
        final TextView  tvNote;
        final TextView  tvMessage;
        final TextView  tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            ratingBar  = itemView.findViewById(R.id.ratingBarCommentaire);
            tvNote     = itemView.findViewById(R.id.tvNoteCommentaire);
            tvMessage  = itemView.findViewById(R.id.tvMessageCommentaire);
            tvDate     = itemView.findViewById(R.id.tvDateCommentaire);
        }
    }
}