package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExaminationsFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String PREFS_NAME =
            "campusconnect_session";

    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;

    private LinearLayout root;
    private LinearLayout examsContainer;

    private ProgressBar progressBar;
    private TextView statusText;
    private Button retryButton;

    public ExaminationsFragment() {
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

        root.setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(28)
        );

        scrollView.addView(root);

        // Eyebrow
        TextView eyebrow = new TextView(context);

        eyebrow.setText("CAMPUSCONNECT");
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);

        eyebrow.setTextColor(
                context.getColor(R.color.primary_accent)
        );

        root.addView(eyebrow);

        // Screen title
        TextView title = new TextView(context);

        title.setText("Examinations");
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);

        title.setTextColor(
                context.getColor(R.color.primary_text)
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(8);

        root.addView(title, titleParams);

        // Subtitle
        TextView subtitle = new TextView(context);

        subtitle.setText(
                "View your upcoming examination schedule and details."
        );

        subtitle.setTextSize(14);

        subtitle.setTextColor(
                context.getColor(R.color.secondary_text)
        );

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(4);

        root.addView(subtitle, subtitleParams);

        // Loading indicator
        progressBar = new ProgressBar(context);

        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(28);

        root.addView(progressBar, progressParams);

        // Status message
        statusText = new TextView(context);

        statusText.setText("Loading your examinations...");
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);

        statusText.setTextColor(
                context.getColor(R.color.secondary_text)
        );

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(12);

        root.addView(statusText, statusParams);

        // Retry button
        retryButton = new Button(context);

        retryButton.setText("Try Again");
        retryButton.setAllCaps(false);

        retryButton.setVisibility(View.GONE);

        retryButton.setOnClickListener(v -> loadExaminations());

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        retryParams.topMargin = dp(8);

        root.addView(retryButton, retryParams);

        // Examination cards
        examsContainer = new LinearLayout(context);

        examsContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams examsParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        examsParams.topMargin = dp(20);

        root.addView(examsContainer, examsParams);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        loadExaminations();
    }

    private void loadExaminations() {

        if (!isAdded()
                || progressBar == null
                || statusText == null
                || examsContainer == null) {
            return;
        }

        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(
                TOKEN_KEY,
                null
        );

        if (TextUtils.isEmpty(token)) {

            showError(
                    "Your session has expired. Please sign in again."
            );

            return;
        }

        if (executor == null || executor.isShutdown()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        statusText.setVisibility(View.VISIBLE);

        statusText.setText("Loading your examinations...");

        retryButton.setVisibility(View.GONE);

        examsContainer.removeAllViews();

        executor.execute(() -> {

            try {

                JSONArray response = getArray(
                        "/api/exams/my",
                        token
                );

                List<JSONObject> examinations =
                        new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {

                    JSONObject item =
                            response.optJSONObject(i);

                    if (item != null) {
                        examinations.add(item);
                    }
                }

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {

                    if (!isAdded()
                            || examsContainer == null) {
                        return;
                    }

                    progressBar.setVisibility(View.GONE);

                    statusText.setVisibility(View.GONE);

                    retryButton.setVisibility(View.GONE);

                    renderExaminations(examinations);
                });

            } catch (Exception e) {

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {

                    if (!isAdded()) {
                        return;
                    }

                    showError(
                            "Unable to load examinations. "
                                    + "Check your connection and try again."
                    );
                });
            }
        });
    }

    private void renderExaminations(
            List<JSONObject> examinations
    ) {

        examsContainer.removeAllViews();

        if (examinations.isEmpty()) {

            addEmptyState(
                    "No examinations are scheduled for your section yet."
            );

            return;
        }

        // Summary card
        LinearLayout summaryCard =
                createCard();

        TextView summaryLabel =
                new TextView(requireContext());

        summaryLabel.setText("YOUR EXAMINATION SCHEDULE");

        summaryLabel.setTextSize(12);

        summaryLabel.setLetterSpacing(0.06f);

        summaryLabel.setTypeface(null, Typeface.BOLD);

        summaryLabel.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text
                )
        );

        summaryCard.addView(summaryLabel);

        TextView summaryCount =
                new TextView(requireContext());

        summaryCount.setText(
                examinations.size()
                        + (examinations.size() == 1
                        ? " examination"
                        : " examinations")
        );

        summaryCount.setTextSize(24);

        summaryCount.setTypeface(null, Typeface.BOLD);

        summaryCount.setTextColor(
                requireContext().getColor(
                        R.color.primary_text
                )
        );

        LinearLayout.LayoutParams countParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        countParams.topMargin = dp(8);

        summaryCard.addView(
                summaryCount,
                countParams
        );

        addCardToContainer(summaryCard);

        // Individual examination cards
        for (JSONObject exam : examinations) {
            addExaminationCard(exam);
        }
    }

    private void addExaminationCard(JSONObject exam) {

        LinearLayout card = createCard();

        // Subject code
        String subjectCode = exam.optString(
                "subjectCode",
                ""
        );

        if (TextUtils.isEmpty(subjectCode)
                || "null".equalsIgnoreCase(subjectCode)) {
            subjectCode = "EXAMINATION";
        }

        TextView code = new TextView(requireContext());

        code.setText(subjectCode);

        code.setTextSize(12);

        code.setTypeface(null, Typeface.BOLD);

        code.setTextColor(
                requireContext().getColor(
                        R.color.primary_accent
                )
        );

        card.addView(code);

        // Exam title
        String examTitle = exam.optString(
                "title",
                "Examination"
        );

        TextView title = new TextView(requireContext());

        title.setText(examTitle);

        title.setTextSize(19);

        title.setTypeface(null, Typeface.BOLD);

        title.setTextColor(
                requireContext().getColor(
                        R.color.primary_text
                )
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(8);

        card.addView(title, titleParams);

        // Subject
        addDetail(
                card,
                "Subject",
                exam.optString(
                        "subjectName",
                        "Not specified"
                )
        );

        // Date
        addDetail(
                card,
                "Exam date",
                formatDate(
                        exam.optString("examDate", "")
                )
        );

        // Time
        String startTime = formatTime(
                exam.optString("startTime", "")
        );

        String endTime = formatTime(
                exam.optString("endTime", "")
        );

        addDetail(
                card,
                "Time",
                startTime + " - " + endTime
        );

        // Maximum marks
        addDetail(
                card,
                "Maximum marks",
                formatMarks(
                        exam.opt("maximumMarks")
                )
        );

        // Section
        addDetail(
                card,
                "Section",
                exam.optString(
                        "sectionName",
                        "Not specified"
                )
        );

        // Description, when provided
        String description = exam.optString(
                "description",
                ""
        );

        if (!TextUtils.isEmpty(description)
                && !"null".equalsIgnoreCase(description)) {

            TextView descriptionLabel =
                    new TextView(requireContext());

            descriptionLabel.setText("Description");

            descriptionLabel.setTextSize(13);

            descriptionLabel.setTypeface(
                    null,
                    Typeface.BOLD
            );

            descriptionLabel.setTextColor(
                    requireContext().getColor(
                            R.color.primary_text
                    )
            );

            LinearLayout.LayoutParams labelParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            labelParams.topMargin = dp(14);

            card.addView(
                    descriptionLabel,
                    labelParams
            );

            TextView descriptionText =
                    new TextView(requireContext());

            descriptionText.setText(description);

            descriptionText.setTextSize(14);

            descriptionText.setTextColor(
                    requireContext().getColor(
                            R.color.secondary_text
                    )
            );

            LinearLayout.LayoutParams descriptionParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            descriptionParams.topMargin = dp(4);

            card.addView(
                    descriptionText,
                    descriptionParams
            );
        }

        addCardToContainer(card);
    }

    private LinearLayout createCard() {

        LinearLayout card =
                new LinearLayout(requireContext());

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        card.setBackgroundResource(R.drawable.bg_card);

        return card;
    }

    private void addCardToContainer(
            LinearLayout card
    ) {

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(14);

        examsContainer.addView(card, params);
    }

    private void addDetail(
            LinearLayout card,
            String label,
            String value
    ) {

        TextView text =
                new TextView(requireContext());

        if (TextUtils.isEmpty(value)
                || "null".equalsIgnoreCase(value)) {

            value = "Not specified";
        }

        text.setText(label + ": " + value);

        text.setTextSize(14);

        text.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(8);

        card.addView(text, params);
    }

    private void addEmptyState(String message) {

        LinearLayout card = createCard();

        card.setGravity(Gravity.CENTER);

        TextView title =
                new TextView(requireContext());

        title.setText("No examinations found");

        title.setTextSize(18);

        title.setTypeface(null, Typeface.BOLD);

        title.setGravity(Gravity.CENTER);

        title.setTextColor(
                requireContext().getColor(
                        R.color.primary_text
                )
        );

        card.addView(title);

        TextView description =
                new TextView(requireContext());

        description.setText(message);

        description.setTextSize(14);

        description.setGravity(Gravity.CENTER);

        description.setTextColor(
                requireContext().getColor(
                        R.color.secondary_text
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(8);

        card.addView(description, params);

        addCardToContainer(card);
    }

    private void showError(String message) {

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (statusText != null) {
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(message);
        }

        if (retryButton != null) {
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    private JSONArray getArray(
            String path,
            String token
    ) throws Exception {

        return new JSONArray(
                getString(path, token)
        );
    }

    private String getString(
            String path,
            String token
    ) throws Exception {

        HttpURLConnection connection = null;

        try {

            URL url = new URL(BASE_URL + path);

            connection = (HttpURLConnection)
                    url.openConnection();

            connection.setRequestMethod("GET");

            connection.setConnectTimeout(10000);

            connection.setReadTimeout(10000);

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

            StringBuilder result =
                    new StringBuilder();

            if (stream != null) {

                try (BufferedReader reader =
                             new BufferedReader(
                                     new InputStreamReader(
                                             stream,
                                             StandardCharsets.UTF_8
                                     )
                             )) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            }

            if (status < 200 || status >= 300) {

                throw new IllegalStateException(
                        "Request failed with HTTP " + status
                );
            }

            return result.toString();

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String formatDate(String value) {

        if (TextUtils.isEmpty(value)
                || "null".equalsIgnoreCase(value)) {

            return "Not specified";
        }

        try {

            String[] parts = value.split("-");

            if (parts.length != 3) {
                return value;
            }

            int year = Integer.parseInt(parts[0]);

            int month = Integer.parseInt(parts[1]);

            int day = Integer.parseInt(parts[2]);

            String[] months = {
                    "Jan", "Feb", "Mar", "Apr",
                    "May", "Jun", "Jul", "Aug",
                    "Sep", "Oct", "Nov", "Dec"
            };

            if (month < 1 || month > 12) {
                return value;
            }

            return String.format(
                    Locale.getDefault(),
                    "%02d %s %04d",
                    day,
                    months[month - 1],
                    year
            );

        } catch (Exception e) {
            return value;
        }
    }

    private String formatTime(String value) {

        if (TextUtils.isEmpty(value)
                || "null".equalsIgnoreCase(value)) {

            return "--:--";
        }

        try {

            String[] parts = value.split(":");

            int hour = Integer.parseInt(parts[0]);

            int minute = Integer.parseInt(parts[1]);

            String period = hour >= 12 ? "PM" : "AM";

            int displayHour = hour % 12;

            if (displayHour == 0) {
                displayHour = 12;
            }

            return String.format(
                    Locale.getDefault(),
                    "%02d:%02d %s",
                    displayHour,
                    minute,
                    period
            );

        } catch (Exception e) {
            return value;
        }
    }

    private String formatMarks(Object value) {

        if (value == null
                || JSONObject.NULL.equals(value)) {

            return "Not specified";
        }

        try {

            double marks = Double.parseDouble(
                    String.valueOf(value)
            );

            if (marks == Math.floor(marks)) {

                return String.format(
                        Locale.getDefault(),
                        "%.0f",
                        marks
                );
            }

            return String.valueOf(value);

        } catch (Exception e) {

            return String.valueOf(value);
        }
    }

    private int dp(int value) {

        return Math.round(
                value * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    @Override
    public void onDestroy() {

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        super.onDestroy();
    }
}