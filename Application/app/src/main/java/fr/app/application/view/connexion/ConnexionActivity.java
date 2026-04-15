package fr.app.application.view.connexion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import fr.app.application.R;
import fr.app.application.utils.ApiConfig;
import fr.app.application.view.inscription.InscriptionActivity;
import fr.app.application.view.lieux.ListeLieuxActivity;

public class ConnexionActivity extends AppCompatActivity {

    private static final String LOGIN_ENDPOINT = "/api/login_check";

    private TextInputLayout    tilEmail, tilPassword;
    private TextInputEditText  etEmail, etPassword;
    private MaterialButton     btnLogin, btnRegister;
    private ProgressBar        progressBar;
    private TextView           tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegistration);
        progressBar = findViewById(R.id.progressBar);
        tvError     = findViewById(R.id.tvError);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String mdp   = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            if (validateInputs(email, mdp)) {
                login(email, mdp);
            }
        });

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), InscriptionActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Valide la conformité des saisies utilisateur avant l'envoi à l'API.
     *
     * @param email L'adresse email saisie.
     * @param mdp   Le mot de passe saisi.
     * @return true si toutes les conditions de validation sont remplies.
     */
    private boolean validateInputs(String email, String mdp) {
        boolean valid = true;
        tilEmail.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);

        if (email.isEmpty()) {
            tilEmail.setError("L'e-mail est requis");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Format d'e-mail invalide");
            valid = false;
        }

        if (mdp.isEmpty()) {
            tilPassword.setError("Le mot de passe est requis");
            valid = false;
        } else if (mdp.length() < 6) {
            tilPassword.setError("Minimum 6 caractères");
            valid = false;
        }

        return valid;
    }

    /**
     * Exécute la requête d'authentification vers l'API.
     *
     * @param email L'identifiant utilisateur.
     * @param mdp   Le mot de passe associé.
     */
    private void login(String email, String mdp) {
        setLoading(true);

        // L'URL de base est lue depuis le singleton au moment de l'appel
        String loginUrl = ApiConfig.getInstance(this).getUrl(LOGIN_ENDPOINT);

        new Thread(() -> {
            String result     = null;
            String errorMsg   = null;
            int    responseCode = 0;

            try {
                URL url = new URL(loginUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", mdp);

                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.close();

                responseCode = conn.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder sb = new StringBuilder();
                    while (scanner.hasNextLine()) sb.append(scanner.nextLine());
                    scanner.close();
                    result = sb.toString();
                } else {
                    Scanner scanner = new Scanner(conn.getErrorStream());
                    StringBuilder sb = new StringBuilder();
                    while (scanner.hasNextLine()) sb.append(scanner.nextLine());
                    scanner.close();
                    errorMsg = sb.toString();
                }

                conn.disconnect();

            } catch (Exception e) {
                errorMsg = "Erreur réseau : " + e.getMessage();
            }

            final String finalResult = result;
            final String finalError  = errorMsg;
            final int    finalCode   = responseCode;

            runOnUiThread(() -> {
                setLoading(false);
                handleLoginResponse(finalCode, finalResult, finalError);
            });

        }).start();
    }

    /**
     * Analyse la réponse HTTP retournée par le serveur.
     * * En cas de code 200 (OK), extrait le jeton JWT du JSON.
     * Gère les erreurs spécifiques comme le code 401 (identifiants invalides)
     * ou les erreurs de formatage JSON.
     *
     * @param code     Le code de réponse HTTP.
     * @param result   Le corps de la réponse en cas de succès.
     * @param errorMsg Le message d'erreur en cas d'échec technique.
     */
    private void handleLoginResponse(int code, String result, String errorMsg) {
        if (code == HttpURLConnection.HTTP_OK && result != null) {
            try {
                String cleanResult = result.trim();
                JSONObject json = new JSONObject(cleanResult);

                if (json.has("token")) {
                    String token = json.getString("token");
                    saveToken(token);
                    navigateToMain();
                } else {
                    showError("Clé 'token' absente de la réponse");
                }
            } catch (Exception e) {
                android.util.Log.e("JSON_PARSE_ERROR", "Erreur sur : " + result);
                showError("Erreur de format JSON : " + e.getMessage());
            }
        } else if (code == 401) {
            showError("Email ou mot de passe incorrect");
        } else if (errorMsg != null && !errorMsg.isEmpty()) {
            showError("Erreur : " + errorMsg);
        } else {
            showError("Erreur serveur (code " + code + ")");
        }
    }

    /**
     * Enregistre le jeton de sécurité de manière persistante.
     * * Utilise les SharedPreferences pour conserver le jeton JWT, permettant
     * ainsi d'authentifier les requêtes ultérieures vers l'API.
     *
     * @param token Le jeton JWT fourni par le serveur.
     */
    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit().putString("jwt_token", token).apply();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, ListeLieuxActivity.class));
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    /**
     * Gère l'état visuel des composants pendant le chargement.
     *
     * @param loading true pour afficher l'état de chargement.
     */
    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvError.setVisibility(View.GONE);
    }
}
