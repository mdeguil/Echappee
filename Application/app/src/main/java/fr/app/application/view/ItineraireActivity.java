package fr.app.application.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.app.application.R;
import fr.app.application.controller.ItineraireController;
import fr.app.application.model.Itineraire;

/**
 * Affiche la liste de tous les itinéraires disponibles.
 * Lancée depuis ListeLieuxActivity via le bouton "Voir itinéraire".
 */
public class ItineraireActivity extends AppCompatActivity {

    private ProgressBar          barreChargement;
    private TextView             tvAucunItineraire;
    private RecyclerView         recyclerItineraires;
    private ItineraireAdapter    adaptateur;
    private ItineraireController controleur;

    private List<Itineraire> listeItineraires = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itineraire);

        // Bouton retour dans la toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Itinéraires");
        }

        initVues();
        chargerItineraires();
    }

    private void initVues() {
        barreChargement   = findViewById(R.id.barreChargementItineraire);
        tvAucunItineraire = findViewById(R.id.tvAucunItineraire);
        recyclerItineraires = findViewById(R.id.recyclerItineraires);

        recyclerItineraires.setLayoutManager(new LinearLayoutManager(this));
        adaptateur = new ItineraireAdapter(this, listeItineraires);
        recyclerItineraires.setAdapter(adaptateur);

        controleur = new ItineraireController(this);
    }

    private void chargerItineraires() {
        barreChargement.setVisibility(View.VISIBLE);
        tvAucunItineraire.setVisibility(View.GONE);

        controleur.recupererItineraires(new ItineraireController.CallbackItineraires() {
            @Override
            public void onSucces(List<Itineraire> itineraires) {
                barreChargement.setVisibility(View.GONE);

                listeItineraires.clear();
                listeItineraires.addAll(itineraires);
                adaptateur.notifyDataSetChanged();

                if (listeItineraires.isEmpty()) {
                    tvAucunItineraire.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onErreur(String messageErreur) {
                barreChargement.setVisibility(View.GONE);
                Toast.makeText(ItineraireActivity.this,
                        "Erreur : " + messageErreur,
                        Toast.LENGTH_LONG).show();
            }
        });
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
