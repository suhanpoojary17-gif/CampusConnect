package com.campusconnect.app.features.materials;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
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
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;

import org.json.JSONArray;
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

public class SubjectMaterialsDetailFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private static final String ARG_SUBJECT_CODE = "subject_code";
    private static final String ARG_SUBJECT_NAME = "subject_name";

    private String subjectCode;
    private String subjectName;

    private ExecutorService executor;
    private LinearLayout filesContainer;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button retryButton;

    private ActivityResultLauncher<String> createDocumentLauncher;
    private String pendingDownloadMaterialId;
    private String pendingDownloadFileName;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public static SubjectMaterialsDetailFragment newInstance(String code, String name) {
        SubjectMaterialsDetailFragment fragment = new SubjectMaterialsDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SUBJECT_CODE, code);
        args.putString(ARG_SUBJECT_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            subjectCode = getArguments().getString(ARG_SUBJECT_CODE);
            subjectName = getArguments().getString(ARG_SUBJECT_NAME);
        }

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/octet-stream"),
                uri -> {
                    String materialId = pendingDownloadMaterialId;
                    String fileName = pendingDownloadFileName;

                    pendingDownloadMaterialId = null;
                    pendingDownloadFileName = null;

                    if (uri == null) return;
                    if (TextUtils.isEmpty(materialId)) {
                        showToast("Unable to identify the course material.");
                        return;
                    }

                    downloadAttachment(materialId, fileName, uri);
                }
        );
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

        // Screen Eyebrow: Adapts to theme (Black in Light, White in Dark)
        TextView eyebrow = new TextView(context);
        eyebrow.setText(subjectCode != null ? subjectCode.toUpperCase() : "COURSE FILES");
        eyebrow.setTextColor(context.getColor(R.color.primary_accent));
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.15f);
        eyebrow.setTypeface(null, Typeface.BOLD);
        root.addView(eyebrow);

        // Screen Title: Adapts to theme (Black in Light, White in Dark)
        TextView title = new TextView(context);
        title.setText(subjectName != null ? subjectName : "Subject Materials");
        title.setTextColor(context.getColor(R.color.primary_accent));
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(8);
        titleParams.bottomMargin = dp(20); // Spacing before the first card begins
        root.addView(title, titleParams);

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
        statusText.setText("Loading subject files...");
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
        retryButton.setOnClickListener(v -> loadSubjectFiles());

        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        retryParams.topMargin = dp(12);
        root.addView(retryButton, retryParams);

        filesContainer = new LinearLayout(context);
        filesContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams filesParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        filesParams.topMargin = dp(4);
        root.addView(filesContainer, filesParams);

        return scrollView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        executor = Executors.newSingleThreadExecutor();
        loadSubjectFiles();
    }

    private void loadSubjectFiles() {
        if (filesContainer == null || !isAdded()) return;

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
                JSONArray filteredFiles = new JSONArray();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject item = response.optJSONObject(i);
                    if (item == null) continue;

                    String code = item.optString("subjectCode", "");
                    String name = item.optString("subjectName", "");

                    boolean matches = (subjectCode != null && subjectCode.equalsIgnoreCase(code)) ||
                            (subjectName != null && subjectName.equalsIgnoreCase(name));

                    if (matches) {
                        filteredFiles.put(item);
                    }
                }

                mainHandler.post(() -> {
                    if (!isAdded() || filesContainer == null) return;
                    renderFiles(filteredFiles);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) return;
                    showMessage("Unable to load files for this subject.", true);
                });
            }
        });
    }

    private void renderFiles(JSONArray files) {
        if (filesContainer == null) return;
        filesContainer.removeAllViews();

        if (progressBar != null) progressBar.setVisibility(View.GONE);
        if (retryButton != null) retryButton.setVisibility(View.GONE);

        if (files.length() == 0) {
            showMessage("No materials uploaded for this subject yet.", false);
            return;
        }

        if (statusText != null) statusText.setVisibility(View.GONE);

        Context context = requireContext();

        for (int i = 0; i < files.length(); i++) {
            JSONObject item = files.optJSONObject(i);
            if (item == null) continue;

            String materialId = item.optString("id", "");
            String title = item.optString("title", "Course Material");
            String description = item.optString("description", "");
            String teacherEmail = item.optString("teacherEmail", "");
            String attachmentPath = item.optString("attachmentPath", "");

            boolean hasAttachment = !TextUtils.isEmpty(attachmentPath) && !"null".equalsIgnoreCase(attachmentPath);

            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(18), dp(18), dp(18), dp(18));

            // Apple Wallet Cream Surface (#E6DFD3, 16dp radius, 2dp elevation)
            GradientDrawable cardBg = new GradientDrawable();
            cardBg.setShape(GradientDrawable.RECTANGLE);
            cardBg.setColor(context.getColor(R.color.card_surface_cream));
            cardBg.setCornerRadius(dp(16));
            card.setBackground(cardBg);
            card.setElevation(dp(2));

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = dp(14);
            filesContainer.addView(card, cardParams);

            // Card Title: High-contrast primary #1A1A1A
            TextView titleView = new TextView(context);
            titleView.setText(title);
            titleView.setTextColor(context.getColor(R.color.card_text_primary));
            titleView.setTextSize(17);
            titleView.setTypeface(null, Typeface.BOLD);
            card.addView(titleView);

            if (!TextUtils.isEmpty(description)) {
                addBodyText(card, description);
            }
            if (!TextUtils.isEmpty(teacherEmail)) {
                addMetaRow(card, "Uploaded by", teacherEmail);
            }

            addMetaRow(
                    card,
                    "Attachment",
                    hasAttachment ? "Available for download" : "No attachment provided"
            );

            if (hasAttachment) {
                Button downloadButton = new Button(context);
                downloadButton.setText("Download Attachment");
                downloadButton.setAllCaps(false);
                downloadButton.setTextSize(14);
                downloadButton.setTypeface(null, Typeface.BOLD);
                downloadButton.setTextColor(context.getColor(R.color.button_secondary_text));

                // Secondary Action Button: Soft cream surface #E3DCD2 with 12dp radius
                GradientDrawable btnBg = new GradientDrawable();
                btnBg.setShape(GradientDrawable.RECTANGLE);
                btnBg.setColor(context.getColor(R.color.button_secondary_bg));
                btnBg.setCornerRadius(dp(12));
                downloadButton.setBackground(btnBg);

                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dp(48)
                );
                btnParams.topMargin = dp(14);

                downloadButton.setOnClickListener(v -> {
                    if (TextUtils.isEmpty(materialId)) {
                        showToast("Material ID unavailable.");
                        return;
                    }
                    if (createDocumentLauncher == null) {
                        showToast("File saving not available.");
                        return;
                    }

                    pendingDownloadMaterialId = materialId;
                    pendingDownloadFileName = buildFileName(title, attachmentPath);
                    createDocumentLauncher.launch(pendingDownloadFileName);
                });

                card.addView(downloadButton, btnParams);
            }
        }
    }

    private void addBodyText(LinearLayout card, String value) {
        TextView text = new TextView(requireContext());
        text.setText(value);
        text.setTextColor(requireContext().getColor(R.color.card_text_secondary));
        text.setTextSize(14);
        text.setLineSpacing(dp(2), 1.1f);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(8);
        card.addView(text, params);
    }

    private void addMetaRow(LinearLayout card, String label, String value) {
        Context context = requireContext();
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView labelView = new TextView(context);
        labelView.setText(label + ": ");
        labelView.setTextSize(13);
        labelView.setTextColor(context.getColor(R.color.card_text_secondary));
        row.addView(labelView);

        TextView valueView = new TextView(context);
        valueView.setText(value);
        valueView.setTextSize(13);
        valueView.setTypeface(null, Typeface.BOLD);
        valueView.setTextColor(context.getColor(R.color.card_text_primary));
        row.addView(valueView);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.topMargin = dp(6);
        card.addView(row, rowParams);
    }

    private String buildFileName(String title, String attachmentPath) {
        String safeName = TextUtils.isEmpty(title)
                ? "course_material"
                : title.trim().replaceAll("[\\\\/:*?\"<>|]", "_");

        String extension = ".pdf";
        if (!TextUtils.isEmpty(attachmentPath)) {
            String path = attachmentPath;
            int qIdx = path.indexOf('?');
            if (qIdx >= 0) path = path.substring(0, qIdx);
            int dotIdx = path.lastIndexOf('.');
            int slashIdx = path.lastIndexOf('/');
            if (dotIdx > slashIdx && dotIdx < path.length() - 1) {
                String candidate = path.substring(dotIdx);
                if (candidate.matches("\\.[A-Za-z0-9]{1,10}")) {
                    extension = candidate;
                }
            }
        }
        return safeName + "_attachment" + extension;
    }

    private void downloadAttachment(String materialId, String fileName, Uri destinationUri) {
        if (!isAdded()) return;

        Context appContext = requireContext().getApplicationContext();
        SharedPreferences preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showToast("Session expired.");
            return;
        }

        showToast("Downloading attachment...");

        executor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(BASE_URL + "/api/course-materials/" + materialId + "/attachment");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                int status = connection.getResponseCode();
                if (status < 200 || status >= 300) {
                    throw new IllegalStateException("HTTP error " + status);
                }

                try (InputStream input = connection.getInputStream();
                     OutputStream output = appContext.getContentResolver().openOutputStream(destinationUri, "w")) {
                    if (output == null) throw new IllegalStateException("Could not open stream.");

                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                    output.flush();
                }

                showToast("Attachment saved successfully.");
            } catch (Exception e) {
                showToast("Failed to download attachment.");
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
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
            statusText.setText("Loading files...");
            statusText.setVisibility(View.VISIBLE);
        }
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (retryButton != null) retryButton.setVisibility(View.GONE);
        if (filesContainer != null) filesContainer.removeAllViews();
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

    private void showToast(String message) {
        Context context = getContext();
        if (context == null) return;
        mainHandler.post(() -> Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show());
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
        filesContainer = null;
        statusText = null;
        progressBar = null;
        retryButton = null;
        super.onDestroyView();
    }
}