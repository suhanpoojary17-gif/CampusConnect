package com.campusconnect.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.util.Log;
import com.campusconnect.app.navigation.AppNavigator;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    /*
- Android emulator address for a backend running
- on the development computer.
     */
    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String LOGIN_ENDPOINT =
            BASE_URL + "/api/auth/login";

    private static final String PREFS_NAME =
            "campusconnect_session";

    private static final String TOKEN_KEY = "jwt_token";
    private static final String ROLE_KEY = "user_role";
    private static final String EMAIL_KEY = "user_email";

    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    private MaterialButton loginButton;
    private ProgressBar progressBar;
    private TextView statusText;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        statusText = findViewById(R.id.statusText);

        executorService = Executors.newSingleThreadExecutor();

        loginButton.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {

        emailLayout.setError(null);
        passwordLayout.setError(null);
        statusText.setVisibility(View.GONE);

        String email = emailInput.getText() == null
                ? ""
                : emailInput.getText().toString().trim();

        String password = passwordInput.getText() == null
                ? ""
                : passwordInput.getText().toString();

        boolean valid = true;

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            valid = false;
        }

        if (!valid) {
            return;
        }

        setLoading(true);

        executorService.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(LOGIN_ENDPOINT);
                Log.d("CampusConnectLogin",
                        "Starting login request: " + LOGIN_ENDPOINT);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                Log.d("CampusConnectLogin",
                        "Connection configured; preparing request body");

                JSONObject requestBody = new JSONObject();
                requestBody.put("email", email);
                requestBody.put("password", password);

                byte[] requestBytes = requestBody
                        .toString()
                        .getBytes(StandardCharsets.UTF_8);

                Log.d("CampusConnectLogin",
                        "Writing login request body");

                try (OutputStream outputStream =
                             connection.getOutputStream()) {

                    outputStream.write(requestBytes);
                    outputStream.flush();
                }

                Log.d("CampusConnectLogin",
                        "Request body sent; waiting for response");

                int responseCode = connection.getResponseCode();

                Log.d("CampusConnectLogin",
                        "Received HTTP response: " + responseCode);

                InputStream responseStream;

                if (responseCode >= 200 && responseCode < 300) {
                    responseStream = connection.getInputStream();
                } else {
                    responseStream = connection.getErrorStream();
                }

                String responseBody =
                        readResponse(responseStream);

                JSONObject responseJson;

                if (TextUtils.isEmpty(responseBody)) {
                    responseJson = new JSONObject();
                } else {
                    responseJson = new JSONObject(responseBody);
                }

                if (responseCode >= 200 && responseCode < 300) {

                    String token =
                            responseJson.optString("token", "");

                    String role =
                            responseJson.optString("role", "");

                    if (TextUtils.isEmpty(token)
                            || TextUtils.isEmpty(role)) {

                        showError(
                                "The server returned an invalid login response."
                        );

                        return;
                    }

                    if (!"STUDENT".equalsIgnoreCase(role)) {

                        showError(
                                "This account is not a student account."
                        );

                        return;
                    }

                    saveSession(token, role, email);
                    runOnUiThread(() -> {
                        setLoading(false);
                        AppNavigator.openStudentDashboard(MainActivity.this);
                    });

                } else {

                    String message = responseJson.optString(
                            "error",
                            "Login failed. Please check your credentials."
                    );

                    showError(message);
                }

            } catch (java.net.SocketTimeoutException e) {

                Log.e("CampusConnectLogin",
                        "Login request timed out: " + LOGIN_ENDPOINT, e);

                showError(
                        "The server took too long to respond. Please try again."
                );

            } catch (java.net.ConnectException e) {

                showError(
                        "Cannot connect to CampusConnect. Check that the backend is running."
                );

            } catch (Exception e) {

                Log.e("CampusConnectLogin",
                        "Unexpected login error", e);

                showError(
                        "Unable to complete login. Please try again."
                );

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readResponse(InputStream inputStream)
            throws Exception {

        if (inputStream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                ))) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    private void saveSession(
            String token,
            String role,
            String email) {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREFS_NAME,
                        MODE_PRIVATE
                );

        preferences.edit()
                .putString(TOKEN_KEY, token)
                .putString(ROLE_KEY, role)
                .putString(EMAIL_KEY, email)
                .apply();
    }

    private void showError(String message) {

        runOnUiThread(() -> {
            setLoading(false);

            statusText.setText(message);

            statusText.setTextColor(
                    getColor(android.R.color.holo_red_dark)
            );

            statusText.setVisibility(View.VISIBLE);
        });
    }

    private void setLoading(boolean loading) {

        loginButton.setEnabled(!loading);

        loginButton.setText(
                loading ? "Signing in..." : "Sign In"
        );

        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}