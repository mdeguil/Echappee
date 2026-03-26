package fr.app.application.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import fr.app.application.R;
import fr.app.application.controller.LieuController;
import fr.app.application.model.Lieu;

/**
 * Activity principale affichant la liste des lieux dans un RecyclerView.
 */
public class ListeLieuxActivity extends AppCompatActivity {

    private RecyclerView  recyclerView;
    private LieuAdapter   adaptateur;
    private ProgressBar   chargement;
    private LieuController controleurLieu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_lieux);

        // Initialisation des vues
        recyclerView = findViewById(R.id.recyclerViewLieux);
        chargement   = findViewById(R.id.progressBarChargement);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adaptateur = new LieuAdapter(this, new ArrayList<>(), this::onLieuClique);
        recyclerView.setAdapter(adaptateur);

        // Controller
        controleurLieu = new LieuController(this);

        // Chargement des lieux
        chargerLieux();
    }

    private void chargerLieux() {
        chargement.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);

        controleurLieu.recupererLieux(new LieuController.CallbackLieux() {
            @Override
            public void onSucces(java.util.List<Lieu> lieux) {
                chargement.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adaptateur.mettreAJourListe(lieux);
            }

            @Override
            public void onErreur(String messageErreur) {
                chargement.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(ListeLieuxActivity.this, messageErreur, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void onLieuClique(Lieu lieu) {
        startActivity(DetailLieuActivity.creerIntent(this, lieu));
    }
}