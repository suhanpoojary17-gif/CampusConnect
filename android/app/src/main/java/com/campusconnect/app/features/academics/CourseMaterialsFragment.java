package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CourseMaterialsFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;

    private LinearLayout materialsContainer;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button retryButton;

    private ActivityResultLauncher<String> createDocumentLauncher;

    private String pendingDownloadMaterialId;
    private String pendingDownloadFileName;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public CourseMaterialsFragment() {
        // Required empty constructor.
    }

    @Override
    public void onCreate(@Nullable android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(
                        "application/octet-stream"
                ),
                uri -> {
                    String materialId = pendingDownloadMaterialId;
                    String fileName = pendingDownloadFileName;

                    pendingDownloadMaterialId = null;
                    pendingDownloadFileName = null;

                    if (uri == null) {
                        // The user cancelled the save dialog.
                        return;
                    }

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
            @Nullable android.os.Bundle savedInstanceState
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
        title.setText("Course Materials");
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
                "Study resources and learning materials for your subjects."
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

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(24);

        root.addView(progressBar, progressParams);

        statusText = new TextView(context);
        statusText.setText("Loading course materials...");
        statusText.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        statusParams.topMargin = dp(12);

        root.addView(statusText, statusParams);

        retryButton = new Button(context);
        retryButton.setText("Retry");
        retryButton.setAllCaps(false);
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> loadMaterials());

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        retryParams.topMargin = dp(8);

        root.addView(retryButton, retryParams);

        materialsContainer = new LinearLayout(context);
        materialsContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams materialsParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        materialsParams.topMargin = dp(4);

        root.addView(materialsContainer, materialsParams);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable android.os.Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();
        loadMaterials();
    }

    private void loadMaterials() {
        if (materialsContainer == null || !isAdded()) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showMessage(
                    "Your session has expired. Please sign in again.",
                    false
            );
            return;
        }

        showLoading();

        executor.execute(() -> {
            try {
                JSONArray response = getArray(
                        "/api/course-materials",
                        token
                );

                List<JSONObject> materials = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject item = response.optJSONObject(i);

                    if (item != null) {
                        materials.add(item);
                    }
                }

                materials.sort(
                        Comparator.comparing(
                                item -> item.optString(
                                        "subjectName",
                                        ""
                                ).toLowerCase()
                                        + "|"
                                        + item.optString(
                                                "title",
                                                ""
                                        ).toLowerCase()
                        )
                );

                mainHandler.post(() -> {
                    if (!isAdded() || materialsContainer == null) {
                        return;
                    }

                    renderMaterials(materials);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    showMessage(
                            "Unable to load course materials. "
                                    + "Check your connection and retry.",
                            true
                    );
                });
            }
        });
    }

    private void renderMaterials(List<JSONObject> materials) {
        if (materialsContainer == null) {
            return;
        }

        materialsContainer.removeAllViews();

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }

        if (materials.isEmpty()) {
            showMessage(
                    "No course materials are available yet.",
                    false
            );
            return;
        }

        if (statusText != null) {
            statusText.setVisibility(View.GONE);
        }

        for (JSONObject item : materials) {
            LinearLayout card = createCard();

            String title = item.optString(
                    "title",
                    "Course Material"
            );

            String description = item.optString(
                    "description",
                    ""
            );

            String subject = item.optString(
                    "subjectName",
                    ""
            );

            String subjectCode = item.optString(
                    "subjectCode",
                    ""
            );

            String section = item.optString(
                    "sectionName",
                    ""
            );

            String teacherEmail = item.optString(
                    "teacherEmail",
                    ""
            );

            String attachmentPath = item.optString(
                    "attachmentPath",
                    ""
            );

            boolean hasAttachment =
                    !TextUtils.isEmpty(attachmentPath)
                            && !"null".equalsIgnoreCase(attachmentPath);

            TextView materialTitle = new TextView(requireContext());
            materialTitle.setText(title);
            materialTitle.setTextColor(
                    requireContext().getColor(R.color.primary_text)
            );
            materialTitle.setTextSize(18);
            materialTitle.setTypeface(null, Typeface.BOLD);

            card.addView(materialTitle);

            if (isAvailable(subject)) {
                String subjectLabel = subject;

                if (isAvailable(subjectCode)) {
                    subjectLabel += " (" + subjectCode + ")";
                }

                addBodyText(card, subjectLabel);
            }

            if (isAvailable(section)) {
                addBodyText(card, "Section: " + section);
            }

            if (isAvailable(description)) {
                addBodyText(card, description);
            }

            if (isAvailable(teacherEmail)) {
                addBodyText(card, "Provided by: " + teacherEmail);
            }

            addBodyText(
                    card,
                    hasAttachment
                            ? "Attachment available"
                            : "No attachment provided"
            );

            if (hasAttachment) {
                Button downloadButton = new Button(requireContext());
                downloadButton.setText("Download Attachment");
                downloadButton.setAllCaps(false);

                LinearLayout.LayoutParams buttonParams =
                        new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );

                buttonParams.topMargin = dp(12);

                String materialId = item.optString("id", "");

                downloadButton.setOnClickListener(v -> {
                    if (TextUtils.isEmpty(materialId)
                            || "null".equalsIgnoreCase(materialId)) {
                        showToast("Material ID is unavailable.");
                        return;
                    }

                    if (createDocumentLauncher == null) {
                        showToast("File saving is not available right now.");
                        return;
                    }

                    pendingDownloadMaterialId = materialId;
                    pendingDownloadFileName =
                            buildFileName(title, attachmentPath);

                    createDocumentLauncher.launch(
                            pendingDownloadFileName
                    );
                });

                card.addView(downloadButton, buttonParams);
            }
        }

        if (statusText != null) {
            statusText.setVisibility(View.GONE);
        }
    }

    private String buildFileName(String title, String attachmentPath) {
        String safeName = TextUtils.isEmpty(title)
                ? "course_material"
                : title.trim();

        safeName = safeName.replaceAll("[\\\\/:*?\"<>|]", "_");

        if (safeName.isEmpty()) {
            safeName = "course_material";
        }

        String extension = "";

        if (isAvailable(attachmentPath)) {
            String path = attachmentPath;
            int queryIndex = path.indexOf('?');

            if (queryIndex >= 0) {
                path = path.substring(0, queryIndex);
            }

            int slashIndex = path.lastIndexOf('/');
            int dotIndex = path.lastIndexOf('.');

            if (dotIndex > slashIndex && dotIndex < path.length() - 1) {
                String candidate = path.substring(dotIndex);

                if (candidate.matches("\\.[A-Za-z0-9]{1,10}")) {
                    extension = candidate;
                }
            }
        }

        return safeName + "_attachment" + extension;
    }

    private void downloadAttachment(
            String materialId,
            String fileName,
            Uri destinationUri
    ) {
        if (!isAdded()) {
            return;
        }

        Context appContext =
                requireContext().getApplicationContext();

        SharedPreferences preferences =
                appContext.getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showToast("Your session has expired. Please sign in again.");
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        showToast("Downloading attachment...");

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        BASE_URL
                                + "/api/course-materials/"
                                + materialId
                                + "/attachment"
                );

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);

                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + token
                );

                connection.setRequestProperty(
                        "Accept",
                        "*/*"
                );

                int status = connection.getResponseCode();

                if (status < 200 || status >= 300) {
                    String errorMessage = readErrorResponse(connection);

                    throw new IllegalStateException(
                            "Download failed with HTTP "
                                    + status
                                    + (TextUtils.isEmpty(errorMessage)
                                    ? ""
                                    : ": " + errorMessage)
                    );
                }

                try (InputStream input = connection.getInputStream();
                     OutputStream output =
                             appContext.getContentResolver()
                                     .openOutputStream(destinationUri, "w")) {

                    if (output == null) {
                        throw new IllegalStateException(
                                "Could not open the selected save location."
                        );
                    }

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }

                    output.flush();
                }

                showToast("Attachment saved successfully.");

            } catch (Exception e) {
                String error = e.getMessage();

                showToast(
                        "Unable to download attachment."
                                + (TextUtils.isEmpty(error)
                                ? ""
                                : " " + error)
                );

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readErrorResponse(HttpURLConnection connection) {
        InputStream stream = null;

        try {
            stream = connection.getErrorStream();

            if (stream == null) {
                return "";
            }

            StringBuilder result = new StringBuilder();

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

            return result.toString();

        } catch (Exception ignored) {
            return "";
        }
    }

    private void showToast(String message) {
        Context context = getContext();

        if (context == null) {
            return;
        }

        Context appContext = context.getApplicationContext();

        mainHandler.post(() ->
                Toast.makeText(
                        appContext,
                        message,
                        Toast.LENGTH_LONG
                ).show()
        );
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

        materialsContainer.addView(card, params);

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
                                + ": " + result
                );
            }

            return result.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isAvailable(String value) {
        return !TextUtils.isEmpty(value)
                && !"null".equalsIgnoreCase(value);
    }

    private void showLoading() {
        if (statusText != null) {
            statusText.setText("Loading course materials...");
            statusText.setVisibility(View.VISIBLE);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }

        if (materialsContainer != null) {
            materialsContainer.removeAllViews();
        }
    }

    private void showMessage(String message, boolean allowRetry) {
        if (!isAdded()) {
            return;
        }

        mainHandler.post(() -> {
            if (!isAdded() || statusText == null) {
                return;
            }

            statusText.setText(message);
            statusText.setVisibility(View.VISIBLE);

            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            if (retryButton != null) {
                retryButton.setVisibility(
                        allowRetry ? View.VISIBLE : View.GONE
                );
            }
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
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        materialsContainer = null;
        statusText = null;
        progressBar = null;
        retryButton = null;

        super.onDestroyView();
    }
}