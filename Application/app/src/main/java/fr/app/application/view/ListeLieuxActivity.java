package fr.app.application.view;

import android.Manifest;
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

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import fr.app.application.R;
import fr.app.application.model.Lieu;
import fr.app.application.model.ReponseLieux;
import fr.app.application.utils.VolleyUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListeLieuxActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ─── Constantes ──────────────────────────────────────────────────────────
    private static final String URL_API        = "http://10.98.175.102:8000/api/lieus";
    private static final int    CODE_PERMISSION = 1001;
    private static final LatLng CENTRE_CHARENTE = new LatLng(45.6466, 0.1560);
    private static final float  ZOOM_INITIAL    = 9f;

    // ─── Attributs ───────────────────────────────────────────────────────────
    private GoogleMap     carteMaps;
    private LieuAdapter   adaptateur;
    private ProgressBar   barreChargement;
    private List<Lieu>    listeLieux = new ArrayList<>();
    private List<Lieu> listeLieuxComplete = new ArrayList<>();

    // Associe chaque lieu à son marqueur sur la carte
    private Map<Integer, Marker> marqueurParId = new HashMap<>();

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_lieux);

        barreChargement = findViewById(R.id.barreChargement);

        // ── RecyclerView ──
        RecyclerView recyclerLieux = findViewById(R.id.recyclerLieux);
        recyclerLieux.setLayoutManager(new LinearLayoutManager(this));

        adaptateur = new LieuAdapter(this, listeLieux, lieu -> {
            // Clic sur un lieu → centrer la carte sur son marqueur
            centrerCarteOnLieu(lieu);
        });
        recyclerLieux.setAdapter(adaptateur);


        // ── Barre de recherche ──
        EditText champRecherche = findViewById(R.id.champRecherche);
        champRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filtrerLieux(s.toString());
            }
        });

        // ── Google Maps ──
        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarte);
        if (fragmentCarte != null) {
            fragmentCarte.getMapAsync(this);
        }
    }

    // ─── Filtrage ─────────────────────────────────────────────────────────────

    private void filtrerLieux(String texte) {
        List<Lieu> resultat = new ArrayList<>();

        if (texte.trim().isEmpty()) {
            resultat.addAll(listeLieuxComplete);
        } else {
            String recherche = texte.toLowerCase().trim();
            for (Lieu lieu : listeLieuxComplete) {
                if (corresponde(lieu, recherche)) {
                    resultat.add(lieu);
                }
            }
        }

        adaptateur.mettreAJourListe(resultat);
    }

    /**
     * Vérifie si le lieu correspond à la recherche.
     * Recherche dans le nom ET la catégorie, avec tolérance aux fautes.
     */
    private boolean corresponde(Lieu lieu, String recherche) {
        String nom       = lieu.getNom()       != null ? lieu.getNom().toLowerCase()       : "";
        String categorie = lieu.getCategorie() != null ? lieu.getCategorie().toLowerCase() : "";

        // Correspondance exacte (contient)
        if (nom.contains(recherche) || categorie.contains(recherche)) {
            return true;
        }

        // Correspondance approximative mot par mot
        String[] mots = recherche.split("\\s+");
        for (String mot : mots) {
            if (mot.length() < 3) continue; // ignore les mots trop courts
            if (contiendApproximativement(nom, mot) || contiendApproximativement(categorie, mot)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Vérifie si le texte contient un mot approchant via distance de Levenshtein.
     * Tolère 1 faute pour les mots de 4-6 lettres, 2 fautes au-delà.
     */
    private boolean contiendApproximativement(String texte, String mot) {
        String[] motsTexte = texte.split("\\s+");
        int tolerance = mot.length() <= 6 ? 1 : 2;

        for (String motTexte : motsTexte) {
            if (distanceLevenshtein(motTexte, mot) <= tolerance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcule la distance de Levenshtein entre deux chaînes.
     * (nombre minimal d'insertions, suppressions, substitutions pour passer de a à b)
     */
    private int distanceLevenshtein(String a, String b) {
        int[] prev = new int[b.length() + 1];
        int[] curr = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) prev[j] = j;

        for (int i = 1; i <= a.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    curr[j] = prev[j - 1];
                } else {
                    curr[j] = 1 + Math.min(prev[j - 1], Math.min(prev[j], curr[j - 1]));
                }
            }
            int[] temp = prev; prev = curr; curr = temp;
        }

        return prev[b.length()];
    }

    private String supprimerAccents(String texte) {
        return Normalizer.normalize(texte, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    // ─── Callback Maps ───────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        carteMaps = googleMap;

        carteMaps.getUiSettings().setZoomControlsEnabled(true);
        carteMaps.getUiSettings().setCompassEnabled(true);

        // Position par défaut sur la Charente
        carteMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE_CHARENTE, ZOOM_INITIAL));

        // Clic sur un marqueur → faire défiler la liste jusqu'au lieu correspondant
        carteMaps.setOnMarkerClickListener(marqueur -> {
            Integer idLieu = (Integer) marqueur.getTag();
            if (idLieu != null) {
                faireDefilerListeVers(idLieu);
            }
            marqueur.showInfoWindow();
            return true;
        });

        activerPositionUtilisateur();

        // Charger les lieux une fois la carte prête
        chargerLieux(URL_API);
    }

    // ─── Chargement API ──────────────────────────────────────────────────────

    private void chargerLieux(String url) {
        barreChargement.setVisibility(View.VISIBLE);

        JsonObjectRequest requete = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                reponse -> {
                    try {
                        // Parsing avec Gson
                        Gson gson = new Gson();
                        ReponseLieux reponseLieux = gson.fromJson(
                                reponse.toString(), ReponseLieux.class);

                        List<Lieu> nouveauxLieux = reponseLieux.getData();
                        if (nouveauxLieux != null) {
                            for (Lieu lieu : nouveauxLieux) {
                                listeLieux.add(lieu);
                                listeLieuxComplete.add(lieu);
                                ajouterMarqueurSurCarte(lieu);
                            }
                            adaptateur.notifyDataSetChanged();
                        }

                        // Pagination : charger la page suivante si elle existe
                        JSONObject view = reponse.optJSONObject("hydra:view");
                        if (view != null && view.has("hydra:next")) {
                            String pageSuivante = "http://172.20.10.2:8000"
                                    + view.getString("hydra:next");
                            chargerLieux(pageSuivante);
                        } else {
                            // Fin du chargement
                            barreChargement.setVisibility(View.GONE);
                            ajusterCameraAuxMarqueurs();
                        }

                    } catch (Exception e) {
                        barreChargement.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Erreur lecture : " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                },
                erreur -> {
                    barreChargement.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Erreur réseau : " + erreur.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
        );

        VolleyUtils.getInstance(this).addToRequestQueue(requete);
    }

    // ─── Marqueurs ────────────────────────────────────────────────────────────

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
            // Stocke l'id du lieu dans le tag du marqueur
            marqueur.setTag(lieu.getId());
            marqueurParId.put(lieu.getId(), marqueur);
        }
    }

    private float obtenirCouleurCategorie(String categorie) {
        if (categorie == null) return BitmapDescriptorFactory.HUE_RED;
        switch (categorie) {
            case "Musée":
                return BitmapDescriptorFactory.HUE_BLUE;
            case "Site et monument historique":
                return BitmapDescriptorFactory.HUE_ORANGE;
            case "Parc et jardin":
                return BitmapDescriptorFactory.HUE_GREEN;
            case "Entreprise à visiter":
                return BitmapDescriptorFactory.HUE_YELLOW;
            case "Centre d'interprétation":
                return BitmapDescriptorFactory.HUE_VIOLET;
            default:
                return BitmapDescriptorFactory.HUE_RED;
        }
    }

    // ─── Synchronisation carte ↔ liste ───────────────────────────────────────

    /**
     * Clic sur un lieu dans la liste → centrer la carte sur son marqueur
     */
    private void centrerCarteOnLieu(Lieu lieu) {
        if (carteMaps == null) return;
        if (lieu.getLatitude() == null || lieu.getLongitude() == null) return;

        LatLng position = new LatLng(lieu.getLatitude(), lieu.getLongitude());
        carteMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));

        // Afficher l'info-bulle du marqueur correspondant
        Marker marqueur = marqueurParId.get(lieu.getId());
        if (marqueur != null) {
            marqueur.showInfoWindow();
        }
    }

    /**
     * Clic sur un marqueur → faire défiler la RecyclerView jusqu'au lieu
     */
    private void faireDefilerListeVers(int idLieu) {
        for (int i = 0; i < listeLieux.size(); i++) {
            if (listeLieux.get(i).getId() == idLieu) {
                RecyclerView recycler = findViewById(R.id.recyclerLieux);
                recycler.smoothScrollToPosition(i);
                break;
            }
        }
    }

    /**
     * Ajuste le zoom pour englober tous les marqueurs
     */
    private void ajusterCameraAuxMarqueurs() {
        if (carteMaps == null || listeLieux.isEmpty()) return;

        LatLngBounds.Builder constructeur = new LatLngBounds.Builder();
        boolean aucunPoint = true;

        for (Lieu lieu : listeLieux) {
            if (lieu.getLatitude() != null && lieu.getLongitude() != null) {
                constructeur.include(new LatLng(lieu.getLatitude(), lieu.getLongitude()));
                aucunPoint = false;
            }
        }

        if (!aucunPoint) {
            carteMaps.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(constructeur.build(), 80));
        }
    }

    // ─── Permission GPS ──────────────────────────────────────────────────────

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