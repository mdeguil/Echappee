package fr.app.application.view.lieux;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.app.application.R;
import fr.app.application.controller.LieuController;
import fr.app.application.model.Lieu;
import fr.app.application.utils.BDD.AppDatabase;
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
    private boolean     lieuxDejaCharges   = false; // évite double chargement

    private Map<Integer, Marker>    marqueurParId = new HashMap<>();
    private MaterialButton btnCreerItineraire, btnVoirItineraires, btnHistorique;

    private LieuController lieuController;
    private AppDatabase    db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_lieux);

        db             = AppDatabase.getDatabase(this);
        lieuController = new LieuController(this);

        barreChargement    = findViewById(R.id.barreChargement);
        btnCreerItineraire = findViewById(R.id.btnCreerItineraire);
        btnVoirItineraires = findViewById(R.id.btnVoirItineraires);
        btnHistorique = findViewById(R.id.btnHistorique);

        // ── Bouton créer itinéraire : désactivé si hors ligne ─────────────
        boolean enLigne = estConnecte();
        btnCreerItineraire.setEnabled(enLigne);
        btnCreerItineraire.setAlpha(enLigne ? 1.0f : 0.4f);
        if (enLigne) {
            btnCreerItineraire.setOnClickListener(v ->
                    startActivity(new Intent(this, CreerItineraireActivity.class))
            );
        } else {
            btnCreerItineraire.setOnClickListener(v ->
                    Toast.makeText(this,
                            "Connexion requise pour créer un itinéraire",
                            Toast.LENGTH_SHORT).show()
            );
        }

        btnVoirItineraires.setOnClickListener(v ->
                startActivity(new Intent(this, ItineraireActivity.class))
        );


        btnVoirItineraires.setOnClickListener(v -> {
            Intent intent = new Intent(this, ItineraireActivity.class);
            startActivity(intent);
        });

        btnHistorique.setOnClickListener(v ->
                startActivity(new Intent(this, HistoriqueVisiteActivity.class))
        );

        RecyclerView recyclerLieux = findViewById(R.id.recyclerLieux);
        recyclerLieux.setLayoutManager(new LinearLayoutManager(this));
        adaptateur = new LieuAdapter(this, listeLieux, lieu -> centrerCarteOnLieu(lieu));
        recyclerLieux.setAdapter(adaptateur);

        // ── Chargement immédiat des lieux (sans attendre la carte) ────────
        chargerLieux();

        // ── Carte ─────────────────────────────────────────────────────────
        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarte);
        if (fragmentCarte != null) {
            fragmentCarte.getMapAsync(this);
        }

        // ── Recherche ─────────────────────────────────────────────────────
        EditText champRecherche = findViewById(R.id.champRecherche);
        champRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { filtrerLieux(s.toString()); }
        });
    }

    // ── Connectivité ──────────────────────────────────────────────────────

    private boolean estConnecte() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    // ── Chargement des lieux ──────────────────────────────────────────────

    /**
     * Charge les lieux via LieuController (API → fallback BDD si hors ligne).
     * Appelé dès onCreate, sans attendre la carte.
     */
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

                // Ajouter les marqueurs si la carte est déjà prête
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
            public void onErreur(String messageErreur) {
                barreChargement.setVisibility(View.GONE);
                Toast.makeText(ListeLieuxActivity.this,
                        "Erreur : " + messageErreur,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    // ── Carte ─────────────────────────────────────────────────────────────

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

        // Si les lieux sont déjà chargés (chargerLieux() plus rapide que la carte),
        // on place les marqueurs maintenant
        if (lieuxDejaCharges && !listeLieuxComplete.isEmpty()) {
            for (Lieu lieu : listeLieuxComplete) ajouterMarqueurSurCarte(lieu);
        }
        // Sinon, chargerLieux() s'en chargera quand il recevra la réponse
    }

    // ── Filtrage ──────────────────────────────────────────────────────────

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
        String[] mots = recherche.split("\\s+");
        for (String mot : mots) {
            if (mot.length() < 3) continue;
            if (contiendApproximativement(nom, mot) || contiendApproximativement(categorie, mot))
                return true;
        }
        return false;
    }

    private boolean contiendApproximativement(String texte, String mot) {
        String[] motsTexte = texte.split("\\s+");
        int tolerance = mot.length() <= 6 ? 1 : 2;
        for (String motTexte : motsTexte) {
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

    // ── Carte helpers ─────────────────────────────────────────────────────

    private void ajouterMarqueurSurCarte(Lieu lieu) {
        if (carteMaps == null) return;
        if (lieu.getLatitude() == null || lieu.getLongitude() == null) return;
        LatLng position = new LatLng(lieu.getLatitude(), lieu.getLongitude());
        Marker marqueur = carteMaps.addMarker(new MarkerOptions()
                .position(position)
                .title(lieu.getNom())
                .snippet(lieu.getCategorie())
                .icon(BitmapDescriptorFactory.defaultMarker(
                        obtenirCouleurCategorie(lieu.getCategorie()))));
        if (marqueur != null) {
            marqueur.setTag(lieu.getId());
            marqueurParId.put(lieu.getId(), marqueur);
        }
    }

    private float obtenirCouleurCategorie(String categorie) {
        if (categorie == null) return BitmapDescriptorFactory.HUE_RED;
        switch (categorie) {
            case "Musée":                       return BitmapDescriptorFactory.HUE_BLUE;
            case "Site et monument historique": return BitmapDescriptorFactory.HUE_ORANGE;
            case "Parc et jardin":              return BitmapDescriptorFactory.HUE_GREEN;
            case "Entreprise à visiter":        return BitmapDescriptorFactory.HUE_YELLOW;
            case "Centre d'interprétation":     return BitmapDescriptorFactory.HUE_VIOLET;
            default:                            return BitmapDescriptorFactory.HUE_RED;
        }
    }

    private void centrerCarteOnLieu(Lieu lieu) {
        if (carteMaps == null) return;
        if (lieu.getLatitude() == null || lieu.getLongitude() == null) return;
        LatLng position = new LatLng(lieu.getLatitude(), lieu.getLongitude());
        carteMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));
        Marker marqueur = marqueurParId.get(lieu.getId());
        if (marqueur != null) marqueur.showInfoWindow();
    }

    private void faireDefilerListeVers(int idLieu) {
        for (int i = 0; i < listeLieux.size(); i++) {
            if (listeLieux.get(i).getId() == idLieu) {
                RecyclerView recycler = findViewById(R.id.recyclerLieux);
                recycler.smoothScrollToPosition(i);
                break;
            }
        }
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
}