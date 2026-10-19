package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AcademicPerformanceFragment extends Fragment {

    private static final String BASE_URL =
            "http://127.0.0.1:8080";

    private static final String PREFS_NAME =
            "campusconnect_session";

    private static final String TOKEN_KEY =
            "jwt_token";

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private LinearLayout contentLayout;
    private ProgressBar progressBar;
    private TextView statusText;
    private Button retryButton;

    private boolean requestInProgress = false;

    public AcademicPerformanceFragment() {
        // Required empty public constructor.
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull android.view.LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        Context context = requireContext();

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(
                getResources().getColor(R.color.main_background)
        );

        root.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
        );

        // Screen heading.
        TextView heading = new TextView(context);
        heading.setText("Academic Performance");
        heading.setTextSize(24);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(
                getResources().getColor(R.color.primary_text)
        );

        heading.setPadding(
                dp(20),
                dp(20),
                dp(20),
                dp(4)
        );

        root.addView(heading);

        // Screen subtitle.
        TextView subtitle = new TextView(context);
        subtitle.setText(
                "Track your overall progress, subject scores "
                        + "and individual assessments."
        );

        subtitle.setTextSize(14);
        subtitle.setTextColor(
                getResources().getColor(R.color.secondary_text)
        );

        subtitle.setPadding(
                dp(20),
                0,
                dp(20),
                dp(16)
        );

        root.addView(subtitle);

        // Scrollable content.
        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1
                );

        root.addView(scrollView, scrollParams);

        contentLayout = new LinearLayout(context);
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        contentLayout.setPadding(
                dp(16),
                dp(4),
                dp(16),
                dp(24)
        );

        scrollView.addView(
                contentLayout,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        // Loading indicator.
        progressBar = new ProgressBar(context);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        dp(44),
                        dp(44)
                );

        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(40);

        contentLayout.addView(
                progressBar,
                progressParams
        );

        // Loading/error/empty status.
        statusText = new TextView(context);
        statusText.setText("Loading your academic performance...");
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);

        statusText.setTextColor(
                getResources().getColor(R.color.secondary_text)
        );

        statusText.setPadding(
                dp(12),
                dp(16),
                dp(12),
                dp(16)
        );

        contentLayout.addView(statusText);

        // Retry button.
        retryButton = new Button(context);
        retryButton.setText("Retry");

        retryButton.setVisibility(View.GONE);

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        retryParams.gravity = Gravity.CENTER_HORIZONTAL;

        contentLayout.addView(
                retryButton,
                retryParams
        );

        retryButton.setOnClickListener(v -> loadPerformance());

        loadPerformance();

        return root;
    }

    private void loadPerformance() {

        if (!isAdded()
                || contentLayout == null
                || requestInProgress) {
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

        if (token == null || token.trim().isEmpty()) {
            showError(
                    "Your session could not be found. "
                            + "Please log in again."
            );
            return;
        }

        requestInProgress = true;

        progressBar.setVisibility(View.VISIBLE);
        statusText.setVisibility(View.VISIBLE);
        statusText.setText(
                "Loading your academic performance..."
        );

        retryButton.setVisibility(View.GONE);

        // Remove previously rendered performance cards.
        contentLayout.removeViews(
                3,
                Math.max(0, contentLayout.getChildCount() - 3)
        );

        try {
            executor.execute(() -> {

                HttpURLConnection connection = null;

                try {
                    URL url = new URL(
                            BASE_URL + "/api/performance/my"
                    );

                    connection =
                            (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(15000);
                    connection.setReadTimeout(15000);

                    connection.setRequestProperty(
                            "Authorization",
                            "Bearer " + token
                    );

                    connection.setRequestProperty(
                            "Accept",
                            "application/json"
                    );

                    int responseCode =
                            connection.getResponseCode();

                    InputStream inputStream;

                    if (responseCode >= 200
                            && responseCode < 300) {

                        inputStream = connection.getInputStream();

                    } else {

                        inputStream = connection.getErrorStream();
                    }

                    String responseBody =
                            readResponse(inputStream);

                    if (responseCode >= 200
                            && responseCode < 300) {

                        JSONObject performance =
                                new JSONObject(responseBody);

                        mainHandler.post(() -> {

                            if (!isAdded()
                                    || contentLayout == null) {
                                return;
                            }

                            requestInProgress = false;
                            renderPerformance(performance);
                        });

                    } else {

                        String message =
                                getHttpErrorMessage(
                                        responseCode,
                                        responseBody
                                );

                        mainHandler.post(() -> {

                            if (!isAdded()
                                    || contentLayout == null) {
                                return;
                            }

                            requestInProgress = false;
                            showError(message);
                        });
                    }

                } catch (Exception e) {

                    String message =
                            "Unable to load academic performance. "
                                    + "Check your connection and try again.";

                    mainHandler.post(() -> {

                        if (!isAdded()
                                || contentLayout == null) {
                            return;
                        }

                        requestInProgress = false;
                        showError(message);
                    });

                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            });

        } catch (RuntimeException e) {

            requestInProgress = false;

            showError(
                    "Unable to start the request. "
                            + "Please try again."
            );
        }
    }

    private String readResponse(InputStream inputStream)
            throws Exception {

        if (inputStream == null) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(inputStream)
                     )) {

            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    private String getHttpErrorMessage(
            int responseCode,
            String responseBody
    ) {

        if (responseCode == 401) {
            return "Your session has expired. "
                    + "Please log in again.";
        }

        if (responseCode == 403) {
            return "You do not have permission to view "
                    + "academic performance.";
        }

        if (responseCode == 404) {
            return "The academic performance endpoint "
                    + "could not be found.";
        }

        if (responseCode >= 500) {
            return "The server encountered a problem. "
                    + "Please try again later.";
        }

        try {
            JSONObject error = new JSONObject(responseBody);

            String message = error.optString(
                    "message",
                    ""
            );

            if (!message.trim().isEmpty()) {
                return message;
            }

        } catch (Exception ignored) {
            // Use the fallback message below.
        }

        return "Unable to load academic performance. "
                + "HTTP status: " + responseCode;
    }

    private void renderPerformance(JSONObject performance) {

        if (contentLayout == null) {
            return;
        }

        contentLayout.removeAllViews();

        progressBar.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);

        BigDecimal totalObtained = getDecimal(
                performance,
                "totalMarksObtained"
        );

        BigDecimal totalMaximum = getDecimal(
                performance,
                "totalMaximumMarks"
        );

        BigDecimal overallPercentage = getDecimal(
                performance,
                "overallPercentage"
        );

        String performanceLevel = getStringValue(
                performance,
                "performanceLevel",
                "Not available"
        );

        JSONArray subjects =
                performance.optJSONArray("subjects");

        int subjectCount =
                subjects == null ? 0 : subjects.length();

        // Overall performance card.
        LinearLayout overallCard = createCard();

        TextView overallTitle = createText(
                "OVERALL PERFORMANCE",
                12,
                R.color.secondary_text,
                true
        );

        overallCard.addView(overallTitle);

        TextView percentageText = createText(
                formatNumber(overallPercentage) + "%",
                36,
                R.color.primary_accent,
                true
        );

        LinearLayout.LayoutParams percentageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        percentageParams.topMargin = dp(12);

        overallCard.addView(
                percentageText,
                percentageParams
        );

        TextView levelText = createText(
                performanceLevel,
                16,
                R.color.primary_text,
                true
        );

        LinearLayout.LayoutParams levelParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        levelParams.topMargin = dp(4);

        overallCard.addView(
                levelText,
                levelParams
        );

        TextView subjectsSummary = createText(
                subjectCount
                        + (subjectCount == 1
                        ? " subject available"
                        : " subjects available"),
                13,
                R.color.secondary_text,
                false
        );

        LinearLayout.LayoutParams summaryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        summaryParams.topMargin = dp(12);

        overallCard.addView(
                subjectsSummary,
                summaryParams
        );

        contentLayout.addView(overallCard);

        // Marks summary card.
        LinearLayout marksCard = createCard();

        marksCard.addView(
                createText(
                        "MARKS SUMMARY",
                        12,
                        R.color.secondary_text,
                        true
                )
        );

        addDivider(marksCard);

        addLabelValue(
                marksCard,
                "Marks obtained",
                formatNumber(totalObtained)
        );

        addLabelValue(
                marksCard,
                "Maximum marks",
                formatNumber(totalMaximum)
        );

        addLabelValue(
                marksCard,
                "Overall percentage",
                formatNumber(overallPercentage) + "%"
        );

        contentLayout.addView(marksCard);

        // Subject-wise performance.
        TextView subjectHeading = createText(
                "Subject-wise Performance",
                19,
                R.color.primary_text,
                true
        );

        LinearLayout.LayoutParams subjectHeadingParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subjectHeadingParams.topMargin = dp(20);
        subjectHeadingParams.bottomMargin = dp(10);

        contentLayout.addView(
                subjectHeading,
                subjectHeadingParams
        );

        if (subjects == null || subjects.length() == 0) {

            TextView emptyText = createText(
                    "No assessment marks are available yet.",
                    14,
                    R.color.secondary_text,
                    false
            );

            contentLayout.addView(emptyText);

            return;
        }

        for (int i = 0; i < subjects.length(); i++) {

            JSONObject subject = subjects.optJSONObject(i);

            if (subject == null) {
                continue;
            }

            addSubjectCard(subject);
        }
    }

    private void addSubjectCard(JSONObject subject) {

        LinearLayout subjectCard = createCard();

        String subjectName = getStringValue(
                subject,
                "subjectName",
                "Unnamed subject"
        );

        String subjectCode = getStringValue(
                subject,
                "subjectCode",
                ""
        );

        BigDecimal obtained = getDecimal(
                subject,
                "totalMarksObtained"
        );

        BigDecimal maximum = getDecimal(
                subject,
                "totalMaximumMarks"
        );

        BigDecimal percentage = getDecimal(
                subject,
                "percentage"
        );

        String level = getStringValue(
                subject,
                "performanceLevel",
                "Not available"
        );

        subjectCard.addView(
                createText(
                        subjectName,
                        18,
                        R.color.primary_text,
                        true
                )
        );

        if (!subjectCode.isEmpty()) {

            TextView codeText = createText(
                    subjectCode,
                    12,
                    R.color.secondary_text,
                    false
            );

            LinearLayout.LayoutParams codeParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            codeParams.topMargin = dp(3);

            subjectCard.addView(codeText, codeParams);
        }

        TextView percentageText = createText(
                formatNumber(percentage) + "%",
                27,
                R.color.primary_accent,
                true
        );

        LinearLayout.LayoutParams percentageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        percentageParams.topMargin = dp(12);

        subjectCard.addView(
                percentageText,
                percentageParams
        );

        TextView levelText = createText(
                level,
                14,
                R.color.primary_text,
                true
        );

        LinearLayout.LayoutParams levelParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        levelParams.topMargin = dp(3);

        subjectCard.addView(levelText, levelParams);

        addDivider(subjectCard);

        addLabelValue(
                subjectCard,
                "Marks obtained",
                formatNumber(obtained)
        );

        addLabelValue(
                subjectCard,
                "Maximum marks",
                formatNumber(maximum)
        );

        JSONArray assessments =
                subject.optJSONArray("assessments");

        TextView assessmentHeading = createText(
                "Assessment Breakdown",
                15,
                R.color.primary_text,
                true
        );

        LinearLayout.LayoutParams assessmentHeadingParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        assessmentHeadingParams.topMargin = dp(16);
        assessmentHeadingParams.bottomMargin = dp(8);

        subjectCard.addView(
                assessmentHeading,
                assessmentHeadingParams
        );

        if (assessments == null || assessments.length() == 0) {

            subjectCard.addView(
                    createText(
                            "No individual assessments available.",
                            13,
                            R.color.secondary_text,
                            false
                    )
            );

        } else {

            for (int i = 0; i < assessments.length(); i++) {

                JSONObject assessment =
                        assessments.optJSONObject(i);

                if (assessment == null) {
                    continue;
                }

                addAssessment(
                        subjectCard,
                        assessment
                );
            }
        }

        contentLayout.addView(subjectCard);
    }

    private void addAssessment(
            LinearLayout parent,
            JSONObject assessment
    ) {

        LinearLayout assessmentContainer =
                new LinearLayout(requireContext());

        assessmentContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        assessmentContainer.setPadding(
                dp(12),
                dp(10),
                dp(12),
                dp(10)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                getResources().getColor(
                        R.color.main_background
                )
        );

        background.setCornerRadius(dp(10));

        assessmentContainer.setBackground(background);

        LinearLayout.LayoutParams containerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        containerParams.bottomMargin = dp(8);

        parent.addView(
                assessmentContainer,
                containerParams
        );

        String title = getStringValue(
                assessment,
                "assessmentTitle",
                "Assessment"
        );

        String type = getStringValue(
                assessment,
                "assessmentType",
                "Assessment"
        );

        String date = getStringValue(
                assessment,
                "assessmentDate",
                "Date unavailable"
        );

        BigDecimal marksObtained = getDecimal(
                assessment,
                "marksObtained"
        );

        BigDecimal maximumMarks = getDecimal(
                assessment,
                "maximumMarks"
        );

        BigDecimal percentage = getDecimal(
                assessment,
                "percentage"
        );

        assessmentContainer.addView(
                createText(
                        title,
                        14,
                        R.color.primary_text,
                        true
                )
        );

        TextView typeText = createText(
                type,
                12,
                R.color.secondary_text,
                false
        );

        LinearLayout.LayoutParams typeParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        typeParams.topMargin = dp(4);

        assessmentContainer.addView(
                typeText,
                typeParams
        );

        TextView dateText = createText(
                "Date: " + date,
                12,
                R.color.secondary_text,
                false
        );

        LinearLayout.LayoutParams dateParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        dateParams.topMargin = dp(4);

        assessmentContainer.addView(
                dateText,
                dateParams
        );

        TextView marksText = createText(
                "Marks: "
                        + formatNumber(marksObtained)
                        + " / "
                        + formatNumber(maximumMarks),
                13,
                R.color.primary_text,
                true
        );

        LinearLayout.LayoutParams marksParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        marksParams.topMargin = dp(8);

        assessmentContainer.addView(
                marksText,
                marksParams
        );

        TextView percentageText = createText(
                formatNumber(percentage) + "%",
                13,
                R.color.primary_accent,
                true
        );

        LinearLayout.LayoutParams percentageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        percentageParams.topMargin = dp(3);

        assessmentContainer.addView(
                percentageText,
                percentageParams
        );
    }

    private LinearLayout createCard() {

        LinearLayout card = new LinearLayout(
                requireContext()
        );

        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                getResources().getColor(
                        R.color.card_surface
                )
        );

        background.setCornerRadius(dp(16));

        card.setBackground(background);

        card.setElevation(dp(2));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(14);

        card.setLayoutParams(params);

        return card;
    }

    private TextView createText(
            String text,
            float size,
            int colorResource,
            boolean bold
    ) {

        TextView textView = new TextView(
                requireContext()
        );

        textView.setText(text);
        textView.setTextSize(size);

        textView.setTextColor(
                getResources().getColor(colorResource)
        );

        if (bold) {
            textView.setTypeface(
                    null,
                    Typeface.BOLD
            );
        }

        return textView;
    }

    private void addDivider(LinearLayout parent) {

        View divider = new View(requireContext());

        divider.setBackgroundColor(
                getResources().getColor(
                        R.color.main_background
                )
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(1)
                );

        params.topMargin = dp(14);
        params.bottomMargin = dp(10);

        parent.addView(divider, params);
    }

    private void addLabelValue(
            LinearLayout parent,
            String label,
            String value
    ) {

        LinearLayout row = new LinearLayout(
                requireContext()
        );

        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView labelView = createText(
                label,
                13,
                R.color.secondary_text,
                false
        );

        TextView valueView = createText(
                value,
                14,
                R.color.primary_text,
                true
        );

        valueView.setGravity(Gravity.END);

        row.addView(
                labelView,
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1
                )
        );

        row.addView(
                valueView,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rowParams.topMargin = dp(8);

        parent.addView(row, rowParams);
    }

    private void showError(String message) {

        if (contentLayout == null) {
            return;
        }

        progressBar.setVisibility(View.GONE);

        statusText.setText(message);
        statusText.setVisibility(View.VISIBLE);

        retryButton.setVisibility(View.VISIBLE);
    }

    private BigDecimal getDecimal(
            JSONObject object,
            String key
    ) {

        Object value = object.opt(key);

        if (value == null || value == JSONObject.NULL) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(value.toString());

        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String getStringValue(
            JSONObject object,
            String key,
            String fallback
    ) {

        String value = object.optString(key, "");

        if (value.trim().isEmpty()
                || "null".equalsIgnoreCase(value)) {
            return fallback;
        }

        return value;
    }

    private String formatNumber(BigDecimal value) {

        DecimalFormat format =
                new DecimalFormat("0.##");

        return format.format(value);
    }

    private int dp(float value) {

        return (int) (
                value
                        * getResources()
                        .getDisplayMetrics()
                        .density
                        + 0.5f
        );
    }

    @Override
    public void onDestroy() {

        executor.shutdownNow();

        mainHandler.removeCallbacksAndMessages(null);

        contentLayout = null;
        progressBar = null;
        statusText = null;
        retryButton = null;

        super.onDestroy();
    }
}