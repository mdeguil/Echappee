package fr.app.application.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;

import fr.app.application.R;
import fr.app.application.model.Lieu;

public class DetailLieuActivity extends AppCompatActivity {

    // ── Clés des extras ───────────────────────────────────────────────────
    public static final String EXTRA_ID          = "extra_id";
    public static final String EXTRA_NOM         = "extra_nom";
    public static final String EXTRA_PHOTO       = "extra_photo";
    public static final String EXTRA_CATEGORIE   = "extra_categorie";
    public static final String EXTRA_NOTE        = "extra_note";
    public static final String EXTRA_LATITUDE    = "extra_latitude";
    public static final String EXTRA_LONGITUDE   = "extra_longitude";
    public static final String EXTRA_COMMENTAIRE = "extra_commentaire";

    // ── Vues ──────────────────────────────────────────────────────────────
    private ImageView  imgPhoto;
    private TextView   tvNom;
    private Chip       chipCategorie;
    private RatingBar  ratingBar;
    private TextView   tvNote;
    private TextView   tvCommentaire;
    private TextView   tvCoordonnees;
    private View       sectionCommentaire;
    private View       sectionCoordonnees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_lieu);

        // Toolbar avec bouton retour
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(""); // Le titre est géré par le TextView
        }

        initVues();
        remplirDonnees();
    }

    private void initVues() {
        imgPhoto           = findViewById(R.id.imgPhotoDetail);
        tvNom              = findViewById(R.id.tvNomDetail);
        chipCategorie      = findViewById(R.id.chipCategorieDetail);
        ratingBar          = findViewById(R.id.ratingBarDetail);
        tvNote             = findViewById(R.id.tvNoteDetail);
        tvCommentaire      = findViewById(R.id.tvCommentaireDetail);
        tvCoordonnees      = findViewById(R.id.tvCoordonneesDetail);
        sectionCommentaire = findViewById(R.id.sectionCommentaire);
        sectionCoordonnees = findViewById(R.id.sectionCoordonnees);
    }

    private void remplirDonnees() {
        Intent intent = getIntent();

        // ── Nom ───────────────────────────────────────────────────────────
        String nom = intent.getStringExtra(EXTRA_NOM);
        tvNom.setText(nom != null ? nom : "Lieu inconnu");

        // ── Photo ─────────────────────────────────────────────────────────
        String photo = intent.getStringExtra(EXTRA_PHOTO);
        if (photo != null && !photo.isEmpty()) {
            Glide.with(this)
                    .load(photo)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(imgPhoto);
        } else {
            imgPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // ── Catégorie ─────────────────────────────────────────────────────
        String categorie = intent.getStringExtra(EXTRA_CATEGORIE);
        chipCategorie.setText(categorie != null && !categorie.isEmpty() ? categorie : "Non classé");

        // ── Note ──────────────────────────────────────────────────────────
        // getIntExtra retourne -1 si la clé est absente (note non renseignée)
        int note = intent.getIntExtra(EXTRA_NOTE, -1);
        if (note >= 0) {
            ratingBar.setRating(note);
            tvNote.setText(note + " / 5");
            ratingBar.setVisibility(View.VISIBLE);
            tvNote.setVisibility(View.VISIBLE);
        } else {
            ratingBar.setVisibility(View.GONE);
            tvNote.setText("Non noté");
        }

        // ── Commentaire ───────────────────────────────────────────────────
        String commentaire = intent.getStringExtra(EXTRA_COMMENTAIRE);
        if (commentaire != null && !commentaire.isEmpty()) {
            tvCommentaire.setText(commentaire);
            sectionCommentaire.setVisibility(View.VISIBLE);
        } else {
            sectionCommentaire.setVisibility(View.GONE);
        }

        // ── Coordonnées GPS ───────────────────────────────────────────────
        double lat = intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN);
        double lon = intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN);
        if (!Double.isNaN(lat) && !Double.isNaN(lon)) {
            tvCoordonnees.setText(
                    String.format("%.5f° N,  %.5f° E", lat, lon)
            );
            sectionCoordonnees.setVisibility(View.VISIBLE);
        } else {
            sectionCoordonnees.setVisibility(View.GONE);
        }
    }

    /** Bouton retour dans la toolbar */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Méthode utilitaire statique ───────────────────────────────────────

    /**
     * Crée un Intent prêt à l'emploi pour ouvrir le détail d'un lieu.
     * Utilisation dans ListeLieuxActivity :
     *   startActivity(DetailLieuActivity.creerIntent(this, lieu));
     */
    public static Intent creerIntent(android.content.Context contexte, Lieu lieu) {
        Intent intent = new Intent(contexte, DetailLieuActivity.class);
        intent.putExtra(EXTRA_ID,    lieu.getId());
        intent.putExtra(EXTRA_NOM,   lieu.getNom());
        intent.putExtra(EXTRA_PHOTO, lieu.getPhoto());
        intent.putExtra(EXTRA_CATEGORIE,   lieu.getCategorie());
        intent.putExtra(EXTRA_COMMENTAIRE, lieu.getCommentaire());

        if (lieu.getNoteMoyen() != null) {
            intent.putExtra(EXTRA_NOTE, (int) lieu.getNoteMoyen());
        }
        if (lieu.getLatitude() != null) {
            intent.putExtra(EXTRA_LATITUDE, (double) lieu.getLatitude());
        }
        if (lieu.getLongitude() != null) {
            intent.putExtra(EXTRA_LONGITUDE, (double) lieu.getLongitude());
        }
        return intent;
    }
}