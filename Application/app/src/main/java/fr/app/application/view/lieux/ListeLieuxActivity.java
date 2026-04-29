package fr.app.application.view.lieux;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.app.application.R;
import fr.app.application.controller.LieuController;
import fr.app.application.model.Lieu;
import fr.app.application.utils.BDD.AppDatabase;
import fr.app.application.utils.NetworkMonitor;
import fr.app.application.view.adapter.LieuAdapter;
import fr.app.application.view.itiniraires.CreerItineraireActivity;
import fr.app.application.view.itiniraires.ItineraireActivity;
import fr.app.application.view.visite.HistoriqueVisiteActivity;

public class ListeLieuxActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int    CODE_PERMISSION = 1001;
    private static final LatLng CENTRE_CHARENTE = new LatLng(45.6466, 0.1560);
    private static final float  ZOOM_INITIAL    = 9f;

    private GoogleMap   carteMaps;
    private LieuAdapter adaptateur;
    private ProgressBar barreChargement;
    private List<Lieu>  listeLieux         = new ArrayList<>();
    private List<Lieu>  listeLieuxComplete = new ArrayList<>();
    private boolean     lieuxDejaCharges   = false;

    private Map<Integer, Marker> marqueurParId = new HashMap<>();
    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;

    private LieuController lieuController;
    private AppDatabase    db;

    private final NetworkMonitor.Observer networkObserver = new NetworkMonitor.Observer() {
        @Override
        public void onConnexionRetablie() {
            configurerBoutons();
            chargerLieux();
            Toast.makeText(ListeLieuxActivity.this,
                    "Connexion rétablie", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnexionPerdue() {
            configurerBoutons();
            Toast.makeText(ListeLieuxActivity.this,
                    "Connexion perdue — mode hors ligne", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_lieux);

        db             = AppDatabase.getDatabase(this);
        lieuController = new LieuController(this);

        barreChargement    = findViewById(R.id.barreChargement);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        configurerBoutons();

        RecyclerView recyclerLieux = findViewById(R.id.recyclerLieux);
        recyclerLieux.setLayoutManager(new LinearLayoutManager(this));
        adaptateur = new LieuAdapter(this, listeLieux, this::centrerCarteOnLieu);
        recyclerLieux.setAdapter(adaptateur);

        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarte);
        if (fragmentCarte != null) fragmentCarte.getMapAsync(this);

        EditText champRecherche = findViewById(R.id.champRecherche);
        champRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { filtrerLieux(s.toString()); }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkMonitor.getInstance(this).ajouterObserver(networkObserver);
        chargerLieux();
        configurerBoutons();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NetworkMonitor.getInstance(this).retirerObserver(networkObserver);
    }

    private boolean estConnecte() {
        return NetworkMonitor.getInstance(this).estConnecte();
    }

    private void configurerBoutons() {
        boolean enLigne = estConnecte();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_liste_lieux) {
                // On est déjà sur cette page, ne rien faire
                return true;

            } else if (id == R.id.nav_creer_itineraire) {
                if (enLigne) {
                    startActivity(new Intent(this, CreerItineraireActivity.class));
                } else {
                    Toast.makeText(this, "Connexion requise pour créer un itinéraire", Toast.LENGTH_SHORT).show();
                }
                return true;

            } else if (id == R.id.nav_liste_itineraires) {
                startActivity(new Intent(this, ItineraireActivity.class));
                return true;

            } else if (id == R.id.nav_historique) {
                if (enLigne) {
                    startActivity(new Intent(this, HistoriqueVisiteActivity.class));
                } else {
                    Toast.makeText(this, "Connexion requise pour voir l'historique", Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            return false;
        });

        // Griser les items indisponibles hors ligne
        bottomNavigationView.getMenu().findItem(R.id.nav_creer_itineraire).setEnabled(enLigne);
        bottomNavigationView.getMenu().findItem(R.id.nav_historique).setEnabled(enLigne);

        // Sélectionner l'onglet courant
        bottomNavigationView.setSelectedItemId(R.id.nav_liste_lieux);
    }

    private void chargerLieux() {
        barreChargement.setVisibility(View.VISIBLE);

        lieuController.recupererLieux(new LieuController.CallbackLieux() {
            @Override
            public void onSucces(List<Lieu> lieux) {
                listeLieux.clear();
                listeLieuxComplete.clear();
                listeLieux.addAll(lieux);
                listeLieuxComplete.addAll(lieux);
                adaptateur.notifyDataSetChanged();
                barreChargement.setVisibility(View.GONE);
                lieuxDejaCharges = true;

                if (carteMaps != null) {
                    carteMaps.clear();
                    marqueurParId.clear();
                    for (Lieu lieu : lieux) ajouterMarqueurSurCarte(lieu);
                }

                if (!estConnecte()) {
                    Toast.makeText(ListeLieuxActivity.this,
                            "Mode hors ligne — données locales affichées",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onErreur(String msg) {
                barreChargement.setVisibility(View.GONE);
                Toast.makeText(ListeLieuxActivity.this, "Erreur : " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        carteMaps = googleMap;
        carteMaps.getUiSettings().setZoomControlsEnabled(true);
        carteMaps.getUiSettings().setCompassEnabled(true);
        carteMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE_CHARENTE, ZOOM_INITIAL));

        carteMaps.setOnMarkerClickListener(marqueur -> {
            Integer idLieu = (Integer) marqueur.getTag();
            if (idLieu != null) faireDefilerListeVers(idLieu);
            marqueur.showInfoWindow();
            return true;
        });

        activerPositionUtilisateur();

        if (lieuxDejaCharges && !listeLieuxComplete.isEmpty()) {
            for (Lieu lieu : listeLieuxComplete) ajouterMarqueurSurCarte(lieu);
        }
    }

    private void filtrerLieux(String texte) {
        List<Lieu> resultat = new ArrayList<>();
        if (texte.trim().isEmpty()) {
            resultat.addAll(listeLieuxComplete);
        } else {
            String recherche = texte.toLowerCase().trim();
            for (Lieu lieu : listeLieuxComplete) {
                if (corresponde(lieu, recherche)) resultat.add(lieu);
            }
        }
        adaptateur.mettreAJourListe(resultat);
    }

    private boolean corresponde(Lieu lieu, String recherche) {
        String nom       = lieu.getNom()       != null ? lieu.getNom().toLowerCase()       : "";
        String categorie = lieu.getCategorie() != null ? lieu.getCategorie().toLowerCase() : "";
        if (nom.contains(recherche) || categorie.contains(recherche)) return true;
        for (String mot : recherche.split("\\s+")) {
            if (mot.length() < 3) continue;
            if (contiendApproximativement(nom, mot) || contiendApproximativement(categorie, mot)) return true;
        }
        return false;
    }

    private boolean contiendApproximativement(String texte, String mot) {
        int tolerance = mot.length() <= 6 ? 1 : 2;
        for (String motTexte : texte.split("\\s+")) {
            if (distanceLevenshtein(motTexte, mot) <= tolerance) return true;
        }
        return false;
    }

    private int distanceLevenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) prev[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) curr[j] = prev[j - 1];
                else curr[j] = 1 + Math.min(prev[j - 1], Math.min(prev[j], curr[j - 1]));
            }
            int[] temp = prev; prev = curr; curr = temp;
        }
        return prev[b.length()];
    }

    private void ajouterMarqueurSurCarte(Lieu lieu) {
        if (carteMaps == null) return;
        if (lieu.getLatitude() == null || lieu.getLongitude() == null) return;
        LatLng pos = new LatLng(lieu.getLatitude(), lieu.getLongitude());
        Marker m = carteMaps.addMarker(new MarkerOptions()
                .position(pos).title(lieu.getNom()).snippet(lieu.getCategorie())
                .icon(BitmapDescriptorFactory.defaultMarker(obtenirCouleurCategorie(lieu.getCategorie()))));
        if (m != null) { m.setTag(lieu.getId()); marqueurParId.put(lieu.getId(), m); }
    }

    private float obtenirCouleurCategorie(String c) {
        if (c == null) return BitmapDescriptorFactory.HUE_RED;
        switch (c) {
            case "Musée":                       return BitmapDescriptorFactory.HUE_BLUE;
            case "Site et monument historique": return BitmapDescriptorFactory.HUE_ORANGE;
            case "Parc et jardin":              return BitmapDescriptorFactory.HUE_GREEN;
            case "Entreprise à visiter":        return BitmapDescriptorFactory.HUE_YELLOW;
            case "Centre d'interprétation":     return BitmapDescriptorFactory.HUE_VIOLET;
            default:                            return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private void centrerCarteOnLieu(Lieu lieu) {
        if (carteMaps == null || lieu.getLatitude() == null || lieu.getLongitude() == null) return;
        LatLng pos = new LatLng(lieu.getLatitude(), lieu.getLongitude());
        carteMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 14f));
        Marker m = marqueurParId.get(lieu.getId());
        if (m != null) m.showInfoWindow();
    }

    private void faireDefilerListeVers(int idLieu) {
        for (int i = 0; i < listeLieux.size(); i++) {
            if (listeLieux.get(i).getId() == idLieu) {
                ((RecyclerView) findViewById(R.id.recyclerLieux)).smoothScrollToPosition(i);
                break;
            }
        }
    }

    private void activerPositionUtilisateur() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            carteMaps.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == CODE_PERMISSION && res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED
                && carteMaps != null
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            carteMaps.setMyLocationEnabled(true);
        }
    }
}