package fr.app.application.view.visite;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.app.application.R;
import fr.app.application.controller.VisiteController;
import fr.app.application.model.Visite;

public class VisiteActivity extends AppCompatActivity {

    public static final String EXTRA_LIEU_ID  = "extra_lieu_id";
    public static final String EXTRA_LIEU_NOM = "extra_lieu_nom";

    private int      lieuId;
    private Calendar dateSelectionnee;

    private TextView          tvNomLieu;
    private MaterialButton    btnDate;
    private RatingBar         ratingBar;
    private TextInputEditText etCommentaire;
    private MaterialButton    btnEnregistrer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visite);

        lieuId = getIntent().getIntExtra(EXTRA_LIEU_ID, -1);
        String nomLieu = getIntent().getStringExtra(EXTRA_LIEU_NOM);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Enregistrer ma visite");
        }

        initVues();
        tvNomLieu.setText(nomLieu != null ? nomLieu : "Lieu");

        // Date du jour par défaut
        dateSelectionnee = Calendar.getInstance();
        mettreAJourBoutonDate();

        btnDate.setOnClickListener(v -> ouvrirDatePicker());
        btnEnregistrer.setOnClickListener(v -> enregistrerVisite());
    }

    @SuppressLint("WrongViewCast")
    private void initVues() {
        tvNomLieu = findViewById(R.id.tvVisiteNomLieu);
        btnDate = findViewById(R.id.btnDateVisite);
        ratingBar = findViewById(R.id.ratingBarVisite);
        etCommentaire = findViewById(R.id.etCommentaireVisite);
        btnEnregistrer = findViewById(R.id.btnEnregistrerVisite);
    }

    private void mettreAJourBoutonDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE);
        btnDate.setText(sdf.format(dateSelectionnee.getTime()));
    }

    private void ouvrirDatePicker() {
        new DatePickerDialog(
                this,
                (view, annee, mois, jour) -> {
                    dateSelectionnee.set(annee, mois, jour);
                    mettreAJourBoutonDate();
                },
                dateSelectionnee.get(Calendar.YEAR),
                dateSelectionnee.get(Calendar.MONTH),
                dateSelectionnee.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void enregistrerVisite() {
        String message = etCommentaire.getText() != null
                ? etCommentaire.getText().toString().trim() : "";
        int note = (int) ratingBar.getRating();

        if (message.isEmpty()) {
            etCommentaire.setError("Veuillez saisir un commentaire");
            return;
        }

        btnEnregistrer.setEnabled(false);
        btnEnregistrer.setText("Envoi en cours...");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        String date = sdf.format(dateSelectionnee.getTime());

        VisiteController controller = new VisiteController(this);
        controller.creerVisite(date, note, message, lieuId,
                new VisiteController.CallbackCreerVisite() {
                    @Override
                    public void onSucces(Visite visite) {
                        Toast.makeText(VisiteActivity.this,
                                "✅ Visite enregistrée !", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onErreur(String messageErreur) {
                        btnEnregistrer.setEnabled(true);
                        btnEnregistrer.setText("✅ Enregistrer la visite");
                        Toast.makeText(VisiteActivity.this,
                                "❌ " + messageErreur, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private Map<String, String> entetes() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type",  "application/json");
        headers.put("Accept",        "application/json");
        // headers.put("Authorization", "Bearer " + token); // si JWT
        return headers;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}