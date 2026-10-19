package com.campusconnect.app.features.notices;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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
import androidx.appcompat.app.AlertDialog;
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

public class NoticesFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private ExecutorService executor;

    private LinearLayout noticesContainer;
    private TextView statusText;
    private ProgressBar progressBar;
    private Button retryButton;

    private ActivityResultLauncher<String> createDocumentLauncher;

    private String pendingNoticeId;
    private String pendingFileName;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public NoticesFragment() {
        // Required empty constructor.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument(
                        "application/octet-stream"
                ),
                uri -> {
                    String noticeId = pendingNoticeId;
                    String fileName = pendingFileName;

                    pendingNoticeId = null;
                    pendingFileName = null;

                    if (uri == null) {
                        // The user cancelled the save dialog.
                        return;
                    }

                    if (TextUtils.isEmpty(noticeId)) {
                        showToast("Unable to identify this notice.");
                        return;
                    }

                    downloadAttachment(noticeId, fileName, uri);
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
        scrollView.setBackgroundColor(
                context.getColor(R.color.main_background)
        );

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(20), dp(20), dp(28));

        scrollView.addView(root);

        // Header label.
        TextView eyebrow = new TextView(context);
        eyebrow.setText("CAMPUSCONNECT");
        eyebrow.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        eyebrow.setTextSize(12);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);

        root.addView(eyebrow);

        // Screen title.
        TextView title = new TextView(context);
        title.setText("Campus Notices");
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

        // Screen subtitle.
        TextView subtitle = new TextView(context);
        subtitle.setText(
                "Announcements and important updates from your campus."
        );
        subtitle.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        subtitle.setTextSize(14);
        subtitle.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(5);

        root.addView(subtitle, subtitleParams);

        // Loading indicator.
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

        // Loading, empty, and error messages.
        statusText = new TextView(context);
        statusText.setText("Loading campus notices...");
        statusText.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        statusText.setTextSize(14);
        statusText.setGravity(Gravity.CENTER);
        statusText.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(12);

        root.addView(statusText, statusParams);

        // Retry button.
        retryButton = new Button(context);
        retryButton.setText("Retry");
        retryButton.setAllCaps(false);
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(v -> loadNotices());

        LinearLayout.LayoutParams retryParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        retryParams.gravity = Gravity.CENTER_HORIZONTAL;
        retryParams.topMargin = dp(8);

        root.addView(retryButton, retryParams);

        // Notice cards are added here.
        noticesContainer = new LinearLayout(context);
        noticesContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams noticesParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        noticesParams.topMargin = dp(4);

        root.addView(noticesContainer, noticesParams);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        loadNotices();
    }

    private void loadNotices() {
        if (!isAdded() || noticesContainer == null) {
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
                        "/api/notices",
                        token
                );

                List<JSONObject> notices = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject notice = response.optJSONObject(i);

                    if (notice != null) {
                        notices.add(notice);
                    }
                }

                // Show the newest notices first when dates are available.
                notices.sort(
                        Comparator.comparing(
                                notice -> notice.optString(
                                        "date",
                                        ""
                                ),
                                Comparator.reverseOrder()
                        )
                );

                mainHandler.post(() -> {
                    if (!isAdded() || noticesContainer == null) {
                        return;
                    }

                    renderNotices(notices);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    showMessage(
                            "Unable to load campus notices. "
                                    + "Check your connection and retry.",
                            true
                    );
                });
            }
        });
    }

    private void renderNotices(List<JSONObject> notices) {
        if (noticesContainer == null || !isAdded()) {
            return;
        }

        noticesContainer.removeAllViews();

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }

        if (notices.isEmpty()) {
            showMessage(
                    "There are no notices available for your section yet.",
                    false
            );
            return;
        }

        if (statusText != null) {
            statusText.setVisibility(View.GONE);
        }

        for (JSONObject notice : notices) {
            createNoticeCard(notice);
        }
    }

    private void createNoticeCard(JSONObject notice) {
        String noticeId = notice.optString("id", "");

        String title = notice.optString(
                "title",
                "Campus Notice"
        );

        String description = notice.optString(
                "description",
                ""
        );

        String date = notice.optString(
                "date",
                ""
        );

        String sectionName = notice.optString(
                "targetSectionName",
                ""
        );

        String creatorEmail = notice.optString(
                "creatorEmail",
                ""
        );

        String attachmentPath = notice.optString(
                "attachmentPath",
                ""
        );

        boolean hasAttachment = isAvailable(attachmentPath);

        LinearLayout card = createCard();

        // Notice heading.
        TextView titleView = new TextView(requireContext());
        titleView.setText(title);
        titleView.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );
        titleView.setTextSize(19);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setLineSpacing(dp(2), 1.0f);

        card.addView(titleView);

        // Date.
        if (isAvailable(date)) {
            addMetadataText(card, "Published: " + date);
        }

        // Section.
        if (isAvailable(sectionName)) {
            addMetadataText(card, "Section: " + sectionName);
        }

        // Creator.
        if (isAvailable(creatorEmail)) {
            addMetadataText(card, "Posted by: " + creatorEmail);
        }

        // Notice description preview.
        if (isAvailable(description)) {
            String preview = description.trim();

            if (preview.length() > 180) {
                preview = preview.substring(0, 180).trim() + "...";
            }

            addBodyText(card, preview);
        } else {
            addBodyText(card, "No description provided.");
        }

        // Attachment availability.
        if (hasAttachment) {
            addMetadataText(card, "📎 Attachment available");
        }

        // Read-more action.
        Button detailsButton = new Button(requireContext());
        detailsButton.setText("Read Full Notice");
        detailsButton.setAllCaps(false);

        LinearLayout.LayoutParams detailsParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        detailsParams.topMargin = dp(10);

        detailsButton.setOnClickListener(v ->
                showNoticeDetails(
                        title,
                        description,
                        date,
                        sectionName,
                        creatorEmail,
                        noticeId,
                        hasAttachment,
                        attachmentPath
                )
        );

        card.addView(detailsButton, detailsParams);

        // Direct attachment download.
        if (hasAttachment) {
            Button downloadButton = new Button(requireContext());
            downloadButton.setText("Download Attachment");
            downloadButton.setAllCaps(false);

            LinearLayout.LayoutParams downloadParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            downloadParams.topMargin = dp(4);

            downloadButton.setOnClickListener(v -> {
                if (!isAvailable(noticeId)) {
                    showToast("Notice ID is unavailable.");
                    return;
                }

                pendingNoticeId = noticeId;
                pendingFileName = buildFileName(
                        title,
                        attachmentPath
                );

                createDocumentLauncher.launch(pendingFileName);
            });

            card.addView(downloadButton, downloadParams);
        }
    }

    private void showNoticeDetails(
            String title,
            String description,
            String date,
            String sectionName,
            String creatorEmail,
            String noticeId,
            boolean hasAttachment,
            String attachmentPath
    ) {
        if (!isAdded()) {
            return;
        }

        ScrollView scrollView = new ScrollView(requireContext());

        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(12), dp(20), dp(12));

        scrollView.addView(content);

        TextView detailsTitle = new TextView(requireContext());
        detailsTitle.setText(title);
        detailsTitle.setTextSize(21);
        detailsTitle.setTypeface(null, Typeface.BOLD);
        detailsTitle.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );

        content.addView(detailsTitle);

        if (isAvailable(date)) {
            addBodyText(content, "Published: " + date);
        }

        if (isAvailable(sectionName)) {
            addBodyText(content, "Section: " + sectionName);
        }

        if (isAvailable(creatorEmail)) {
            addBodyText(content, "Posted by: " + creatorEmail);
        }

        TextView fullDescription = new TextView(requireContext());

        fullDescription.setText(
                isAvailable(description)
                        ? description
                        : "No description provided."
        );

        fullDescription.setTextSize(15);
        fullDescription.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );
        fullDescription.setLineSpacing(dp(5), 1.0f);

        LinearLayout.LayoutParams descriptionParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        descriptionParams.topMargin = dp(18);

        content.addView(fullDescription, descriptionParams);

        if (hasAttachment) {
            addBodyText(content, "An attachment is available for this notice.");
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Notice Details")
                .setView(scrollView)
                .setPositiveButton("Close", null)
                .create();

        dialog.show();
    }

    private void downloadAttachment(
            String noticeId,
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
            showToast(
                    "Your session has expired. Please sign in again."
            );
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        showToast("Downloading notice attachment...");

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        BASE_URL
                                + "/api/notices/"
                                + noticeId
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

                connection.setRequestProperty("Accept", "*/*");

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

                try (
                        InputStream input = connection.getInputStream();
                        OutputStream output =
                                appContext.getContentResolver()
                                        .openOutputStream(destinationUri, "w")
                ) {
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

                showToast("Notice attachment saved successfully.");

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
                try (
                        BufferedReader reader =
                                new BufferedReader(
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

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "Request failed with HTTP "
                                + status
                                + ": "
                                + result
                );
            }

            return result.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readErrorResponse(HttpURLConnection connection) {
        InputStream stream = null;

        try {
            stream = connection.getErrorStream();

            if (stream == null) {
                return "";
            }

            StringBuilder result = new StringBuilder();

            try (
                    BufferedReader reader =
                            new BufferedReader(
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

            return result.toString();

        } catch (Exception ignored) {
            return "";
        }
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

        noticesContainer.addView(card, params);

        return card;
    }

    private void addMetadataText(
            LinearLayout container,
            String value
    ) {
        TextView text = new TextView(requireContext());

        text.setText(value);
        text.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        text.setTextSize(12);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(7);

        container.addView(text, params);
    }

    private void addBodyText(
            LinearLayout container,
            String value
    ) {
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

        container.addView(text, params);
    }

    private String buildFileName(
            String title,
            String attachmentPath
    ) {
        String safeName = isAvailable(title)
                ? title.trim()
                : "campus_notice";

        safeName = safeName.replaceAll("[\\\\/:*?\"<>|]", "_");

        if (safeName.isEmpty()) {
            safeName = "campus_notice";
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

    private boolean isAvailable(String value) {
        return !TextUtils.isEmpty(value)
                && !"null".equalsIgnoreCase(value.trim());
    }

    private void showLoading() {
        if (statusText != null) {
            statusText.setText("Loading campus notices...");
            statusText.setVisibility(View.VISIBLE);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }

        if (noticesContainer != null) {
            noticesContainer.removeAllViews();
        }
    }

    private void showMessage(
            String message,
            boolean allowRetry
    ) {
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

        noticesContainer = null;
        statusText = null;
        progressBar = null;
        retryButton = null;

        super.onDestroyView();
    }
}