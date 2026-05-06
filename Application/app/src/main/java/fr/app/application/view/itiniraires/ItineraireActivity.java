package fr.app.application.view.itiniraires;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.app.application.R;
import fr.app.application.controller.ItineraireController;
import fr.app.application.model.Itineraire;
import fr.app.application.utils.NetworkMonitor;
import fr.app.application.view.adapter.ItineraireAdapter;
import fr.app.application.view.lieux.ListeLieuxActivity;
import fr.app.application.view.visite.HistoriqueVisiteActivity;

public class ItineraireActivity extends AppCompatActivity {

    private ProgressBar        barreChargement;
    private View               layoutAucunItineraire;
    private RecyclerView       recyclerItineraires;
    private ItineraireAdapter  adaptateur;
    private ItineraireController controleur;
    private final List<Itineraire> listeItineraires = new ArrayList<>();

    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;

    private final NetworkMonitor.Observer networkObserver = new NetworkMonitor.Observer() {
        @Override
        public void onConnexionRetablie() {
            configurerBottomNav();
            chargerItineraires();
            Toast.makeText(ItineraireActivity.this,
                    "Connexion rétablie", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onConnexionPerdue() {
            configurerBottomNav();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itineraire);

        Toolbar toolbar = findViewById(R.id.toolbarItineraires);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initVues();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkMonitor.getInstance(this).ajouterObserver(networkObserver);
        configurerBottomNav();
        chargerItineraires();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NetworkMonitor.getInstance(this).retirerObserver(networkObserver);
    }


    private void initVues() {
        barreChargement       = findViewById(R.id.barreChargementItineraire);
        layoutAucunItineraire = findViewById(R.id.layoutAucunItineraire);
        recyclerItineraires   = findViewById(R.id.recyclerItineraires);
        bottomNavigationView  = findViewById(R.id.bottomNavigationView);

        recyclerItineraires.setLayoutManager(new LinearLayoutManager(this));
        adaptateur = new ItineraireAdapter(this, listeItineraires, this::supprimerItineraire);
        recyclerItineraires.setAdapter(adaptateur);

        controleur = new ItineraireController(this);
    }


    private void configurerBottomNav() {
        boolean enLigne = estConnecte();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_liste_itineraires) {
                return true;

            } else if (id == R.id.nav_liste_lieux) {
                startActivity(new Intent(this, ListeLieuxActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_creer_itineraire) {
                if (enLigne) {
                    startActivity(new Intent(this, CreerItineraireActivity.class));
                    finish();
                } else {
                    Toast.makeText(ItineraireActivity.this,
                            "Connexion requise pour créer un itinéraire",
                            Toast.LENGTH_SHORT).show();
                }
                return true;

            } else if (id == R.id.nav_historique) {
                if (enLigne) {
                    startActivity(new Intent(this, HistoriqueVisiteActivity.class));
                    finish();
                } else {
                    Toast.makeText(ItineraireActivity.this,
                            "Connexion requise pour voir l'historique",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            return false;
        });

        applyItemAlpha(R.id.nav_creer_itineraire, enLigne ? 1f : 0.4f);
        applyItemAlpha(R.id.nav_historique,       enLigne ? 1f : 0.4f);

        bottomNavigationView.setSelectedItemId(R.id.nav_liste_itineraires);
    }

    private void applyItemAlpha(int menuItemId, float alpha) {
        for (int i = 0; i < bottomNavigationView.getChildCount(); i++) {
            View child = bottomNavigationView.getChildAt(i);
            if (child instanceof android.view.ViewGroup) {
                android.view.ViewGroup menuView = (android.view.ViewGroup) child;
                for (int j = 0; j < menuView.getChildCount(); j++) {
                    View itemView = menuView.getChildAt(j);
                    if (itemView.getId() == menuItemId) {
                        itemView.setAlpha(alpha);
                        return;
                    }
                }
            }
        }
    }


    private boolean estConnecte() {
        return NetworkMonitor.getInstance(this).estConnecte();
    }

    private void chargerItineraires() {
        barreChargement.setVisibility(View.VISIBLE);
        layoutAucunItineraire.setVisibility(View.GONE);

        controleur.recupererItineraires(new ItineraireController.CallbackItineraires() {
            @Override
            public void onSucces(List<Itineraire> itineraires) {
                barreChargement.setVisibility(View.GONE);
                listeItineraires.clear();
                listeItineraires.addAll(itineraires);
                adaptateur.notifyDataSetChanged();

                if (listeItineraires.isEmpty()) {
                    layoutAucunItineraire.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onErreur(String messageErreur) {
                barreChargement.setVisibility(View.GONE);
                Toast.makeText(ItineraireActivity.this,
                        "Erreur réseau : " + messageErreur,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void supprimerItineraire(Itineraire itineraire, int position) {
        controleur.supprimerItineraire(itineraire.getId(),
                new ItineraireController.CallbackSupprimer() {
                    @Override
                    public void onSucces() {
                        listeItineraires.remove(position);
                        adaptateur.notifyItemRemoved(position);

                        if (listeItineraires.isEmpty()) {
                            layoutAucunItineraire.setVisibility(View.VISIBLE);
                        }

                        Toast.makeText(ItineraireActivity.this,
                                "Itinéraire supprimé",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onErreur(String messageErreur) {
                        Toast.makeText(ItineraireActivity.this,
                                "Erreur suppression : " + messageErreur,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}