package fr.app.application.view.itiniraires;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.app.application.R;
import fr.app.application.model.Itineraire;
import fr.app.application.utils.VolleyUtils;

/**
 * Affiche le détail d'un itinéraire :
 * - Carte avec marqueurs colorés (🟢 départ, 🔵 étapes, 🔴 arrivée)
 * - Tracé piéton réel via OSRM (gratuit, sans clé API)
 * - Infos : durée, nombre de lieux, départ → arrivée
 *
 * Données reçues via Intent :
 *   - EXTRA_ITINERAIRE : objet Itineraire sérialisé en JSON
 */
public class DetailItineraireActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_ITINERAIRE = "extra_itineraire";

    private static final String ORS_BASE_URL = "https://api.openrouteservice.org/v2/directions/foot-walking/geojson";
    private static final String ORS_API_KEY  = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjJlYjU0ZWNjMmEyYzQwOTliMTM4NDFmMzMzMTA2Yjc4IiwiaCI6Im11cm11cjY0In0=";

    private GoogleMap  carteMaps;
    private Itineraire itineraire;

    private TextView    tvTitre;
    private TextView    tvDuree;
    private TextView    tvNbLieux;
    private TextView    tvDepartArrivee;
    private ProgressBar barreChargementTrace;
    private TextView    tvChargementTrace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_itineraire);

        String json = getIntent().getStringExtra(EXTRA_ITINERAIRE);
        itineraire  = new Gson().fromJson(json, Itineraire.class);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Itinéraire #" + itineraire.getId());
        }

        initVues();
        remplirInfos();
        initCarte();
    }

    // ── Initialisation ────────────────────────────────────────────────────

    private void initVues() {
        tvTitre              = findViewById(R.id.tvDetailItineraireTitre);
        tvDuree              = findViewById(R.id.tvDetailItineraireDuree);
        tvNbLieux            = findViewById(R.id.tvDetailItineraireNbLieux);
        tvDepartArrivee      = findViewById(R.id.tvDetailItineraireDepartArrivee);
        barreChargementTrace = findViewById(R.id.barreChargementTrace);
        tvChargementTrace    = findViewById(R.id.tvChargementTrace);
    }

    private void remplirInfos() {
        tvTitre.setText("Itinéraire #" + itineraire.getId());

        // Durée
        if (itineraire.getDureTotal() != null && itineraire.getDureTotal() > 0) {
            int heures  = itineraire.getDureTotal() / 60;
            int minutes = itineraire.getDureTotal() % 60;
            if (heures > 0) {
                tvDuree.setText("🚶 " + heures + "h" + (minutes > 0 ? String.format("%02d", minutes) : ""));
            } else {
                tvDuree.setText("🚶 " + minutes + " min à pied");
            }
        } else {
            tvDuree.setText("Durée non définie");
        }

        // Nombre de lieux
        List<Itineraire.LieuRef> lieux = itineraire.getLieux();
        int nbLieux = lieux != null ? lieux.size() : 0;
        tvNbLieux.setText(nbLieux + " lieu" + (nbLieux > 1 ? "x" : ""));

        // Départ → Arrivée
        if (lieux != null && !lieux.isEmpty()) {
            String depart  = lieux.get(lieux.size() - 1).getNom();
            String arrivee = lieux.get(0).getNom();
            if (lieux.size() == 1) {
                tvDepartArrivee.setText("📍 " + depart);
            } else {
                tvDepartArrivee.setText("📍 " + depart + "  →  🏁 " + arrivee);
            }
        }
    }

    private void initCarte() {
        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarteDetail);
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

        List<Itineraire.LieuRef> lieux = itineraire.getLieux();
        if (lieux == null || lieux.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean auMoinsUnPoint = false;

        // ── Placer les marqueurs ───────────────────────────────────────────
        for (int i = 0; i < lieux.size(); i++) {
            Itineraire.LieuRef lieu = lieux.get(i);
            if (lieu.getLat() == null || lieu.getLng() == null) continue;

            LatLng position = new LatLng(lieu.getLat(), lieu.getLng());
            boundsBuilder.include(position);
            auMoinsUnPoint = true;

            float couleur;
            String snippet;
            if (i == 0) {
                couleur  = BitmapDescriptorFactory.HUE_RED;
                snippet  = "Arrivée";

            } else if (i == lieux.size() - 1) {
                couleur  = BitmapDescriptorFactory.HUE_GREEN;
                snippet  = "Départ";
            } else {
                couleur  = BitmapDescriptorFactory.HUE_AZURE;
                snippet  = "Étape " + i;
            }

            carteMaps.addMarker(new MarkerOptions()
                    .position(position)
                    .title((i + 1) + ". " + lieu.getNom())
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(couleur)));
        }

        // ── Ajuster la caméra ─────────────────────────────────────────────
        if (auMoinsUnPoint) {
            final LatLngBounds bounds = boundsBuilder.build();
            carteMaps.setOnMapLoadedCallback(() ->
                    carteMaps.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 120)
                    )
            );
        }

        // ── Tracé piéton OSRM ─────────────────────────────────────────────
        if (lieux.size() >= 2) {
            chargerTraceOSRM(lieux);
        }
    }

    // ── Tracé OSRM ────────────────────────────────────────────────────────

    /**
     * Appelle OSRM pour récupérer la géométrie du chemin piéton
     * et dessine une Polyline bleue sur la carte.
     */
    private void chargerTraceOSRM(List<Itineraire.LieuRef> lieux) {
        barreChargementTrace.setVisibility(View.VISIBLE);
        tvChargementTrace.setVisibility(View.VISIBLE);

        try {
            // Construction du corps JSON
            // ORS attend : { "coordinates": [[lng, lat], [lng, lat], ...] }
            JSONArray coordinates = new JSONArray();
            for (Itineraire.LieuRef lieu : lieux) {
                if (lieu.getLat() == null || lieu.getLng() == null) continue;
                JSONArray coord = new JSONArray();
                coord.put(lieu.getLng()); // longitude en premier
                coord.put(lieu.getLat());
                coordinates.put(coord);
            }

            JSONObject body = new JSONObject();
            body.put("coordinates", coordinates);

            JsonObjectRequest requete = new JsonObjectRequest(
                    Request.Method.POST,
                    ORS_BASE_URL,
                    body,
                    reponse -> {
                        barreChargementTrace.setVisibility(View.GONE);
                        tvChargementTrace.setVisibility(View.GONE);
                        try {
                            // Extraire les coordonnées du tracé GeoJSON
                            JSONArray coords = reponse
                                    .getJSONArray("features")
                                    .getJSONObject(0)
                                    .getJSONObject("geometry")
                                    .getJSONArray("coordinates");

                            List<LatLng> points = new ArrayList<>();
                            for (int i = 0; i < coords.length(); i++) {
                                JSONArray coord = coords.getJSONArray(i);
                                double lng = coord.getDouble(0);
                                double lat = coord.getDouble(1);
                                points.add(new LatLng(lat, lng));
                            }

                            if (!points.isEmpty()) {
                                carteMaps.addPolyline(new PolylineOptions()
                                        .addAll(points)
                                        .color(Color.parseColor("#2979FF"))
                                        .width(10f)
                                        .geodesic(true));
                            }

                        } catch (Exception e) {
                            // Tracé indisponible, marqueurs restent visibles
                        }
                    },
                    erreur -> {
                        barreChargementTrace.setVisibility(View.GONE);
                        tvChargementTrace.setVisibility(View.GONE);
                    }
            ) {
                // Ajouter le header Authorization avec la clé ORS
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Authorization", ORS_API_KEY);
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleyUtils.getInstance(this).addToRequestQueue(requete);

        } catch (Exception e) {
            barreChargementTrace.setVisibility(View.GONE);
            tvChargementTrace.setVisibility(View.GONE);
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