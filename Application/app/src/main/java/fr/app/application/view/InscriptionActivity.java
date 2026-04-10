package fr.app.application.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import fr.app.application.R;
import fr.app.application.utils.VolleyUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONObject;

public class InscriptionActivity extends AppCompatActivity {

    private static final String URL_INSCRIPTION = "http://192.168.0.70:8000/api/utilisateurs";

    // ─── Vues ────────────────────────────────────────────────────────────────
    private TextInputLayout   tilEmail, tilMotDePasse, tilConfirmationMotDePasse;
    private TextInputEditText etEmail, etMotDePasse, etConfirmationMotDePasse;
    private TextView           tvErreur;
    private ProgressBar        barreChargement;
    private MaterialButton     btnInscription, btnRetourConnexion;

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        initialiserVues();
        configurerBoutons();
    }

    // ─── Initialisation ──────────────────────────────────────────────────────

    private void initialiserVues() {
        tilEmail                 = findViewById(R.id.tilEmail);
        tilMotDePasse            = findViewById(R.id.tilMotDePasse);
        tilConfirmationMotDePasse= findViewById(R.id.tilConfirmationMotDePasse);

        etEmail                  = findViewById(R.id.etEmail);
        etMotDePasse             = findViewById(R.id.etMotDePasse);
        etConfirmationMotDePasse = findViewById(R.id.etConfirmationMotDePasse);

        tvErreur                 = findViewById(R.id.tvErreur);
        barreChargement          = findViewById(R.id.barreChargement);
        btnInscription           = findViewById(R.id.btnInscription);
        btnRetourConnexion       = findViewById(R.id.btnRetourConnexion);
    }

    private void configurerBoutons() {
        btnInscription.setOnClickListener(v -> tenterInscription());

        btnRetourConnexion.setOnClickListener(v -> {
            startActivity(new Intent(this, ConnexionActivity.class));
            finish();
        });
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    private boolean validerFormulaire(String email, String motDePasse,
                                      String confirmation) {
        // Réinitialiser les erreurs
        tilEmail.setError(null);
        tilMotDePasse.setError(null);
        tilConfirmationMotDePasse.setError(null);
        masquerErreur();

        boolean valide = true;

        if (email.isEmpty()) {
            tilEmail.setError("L'e-mail est obligatoire");
            valide = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Adresse e-mail invalide");
            valide = false;
        }
        if (motDePasse.isEmpty()) {
            tilMotDePasse.setError("Le mot de passe est obligatoire");
            valide = false;
        } else if (motDePasse.length() < 6) {
            tilMotDePasse.setError("Minimum 6 caractères");
            valide = false;
        }
        if (!motDePasse.equals(confirmation)) {
            tilConfirmationMotDePasse.setError("Les mots de passe ne correspondent pas");
            valide = false;
        }

        return valide;
    }

    // ─── Inscription ─────────────────────────────────────────────────────────

    private void tenterInscription() {
        String email        = texte(etEmail);
        String motDePasse   = texte(etMotDePasse);
        String confirmation = texte(etConfirmationMotDePasse);

        if (!validerFormulaire(email, motDePasse, confirmation)) {
            return;
        }

        afficherChargement(true);

        try {
            JSONObject corps = new JSONObject();
            corps.put("email",       email);
            corps.put("password",    motDePasse);

            JsonObjectRequest requete = new JsonObjectRequest(
                    Request.Method.POST,
                    URL_INSCRIPTION,
                    corps,
                    reponse -> {
                        afficherChargement(false);
                        // Inscription réussie → retour à la connexion
                        startActivity(new Intent(this, ConnexionActivity.class));
                        finish();
                    },
                    erreur -> {
                        afficherChargement(false);
                        String messageErreur = "Erreur lors de l'inscription";
                        if (erreur.networkResponse != null
                                && erreur.networkResponse.statusCode == 422) {
                            messageErreur = "Cet e-mail est déjà utilisé";
                        }
                        afficherErreur(messageErreur);
                    }
            );

            requete.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                    10000,
                    0,    
                    com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleyUtils.getInstance(this).addToRequestQueue(requete);

        } catch (Exception e) {
            afficherChargement(false);
            afficherErreur("Erreur : " + e.getMessage());
        }
    }

    // ─── Utilitaires ─────────────────────────────────────────────────────────

    private String texte(TextInputEditText champ) {
        return champ.getText() != null ? champ.getText().toString().trim() : "";
    }

    private void afficherErreur(String message) {
        tvErreur.setText(message);
        tvErreur.setVisibility(View.VISIBLE);
    }

    private void masquerErreur() {
        tvErreur.setVisibility(View.GONE);
    }

    private void afficherChargement(boolean visible) {
        barreChargement.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnInscription.setEnabled(!visible);
    }
}