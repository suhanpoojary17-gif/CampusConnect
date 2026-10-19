package com.campusconnect.app.features.campus;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

public class BusPassFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String BUS_PASSES_ENDPOINT = "/api/bus-passes/my";

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExecutorService executor;

    private LinearLayout root;
    private LinearLayout passesContainer;
    private ProgressBar progressBar;
    private TextView statusText;
    private Button retryButton;

    public BusPassFragment() {
        // Required empty constructor.
    }

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

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));

        scrollView.addView(
                root,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView eyebrow = new TextView(context);
        eyebrow.setText("CAMPUSCONNECT");
        eyebrow.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);

        root.addView(eyebrow);

        TextView title = new TextView(context);
        title.setText("Digital Bus Pass");
        title.setTextColor(
                context.getColor(R.color.primary_text)
        );
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        titleParams.topMargin = dp(8);

        root.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText(
                "Apply for your student bus pass and track its status."
        );
        subtitle.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        subtitle.setTextSize(14);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        subtitleParams.topMargin = dp(6);

        root.addView(subtitle, subtitleParams);

        addSectionHeading(root, "BUS PASS SERVICES", dp(28));

        addNavigationCard(
                root,
                "Apply for Bus Pass",
                "Submit a new application with your route and payment details.",
                "01",
                this::openApplication
        );

        addSectionHeading(root, "MY BUS PASSES", dp(28));

        LinearLayout statusCard = new LinearLayout(context);
        statusCard.setOrientation(LinearLayout.VERTICAL);
        statusCard.setPadding(dp(16), dp(16), dp(16), dp(16));
        statusCard.setBackgroundResource(R.drawable.bg_card);

        root.addView(
                statusCard,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        TextView statusHeading = new TextView(context);
        statusHeading.setText("Application History");
        statusHeading.setTextColor(
                context.getColor(R.color.primary_text)
        );
        statusHeading.setTextSize(16);
        statusHeading.setTypeface(null, Typeface.BOLD);

        statusCard.addView(statusHeading);

        statusText = new TextView(context);
        statusText.setText("Loading your bus pass information...");
        statusText.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        statusText.setTextSize(13);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        statusParams.topMargin = dp(10);

        statusCard.addView(statusText, statusParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        dp(32),
                        dp(32)
                );
        progressParams.topMargin = dp(12);

        statusCard.addView(progressBar, progressParams);

        retryButton = new Button(context);
        retryButton.setText("Retry");
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> loadBusPasses());

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        retryParams.topMargin = dp(8);

        statusCard.addView(retryButton, retryParams);

        passesContainer = new LinearLayout(context);
        passesContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams passesParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        passesParams.topMargin = dp(12);

        root.addView(passesContainer, passesParams);

        addRefreshButton(root);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();
        loadBusPasses();
    }

    private void addSectionHeading(
            LinearLayout parent,
            String text,
            int topMargin
    ) {
        TextView heading = new TextView(requireContext());
        heading.setText(text);
        heading.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        heading.setTextSize(11);
        heading.setLetterSpacing(0.08f);
        heading.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = topMargin;
        parent.addView(heading, params);
    }

    private void addNavigationCard(
            LinearLayout parent,
            String heading,
            String description,
            String number,
            Runnable action
    ) {
        Context context = requireContext();

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(18), dp(16), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.topMargin = dp(12);
        parent.addView(card, cardParams);

        TextView numberView = new TextView(context);
        numberView.setText(number);
        numberView.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        numberView.setTextSize(15);
        numberView.setTypeface(null, Typeface.BOLD);
        numberView.setGravity(Gravity.CENTER);

        card.addView(
                numberView,
                new LinearLayout.LayoutParams(dp(42), dp(42))
        );

        LinearLayout textContainer = new LinearLayout(context);
        textContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );
        textParams.leftMargin = dp(14);

        card.addView(textContainer, textParams);

        TextView title = new TextView(context);
        title.setText(heading);
        title.setTextColor(
                context.getColor(R.color.primary_text)
        );
        title.setTextSize(16);
        title.setTypeface(null, Typeface.BOLD);

        textContainer.addView(title);

        TextView descriptionView = new TextView(context);
        descriptionView.setText(description);
        descriptionView.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        descriptionView.setTextSize(13);
        descriptionView.setLineSpacing(dp(2), 1f);

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        descriptionParams.topMargin = dp(5);

        textContainer.addView(
                descriptionView,
                descriptionParams
        );

        TextView arrow = new TextView(context);
        arrow.setText("›");
        arrow.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        arrow.setTextSize(28);
        arrow.setGravity(Gravity.CENTER);

        card.addView(
                arrow,
                new LinearLayout.LayoutParams(
                        dp(24),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        card.setOnClickListener(v -> action.run());
    }

    private void addRefreshButton(LinearLayout parent) {
        Button refreshButton = new Button(requireContext());
        refreshButton.setText("Refresh Bus Passes");
        refreshButton.setOnClickListener(v -> loadBusPasses());

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(20);
        parent.addView(refreshButton, params);
    }

    private void openApplication() {
        if (!isAdded()) {
            return;
        }

        getParentFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.fragmentContainer,
                        new BusPassApplicationFragment()
                )
                .addToBackStack(null)
                .commit();
    }

    private void loadBusPasses() {
        if (!isAdded() || executor == null || executor.isShutdown()) {
            return;
        }

        setLoadingState();

        SharedPreferences preferences = requireContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (token == null || token.trim().isEmpty()) {
            showError("Your session has expired. Please log in again.");
            return;
        }

        executor.execute(() -> {
            try {
                JSONArray passes = getBusPasses(token);

                mainHandler.post(() -> {
                    if (isAdded()) {
                        displayBusPasses(passes);
                    }
                });

            } catch (Exception exception) {
                String message = exception.getMessage();

                mainHandler.post(() -> {
                    if (isAdded()) {
                        showError(
                                message == null
                                        ? "Unable to load bus passes."
                                        : message
                        );
                    }
                });
            }
        });
    }

    private JSONArray getBusPasses(String token) throws Exception {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(
                    BASE_URL + BUS_PASSES_ENDPOINT
            );

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

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new Exception(
                        "Your session has expired. Please log in again."
                );
            }

            if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new Exception(
                        "You do not have permission to view these bus passes."
                );
            }

            if (responseCode < 200 || responseCode >= 300) {
                throw new Exception(
                        "Server returned HTTP " + responseCode + "."
                );
            }

            String responseBody = readResponse(
                    connection.getInputStream()
            );

            return new JSONArray(responseBody);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream inputStream)
            throws Exception {

        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private void displayBusPasses(JSONArray passes) {
        if (!isAdded()) {
            return;
        }

        progressBar.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        passesContainer.removeAllViews();

        if (passes.length() == 0) {
            statusText.setText(
                    "You haven't submitted any bus pass applications yet."
            );
            return;
        }

        statusText.setText(
                "Found " + passes.length()
                        + (passes.length() == 1
                        ? " application."
                        : " applications.")
        );

        for (int i = 0; i < passes.length(); i++) {
            JSONObject pass = passes.optJSONObject(i);

            if (pass != null) {
                addPassCard(pass);
            }
        }
    }

    private void addPassCard(JSONObject pass) {
        Context context = requireContext();

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.topMargin = dp(12);
        passesContainer.addView(card, cardParams);

        String route = pass.optString("route", "Not specified");
        String status = pass.optString("status", "UNKNOWN");

        TextView routeTitle = new TextView(context);
        routeTitle.setText(route);
        routeTitle.setTextColor(
                context.getColor(R.color.primary_text)
        );
        routeTitle.setTextSize(17);
        routeTitle.setTypeface(null, Typeface.BOLD);

        card.addView(routeTitle);

        TextView statusView = new TextView(context);
        statusView.setText("Status: " + status.replace('_', ' '));
        statusView.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        statusView.setTextSize(13);
        statusView.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        statusParams.topMargin = dp(8);

        card.addView(statusView, statusParams);

        addDetail(card, "Start date", pass.optString("startDate", "—"));
        addDetail(card, "Expiry date", pass.optString("expiryDate", "—"));
        addDetail(
                card,
                "Receipt number",
                pass.optString("paymentReceiptNo", "—")
        );
        addDetail(
                card,
                "Payment date",
                pass.optString("paymentDate", "—")
        );

        String amount = pass.optString("paymentAmount", "");

        if (!amount.isEmpty() && !"null".equals(amount)) {
            addDetail(card, "Payment amount", "₹" + amount);
        }
    }

    private void addDetail(
            LinearLayout parent,
            String label,
            String value
    ) {
        TextView detail = new TextView(requireContext());
        detail.setText(label + ": " + value);
        detail.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        detail.setTextSize(13);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(6);
        parent.addView(detail, params);
    }

    private void setLoadingState() {
        if (!isAdded() || progressBar == null) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.GONE);
        statusText.setText("Loading your bus pass information...");
        passesContainer.removeAllViews();
    }

    private void showError(String message) {
        if (!isAdded() || statusText == null) {
            return;
        }

        progressBar.setVisibility(View.GONE);
        retryButton.setVisibility(View.VISIBLE);
        statusText.setText(message);
    }

    private int dp(int value) {
        return Math.round(
                value * getResources()
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

        root = null;
        passesContainer = null;
        progressBar = null;
        statusText = null;
        retryButton = null;

        super.onDestroyView();
    }
}