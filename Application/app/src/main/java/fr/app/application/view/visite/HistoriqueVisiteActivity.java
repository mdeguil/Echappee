package fr.app.application.view.visite;

import android.content.Intent;
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
import fr.app.application.controller.VisiteController;
import fr.app.application.model.Visite;
import fr.app.application.view.adapter.HistoriqueVisiteAdapter;
import fr.app.application.view.itiniraires.CreerItineraireActivity;
import fr.app.application.view.itiniraires.ItineraireActivity;
import fr.app.application.view.lieux.ListeLieuxActivity;

public class HistoriqueVisiteActivity extends AppCompatActivity {

    private ProgressBar    barreChargement;
    private TextView       tvAucuneVisite;
    private RecyclerView   recyclerVisites;
    private HistoriqueVisiteAdapter adapter;
    private VisiteController visiteController;

    private com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigationView;

    private final List<Visite> visites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historique_visite);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Historique des visites");
        }

        visiteController = new VisiteController(this);

        initVues();
        configurerBottomNav();
        chargerVisites();
    }

    private void initVues() {
        barreChargement      = findViewById(R.id.barreChargement);
        tvAucuneVisite       = findViewById(R.id.tvAucuneVisite);
        recyclerVisites      = findViewById(R.id.recyclerVisites);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        recyclerVisites.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoriqueVisiteAdapter(visites, this::supprimerVisite);
        recyclerVisites.setAdapter(adapter);
    }

    private void configurerBottomNav() {
        bottomNavigationView.setSelectedItemId(R.id.nav_historique);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_historique) {
                return true;

            } else if (id == R.id.nav_liste_lieux) {
                startActivity(new Intent(this, ListeLieuxActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_creer_itineraire) {
                startActivity(new Intent(this, CreerItineraireActivity.class));
                finish();
                return true;

            } else if (id == R.id.nav_liste_itineraires) {
                startActivity(new Intent(this, ItineraireActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void chargerVisites() {
        barreChargement.setVisibility(View.VISIBLE);
        tvAucuneVisite.setVisibility(View.GONE);

        visiteController.recupererVisites(new VisiteController.CallbackVisites() {
            @Override
            public void onSucces(List<Visite> liste) {
                barreChargement.setVisibility(View.GONE);
                visites.clear();
                visites.addAll(liste);
                adapter.notifyDataSetChanged();
                tvAucuneVisite.setVisibility(visites.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onErreur(String messageErreur) {
                barreChargement.setVisibility(View.GONE);
                Toast.makeText(HistoriqueVisiteActivity.this,
                        "❌ " + messageErreur, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void supprimerVisite(Visite visite, int position) {
        visiteController.supprimerVisite(visite.getId(),
                new VisiteController.CallbackSupprimer() {
                    @Override
                    public void onSucces() {
                        visites.remove(position);
                        adapter.notifyItemRemoved(position);
                        tvAucuneVisite.setVisibility(
                                visites.isEmpty() ? View.VISIBLE : View.GONE);
                        Toast.makeText(HistoriqueVisiteActivity.this,
                                "✅ Visite supprimée", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onErreur(String messageErreur) {
                        Toast.makeText(HistoriqueVisiteActivity.this,
                                "❌ " + messageErreur, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}