package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssignmentsFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;

    private LinearLayout assignmentsContainer;
    private TextView statusText;

    public AssignmentsFragment() {
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

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(28));

        scrollView.addView(root);

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
        title.setText("Assignments");
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
                "View your coursework and submission deadlines."
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
        subtitleParams.topMargin = dp(4);

        root.addView(subtitle, subtitleParams);

        statusText = new TextView(context);
        statusText.setText("Loading assignments...");
        statusText.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(80)
                );
        statusParams.topMargin = dp(12);

        root.addView(statusText, statusParams);

        assignmentsContainer = new LinearLayout(context);
        assignmentsContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(
                assignmentsContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();
        loadAssignments();
    }

    private void loadAssignments() {
        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showStatus("Your session has expired. Please sign in again.");
            return;
        }

        executor.execute(() -> {
            try {
                JSONArray response = getArray(
                        "/api/assignments",
                        token
                );

                List<JSONObject> assignments = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject item = response.optJSONObject(i);

                    if (item != null) {
                        assignments.add(item);
                    }
                }

                assignments.sort(
                        Comparator.comparing(
                                item -> item.optString(
                                        "dueDateTime",
                                        "9999"
                                )
                        )
                );

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    renderAssignments(assignments);
                });

            } catch (Exception e) {
                showStatus(
                        "Unable to load assignments. "
                                + "Please try again."
                );
            }
        });
    }

    private void renderAssignments(List<JSONObject> assignments) {
        assignmentsContainer.removeAllViews();

        statusText.setVisibility(View.GONE);

        if (assignments.isEmpty()) {
            showStatus("No assignments are available.");
            return;
        }

        for (JSONObject item : assignments) {
            LinearLayout card = createCard();

            String title = item.optString(
                    "title",
                    "Assignment"
            );

            String subject = item.optString(
                    "subjectName",
                    ""
            );

            String subjectCode = item.optString(
                    "subjectCode",
                    ""
            );

            String description = item.optString(
                    "description",
                    ""
            );

            String deadline = item.optString(
                    "dueDateTime",
                    ""
            );

            String marks = item.optString(
                    "maximumMarks",
                    ""
            );

            TextView assignmentTitle = new TextView(requireContext());
            assignmentTitle.setText(title);
            assignmentTitle.setTextColor(
                    requireContext().getColor(R.color.primary_text)
            );
            assignmentTitle.setTextSize(18);
            assignmentTitle.setTypeface(null, Typeface.BOLD);

            card.addView(assignmentTitle);

            if (!TextUtils.isEmpty(subject)
                    && !"null".equalsIgnoreCase(subject)) {

                String subjectText = subject;

                if (!TextUtils.isEmpty(subjectCode)
                        && !"null".equalsIgnoreCase(subjectCode)) {
                    subjectText += " (" + subjectCode + ")";
                }

                addBodyText(card, subjectText);
            }

            if (!TextUtils.isEmpty(deadline)
                    && !"null".equalsIgnoreCase(deadline)) {
                addBodyText(
                        card,
                        "Due: " + deadline.replace('T', ' ')
                );
            } else {
                addBodyText(card, "Deadline unavailable");
            }

            if (!TextUtils.isEmpty(marks)
                    && !"null".equalsIgnoreCase(marks)) {
                addBodyText(card, "Maximum marks: " + marks);
            }

            if (!TextUtils.isEmpty(description)
                    && !"null".equalsIgnoreCase(description)) {

                String summary = description;

                if (summary.length() > 120) {
                    summary = summary.substring(0, 117) + "...";
                }

                addBodyText(card, summary);
            }

            TextView openButton = new TextView(requireContext());
            openButton.setText("View assignment  →");
            openButton.setTextColor(
                    requireContext().getColor(R.color.primary_accent)
            );
            openButton.setTextSize(14);
            openButton.setTypeface(null, Typeface.BOLD);
            openButton.setPadding(0, dp(12), 0, dp(4));
            openButton.setOnClickListener(
                    v -> openAssignmentDetails(item)
            );

            card.addView(openButton);

        }
    }

    private void openAssignmentDetails(JSONObject assignment) {
        String assignmentId = assignment.optString("id", "");

        if (TextUtils.isEmpty(assignmentId)
                || "null".equalsIgnoreCase(assignmentId)) {
            showStatus("This assignment has no valid ID.");
            return;
        }

        AssignmentDetailsFragment fragment =
                AssignmentDetailsFragment.newInstance(assignmentId);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private LinearLayout createCard() {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(14);

        assignmentsContainer.addView(card, params);

        return card;
    }

    private void addBodyText(LinearLayout card, String value) {
        TextView text = new TextView(requireContext());
        text.setText(value);
        text.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        text.setTextSize(14);
        text.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(8);

        card.addView(text, params);
    }

    private JSONArray getArray(String path, String token)
            throws Exception {
        return new JSONArray(getString(path, token));
    }

    private String getString(String path, String token)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(BASE_URL + path).openConnection();

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

            StringBuilder result = new StringBuilder();

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

    private void showStatus(String message) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (!isAdded()) {
                return;
            }

            statusText.setText(message);
            statusText.setVisibility(View.VISIBLE);
        });
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
        super.onDestroyView();

        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        assignmentsContainer = null;
        statusText = null;
    }
}