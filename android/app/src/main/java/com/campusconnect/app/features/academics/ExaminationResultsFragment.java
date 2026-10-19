package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExaminationResultsFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;

    private LinearLayout contentContainer;
    private ProgressBar progressBar;
    private TextView loadingText;
    private Button retryButton;

    public ExaminationResultsFragment() {
        // Required empty public constructor.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        Context context = requireContext();

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(
                context.getColor(R.color.main_background));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(24));

        scrollView.addView(root);

        TextView appTitle = new TextView(context);
        appTitle.setText("CAMPUSCONNECT");
        appTitle.setTextSize(12);
        appTitle.setLetterSpacing(0.12f);
        appTitle.setTypeface(null, Typeface.BOLD);
        appTitle.setTextColor(
                context.getColor(R.color.primary_accent));

        root.addView(appTitle);

        TextView heading = new TextView(context);
        heading.setText("Examination Results");
        heading.setTextSize(28);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(
                context.getColor(R.color.primary_text));

        LinearLayout.LayoutParams headingParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        headingParams.topMargin = dp(8);
        root.addView(heading, headingParams);

        TextView subtitle = new TextView(context);
        subtitle.setText(
                "View your published examination results "
                        + "and subject-wise scores.");
        subtitle.setTextSize(14);
        subtitle.setTextColor(
                context.getColor(R.color.secondary_text));

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        subtitleParams.topMargin = dp(4);
        root.addView(subtitle, subtitleParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(24);

        root.addView(progressBar, progressParams);

        loadingText = new TextView(context);
        loadingText.setText("Loading examination results...");
        loadingText.setTextSize(14);
        loadingText.setTextColor(
                context.getColor(R.color.secondary_text));
        loadingText.setGravity(Gravity.CENTER);
        loadingText.setPadding(0, dp(12), 0, dp(20));

        root.addView(loadingText);

        retryButton = new Button(context);
        retryButton.setText("Retry");
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> loadResults());

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(retryButton, retryParams);

        contentContainer = new LinearLayout(context);
        contentContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(contentContainer);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        loadResults();
    }

    private void loadResults() {

        if (!isAdded() || contentContainer == null) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        SharedPreferences preferences = requireContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE);

        String token = preferences.getString(TOKEN_KEY, "");

        contentContainer.removeAllViews();

        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("Loading examination results...");
        retryButton.setVisibility(View.GONE);

        if (token == null || token.trim().isEmpty()) {

            progressBar.setVisibility(View.GONE);

            loadingText.setText(
                    "Your session has expired. Please sign in again.");

            return;
        }

        executor.execute(() -> {

            List<JSONObject> results = new ArrayList<>();
            String error = null;

            try {

                JSONArray array = new JSONArray(
                        getString("/api/results/my", token));

                for (int i = 0; i < array.length(); i++) {

                    JSONObject item = array.optJSONObject(i);

                    if (item != null) {
                        results.add(item);
                    }
                }

            } catch (Exception e) {

                error = e.getMessage() == null
                        ? "Unable to load examination results."
                        : e.getMessage();
            }

            final String finalError = error;

            if (!isAdded()) {
                return;
            }

            requireActivity().runOnUiThread(() -> {

                if (!isAdded() || contentContainer == null) {
                    return;
                }

                progressBar.setVisibility(View.GONE);
                loadingText.setVisibility(View.GONE);
                contentContainer.removeAllViews();

                if (finalError != null) {

                    showMessage(
                            "Unable to load results",
                            finalError);

                    retryButton.setVisibility(View.VISIBLE);

                    return;
                }

                retryButton.setVisibility(View.GONE);

                renderResults(results);
            });
        });
    }

    private void renderResults(List<JSONObject> results) {

        if (results.isEmpty()) {

            showMessage(
                    "No published results",
                    "Your examination results will appear here "
                            + "when your institution publishes them.");

            return;
        }

        double totalObtained = 0;
        double totalMaximum = 0;
        int validMaximumCount = 0;

        for (JSONObject result : results) {

            double marks = result.optDouble(
                    "marksObtained", 0);

            double maximum = result.optDouble(
                    "maximumMarks", 0);

            totalObtained += marks;

            if (maximum > 0) {
                totalMaximum += maximum;
                validMaximumCount++;
            }
        }

        LinearLayout summaryCard =
                createCard("Results overview");

        TextView count = new TextView(requireContext());

        count.setText(
                results.size()
                        + (results.size() == 1
                        ? " published result"
                        : " published results"));

        count.setTextSize(14);
        count.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text));

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        countParams.topMargin = dp(8);

        summaryCard.addView(count, countParams);

        if (validMaximumCount > 0 && totalMaximum > 0) {

            double overallPercentage =
                    totalObtained * 100.0 / totalMaximum;

            TextView total = new TextView(requireContext());

            total.setText(
                    formatNumber(totalObtained)
                            + " / "
                            + formatNumber(totalMaximum));

            total.setTextSize(30);
            total.setTypeface(null, Typeface.BOLD);
            total.setTextColor(
                    requireContext().getColor(
                            R.color.primary_accent));

            LinearLayout.LayoutParams totalParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

            totalParams.topMargin = dp(12);

            summaryCard.addView(total, totalParams);

            TextView percentage = new TextView(requireContext());

            percentage.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.2f%% of recorded maximum marks",
                            overallPercentage));

            percentage.setTextSize(14);
            percentage.setTextColor(
                    requireContext().getColor(
                            R.color.secondary_text));

            LinearLayout.LayoutParams percentageParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

            percentageParams.topMargin = dp(4);

            summaryCard.addView(
                    percentage,
                    percentageParams);
        }

        LinearLayout.LayoutParams summaryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        summaryParams.bottomMargin = dp(20);

        contentContainer.addView(
                summaryCard,
                summaryParams);

        TextView sectionHeading = new TextView(requireContext());

        sectionHeading.setText("PUBLISHED RESULTS");
        sectionHeading.setTextSize(12);
        sectionHeading.setLetterSpacing(0.08f);
        sectionHeading.setTypeface(null, Typeface.BOLD);
        sectionHeading.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text));

        LinearLayout.LayoutParams sectionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        sectionParams.bottomMargin = dp(8);

        contentContainer.addView(
                sectionHeading,
                sectionParams);

        for (JSONObject result : results) {
            renderResultCard(result);
        }
    }

    private void renderResultCard(JSONObject result) {

        String examTitle = result.optString(
                "examTitle", "Examination");

        String subjectName = result.optString(
                "subjectName", "Unknown subject");

        String subjectCode = result.optString(
                "subjectCode", "");

        String examDate = result.optString(
                "examDate", "");

        String gradedAt = result.optString(
                "gradedAt", "");

        double marks = result.optDouble(
                "marksObtained", 0);

        double maximum = result.optDouble(
                "maximumMarks", 0);

        double percentage = result.optDouble(
                "percentage", -1);

        LinearLayout card = createCard(examTitle);

        TextView subject = new TextView(requireContext());

        subject.setText(
                subjectCode.trim().isEmpty()
                        ? subjectName
                        : subjectName + " • " + subjectCode);

        subject.setTextSize(14);
        subject.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text));

        LinearLayout.LayoutParams subjectParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        subjectParams.topMargin = dp(6);

        card.addView(subject, subjectParams);

        TextView score = new TextView(requireContext());

        score.setText(
                formatNumber(marks)
                        + (maximum > 0
                        ? " / " + formatNumber(maximum)
                        : ""));

        score.setTextSize(28);
        score.setTypeface(null, Typeface.BOLD);
        score.setTextColor(
                requireContext().getColor(
                        R.color.primary_accent));

        LinearLayout.LayoutParams scoreParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        scoreParams.topMargin = dp(14);

        card.addView(score, scoreParams);

        if (percentage < 0 && maximum > 0) {
            percentage = marks * 100.0 / maximum;
        }

        if (percentage >= 0) {

            TextView percentageView =
                    new TextView(requireContext());

            percentageView.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.2f%%",
                            percentage));

            percentageView.setTextSize(14);
            percentageView.setTextColor(
                    requireContext().getColor(
                            R.color.secondary_text));

            LinearLayout.LayoutParams percentageParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

            percentageParams.topMargin = dp(4);

            card.addView(
                    percentageView,
                    percentageParams);

            if (maximum > 0) {

                ProgressBar progress = new ProgressBar(
                        requireContext(),
                        null,
                        android.R.attr.progressBarStyleHorizontal);

                progress.setMax(100);

                progress.setProgress(
                        (int) Math.max(
                                0,
                                Math.min(
                                        100,
                                        Math.round(percentage))));

                LinearLayout.LayoutParams progressParams =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                dp(8));

                progressParams.topMargin = dp(12);

                card.addView(progress, progressParams);
            }
        }

        if (!examDate.trim().isEmpty()
                && !"null".equalsIgnoreCase(examDate)) {

            addDetailLine(
                    card,
                    "Examination date",
                    examDate);
        }

        if (!gradedAt.trim().isEmpty()
                && !"null".equalsIgnoreCase(gradedAt)) {

            addDetailLine(
                    card,
                    "Graded at",
                    gradedAt);
        }

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        cardParams.bottomMargin = dp(12);

        contentContainer.addView(card, cardParams);
    }

    private void addDetailLine(
            LinearLayout card,
            String label,
            String value) {

        LinearLayout row = new LinearLayout(requireContext());

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView labelView = new TextView(requireContext());

        labelView.setText(label);
        labelView.setTextSize(12);
        labelView.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text));

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f);

        row.addView(labelView, labelParams);

        TextView valueView = new TextView(requireContext());

        valueView.setText(value);
        valueView.setTextSize(12);
        valueView.setTextColor(
                requireContext().getColor(
                        R.color.primary_text));

        row.addView(valueView);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        rowParams.topMargin = dp(12);

        card.addView(row, rowParams);
    }

    private LinearLayout createCard(String heading) {

        LinearLayout card = new LinearLayout(requireContext());

        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackgroundResource(R.drawable.bg_card);

        TextView title = new TextView(requireContext());

        title.setText(heading);
        title.setTextSize(17);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(
                requireContext().getColor(
                        R.color.primary_text));

        card.addView(title);

        return card;
    }

    private void showMessage(
            String title,
            String message) {

        LinearLayout card = createCard(title);

        TextView body = new TextView(requireContext());

        body.setText(message);
        body.setTextSize(14);
        body.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text));

        LinearLayout.LayoutParams bodyParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        bodyParams.topMargin = dp(8);

        card.addView(body, bodyParams);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

        cardParams.bottomMargin = dp(12);

        contentContainer.addView(card, cardParams);
    }

    private String formatNumber(double value) {

        if (value == Math.floor(value)) {
            return String.format(
                    Locale.getDefault(),
                    "%.0f",
                    value);
        }

        return String.format(
                Locale.getDefault(),
                "%.2f",
                value);
    }

    private String getString(
            String path,
            String token) throws Exception {

        HttpURLConnection connection = null;

        try {

            URL url = new URL(BASE_URL + path);

            connection =
                    (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            connection.setRequestProperty(
                    "Accept",
                    "application/json");

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token);

            int status = connection.getResponseCode();

            InputStream stream =
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            StringBuilder response = new StringBuilder();

            if (stream != null) {

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             stream,
                                             StandardCharsets.UTF_8))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }

            if (status < 200 || status >= 300) {

                throw new Exception(
                        "Request failed (HTTP "
                                + status + ").");
            }

            return response.toString();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private int dp(int value) {

        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density);
    }

    @Override
    public void onDestroyView() {

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        contentContainer = null;
        progressBar = null;
        loadingText = null;
        retryButton = null;

        super.onDestroyView();
    }
}