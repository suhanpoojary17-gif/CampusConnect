package com.campusconnect.app.features.campus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CampusFacilitiesFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String FACILITIES_ENDPOINT = "/api/facilities";

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExecutorService executor;

    private LinearLayout rootLayout;
    private LinearLayout facilitiesContainer;
    private ProgressBar progressBar;
    private TextView statusText;
    private Button retryButton;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        Context context = requireContext();

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(
                context.getColor(R.color.main_background)
        );

        rootLayout = new LinearLayout(context);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(24)
        );

        scrollView.addView(
                rootLayout,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView title = new TextView(context);
        title.setText("Campus Facilities");
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setTextColor(context.getColor(R.color.primary_text));

        rootLayout.addView(
                title,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView subtitle = new TextView(context);
        subtitle.setText("Explore the facilities available on campus.");
        subtitle.setTextSize(14);
        subtitle.setTextColor(context.getColor(R.color.secondary_text));

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(6);

        rootLayout.addView(subtitle, subtitleParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(28);

        rootLayout.addView(progressBar, progressParams);

        statusText = new TextView(context);
        statusText.setTextColor(context.getColor(R.color.secondary_text));
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(16);

        rootLayout.addView(statusText, statusParams);

        retryButton = new Button(context);
        retryButton.setText("Try Again");
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> loadFacilities());

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        retryParams.topMargin = dp(12);

        rootLayout.addView(retryButton, retryParams);

        facilitiesContainer = new LinearLayout(context);
        facilitiesContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams facilitiesParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        facilitiesParams.topMargin = dp(20);

        rootLayout.addView(facilitiesContainer, facilitiesParams);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        loadFacilities();
    }

    private void loadFacilities() {
        if (!isAdded()
                || rootLayout == null
                || facilitiesContainer == null) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        Context context = requireContext();

        SharedPreferences preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showError(
                    "Your session has expired. Please sign in again.",
                    false
            );
            return;
        }

        showLoading();

        executor.execute(() -> {
            try {
                JSONArray response = getFacilities(token);

                mainHandler.post(() -> {
                    if (!isAdded() || facilitiesContainer == null) {
                        return;
                    }

                    renderFacilities(response);
                });

            } catch (Exception exception) {
                String message = getFriendlyErrorMessage(exception);

                mainHandler.post(() -> {
                    if (!isAdded() || facilitiesContainer == null) {
                        return;
                    }

                    showError(message, true);
                });
            }
        });
    }

    private JSONArray getFacilities(String token) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + FACILITIES_ENDPOINT);

            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            int status = connection.getResponseCode();

            InputStream stream =
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            StringBuilder result = new StringBuilder();

            if (stream != null) {
                try (
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        stream,
                                        StandardCharsets.UTF_8
                                )
                        )
                ) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            }

            if (status == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new IllegalStateException(
                        "Your session has expired. Please sign in again."
                );
            }

            if (status == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new IllegalStateException(
                        "You do not have permission to view these facilities."
                );
            }

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "Unable to load facilities. HTTP "
                                + status
                );
            }

            return new JSONArray(result.toString());

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void renderFacilities(JSONArray facilities) {
        if (facilitiesContainer == null || !isAdded()) {
            return;
        }

        facilitiesContainer.removeAllViews();

        progressBar.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);

        if (facilities.length() == 0) {
            statusText.setText("No campus facilities are available yet.");
            statusText.setVisibility(View.VISIBLE);
            return;
        }

        for (int i = 0; i < facilities.length(); i++) {
            JSONObject facility = facilities.optJSONObject(i);

            if (facility == null) {
                continue;
            }

            addFacilityCard(facility);
        }

        if (facilitiesContainer.getChildCount() == 0) {
            statusText.setText("No valid facility records were returned.");
            statusText.setVisibility(View.VISIBLE);
        }
    }

    private void addFacilityCard(JSONObject facility) {
        Context context = requireContext();

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        card.setBackgroundResource(R.drawable.bg_card);

        TextView name = new TextView(context);
        name.setText(getDisplayValue(facility, "name", "Unnamed facility"));
        name.setTextSize(18);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setTextColor(context.getColor(R.color.primary_text));

        card.addView(
                name,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        addDetail(
                card,
                "Type",
                getDisplayValue(facility, "type", "Not specified")
        );

        addDetail(
                card,
                "Location",
                getDisplayValue(facility, "location", "Not specified")
        );

        String capacity;

        if (facility.isNull("capacity")
                || !facility.has("capacity")) {
            capacity = "Not specified";
        } else {
            capacity = facility.optString("capacity", "Not specified");
        }

        addDetail(card, "Capacity", capacity);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(14);

        facilitiesContainer.addView(card, cardParams);
    }

    private void addDetail(
            LinearLayout card,
            String label,
            String value
    ) {
        Context context = requireContext();

        TextView detail = new TextView(context);
        detail.setText(label + ": " + value);
        detail.setTextSize(14);
        detail.setTextColor(context.getColor(R.color.secondary_text));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(8);

        card.addView(detail, params);
    }

    private String getDisplayValue(
            JSONObject object,
            String key,
            String fallback
    ) {
        String value = object.optString(key, "");

        if (TextUtils.isEmpty(value)
                || "null".equalsIgnoreCase(value.trim())) {
            return fallback;
        }

        return value;
    }

    private void showLoading() {
        if (rootLayout == null) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText("Loading campus facilities...");
        retryButton.setVisibility(View.GONE);
        facilitiesContainer.removeAllViews();
    }

    private void showError(String message, boolean allowRetry) {
        if (rootLayout == null) {
            return;
        }

        progressBar.setVisibility(View.GONE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(message);
        retryButton.setVisibility(
                allowRetry ? View.VISIBLE : View.GONE
        );

        facilitiesContainer.removeAllViews();
    }

    private String getFriendlyErrorMessage(Exception exception) {
        String message = exception.getMessage();

        if (message != null
                && (message.contains("session has expired")
                || message.contains("permission to view"))) {
            return message;
        }

        return "Unable to load campus facilities. "
                + "Check your connection and try again.";
    }

    private int dp(int value) {
        return (int) (
                value * requireContext()
                        .getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    public void onDestroyView() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        mainHandler.removeCallbacksAndMessages(null);

        rootLayout = null;
        facilitiesContainer = null;
        progressBar = null;
        statusText = null;
        retryButton = null;

        super.onDestroyView();
    }
}