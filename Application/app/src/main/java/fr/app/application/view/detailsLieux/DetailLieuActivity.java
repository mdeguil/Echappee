package fr.app.application.view.detailsLieux;

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

import java.util.Locale;

import fr.app.application.R;
import fr.app.application.controller.DetailLieuController;
import fr.app.application.controller.MeteoController;
import fr.app.application.model.DetailLieux;
import fr.app.application.model.Lieu;
import fr.app.application.model.Meteo;

/**
 * Affiche le détail complet d'un lieu touristique.
 *
 * Données transmises par Intent (depuis ListeLieuxActivity) :
 *   - id, nom, photo principale, catégorie, note, latitude, longitude
 *
 * Données chargées depuis l'API détail (GET /api/lieux/{id}) :
 *   - description, horaires, tarif, accessibilite, photos
 *
 * Données chargées depuis OpenWeather (GET /onecall/timemachine) :
 *   - température, ressenti, humidité, vent, description, icône
 */
public class DetailLieuActivity extends AppCompatActivity {

    // ── Clés des extras ───────────────────────────────────────────────────
    public static final String EXTRA_ID        = "extra_id";
    public static final String EXTRA_NOM       = "extra_nom";
    public static final String EXTRA_PHOTO     = "extra_photo";
    public static final String EXTRA_CATEGORIE = "extra_categorie";
    public static final String EXTRA_NOTE      = "extra_note";
    public static final String EXTRA_LATITUDE  = "extra_latitude";   // ← nouveau
    public static final String EXTRA_LONGITUDE = "extra_longitude";  // ← nouveau

    // ── Vues détail ───────────────────────────────────────────────────────
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

    // ── Vues météo ────────────────────────────────────────────────────────
    private View        sectionMeteo;
    private ImageView   imgMeteoIcone;
    private TextView    tvMeteoDescription;
    private TextView    tvMeteoTemperature;
    private TextView    tvMeteoRessenti;
    private TextView    tvMeteoHumidite;
    private TextView    tvMeteoVent;
    private ProgressBar progressBarMeteo;
    private TextView    tvErreurMeteo;

    // ── Controllers ───────────────────────────────────────────────────────
    private DetailLieuController controleurDetail;
    private MeteoController      controleurMeteo;

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
        chargerMeteo();   // ← appel météo
    }

    private void initVues() {
        // Vues existantes
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

        // Vues météo
        sectionMeteo         = findViewById(R.id.sectionMeteo);
        imgMeteoIcone        = findViewById(R.id.imgMeteoIcone);
        tvMeteoDescription   = findViewById(R.id.tvMeteoDescription);
        tvMeteoTemperature   = findViewById(R.id.tvMeteoTemperature);
        tvMeteoRessenti      = findViewById(R.id.tvMeteoRessenti);
        tvMeteoHumidite      = findViewById(R.id.tvMeteoHumidite);
        tvMeteoVent          = findViewById(R.id.tvMeteoVent);
        progressBarMeteo     = findViewById(R.id.progressBarMeteo);
        tvErreurMeteo        = findViewById(R.id.tvErreurMeteo);

        // Controllers
        controleurDetail     = new DetailLieuController(this);
        controleurMeteo      = new MeteoController(this);
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

        // Tarif
        if (detail.getTarif() != 0) {
            tvTarifs.setText(detail.getTarif() + " €");
            sectionTarifs.setVisibility(View.VISIBLE);
        } else {
            tvTarifs.setText("Gratuit");
            sectionTarifs.setVisibility(View.VISIBLE);
        }

        // Accessibilité
        afficherSection(sectionAccessibilite, tvAccessibilite, detail.getAccessibilite());

        // Photo supplémentaire
        String photoSupp = detail.getPhotos();
        if (photoSupp != null && !photoSupp.trim().isEmpty()) {
            ajouterPhotoGalerie(photoSupp.trim());
            sectionPhotos.setVisibility(View.VISIBLE);
        } else {
            sectionPhotos.setVisibility(View.GONE);
        }
    }

    // ── Chargement et affichage de la météo ───────────────────────────────

    private void chargerMeteo() {
        Intent i         = getIntent();
        double latitude  = i.getDoubleExtra(EXTRA_LATITUDE,  Double.MAX_VALUE);
        double longitude = i.getDoubleExtra(EXTRA_LONGITUDE, Double.MAX_VALUE);

        // Si les coordonnées ne sont pas disponibles, on masque la section
        if (latitude == Double.MAX_VALUE || longitude == Double.MAX_VALUE) {
            sectionMeteo.setVisibility(View.GONE);
            return;
        }

        // Afficher la section et le loader
        sectionMeteo.setVisibility(View.VISIBLE);
        progressBarMeteo.setVisibility(View.VISIBLE);
        tvErreurMeteo.setVisibility(View.GONE);

        // Masquer la card le temps du chargement
        imgMeteoIcone.setVisibility(View.INVISIBLE);
        tvMeteoDescription.setVisibility(View.INVISIBLE);
        tvMeteoTemperature.setVisibility(View.INVISIBLE);
        tvMeteoRessenti.setVisibility(View.INVISIBLE);
        tvMeteoHumidite.setVisibility(View.INVISIBLE);
        tvMeteoVent.setVisibility(View.INVISIBLE);

        controleurMeteo.recupererMeteo(latitude, longitude, new MeteoController.CallbackMeteo() {
            @Override
            public void onSucces(Meteo meteo) {
                progressBarMeteo.setVisibility(View.GONE);
                afficherMeteo(meteo);
            }

            @Override
            public void onErreur(String messageErreur) {
                progressBarMeteo.setVisibility(View.GONE);
                tvErreurMeteo.setText("Météo indisponible");
                tvErreurMeteo.setVisibility(View.VISIBLE);
            }
        });
    }

    private void afficherMeteo(Meteo meteo) {
        Meteo.DataPoint point = meteo.getPremierPoint();
        if (point == null) return;

        // Température et ressenti (arrondi à l'entier)
        int tempC     = (int) Math.round(point.getTemp());
        int ressentiC = (int) Math.round(point.getFeelsLike());

        tvMeteoTemperature.setText(tempC + " °C");
        tvMeteoRessenti.setText("Ressenti : " + ressentiC + " °C");
        tvMeteoHumidite.setText("💧 Humidité : " + point.getHumidity() + " %");
        tvMeteoVent.setText(String.format(Locale.getDefault(),
                "🌬 Vent : %.1f m/s", point.getWindSpeed()));

        // Description de la condition
        Meteo.WeatherCondition condition = point.getConditionPrincipale();
        if (condition != null) {
            // Première lettre en majuscule
            String desc = condition.getDescription();
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase(Locale.FRENCH) + desc.substring(1);
            }
            tvMeteoDescription.setText(desc);

            // Icône météo via Glide
            Glide.with(this)
                    .load(condition.getIconUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imgMeteoIcone);
        }

        // Rendre toutes les vues visibles
        imgMeteoIcone.setVisibility(View.VISIBLE);
        tvMeteoDescription.setVisibility(View.VISIBLE);
        tvMeteoTemperature.setVisibility(View.VISIBLE);
        tvMeteoRessenti.setVisibility(View.VISIBLE);
        tvMeteoHumidite.setVisibility(View.VISIBLE);
        tvMeteoVent.setVisibility(View.VISIBLE);
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

    /**
     * Crée l'Intent avec toutes les données nécessaires, y compris les coordonnées
     * GPS pour l'appel météo.
     */
    public static Intent creerIntent(Context contexte, Lieu lieu) {
        Intent intent = new Intent(contexte, DetailLieuActivity.class);
        intent.putExtra(EXTRA_ID,        lieu.getId());
        intent.putExtra(EXTRA_NOM,       lieu.getNom());
        intent.putExtra(EXTRA_PHOTO,     lieu.getPhoto());
        intent.putExtra(EXTRA_CATEGORIE, lieu.getCategorie());
        if (lieu.getNoteMoyen() != null) {
            intent.putExtra(EXTRA_NOTE, (int) lieu.getNoteMoyen());
        }
        // Coordonnées pour la météo
        if (lieu.getLatitude() != null) {
            intent.putExtra(EXTRA_LATITUDE,  lieu.getLatitude());
        }
        if (lieu.getLongitude() != null) {
            intent.putExtra(EXTRA_LONGITUDE, lieu.getLongitude());
        }
        return intent;
    }
}