package fr.app.application.view.itiniraires;

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
import fr.app.application.view.adapter.LieuSelectionneAdapter;

public class CreerItineraireActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Durée estimée par lieu en minutes
    private static final int    DUREE_PAR_LIEU   = 30;
    private static final int    CODE_PERMISSION  = 1002;
    private static final LatLng CENTRE_CHARENTE  = new LatLng(45.6466, 0.1560);
    private static final float  ZOOM_INITIAL     = 9f;
    private GoogleMap            carteMaps;
    private ProgressBar          barreChargement;
    private TextView             tvDureeCalculee;
    private TextView             tvAucunLieu;
    private RecyclerView         recyclerLieuxSelectionnes;
    private MaterialButton       btnCreer;
    private final List<Lieu>           tousLesLieux         = new ArrayList<>();
    private final List<Lieu>           lieuxSelectionnes    = new ArrayList<>();
    private final Map<Integer, Marker> marqueurParId        = new HashMap<>();
    private final Map<Integer, Boolean>lieuxSelectionnesMap = new HashMap<>();
    private LieuSelectionneAdapter adaptateur;
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

    /**
     * Initialise la carte et configure l'écouteur de clic sur les marqueurs.
     * * Chaque clic sur un marqueur déclenche la méthode {@link #basculerSelectionLieu}.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        carteMaps = googleMap;
        carteMaps.getUiSettings().setZoomControlsEnabled(true);
        carteMaps.getUiSettings().setCompassEnabled(true);
        carteMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE_CHARENTE, ZOOM_INITIAL));
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
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if (marqueur != null) {
            marqueur.setTag(lieu.getId());
            marqueurParId.put(lieu.getId(), marqueur);
        }
    }

    /**
     * Gère l'ajout ou le retrait d'un lieu dans l'itinéraire en cours de création.
     *
     * @param idLieu   L'identifiant du lieu cliqué.
     * @param marqueur La référence visuelle du marqueur sur la carte.
     */
    private void basculerSelectionLieu(int idLieu, Marker marqueur) {
        boolean estSelectionne = Boolean.TRUE.equals(lieuxSelectionnesMap.get(idLieu));

        if (estSelectionne) {
            retirerLieuParId(idLieu);
            marqueur.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            marqueur.setSnippet("Appuyer pour sélectionner");
        } else {
            Lieu lieu = trouverLieuParId(idLieu);
            if (lieu != null) {
                lieuxSelectionnes.add(lieu);
                lieuxSelectionnesMap.put(idLieu, true);
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

    /**
     * Supprime un lieu de la sélection actuelle.
     *
     * @param lieu L'objet Lieu à retirer.
     */
    private void retirerLieu(Lieu lieu) {
        retirerLieuParId(lieu.getId());
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

    /**
     * Calcule et affiche la durée estimée du parcours en fonction du nombre
     * de lieux sélectionnés (basé sur une constante de temps par site).
     * * Formate le texte pour afficher des heures ou uniquement des minutes.
     */
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

    /**
     * Compile les données de l'itinéraire
     */
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
