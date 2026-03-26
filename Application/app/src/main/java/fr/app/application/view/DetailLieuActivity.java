package fr.app.application.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.chip.Chip;

import fr.app.application.R;
import fr.app.application.controller.DetailLieuController;
import fr.app.application.model.DetailLieux;
import fr.app.application.model.Lieu;

/**
 * Affiche le détail complet d'un lieu touristique.
 *
 * Données transmises par Intent (depuis ListeLieuxActivity) :
 *   - id, nom, photo principale, catégorie, note
 *
 * Données chargées depuis l'API (GET /api/lieux/{id}) :
 *   - description, horaires, tarif, accessibilite, photos
 */
public class DetailLieuActivity extends AppCompatActivity {

    // ── Clés des extras ───────────────────────────────────────────────────
    public static final String EXTRA_ID        = "extra_id";
    public static final String EXTRA_NOM       = "extra_nom";
    public static final String EXTRA_PHOTO     = "extra_photo";
    public static final String EXTRA_CATEGORIE = "extra_categorie";
    public static final String EXTRA_NOTE      = "extra_note";

    // ── Vues ──────────────────────────────────────────────────────────────
    private ImageView    imgPhoto;
    private TextView     tvNom;
    private Chip         chipCategorie;
    private RatingBar    ratingBar;
    private TextView     tvNote;
    private ProgressBar  progressBar;
    private TextView     tvErreurDetail;

    private View         sectionDescription;
    private TextView     tvDescription;

    private View         sectionHoraires;
    private TextView     tvHoraires;

    private View         sectionTarifs;
    private TextView     tvTarifs;

    private View         sectionAccessibilite;
    private TextView     tvAccessibilite;

    private View         sectionPhotos;
    private LinearLayout galeriePhotos;

    // ── Controller ────────────────────────────────────────────────────────
    private DetailLieuController controleurDetail;

    // ── Cycle de vie ──────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_lieu);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        initVues();
        afficherDonneesBase();
        chargerDetail();
    }

    private void initVues() {
        imgPhoto             = findViewById(R.id.imgPhotoDetail);
        tvNom                = findViewById(R.id.tvNomDetail);
        chipCategorie        = findViewById(R.id.chipCategorieDetail);
        ratingBar            = findViewById(R.id.ratingBarDetail);
        tvNote               = findViewById(R.id.tvNoteDetail);
        progressBar          = findViewById(R.id.progressBarDetail);
        tvErreurDetail       = findViewById(R.id.tvErreurDetail);

        sectionDescription   = findViewById(R.id.sectionDescription);
        tvDescription        = findViewById(R.id.tvDescriptionDetail);

        sectionHoraires      = findViewById(R.id.sectionHoraires);
        tvHoraires           = findViewById(R.id.tvHorairesDetail);

        sectionTarifs        = findViewById(R.id.sectionTarifs);
        tvTarifs             = findViewById(R.id.tvTarifsDetail);

        sectionAccessibilite = findViewById(R.id.sectionAccessibilite);
        tvAccessibilite      = findViewById(R.id.tvAccessibiliteDetail);

        sectionPhotos        = findViewById(R.id.sectionPhotos);
        galeriePhotos        = findViewById(R.id.galeriePhotos);

        controleurDetail     = new DetailLieuController(this);
    }

    // ── Données de base (Intent) ──────────────────────────────────────────

    private void afficherDonneesBase() {
        Intent i = getIntent();

        String nom = i.getStringExtra(EXTRA_NOM);
        tvNom.setText(nom != null ? nom : "Lieu inconnu");

        chargerPhoto(imgPhoto, i.getStringExtra(EXTRA_PHOTO), true);

        String cat = i.getStringExtra(EXTRA_CATEGORIE);
        chipCategorie.setText(cat != null && !cat.isEmpty() ? cat : "Non classé");

        int note = i.getIntExtra(EXTRA_NOTE, -1);
        if (note >= 0) {
            ratingBar.setRating(note);
            tvNote.setText(note + " / 5");
            ratingBar.setVisibility(View.VISIBLE);
        } else {
            ratingBar.setVisibility(View.GONE);
            tvNote.setText("Non noté");
        }
    }

    // ── Chargement du détail via API ──────────────────────────────────────

    private void chargerDetail() {
        int id = getIntent().getIntExtra(EXTRA_ID, -1);
        if (id == -1) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvErreurDetail.setVisibility(View.GONE);

        controleurDetail.recupererDetail(id, new DetailLieuController.CallbackDetail() {
            @Override
            public void onSucces(DetailLieux detail) {
                progressBar.setVisibility(View.GONE);
                afficherDetail(detail);
            }

            @Override
            public void onErreur(String messageErreur) {
                progressBar.setVisibility(View.GONE);
                tvErreurDetail.setText("Impossible de charger les détails");
                tvErreurDetail.setVisibility(View.VISIBLE);
            }
        });
    }

    private void afficherDetail(DetailLieux detail) {

        // Description
        afficherSection(sectionDescription, tvDescription, detail.getDescription());

        // Horaires
        afficherSection(sectionHoraires, tvHoraires, detail.getHoraires());

        // Tarif (Integer → "X €", masqué si null)
        if (detail.getFarif() != null) {
            tvTarifs.setText(detail.getFarif() + " €");
            sectionTarifs.setVisibility(View.VISIBLE);
        } else {
            sectionTarifs.setVisibility(View.GONE);
        }

        // Accessibilité
        afficherSection(sectionAccessibilite, tvAccessibilite, detail.getAccessibilite());

        // Photo supplémentaire (String = une URL unique)
        String photoSupp = detail.getPhotos();
        if (photoSupp != null && !photoSupp.trim().isEmpty()) {
            ajouterPhotoGalerie(photoSupp.trim());
            sectionPhotos.setVisibility(View.VISIBLE);
        } else {
            sectionPhotos.setVisibility(View.GONE);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void afficherSection(View section, TextView textView, String valeur) {
        if (valeur != null && !valeur.trim().isEmpty()) {
            textView.setText(valeur.trim());
            section.setVisibility(View.VISIBLE);
        } else {
            section.setVisibility(View.GONE);
        }
    }

    private void ajouterPhotoGalerie(String url) {
        galeriePhotos.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int taillePX  = (int) (200 * density);

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(taillePX, taillePX);
        img.setLayoutParams(params);
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setClipToOutline(true);
        img.setBackgroundResource(R.drawable.bg_photo_arrondie);
        chargerPhoto(img, url, false);
        galeriePhotos.addView(img);
    }

    private void chargerPhoto(ImageView imageView, String url, boolean avecTransition) {
        if (url != null && !url.isEmpty()) {
            RequestBuilder<Drawable> builder = Glide.with(this)
                    .load(url)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .centerCrop();
            if (avecTransition) {
                builder.transition(DrawableTransitionOptions.withCrossFade()).into(imageView);
            } else {
                builder.into(imageView);
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Factory method ────────────────────────────────────────────────────

    public static Intent creerIntent(Context contexte, Lieu lieu) {
        Intent intent = new Intent(contexte, DetailLieuActivity.class);
        intent.putExtra(EXTRA_ID,        lieu.getId());
        intent.putExtra(EXTRA_NOM,       lieu.getNom());
        intent.putExtra(EXTRA_PHOTO,     lieu.getPhoto());
        intent.putExtra(EXTRA_CATEGORIE, lieu.getCategorie());
        if (lieu.getNoteMoyen() != null) {
            intent.putExtra(EXTRA_NOTE, (int) lieu.getNoteMoyen());
        }
        return intent;
    }
}