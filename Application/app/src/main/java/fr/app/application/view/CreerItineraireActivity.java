package fr.app.application.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.app.application.R;
import fr.app.application.controller.ItineraireController;
import fr.app.application.controller.LieuController;
import fr.app.application.model.Lieu;

/**
 * Permet à l'utilisateur de créer un itinéraire en :
 *   1. Tapant sur des marqueurs sur la carte pour sélectionner les lieux
 *   2. La durée est calculée automatiquement (30 min par lieu)
 *   3. Appuyant sur "Créer l'itinéraire" pour envoyer le POST
 */
public class CreerItineraireActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Durée estimée par lieu en minutes
    private static final int    DUREE_PAR_LIEU   = 30;
    private static final int    CODE_PERMISSION  = 1002;
    private static final LatLng CENTRE_CHARENTE  = new LatLng(45.6466, 0.1560);
    private static final float  ZOOM_INITIAL     = 9f;

    // ── Vues ─────────────────────────────────────────────────────────────
    private GoogleMap            carteMaps;
    private ProgressBar          barreChargement;
    private TextView             tvDureeCalculee;
    private TextView             tvAucunLieu;
    private RecyclerView         recyclerLieuxSelectionnes;
    private MaterialButton       btnCreer;

    // ── Données ──────────────────────────────────────────────────────────
    private final List<Lieu>           tousLesLieux         = new ArrayList<>();
    private final List<Lieu>           lieuxSelectionnes    = new ArrayList<>();
    private final Map<Integer, Marker> marqueurParId        = new HashMap<>();
    private final Map<Integer, Boolean>lieuxSelectionnesMap = new HashMap<>();

    // ── Adapters & controllers ────────────────────────────────────────────
    private LieuSelectionneAdapter  adaptateur;
    private LieuController          lieuController;
    private ItineraireController    itineraireController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creer_itineraire);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Créer un itinéraire");
        }

        initVues();
        initCarte();

        lieuController       = new LieuController(this);
        itineraireController = new ItineraireController(this);
    }

    // ── Initialisation ────────────────────────────────────────────────────

    private void initVues() {
        barreChargement          = findViewById(R.id.barreChargementCreer);
        tvDureeCalculee          = findViewById(R.id.tvDureeCalculee);
        tvAucunLieu              = findViewById(R.id.tvAucunLieuSelectionne);
        recyclerLieuxSelectionnes = findViewById(R.id.recyclerLieuxSelectionnes);
        btnCreer                 = findViewById(R.id.btnCreerItineraire);

        recyclerLieuxSelectionnes.setLayoutManager(new LinearLayoutManager(this));
        adaptateur = new LieuSelectionneAdapter(this, lieuxSelectionnes, this::retirerLieu);
        recyclerLieuxSelectionnes.setAdapter(adaptateur);

        btnCreer.setEnabled(false);
        btnCreer.setOnClickListener(v -> creerItineraire());
        mettreAJourDuree();
    }

    private void initCarte() {
        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarteCreer);
        if (fragmentCarte != null) {
            fragmentCarte.getMapAsync(this);
        }
    }

    // ── Carte ─────────────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        carteMaps = googleMap;
        carteMaps.getUiSettings().setZoomControlsEnabled(true);
        carteMaps.getUiSettings().setCompassEnabled(true);
        carteMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE_CHARENTE, ZOOM_INITIAL));

        // Clic sur un marqueur → sélectionner/désélectionner le lieu
        carteMaps.setOnMarkerClickListener(marqueur -> {
            Integer idLieu = (Integer) marqueur.getTag();
            if (idLieu != null) {
                basculerSelectionLieu(idLieu, marqueur);
            }
            return true;
        });

        activerPositionUtilisateur();
        chargerLieuxSurCarte();
    }

    private void chargerLieuxSurCarte() {
        barreChargement.setVisibility(View.VISIBLE);

        lieuController.recupererLieux(new LieuController.CallbackLieux() {
            @Override
            public void onSucces(List<Lieu> lieux) {
                barreChargement.setVisibility(View.GONE);
                tousLesLieux.clear();
                tousLesLieux.addAll(lieux);

                for (Lieu lieu : lieux) {
                    ajouterMarqueur(lieu);
                }
            }

            @Override
            public void onErreur(String messageErreur) {
                barreChargement.setVisibility(View.GONE);
                Toast.makeText(CreerItineraireActivity.this,
                        "Erreur chargement lieux : " + messageErreur,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ajouterMarqueur(Lieu lieu) {
        if (carteMaps == null) return;
        if (lieu.getLatitude() == null || lieu.getLongitude() == null) return;

        LatLng position = new LatLng(lieu.getLatitude(), lieu.getLongitude());

        Marker marqueur = carteMaps.addMarker(new MarkerOptions()
                .position(position)
                .title(lieu.getNom())
                .snippet("Appuyer pour sélectionner")
                // Marqueur gris par défaut = non sélectionné
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if (marqueur != null) {
            marqueur.setTag(lieu.getId());
            marqueurParId.put(lieu.getId(), marqueur);
        }
    }

    // ── Sélection des lieux ───────────────────────────────────────────────

    private void basculerSelectionLieu(int idLieu, Marker marqueur) {
        boolean estSelectionne = Boolean.TRUE.equals(lieuxSelectionnesMap.get(idLieu));

        if (estSelectionne) {
            // Désélectionner
            retirerLieuParId(idLieu);
            marqueur.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            marqueur.setSnippet("Appuyer pour sélectionner");
        } else {
            // Sélectionner
            Lieu lieu = trouverLieuParId(idLieu);
            if (lieu != null) {
                lieuxSelectionnes.add(lieu);
                lieuxSelectionnesMap.put(idLieu, true);
                // Marqueur vert = sélectionné
                marqueur.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                marqueur.setSnippet("✓ Sélectionné - position " + lieuxSelectionnes.size());
                marqueur.showInfoWindow();
            }
        }

        adaptateur.notifyDataSetChanged();
        mettreAJourDuree();
        mettreAJourBouton();
        mettreAJourMessageVide();
    }

    private void retirerLieu(Lieu lieu) {
        retirerLieuParId(lieu.getId());
        // Remettre le marqueur en gris
        Marker marqueur = marqueurParId.get(lieu.getId());
        if (marqueur != null) {
            marqueur.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            marqueur.setSnippet("Appuyer pour sélectionner");
        }
        adaptateur.notifyDataSetChanged();
        mettreAJourDuree();
        mettreAJourBouton();
        mettreAJourMessageVide();
    }

    private void retirerLieuParId(int idLieu) {
        lieuxSelectionnes.removeIf(l -> l.getId() == idLieu);
        lieuxSelectionnesMap.remove(idLieu);
    }

    private Lieu trouverLieuParId(int idLieu) {
        for (Lieu lieu : tousLesLieux) {
            if (lieu.getId() == idLieu) return lieu;
        }
        return null;
    }

    // ── Durée & UI ────────────────────────────────────────────────────────

    private void mettreAJourDuree() {
        int dureeMinutes = lieuxSelectionnes.size() * DUREE_PAR_LIEU;
        int heures  = dureeMinutes / 60;
        int minutes = dureeMinutes % 60;

        String dureeTexte;
        if (lieuxSelectionnes.isEmpty()) {
            dureeTexte = "Sélectionnez des lieux sur la carte";
        } else if (heures > 0) {
            dureeTexte = "Durée estimée : " + heures + "h" + (minutes > 0 ? minutes + "min" : "");
        } else {
            dureeTexte = "Durée estimée : " + dureeMinutes + " min";
        }
        tvDureeCalculee.setText(dureeTexte);
    }

    private void mettreAJourBouton() {
        btnCreer.setEnabled(!lieuxSelectionnes.isEmpty());
    }

    private void mettreAJourMessageVide() {
        tvAucunLieu.setVisibility(lieuxSelectionnes.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerLieuxSelectionnes.setVisibility(lieuxSelectionnes.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ── Création de l'itinéraire ──────────────────────────────────────────

    private void creerItineraire() {
        if (lieuxSelectionnes.isEmpty()) return;

        btnCreer.setEnabled(false);
        barreChargement.setVisibility(View.VISIBLE);

        int dureTotal = lieuxSelectionnes.size() * DUREE_PAR_LIEU;

        List<Integer> idLieux = new ArrayList<>();
        for (Lieu lieu : lieuxSelectionnes) {
            idLieux.add(lieu.getId());
        }

        itineraireController.creerItineraire(dureTotal, idLieux,
                new ItineraireController.CallbackCreerItineraire() {
                    @Override
                    public void onSucces(fr.app.application.model.Itineraire itineraire) {
                        barreChargement.setVisibility(View.GONE);
                        Toast.makeText(CreerItineraireActivity.this,
                                "Itinéraire créé avec " + lieuxSelectionnes.size() + " lieux !",
                                Toast.LENGTH_SHORT).show();
                        finish(); // retour à l'écran précédent
                    }

                    @Override
                    public void onErreur(String messageErreur) {
                        barreChargement.setVisibility(View.GONE);
                        btnCreer.setEnabled(true);
                        Toast.makeText(CreerItineraireActivity.this,
                                "Erreur : " + messageErreur,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── GPS ───────────────────────────────────────────────────────────────

    private void activerPositionUtilisateur() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            carteMaps.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int codeRequete,
                                           @NonNull String[] permissions,
                                           @NonNull int[] resultats) {
        super.onRequestPermissionsResult(codeRequete, permissions, resultats);
        if (codeRequete == CODE_PERMISSION
                && resultats.length > 0
                && resultats[0] == PackageManager.PERMISSION_GRANTED
                && carteMaps != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                carteMaps.setMyLocationEnabled(true);
            }
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
}
