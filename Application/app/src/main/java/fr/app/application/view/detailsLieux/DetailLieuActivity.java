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

import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fr.app.application.R;
import fr.app.application.controller.DetailLieuController;
import fr.app.application.controller.MeteoController;
import fr.app.application.controller.VisiteController;
import fr.app.application.model.Commentaire;
import fr.app.application.model.DetailLieux;
import fr.app.application.model.Lieu;
import fr.app.application.model.Meteo;
import fr.app.application.view.adapter.CommentaireAdapter;


public class DetailLieuActivity extends AppCompatActivity {

    public static final String EXTRA_ID        = "extra_id";
    public static final String EXTRA_NOM       = "extra_nom";
    public static final String EXTRA_PHOTO     = "extra_photo";
    public static final String EXTRA_CATEGORIE = "extra_categorie";
    public static final String EXTRA_LATITUDE  = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";

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

    private View        sectionMeteo;
    private ImageView   imgMeteoIcone;
    private TextView    tvMeteoDescription;
    private TextView    tvMeteoTemperature;
    private TextView    tvMeteoRessenti;
    private TextView    tvMeteoHumidite;
    private TextView    tvMeteoVent;
    private ProgressBar progressBarMeteo;
    private TextView    tvErreurMeteo;

    private DetailLieuController controleurDetail;
    private MeteoController      controleurMeteo;

    private View                 sectionCommentaires;
    private TextView             tvNoteMoyenne;
    private ProgressBar          progressBarCommentaires;
    private TextView             tvErreurCommentaires;
    private RecyclerView recyclerCommentaires;
    private CommentaireAdapter commentaireAdapter;
    private VisiteController visiteController;


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
        chargerMeteo();
        chargerCommentaires();
    }

    private void initVues() {
        imgPhoto             = findViewById(R.id.imgPhotoDetail);
        tvNom                = findViewById(R.id.tvNomDetail);
        chipCategorie        = findViewById(R.id.chipCategorieDetail);
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

        sectionMeteo         = findViewById(R.id.sectionMeteo);
        imgMeteoIcone        = findViewById(R.id.imgMeteoIcone);
        tvMeteoDescription   = findViewById(R.id.tvMeteoDescription);
        tvMeteoTemperature   = findViewById(R.id.tvMeteoTemperature);
        tvMeteoRessenti      = findViewById(R.id.tvMeteoRessenti);
        tvMeteoHumidite      = findViewById(R.id.tvMeteoHumidite);
        tvMeteoVent          = findViewById(R.id.tvMeteoVent);
        progressBarMeteo     = findViewById(R.id.progressBarMeteo);
        tvErreurMeteo        = findViewById(R.id.tvErreurMeteo);

        controleurDetail     = new DetailLieuController(this);
        controleurMeteo      = new MeteoController(this);
        visiteController = new VisiteController(this);

        sectionCommentaires     = findViewById(R.id.sectionCommentaires);
        tvNoteMoyenne           = findViewById(R.id.tvNoteMoyenne);
        progressBarCommentaires = findViewById(R.id.progressBarCommentaires);
        tvErreurCommentaires    = findViewById(R.id.tvErreurCommentaires);
        recyclerCommentaires    = findViewById(R.id.recyclerCommentaires);

        commentaireAdapter = new CommentaireAdapter();
        recyclerCommentaires.setLayoutManager(new LinearLayoutManager(this));
        recyclerCommentaires.setAdapter(commentaireAdapter);
        recyclerCommentaires.setNestedScrollingEnabled(false);
    }


    private void afficherDonneesBase() {
        Intent i = getIntent();

        String nom = i.getStringExtra(EXTRA_NOM);
        tvNom.setText(nom != null ? nom : "Lieu inconnu");

        chargerPhoto(imgPhoto, i.getStringExtra(EXTRA_PHOTO), true);

        String cat = i.getStringExtra(EXTRA_CATEGORIE);
        chipCategorie.setText(cat != null && !cat.isEmpty() ? cat : "Non classé");

    }

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

        afficherSection(sectionDescription, tvDescription, detail.getDescription());

        afficherSection(sectionHoraires, tvHoraires, detail.getHoraires());

        if (detail.getTarif() != 0) {
            tvTarifs.setText(detail.getTarif() + " €");
            sectionTarifs.setVisibility(View.VISIBLE);
        } else {
            tvTarifs.setText("Gratuit");
            sectionTarifs.setVisibility(View.VISIBLE);
        }

        afficherSection(sectionAccessibilite, tvAccessibilite, detail.getAccessibilite());

        String photoSupp = detail.getPhotos();
        if (photoSupp != null && !photoSupp.trim().isEmpty()) {
            ajouterPhotoGalerie(photoSupp.trim());
            sectionPhotos.setVisibility(View.VISIBLE);
        } else {
            sectionPhotos.setVisibility(View.GONE);
        }
    }


    private void chargerMeteo() {
        Intent i         = getIntent();
        double latitude  = i.getDoubleExtra(EXTRA_LATITUDE,  Double.MAX_VALUE);
        double longitude = i.getDoubleExtra(EXTRA_LONGITUDE, Double.MAX_VALUE);

        if (latitude == Double.MAX_VALUE || longitude == Double.MAX_VALUE) {
            sectionMeteo.setVisibility(View.GONE);
            return;
        }

        sectionMeteo.setVisibility(View.VISIBLE);
        progressBarMeteo.setVisibility(View.VISIBLE);
        tvErreurMeteo.setVisibility(View.GONE);

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

        int tempC     = (int) Math.round(point.getTemp());
        int ressentiC = (int) Math.round(point.getFeelsLike());

        tvMeteoTemperature.setText(tempC + " °C");
        tvMeteoRessenti.setText("Ressenti : " + ressentiC + " °C");
        tvMeteoHumidite.setText("💧 Humidité : " + point.getHumidity() + " %");
        tvMeteoVent.setText(String.format(Locale.getDefault(),
                "🌬 Vent : %.1f m/s", point.getWindSpeed()));

        Meteo.WeatherCondition condition = point.getConditionPrincipale();
        if (condition != null) {
            String desc = condition.getDescription();
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase(Locale.FRENCH) + desc.substring(1);
            }
            tvMeteoDescription.setText(desc);

            Glide.with(this)
                    .load(condition.getIconUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(imgMeteoIcone);
        }

        imgMeteoIcone.setVisibility(View.VISIBLE);
        tvMeteoDescription.setVisibility(View.VISIBLE);
        tvMeteoTemperature.setVisibility(View.VISIBLE);
        tvMeteoRessenti.setVisibility(View.VISIBLE);
        tvMeteoHumidite.setVisibility(View.VISIBLE);
        tvMeteoVent.setVisibility(View.VISIBLE);
    }


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

    private void chargerCommentaires() {
        int lieuId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (lieuId == -1) return;

        sectionCommentaires.setVisibility(View.VISIBLE);
        progressBarCommentaires.setVisibility(View.VISIBLE);
        tvErreurCommentaires.setVisibility(View.GONE);

        visiteController.recupererCommentairesDuLieu(lieuId, new VisiteController.CallbackCommentaires() {
            @Override
            public void onSucces(List<Commentaire> commentaires) {
                progressBarCommentaires.setVisibility(View.GONE);

                if (commentaires == null || commentaires.isEmpty()) {
                    tvErreurCommentaires.setText("Aucun avis pour ce lieu.");
                    tvErreurCommentaires.setVisibility(View.VISIBLE);
                    return;
                }

                commentaireAdapter.setCommentaires(commentaires);

                // Calcul de la note moyenne
                double somme = 0;
                for (Commentaire c : commentaires) somme += c.getNote();
                double moyenne = somme / commentaires.size();
                tvNoteMoyenne.setText(String.format(Locale.FRENCH,
                        "★ %.1f  (%d)", moyenne, commentaires.size()));
            }

            @Override
            public void onErreur(String messageErreur) {
                progressBarCommentaires.setVisibility(View.GONE);
                tvErreurCommentaires.setText("Avis indisponibles");
                tvErreurCommentaires.setVisibility(View.VISIBLE);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


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
        if (lieu.getLatitude() != null) {
            intent.putExtra(EXTRA_LATITUDE,  lieu.getLatitude());
        }
        if (lieu.getLongitude() != null) {
            intent.putExtra(EXTRA_LONGITUDE, lieu.getLongitude());
        }
        return intent;
    }
}