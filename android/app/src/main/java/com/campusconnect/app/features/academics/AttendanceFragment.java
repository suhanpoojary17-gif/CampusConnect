package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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

public class AttendanceFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;
    private LinearLayout contentContainer;
    private TextView loadingText;

    public AttendanceFragment() {
        // Required empty public constructor.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable android.view.ViewGroup container,
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
        appTitle.setTextColor(context.getColor(R.color.primary_accent));
        root.addView(appTitle);

        TextView heading = new TextView(context);
        heading.setText("Attendance");
        heading.setTextSize(28);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(context.getColor(R.color.primary_text));

        LinearLayout.LayoutParams headingParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        headingParams.topMargin = dp(8);
        root.addView(heading, headingParams);

        TextView subtitle = new TextView(context);
        subtitle.setText("Track your attendance and stay on target.");
        subtitle.setTextSize(14);
        subtitle.setTextColor(context.getColor(R.color.secondary_text));

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = dp(4);
        root.addView(subtitle, subtitleParams);

        loadingText = new TextView(context);
        loadingText.setText("Loading attendance...");
        loadingText.setTextSize(14);
        loadingText.setTextColor(context.getColor(R.color.secondary_text));
        loadingText.setGravity(Gravity.CENTER);
        loadingText.setPadding(0, dp(28), 0, dp(28));
        root.addView(loadingText);

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
        loadAttendance();
    }

    private void loadAttendance() {
        SharedPreferences preferences = requireContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String token = preferences.getString(TOKEN_KEY, "");

        if (token == null || token.trim().isEmpty()) {
            showMessage("Your session has expired. Please sign in again.");
            return;
        }

        executor.execute(() -> {
            List<JSONObject> records = new ArrayList<>();
            String error = null;

            try {
                // Resolve the signed-in student's ID from the backend.
                JSONObject student = new JSONObject(
                        getString("/api/students/me", token));

                String studentId = student.optString("studentId", "");

                // Retrieve attendance using the authenticated student's ID.
                JSONArray array = new JSONArray(
                        getString(
                                "/api/attendance/student/"
                                        + studentId + "/all-subjects",
                                token));

                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.optJSONObject(i);
                    if (item != null) {
                        records.add(item);
                    }
                }

            } catch (Exception e) {
                error = e.getMessage() == null
                        ? "Unable to load attendance."
                        : e.getMessage();
            }

            final String finalError = error;

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    loadingText.setVisibility(View.GONE);
                    contentContainer.removeAllViews();

                    if (finalError != null) {
                        addMessageCard(
                                "Unable to load attendance",
                                finalError);
                        return;
                    }

                    renderAttendance(records);
                });
            }
        });
    }

    private void renderAttendance(List<JSONObject> records) {
        if (records.isEmpty()) {
            addMessageCard(
                    "No attendance records",
                    "Subject attendance records are not available yet.");
            return;
        }

        int totalPresent = 0;
        int totalClasses = 0;

        for (JSONObject record : records) {
            totalPresent += record.optInt("presentClasses", 0);
            totalClasses += record.optInt("totalClasses", 0);
        }

        double overallPercentage = totalClasses > 0
                ? (totalPresent * 100.0) / totalClasses
                : 0.0;

        String overallStatus = getRiskLevel(overallPercentage);

        LinearLayout overallCard = createCard("Overall attendance");

        TextView percentage = new TextView(requireContext());
        percentage.setText(
                totalClasses > 0
                        ? String.format(
                                Locale.getDefault(),
                                "%.1f%%",
                                overallPercentage)
                        : "N/A");
        percentage.setTextSize(36);
        percentage.setTypeface(null, Typeface.BOLD);
        percentage.setTextColor(
                requireContext().getColor(getStatusColor(overallStatus)));

        overallCard.addView(percentage);

        TextView status = new TextView(requireContext());
        status.setText(getStatusLabel(overallStatus));
        status.setTextSize(14);
        status.setTypeface(null, Typeface.BOLD);
        status.setTextColor(
                requireContext().getColor(getStatusColor(overallStatus)));

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = dp(4);
        overallCard.addView(status, statusParams);

        TextView totals = new TextView(requireContext());
        totals.setText(
                totalClasses > 0
                        ? totalPresent + " attended out of "
                                + totalClasses + " recorded classes"
                        : "No classes have been recorded yet.");
        totals.setTextSize(14);
        totals.setTextColor(
                requireContext().getColor(R.color.secondary_text));

        LinearLayout.LayoutParams totalsParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        totalsParams.topMargin = dp(8);
        overallCard.addView(totals, totalsParams);

        if (totalClasses > 0) {
            ProgressBar progressBar = new ProgressBar(
                    requireContext(), null,
                    android.R.attr.progressBarStyleHorizontal);

            progressBar.setMax(100);
            progressBar.setProgress(
                    (int) Math.round(overallPercentage));

            LinearLayout.LayoutParams progressParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(8));
            progressParams.topMargin = dp(16);
            overallCard.addView(progressBar, progressParams);
        }

        TextView calculationNote = new TextView(requireContext());
        calculationNote.setText(
                "Overall percentage is calculated from the combined "
                        + "present and total class counts.");
        calculationNote.setTextSize(12);
        calculationNote.setTextColor(
                requireContext().getColor(R.color.secondary_text));

        LinearLayout.LayoutParams noteParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = dp(10);
        overallCard.addView(calculationNote, noteParams);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        contentContainer.addView(overallCard, cardParams);

        TextView subjectHeading = new TextView(requireContext());
        subjectHeading.setText("Attendance by subject");
        subjectHeading.setTextSize(20);
        subjectHeading.setTypeface(null, Typeface.BOLD);
        subjectHeading.setTextColor(
                requireContext().getColor(R.color.primary_text));

        LinearLayout.LayoutParams subjectHeadingParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        subjectHeadingParams.topMargin = dp(24);
        subjectHeadingParams.bottomMargin = dp(8);

        contentContainer.addView(subjectHeading, subjectHeadingParams);

        for (JSONObject record : records) {
            renderSubjectCard(record);
        }
    }

    private void renderSubjectCard(JSONObject record) {
        String subjectName =
                record.optString("subjectName", "Unknown subject");

        String subjectCode =
                record.optString("subjectCode", "");

        double attendancePercentage =
                record.optDouble("attendancePercentage", 0.0);

        String riskLevel =
                record.optString(
                        "riskLevel",
                        getRiskLevel(attendancePercentage));

        int presentClasses = record.optInt("presentClasses", 0);
        int absentClasses = record.optInt("absentClasses", 0);
        int totalClasses = record.optInt("totalClasses", 0);
        int classesNeeded = record.optInt("classesNeededFor75", 0);

        LinearLayout card = createCard(subjectName);

        TextView code = new TextView(requireContext());
        code.setText(subjectCode);
        code.setTextSize(12);
        code.setTextColor(
                requireContext().getColor(R.color.secondary_text));

        LinearLayout.LayoutParams codeParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        codeParams.topMargin = dp(2);
        card.addView(code, codeParams);

        TextView percentage = new TextView(requireContext());
        percentage.setText(
                String.format(
                        Locale.getDefault(),
                        "%.1f%%",
                        attendancePercentage));
        percentage.setTextSize(30);
        percentage.setTypeface(null, Typeface.BOLD);
        percentage.setTextColor(
                requireContext().getColor(getStatusColor(riskLevel)));

        LinearLayout.LayoutParams percentageParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        percentageParams.topMargin = dp(12);
        card.addView(percentage, percentageParams);

        TextView risk = new TextView(requireContext());
        risk.setText(getStatusLabel(riskLevel));
        risk.setTextSize(13);
        risk.setTypeface(null, Typeface.BOLD);
        risk.setTextColor(
                requireContext().getColor(getStatusColor(riskLevel)));

        LinearLayout.LayoutParams riskParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        riskParams.topMargin = dp(2);
        card.addView(risk, riskParams);

        ProgressBar progressBar = new ProgressBar(
                requireContext(), null,
                android.R.attr.progressBarStyleHorizontal);

        progressBar.setMax(100);
        progressBar.setProgress(
                (int) Math.max(
                        0,
                        Math.min(100, Math.round(attendancePercentage))));

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(8));
        progressParams.topMargin = dp(12);
        card.addView(progressBar, progressParams);

        addDetailLine(
                card,
                "Classes attended",
                presentClasses + " / " + totalClasses);

        addDetailLine(
                card,
                "Classes absent",
                String.valueOf(absentClasses));

        addDetailLine(
                card,
                "Classes needed to reach 75%",
                String.valueOf(classesNeeded));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
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
        labelView.setTextSize(13);
        labelView.setTextColor(
                requireContext().getColor(R.color.secondary_text));

        LinearLayout.LayoutParams labelParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f);
        row.addView(labelView, labelParams);

        TextView valueView = new TextView(requireContext());
        valueView.setText(value);
        valueView.setTextSize(13);
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setTextColor(
                requireContext().getColor(R.color.primary_text));

        row.addView(valueView);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
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
                requireContext().getColor(R.color.primary_text));

        card.addView(title);

        return card;
    }

    private void addMessageCard(String title, String message) {
        LinearLayout card = createCard(title);

        TextView body = new TextView(requireContext());
        body.setText(message);
        body.setTextSize(14);
        body.setTextColor(
                requireContext().getColor(R.color.secondary_text));

        LinearLayout.LayoutParams bodyParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        bodyParams.topMargin = dp(8);

        card.addView(body, bodyParams);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(12);
        contentContainer.addView(card, cardParams);
    }

    private void showMessage(String message) {
        if (!isAdded()) {
            return;
        }

        loadingText.setVisibility(View.GONE);
        addMessageCard("Attendance", message);
    }

    private String getRiskLevel(double percentage) {
        if (percentage >= 80.0) {
            return "SAFE";
        } else if (percentage >= 75.0) {
            return "MODERATE";
        } else {
            return "HIGH";
        }
    }

    private String getStatusLabel(String riskLevel) {
        if ("SAFE".equalsIgnoreCase(riskLevel)) {
            return "Safe";
        } else if ("MODERATE".equalsIgnoreCase(riskLevel)) {
            return "Needs attention";
        } else if ("HIGH".equalsIgnoreCase(riskLevel)
                || "HIGH_RISK".equalsIgnoreCase(riskLevel)) {
            return "High risk";
        }

        return riskLevel;
    }

    private int getStatusColor(String riskLevel) {
        if ("SAFE".equalsIgnoreCase(riskLevel)) {
            return R.color.status_safe;
        } else if ("MODERATE".equalsIgnoreCase(riskLevel)) {
            return R.color.status_warning;
        }

        return R.color.status_high_risk;
    }

    private String getString(String path, String token)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(BASE_URL + path);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty(
                    "Authorization", "Bearer " + token);

            int status = connection.getResponseCode();

            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder response = new StringBuilder();

            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                stream, StandardCharsets.UTF_8))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
            }

            if (status < 200 || status >= 300) {
                throw new Exception(
                        "Request failed (HTTP " + status + ").");
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
                value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        super.onDestroyView();
    }
}