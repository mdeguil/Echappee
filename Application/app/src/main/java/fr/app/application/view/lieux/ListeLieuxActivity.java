package fr.app.application.view.lieux;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.app.application.R;
import fr.app.application.model.Lieu;
import fr.app.application.model.reponse.ReponseLieux;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.VolleyUtils;
import fr.app.application.view.adapter.LieuAdapter;
import fr.app.application.view.itiniraires.CreerItineraireActivity;
import fr.app.application.view.itiniraires.ItineraireActivity;

public class ListeLieuxActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String ENDPOINT_LIEUX  = "/api/lieus";
    private static final int    CODE_PERMISSION  = 1001;
    private static final LatLng CENTRE_CHARENTE  = new LatLng(45.6466, 0.1560);
    private static final float  ZOOM_INITIAL     = 9f;

    private GoogleMap   carteMaps;
    private LieuAdapter adaptateur;
    private ProgressBar barreChargement;
    private List<Lieu>  listeLieux = new ArrayList<>();

    private MaterialButton btnCreerItineraire, btnVoirItineraires;

    private Map<Integer, Marker> marqueurParId = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_lieux);

        barreChargement = findViewById(R.id.barreChargement);

        btnCreerItineraire = findViewById(R.id.btnCreerItineraire);
        btnVoirItineraires = findViewById(R.id.btnVoirItineraires);


        btnCreerItineraire.setOnClickListener(v ->
                startActivity(new Intent(this, CreerItineraireActivity.class))
        );


        btnVoirItineraires.setOnClickListener(v -> {
            Intent intent = new Intent(this, ItineraireActivity.class);
            startActivity(intent);
        });

        RecyclerView recyclerLieux = findViewById(R.id.recyclerLieux);
        recyclerLieux.setLayoutManager(new LinearLayoutManager(this));

        adaptateur = new LieuAdapter(this, listeLieux, lieu -> centrerCarteOnLieu(lieu));
        recyclerLieux.setAdapter(adaptateur);

        SupportMapFragment fragmentCarte = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentCarte);
        if (fragmentCarte != null) {
            fragmentCarte.getMapAsync(this);
        }
    }

    /**
     * Initialise la carte Google Maps et configure ses interactions.
     *
     * @param googleMap L'instance de la carte Google Maps configurée.
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
                faireDefilerListeVers(idLieu);
            }
            marqueur.showInfoWindow();
            return true;
        });

        activerPositionUtilisateur();

        String url = ApiConfig.getInstance(this).getUrl(ENDPOINT_LIEUX);
        chargerLieux(url);
    }

    /**
     * Charge les lieux depuis l'API et met à jour simultanément la carte et la liste.
     *
     * @param url L'URL complète incluant les éventuels filtres de recherche.
     */
    private void chargerLieux(String url) {
        barreChargement.setVisibility(View.VISIBLE);

        JsonObjectRequest requete = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                reponse -> {
                    try {
                        Gson gson = new Gson();
                        ReponseLieux reponseLieux = gson.fromJson(
                                reponse.toString(), ReponseLieux.class);

                        List<Lieu> nouveauxLieux = reponseLieux.getData();
                        if (nouveauxLieux != null) {
                            for (Lieu lieu : nouveauxLieux) {
                                listeLieux.add(lieu);
                                ajouterMarqueurSurCarte(lieu);
                            }
                            adaptateur.notifyDataSetChanged();
                        }
                        barreChargement.setVisibility(View.GONE);

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

    /**
     * Ajoute un marqueur interactif sur la carte pour un lieu donné.
     *
     * @param lieu L'objet Lieu contenant les coordonnées et les informations à afficher.
     */
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

    /**
     * Détermine la couleur du marqueur Google Maps en fonction de la catégorie du lieu.
     *
     * @param categorie Le nom de la catégorie (ex: "Musée", "Parc et jardin").
     * @return Une valeur float représentant la teinte du marqueur (ex: BitmapDescriptorFactory.HUE_BLUE).
     */
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

    /**
     * Centre la caméra de la carte sur un lieu spécifique avec un effet d'animation.
     *
     * @param lieu L'objet Lieu sur lequel la vue doit se focaliser.
     */
    private void centrerCarteOnLieu(Lieu lieu) {
        if (carteMaps == null) return;
        if (lieu.getLatitude() == null || lieu.getLongitude() == null) return;

        LatLng position = new LatLng(lieu.getLatitude(), lieu.getLongitude());
        carteMaps.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 14f));

        Marker marqueur = marqueurParId.get(lieu.getId());
        if (marqueur != null) marqueur.showInfoWindow();
    }

    /**
     * Fait défiler la liste (RecyclerView) de manière fluide jusqu'à un lieu spécifique.
     *
     * @param idLieu L'identifiant unique du lieu vers lequel la liste doit défiler.
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
     * Active la fonctionnalité de localisation de l'utilisateur sur la carte.
     */
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

    /**
     * Gère la réponse de l'utilisateur à la demande de permissions.
     *
     * @param codeRequete Le code d'identification de la requête envoyé lors de l'appel initial.
     * @param permissions Le tableau des permissions demandées.
     * @param resultats   Le tableau des résultats (accordé ou refusé).
     */
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
