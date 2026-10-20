package com.campusconnect.app.features.materials;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectMaterialsListFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;
    private LinearLayout subjectsContainer;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button retryButton;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public SubjectMaterialsListFragment() {
        // Required empty constructor
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
        scrollView.setBackgroundColor(context.getColor(R.color.main_background));

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(28));
        scrollView.addView(root);

        // Canvas Eyebrow Header: Adapts to theme (Black in Light, White in Dark)
        TextView eyebrow = new TextView(context);
        eyebrow.setText("ACADEMIC RESOURCES");
        eyebrow.setTextColor(context.getColor(R.color.primary_accent));
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.15f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        root.addView(eyebrow);

        // Canvas Title: Adapts to theme (Black in Light, White in Dark)
        TextView title = new TextView(context);
        title.setText("Course Materials");
        title.setTextColor(context.getColor(R.color.primary_accent));
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(8);
        root.addView(title, titleParams);

        // Canvas Subtitle: Adapts to theme (#595550 in Light, #A0A0A0 in Dark)
        TextView subtitle = new TextView(context);
        subtitle.setText("Select a subject to view notes and files uploaded by your teachers.");
        subtitle.setTextColor(context.getColor(R.color.secondary_text));
        subtitle.setTextSize(14);

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.topMargin = dp(6);
        subtitleParams.bottomMargin = dp(20); // Clearance before the card list begins
        root.addView(subtitle, subtitleParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(24);
        root.addView(progressBar, progressParams);

        statusText = new TextView(context);
        statusText.setText("Loading subjects...");
        statusText.setTextColor(context.getColor(R.color.secondary_text));
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = dp(12);
        root.addView(statusText, statusParams);

        // Standardized Primary Action Retry Button
        retryButton = new Button(context);
        retryButton.setText("Retry");
        retryButton.setAllCaps(false);
        retryButton.setTextSize(14);
        retryButton.setTypeface(null, Typeface.BOLD);
        retryButton.setTextColor(context.getColor(R.color.button_primary_text));
        retryButton.setPadding(dp(24), dp(10), dp(24), dp(10));

        GradientDrawable retryBtnBg = new GradientDrawable();
        retryBtnBg.setShape(GradientDrawable.RECTANGLE);
        retryBtnBg.setColor(context.getColor(R.color.button_primary_bg));
        retryBtnBg.setCornerRadius(dp(12));
        retryButton.setBackground(retryBtnBg);

        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> loadSubjects());

        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        retryParams.topMargin = dp(12);
        root.addView(retryButton, retryParams);

        subjectsContainer = new LinearLayout(context);
        subjectsContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams subjectsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subjectsParams.topMargin = dp(4);
        root.addView(subjectsContainer, subjectsParams);

        return scrollView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        loadSubjects();
    }

    private void loadSubjects() {
        if (subjectsContainer == null || !isAdded()) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        SharedPreferences preferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showMessage("Your session has expired. Please sign in again.", false);
            return;
        }

        showLoading();

        executor.execute(() -> {
            try {
                JSONArray response = getArray("/api/course-materials", token);
                Map<String, SubjectSummary> subjectMap = new HashMap<>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject item = response.optJSONObject(i);
                    if (item == null) continue;

                    String subjectName = item.optString("subjectName", "General Subject");
                    String subjectCode = item.optString("subjectCode", "");
                    String teacherEmail = item.optString("teacherEmail", "");

                    String key = !TextUtils.isEmpty(subjectCode) ? subjectCode : subjectName;
                    SubjectSummary summary = subjectMap.get(key);
                    if (summary == null) {
                        summary = new SubjectSummary(subjectName, subjectCode, teacherEmail);
                        subjectMap.put(key, summary);
                    }
                    summary.fileCount++;
                }

                List<SubjectSummary> subjects = new ArrayList<>(subjectMap.values());

                mainHandler.post(() -> {
                    if (!isAdded() || subjectsContainer == null) return;
                    renderSubjects(subjects);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    showMessage("Unable to load subjects. Check your connection and retry.", true);
                });
            }
        });
    }

    private void renderSubjects(List<SubjectSummary> subjects) {
        if (subjectsContainer == null) return;
        subjectsContainer.removeAllViews();

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (retryButton != null) retryButton.setVisibility(View.GONE);

        if (subjects.isEmpty()) {
            showMessage("No subjects with course materials available.", false);
            return;
        }

        if (statusText != null) statusText.setVisibility(View.GONE);

        Context context = requireContext();

        for (SubjectSummary subject : subjects) {
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18), dp(18), dp(18), dp(18));

            // Apple Wallet Cream Surface (#E6DFD3, dp(16) radius, dp(2) elevation)
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setShape(GradientDrawable.RECTANGLE);
            cardBg.setColor(context.getColor(R.color.card_surface_cream));
            cardBg.setCornerRadius(dp(16));
            card.setBackground(cardBg);
            card.setElevation(dp(2));

            card.setClickable(true);
            card.setFocusable(true);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = dp(14);
            subjectsContainer.addView(card, cardParams);

            // Eyebrow Subject Code: High-contrast secondary #333333
            TextView metaView = new TextView(context);
            metaView.setText(!TextUtils.isEmpty(subject.code) ? subject.code.toUpperCase() : "COURSE SUBJECT");
            metaView.setTextColor(context.getColor(R.color.card_text_secondary));
            metaView.setTextSize(11);
            metaView.setLetterSpacing(0.1f);
            metaView.setTypeface(null, Typeface.BOLD);
            card.addView(metaView);

            // Subject Title: High-contrast primary #1A1A1A
            TextView titleView = new TextView(context);
            titleView.setText(subject.name);
            titleView.setTextColor(context.getColor(R.color.card_text_primary));
            titleView.setTextSize(17);
            titleView.setTypeface(null, Typeface.BOLD);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            titleParams.topMargin = dp(4);
            card.addView(titleView, titleParams);

            // Metadata / Teacher Details: High-contrast secondary #333333
            TextView descView = new TextView(context);
            String desc = subject.fileCount + (subject.fileCount == 1 ? " file available" : " files available");
            if (!TextUtils.isEmpty(subject.teacherEmail)) {
                desc += " • " + subject.teacherEmail;
            }
            descView.setText(desc);
            descView.setTextColor(context.getColor(R.color.card_text_secondary));
            descView.setTextSize(13);

            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            descParams.topMargin = dp(6);
            card.addView(descView, descParams);

            card.setOnClickListener(v -> {
                SubjectMaterialsDetailFragment detailFragment =
                        SubjectMaterialsDetailFragment.newInstance(subject.code, subject.name);
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, detailFragment)
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private static class SubjectSummary {
        String name;
        String code;
        String teacherEmail;
        int fileCount = 0;

        SubjectSummary(String name, String code, String teacherEmail) {
            this.name = name;
            this.code = code;
            this.teacherEmail = teacherEmail;
        }
    }

    private JSONArray getArray(String path, String token) throws Exception {
        return new JSONArray(getString(path, token));
    }

    private String getString(String path, String token) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();

            StringBuilder result = new StringBuilder();
            if (stream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            }

            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Request failed with HTTP " + status + ": " + result);
            }
            return result.toString();
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void showLoading() {
        if (statusText != null) {
            statusText.setText("Loading subjects...");
            statusText.setVisibility(View.VISIBLE);
        }
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (retryButton != null) retryButton.setVisibility(View.GONE);
        if (subjectsContainer != null) subjectsContainer.removeAllViews();
    }

    private void showMessage(String message, boolean allowRetry) {
        if (!isAdded()) return;
        mainHandler.post(() -> {
            if (!isAdded() || statusText == null) return;
            statusText.setText(message);
            statusText.setVisibility(View.VISIBLE);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (retryButton != null) retryButton.setVisibility(allowRetry ? View.VISIBLE : View.GONE);
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
        subjectsContainer = null;
        statusText = null;
        progressBar = null;
        retryButton = null;
        super.onDestroyView();
    }
}