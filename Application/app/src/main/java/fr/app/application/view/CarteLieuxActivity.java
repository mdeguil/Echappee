package fr.app.application.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import fr.app.application.R;
import fr.app.application.model.Lieu;
import fr.app.application.utils.VolleyUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CarteLieuxActivity extends AppCompatActivity implements OnMapReadyCallback {

    // ─── Constantes ──────────────────────────────────────────────────────────
    private static final String URL_API_LIEUX    = "http://172.20.10.2:8000/api/lieux";
    private static final int    CODE_PERMISSION  = 1001;

    // Centre de la Charente (coordonnées par défaut)
    private static final LatLng CENTRE_CHARENTE  = new LatLng(45.6466, 0.1560);
    private static final float  ZOOM_INITIAL     = 9f;

    // ─── Attributs ───────────────────────────────────────────────────────────
    private GoogleMap  carteMaps;
    private ProgressBar barreChargement;
    private List<Lieu> listeLieux = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carte_lieux);

        barreChargement = findViewById(R.id.barreChargement);

        // Bouton retour
        findViewById(R.id.boutonRetour).setOnClickListener(v -> finish());

        // Initialisation du fragment Maps
        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarte);
        if (fragmentCarte != null) {
            fragmentCarte.getMapAsync(this);
        }
    }

    // ─── Callback Maps ───────────────────────────────────────────────────────

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        carteMaps = googleMap;

        // Configuration de la carte
        carteMaps.getUiSettings().setZoomControlsEnabled(true);
        carteMaps.getUiSettings().setCompassEnabled(true);
        carteMaps.getUiSettings().setMyLocationButtonEnabled(true);

        // Position par défaut sur la Charente
        carteMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(CENTRE_CHARENTE, ZOOM_INITIAL));

        // Activer la position de l'utilisateur si permission accordée
        activerPositionUtilisateur();

        // Clic sur un marqueur → afficher le nom du lieu
        carteMaps.setOnMarkerClickListener(marqueur -> {
            Toast.makeText(this, marqueur.getTitle(), Toast.LENGTH_SHORT).show();
            return false;
        });

        // Charger les lieux depuis l'API
        chargerLieuxDepuisApi();
    }

    // ─── Chargement des lieux ─────────────────────────────────────────────────

    private void chargerLieuxDepuisApi() {
        barreChargement.setVisibility(View.VISIBLE);

        chargerPage(URL_API_LIEUX);
    }

    private void chargerPage(String url) {
        JsonObjectRequest requete = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                reponse -> {
                    try {
                        JSONArray donnees = reponse.getJSONArray("data");

                        for (int i = 0; i < donnees.length(); i++) {
                            JSONObject obj = donnees.getJSONObject(i);

                            // Récupération des coordonnées
                            if (!obj.isNull("latitude") && !obj.isNull("longitude")) {
                                double latitude  = obj.getDouble("latitude");
                                double longitude = obj.getDouble("longitude");
                                String nom       = obj.optString("nom", "Lieu inconnu");
                                String categorie = obj.optString("categorie", "");

                                ajouterMarqueur(latitude, longitude, nom, categorie);
                            }
                        }

                        // Charger la page suivante si disponible
                        JSONObject liens = reponse.optJSONObject("hydra:view");
                        if (liens != null && liens.has("hydra:next")) {
                            String pageSuivante = "http://172.20.10.2:8000"
                                    + liens.getString("hydra:next");
                            chargerPage(pageSuivante);
                        } else {
                            // Toutes les pages chargées
                            barreChargement.setVisibility(View.GONE);
                            ajusterCameraAuxMarqueurs();
                        }

                    } catch (Exception e) {
                        barreChargement.setVisibility(View.GONE);
                        Toast.makeText(this, "Erreur lecture JSON : " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                },
                erreur -> {
                    barreChargement.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur réseau : " + erreur.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
        );

        VolleyUtils.getInstance(this).addToRequestQueue(requete);
    }

    // ─── Marqueurs ────────────────────────────────────────────────────────────

    private void ajouterMarqueur(double latitude, double longitude,
                                 String nom, String categorie) {
        if (carteMaps == null) return;

        LatLng position = new LatLng(latitude, longitude);

        // Couleur du marqueur selon la catégorie
        float couleur = obtenirCouleurCategorie(categorie);

        carteMaps.addMarker(new MarkerOptions()
                .position(position)
                .title(nom)
                .snippet(categorie)
                .icon(BitmapDescriptorFactory.defaultMarker(couleur)));
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

    private void ajusterCameraAuxMarqueurs() {
        if (carteMaps == null) return;

        // Si aucun marqueur, garder la vue par défaut
        try {
            LatLngBounds.Builder constructeur = new LatLngBounds.Builder();
            // Inclure tous les marqueurs dans les bounds
            // (les marqueurs sont déjà ajoutés à la carte)
            carteMaps.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(CENTRE_CHARENTE, ZOOM_INITIAL)
            );
        } catch (Exception e) {
            // Aucun marqueur, on garde la vue par défaut
        }
    }

    // ─── Position utilisateur ─────────────────────────────────────────────────

    private void activerPositionUtilisateur() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            carteMaps.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    CODE_PERMISSION
            );
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