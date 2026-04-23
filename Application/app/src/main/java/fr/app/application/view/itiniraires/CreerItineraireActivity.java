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
import fr.app.application.model.Itiniraire;
import fr.app.application.model.Lieu;
import fr.app.application.utils.DirectionsUtils;
import fr.app.application.view.adapter.LieuSelectionneAdapter;

public class CreerItineraireActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int    CODE_PERMISSION = 1002;
    private static final LatLng CENTRE_CHARENTE = new LatLng(45.6466, 0.1560);
    private static final float  ZOOM_INITIAL    = 9f;
    private GoogleMap           carteMaps;
    private ProgressBar         barreChargement;
    private TextView            tvDureeCalculee;
    private TextView            tvAucunLieu;
    private RecyclerView        recyclerLieuxSelectionnes;
    private MaterialButton      btnCreer;
    private final List<Lieu>            tousLesLieux         = new ArrayList<>();
    private final List<Lieu>            lieuxSelectionnes    = new ArrayList<>();
    private final Map<Integer, Marker>  marqueurParId        = new HashMap<>();
    private final Map<Integer, Boolean> lieuxSelectionnesMap = new HashMap<>();
    private Integer dureeCalculeeMinutes = null;
    private LieuSelectionneAdapter adaptateur;
    private LieuController         lieuController;
    private ItineraireController   itineraireController;

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
        barreChargement           = findViewById(R.id.barreChargementCreer);
        tvDureeCalculee           = findViewById(R.id.tvDureeCalculee);
        tvAucunLieu               = findViewById(R.id.tvAucunLieuSelectionne);
        recyclerLieuxSelectionnes = findViewById(R.id.recyclerLieuxSelectionnes);
        btnCreer                  = findViewById(R.id.btnCreerItineraire);

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
                for (Lieu lieu : lieux) ajouterMarqueur(lieu);
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
        mettreAJourBouton();
        mettreAJourMessageVide();

        recalculerDuree();
    }

    private void retirerLieu(Lieu lieu) {
        retirerLieuParId(lieu.getId());
        Marker marqueur = marqueurParId.get(lieu.getId());
        if (marqueur != null) {
            marqueur.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            marqueur.setSnippet("Appuyer pour sélectionner");
        }
        adaptateur.notifyDataSetChanged();
        mettreAJourBouton();
        mettreAJourMessageVide();

        recalculerDuree();
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

    private void recalculerDuree() {
        if (lieuxSelectionnes.isEmpty()) {
            dureeCalculeeMinutes = null;
            mettreAJourDuree();
            return;
        }

        if (lieuxSelectionnes.size() == 1) {
            dureeCalculeeMinutes = 0;
            mettreAJourDuree();
            return;
        }

        tvDureeCalculee.setText("Calcul du trajet en cours...");
        btnCreer.setEnabled(false);

        DirectionsUtils.calculerDureeAPied(
                this,
                lieuxSelectionnes,
                new DirectionsUtils.CallbackDuree() {
                    @Override
                    public void onSucces(int dureeEnMinutes) {
                        dureeCalculeeMinutes = dureeEnMinutes;
                        mettreAJourDuree();
                        btnCreer.setEnabled(!lieuxSelectionnes.isEmpty());
                    }

                    @Override
                    public void onErreur(String messageErreur) {
                        dureeCalculeeMinutes = 0;
                        tvDureeCalculee.setText("Durée indisponible (Directions API)");
                        btnCreer.setEnabled(!lieuxSelectionnes.isEmpty());
                        Toast.makeText(CreerItineraireActivity.this,
                                "Impossible de calculer le trajet : " + messageErreur,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void mettreAJourDuree() {
        if (lieuxSelectionnes.isEmpty()) {
            tvDureeCalculee.setText("Sélectionnez des lieux sur la carte");
            return;
        }

        if (lieuxSelectionnes.size() == 1) {
            tvDureeCalculee.setText("Sélectionnez au moins un autre lieu");
            return;
        }

        if (dureeCalculeeMinutes == null) {
            tvDureeCalculee.setText("Calcul du trajet en cours...");
            return;
        }

        if (dureeCalculeeMinutes == 0) {
            tvDureeCalculee.setText("Durée de trajet : indisponible");
            return;
        }

        int heures  = dureeCalculeeMinutes / 60;
        int minutes = dureeCalculeeMinutes % 60;

        String dureeTexte;
        if (heures > 0) {
            dureeTexte = "🚶 Trajet à pied : " + heures + "h" + (minutes > 0 ? minutes + "min" : "");
        } else {
            dureeTexte = "🚶 Trajet à pied : " + dureeCalculeeMinutes + " min";
        }
        tvDureeCalculee.setText(dureeTexte);
    }

    private void mettreAJourBouton() {
        if (dureeCalculeeMinutes != null || lieuxSelectionnes.size() <= 1) {
            btnCreer.setEnabled(!lieuxSelectionnes.isEmpty());
        }
    }

    private void mettreAJourMessageVide() {
        tvAucunLieu.setVisibility(lieuxSelectionnes.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerLieuxSelectionnes.setVisibility(lieuxSelectionnes.isEmpty() ? View.GONE : View.VISIBLE);
    }


    private void creerItineraire() {
        if (lieuxSelectionnes.isEmpty()) return;

        btnCreer.setEnabled(false);
        barreChargement.setVisibility(View.VISIBLE);

        int dureTotal = dureeCalculeeMinutes != null ? dureeCalculeeMinutes : 0;

        List<Integer> idLieux = new ArrayList<>();
        for (Lieu lieu : lieuxSelectionnes) {
            idLieux.add(lieu.getId());
        }

        itineraireController.creerItineraire(dureTotal, idLieux,
                new ItineraireController.CallbackCreerItineraire() {
                    @Override
                    public void onSucces(Itiniraire itineraire) {
                        barreChargement.setVisibility(View.GONE);
                        String dureeAffichee = dureTotal > 0
                                ? formatDuree(dureTotal)
                                : "durée inconnue";
                        Toast.makeText(CreerItineraireActivity.this,
                                "Itinéraire créé ! " + lieuxSelectionnes.size()
                                        + " lieux — " + dureeAffichee + " à pied",
                                Toast.LENGTH_SHORT).show();
                        finish();
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

    private String formatDuree(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        if (h > 0) return h + "h" + (m > 0 ? m + "min" : "");
        return m + " min";
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