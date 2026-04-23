package fr.app.application.view.itiniraires;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
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

public class DetailItineraireActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_ITINERAIRE = "extra_itineraire";
    private static final String ORS_BASE_URL = "https://api.openrouteservice.org/v2/directions/foot-walking/geojson";
    private static final String ORS_API_KEY  = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjJlYjU0ZWNjMmEyYzQwOTliMTM4NDFmMzMzMTA2Yjc4IiwiaCI6Im11cm11cjY0In0=";
    private GoogleMap  carteMaps;
    private Itineraire itineraire;
    private LieuxAdapter lieuxAdapter;

    private final List<Marker> markers = new ArrayList<>();

    private RecyclerView recyclerLieuxVisite;
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

    private void initVues() {
        tvDuree              = findViewById(R.id.tvDetailItineraireDuree);
        tvNbLieux            = findViewById(R.id.tvDetailItineraireNbLieux);
        tvDepartArrivee      = findViewById(R.id.tvDetailItineraireDepartArrivee);
        barreChargementTrace = findViewById(R.id.barreChargementTrace);
        tvChargementTrace    = findViewById(R.id.tvChargementTrace);

        recyclerLieuxVisite  = findViewById(R.id.recyclerLieuxVisite);

        if (recyclerLieuxVisite != null) {
            recyclerLieuxVisite.setLayoutManager(new LinearLayoutManager(this));
            lieuxAdapter = new LieuxAdapter(new ArrayList<>(), this::centrerSurLieu);
            recyclerLieuxVisite.setAdapter(lieuxAdapter);
        }
    }

    private void remplirInfos() {
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

        List<Itineraire.LieuRef> lieux = itineraire.getLieux();
        int nbLieux = lieux != null ? lieux.size() : 0;
        tvNbLieux.setText(nbLieux + " lieu" + (nbLieux > 1 ? "x" : ""));

        if (lieux != null && !lieux.isEmpty()) {
            String depart  = lieux.get(0).getNom();
            String arrivee = lieux.get(lieux.size() - 1).getNom();
            if (lieux.size() == 1) {
                tvDepartArrivee.setText("📍 " + depart);
            } else {
                tvDepartArrivee.setText("📍 " + depart + "  →  🏁 " + arrivee);
            }
        }

        lieuxAdapter.lieux.clear();
        lieuxAdapter.lieux.addAll(lieux);
        lieuxAdapter.notifyDataSetChanged();
    }

    private void initCarte() {
        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarteDetail);
        if (fragmentCarte != null) {
            fragmentCarte.getMapAsync(this);
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        carteMaps = googleMap;
        carteMaps.getUiSettings().setZoomControlsEnabled(true);
        carteMaps.getUiSettings().setCompassEnabled(true);

        List<Itineraire.LieuRef> lieux = itineraire.getLieux();
        if (lieux == null || lieux.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean auMoinsUnPoint = false;

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

        if (auMoinsUnPoint) {
            final LatLngBounds bounds = boundsBuilder.build();
            carteMaps.setOnMapLoadedCallback(() ->
                    carteMaps.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 120)
                    )
            );
        }

        if (lieux.size() >= 2) {
            chargerTraceOSRM(lieux);
        }
    }

    void centrerSurLieu(int index) {
        if (carteMaps == null) return;
        List<Itineraire.LieuRef> lieux = itineraire.getLieux();
        if (lieux == null || index >= lieux.size()) return;

        Itineraire.LieuRef lieu = lieux.get(index);
        if (lieu.getLat() == null || lieu.getLng() == null) return;

        carteMaps.animateCamera(
                CameraUpdateFactory.newLatLngZoom(new LatLng(lieu.getLat(), lieu.getLng()), 16f));

        if (index < markers.size() && markers.get(index) != null) {
            markers.get(index).showInfoWindow();
        }
    }

    interface OnLieuClickListener {
        void onJySuis(int index);
    }

    private static class LieuxAdapter extends RecyclerView.Adapter<LieuxAdapter.LieuViewHolder> {

        private final List<Itineraire.LieuRef> lieux;
        private final OnLieuClickListener      listener;

        LieuxAdapter(List<Itineraire.LieuRef> lieux, OnLieuClickListener listener) {
            this.lieux    = lieux;
            this.listener = listener;
        }

        @NonNull
        @Override
        public LieuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_lieu_itineraire, parent, false);
            return new LieuViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull LieuViewHolder holder, int position) {
            Itineraire.LieuRef lieu = lieux.get(position);

            String emoji;
            if (position == 0 ) {
                emoji = "🟢";
            } else if (position == lieux.size() - 1) {
                emoji = "🔴";
            } else {
                emoji = "🔵";
            }

            holder.tvNumero.setText(emoji + " " + (position + 1) + ".");
            holder.tvNom.setText(lieu.getNom() != null ? lieu.getNom() : "—");

            boolean aCoordonnees = lieu.getLat() != null && lieu.getLng() != null;
            holder.btnJySuis.setEnabled(aCoordonnees);
            holder.btnJySuis.setOnClickListener(v -> listener.onJySuis(position));
        }

        @Override
        public int getItemCount() {
            return lieux != null ? lieux.size() : 0;
        }

        static class LieuViewHolder extends RecyclerView.ViewHolder {
            final TextView tvNumero;
            final TextView tvNom;
            final Button   btnJySuis;

            LieuViewHolder(@NonNull View itemView) {
                super(itemView);
                tvNumero  = itemView.findViewById(R.id.tvLieuNumero);
                tvNom     = itemView.findViewById(R.id.tvLieuNom);
                btnJySuis = itemView.findViewById(R.id.btnLieuJySuis);
            }
        }
    }


    private void chargerTraceOSRM(List<Itineraire.LieuRef> lieux) {
        barreChargementTrace.setVisibility(View.VISIBLE);
        tvChargementTrace.setVisibility(View.VISIBLE);

        try {
            JSONArray coordinates = new JSONArray();
            for (Itineraire.LieuRef lieu : lieux) {
                if (lieu.getLat() == null || lieu.getLng() == null) continue;
                JSONArray coord = new JSONArray();
                coord.put(lieu.getLng());
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
                        }
                    },
                    erreur -> {
                        barreChargementTrace.setVisibility(View.GONE);
                        tvChargementTrace.setVisibility(View.GONE);
                    }
            ) {
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