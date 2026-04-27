package fr.app.application.view.itiniraires;

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
import fr.app.application.view.adapter.ItineraireAdapter;

public class ItineraireActivity extends AppCompatActivity {

    private ProgressBar       barreChargement;
    private View               layoutAucunItineraire;
    private RecyclerView       recyclerItineraires;
    private ItineraireAdapter  adaptateur;
    private ItineraireController controleur;

    private final List<Itineraire> listeItineraires = new ArrayList<>();

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
        chargerItineraires();
    }

    private void initVues() {
        barreChargement       = findViewById(R.id.barreChargementItineraire);
        layoutAucunItineraire = findViewById(R.id.layoutAucunItineraire);
        recyclerItineraires   = findViewById(R.id.recyclerItineraires);

        recyclerItineraires.setLayoutManager(new LinearLayoutManager(this));

        // Passer le callback de suppression à l'adapter
        adaptateur = new ItineraireAdapter(this, listeItineraires, this::supprimerItineraire);
        recyclerItineraires.setAdapter(adaptateur);

        controleur = new ItineraireController(this);
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

    // ── Suppression ───────────────────────────────────────────────────────

    private void supprimerItineraire(Itineraire itineraire, int position) {
        controleur.supprimerItineraire(itineraire.getId(),
                new ItineraireController.CallbackSupprimer() {
                    @Override
                    public void onSucces() {
                        // Retirer de la liste et mettre à jour l'affichage
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