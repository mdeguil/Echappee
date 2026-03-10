package com.example.application;

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

public class ConnexionActivity extends AppCompatActivity {

    // ⚠️ Remplacez par l'URL de votre API Symfony
    private static final String API_BASE_URL = "http://10.0.2.2:8000"; // localhost depuis l'émulateur
    private static final String LOGIN_ENDPOINT = "/api/login_check";

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);

        // Si déjà connecté, rediriger vers MainActivity
        if (isLoggedIn()) {
            navigateToMain();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail    = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail     = findViewById(R.id.etEmail);
        etPassword  = findViewById(R.id.etPassword);
        btnLogin    = findViewById(R.id.btnLogin);
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
    }

    /** Validation locale avant envoi */
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

    /** Appel API Symfony /api/login_check (LexikJWT) */
    private void login(String email, String mdp) {
        setLoading(true);

        new Thread(() -> {
            String result = null;
            String errorMsg = null;
            int responseCode = 0;

            try {
                URL url = new URL(API_BASE_URL + LOGIN_ENDPOINT);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                // Corps JSON : {"username": "...", "password": "..."}
                // Symfony LexikJWT attend "username" par défaut (= votre champ mail)
                JSONObject body = new JSONObject();
                body.put("username", email);
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

            final String finalResult   = result;
            final String finalError    = errorMsg;
            final int    finalCode     = responseCode;

            runOnUiThread(() -> {
                setLoading(false);
                handleLoginResponse(finalCode, finalResult, finalError);
            });

        }).start();
    }

    private void handleLoginResponse(int code, String result, String errorMsg) {
        if (code == HttpURLConnection.HTTP_OK && result != null) {
            try {
                JSONObject json = new JSONObject(result);
                String token = json.getString("token");
                saveToken(token);
                navigateToMain();
            } catch (Exception e) {
                showError("Réponse inattendue du serveur");
            }
        } else if (code == 401) {
            showError("Email ou mot de passe incorrect");
        } else if (errorMsg != null && !errorMsg.isEmpty()) {
            showError("Erreur : " + errorMsg);
        } else {
            showError("Erreur serveur (code " + code + ")");
        }
    }

    /** Sauvegarde le JWT dans SharedPreferences */
    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        prefs.edit().putString("jwt_token", token).apply();
    }

    /** Vérifie si un token est déjà stocké */
    private boolean isLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        return prefs.getString("jwt_token", null) != null;
    }

    private void navigateToMain() {
        startActivity(new Intent(this, MainActivity.class));
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