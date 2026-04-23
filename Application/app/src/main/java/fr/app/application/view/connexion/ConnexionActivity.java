package fr.app.application.view.connexion;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import fr.app.application.model.Utilisateur;
import fr.app.application.utils.ApiConfig;
import fr.app.application.utils.BDD.AppDatabase;
import fr.app.application.utils.SessionManager;
import fr.app.application.view.inscription.InscriptionActivity;
import fr.app.application.view.lieux.ListeLieuxActivity;

public class ConnexionActivity extends AppCompatActivity {

    private static final String TAG            = "ConnexionActivity";
    private static final String LOGIN_ENDPOINT = "/api/login_check";

    private TextInputLayout   tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton    btnLogin, btnRegister;
    private ProgressBar       progressBar;
    private TextView          tvError;

    private SessionManager sessionManager;
    private AppDatabase    db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        sessionManager = new SessionManager(this);
        db             = AppDatabase.getDatabase(this);

        // ── Auto-login offline ────────────────────────────────────────────
        // Si un token JWT est déjà sauvegardé → on passe directement à l'accueil
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Session existante détectée, redirection directe (token présent)");
            navigateToMain();
            return;
        }

        Log.d(TAG, "Aucune session trouvée, affichage du formulaire de connexion");
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
            if (validateInputs(email, mdp)) login(email, mdp);
        });

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(v.getContext(), InscriptionActivity.class))
        );
    }

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

    private void login(String email, String mdp) {
        setLoading(true);
        String loginUrl = ApiConfig.getInstance(this).getUrl(LOGIN_ENDPOINT);

        new Thread(() -> {
            String result       = null;
            String errorMsg     = null;
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
                handleLoginResponse(finalCode, finalResult, finalError, email, mdp);
            });
        }).start();
    }

    private void handleLoginResponse(int code, String result, String errorMsg,
                                     String email, String mdp) {
        if (code == HttpURLConnection.HTTP_OK && result != null) {
            try {
                JSONObject json = new JSONObject(result.trim());
                if (json.has("token")) {
                    String token = json.getString("token");

                    // Tenter d'extraire l'userId du JWT (facultatif)
                    int userId = extraireUserIdDuToken(token);
                    Log.d(TAG, "Login OK — userId extrait du JWT : " + userId);

                    // Sauvegarder la session (token suffit pour isLoggedIn)
                    sessionManager.saveSession(token, userId, email);

                    // Sauvegarder l'utilisateur en BDD locale (thread background)
                    sauvegarderUtilisateurEnBDD(userId, email, mdp, token);

                    navigateToMain();
                } else {
                    showError("Clé 'token' absente de la réponse");
                }
            } catch (Exception e) {
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
     * Tente d'extraire l'ID utilisateur du payload JWT.
     * Retourne 0 si impossible (l'API ne l'inclut pas forcément).
     */
    private int extraireUserIdDuToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return 0;
            String payload = parts[1];
            int padding = (4 - payload.length() % 4) % 4;
            for (int i = 0; i < padding; i++) payload += "=";
            byte[]     decoded = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE);
            JSONObject json    = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            Log.d(TAG, "JWT payload : " + json.toString());
            if (json.has("id"))      return json.getInt("id");
            if (json.has("user_id")) return json.getInt("user_id");
            if (json.has("userId"))  return json.getInt("userId");
        } catch (Exception e) {
            Log.w(TAG, "Impossible de décoder l'userId depuis le JWT : " + e.getMessage());
        }
        return 0; // valeur par défaut non bloquante
    }

    private void sauvegarderUtilisateurEnBDD(int userId, String email, String mdp, String token) {
        new Thread(() -> {
            try {
                Utilisateur u = new Utilisateur();
                u.setId(userId);
                u.setEmail(email);
                u.setPassword(mdp);
                u.setToken(token);
                db.myDao().clearUsers();
                db.myDao().setLastUser(u);
                Log.d(TAG, "Utilisateur sauvegardé en BDD locale (id=" + userId + ")");
            } catch (Exception e) {
                Log.e(TAG, "Erreur sauvegarde BDD : " + e.getMessage());
            }
        }).start();
    }

    private void navigateToMain() {
        startActivity(new Intent(this, ListeLieuxActivity.class));
        finish();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        tvError.setVisibility(View.GONE);
    }
}