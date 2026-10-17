package com.campusconnect.app.features.academics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssignmentDetailsFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String ARG_ASSIGNMENT_ID = "assignment_id";

    private ExecutorService executor;

    private String assignmentId;
    private String token;

    private JSONObject assignment;
    private JSONObject submission;

    private Uri selectedFileUri;

    private LinearLayout detailsContainer;
    private TextView statusText;
    private TextView selectedFileText;

    private Button submitButton;
    private Button attachmentButton;
    private Button chooseFileButton;

    private boolean submissionStatusKnown;
    private boolean uploadInProgress;
    private boolean attachmentDownloadInProgress;

    private ActivityResultLauncher<String[]> filePickerLauncher;
    private ActivityResultLauncher<String> saveAttachmentLauncher;

    private byte[] pendingAttachmentBytes;
    private String pendingAttachmentFileName = "assignment-attachment";

    public AssignmentDetailsFragment() {
        // Required empty constructor.
    }

    public static AssignmentDetailsFragment newInstance(
            String assignmentId
    ) {
        AssignmentDetailsFragment fragment =
                new AssignmentDetailsFragment();

        Bundle arguments = new Bundle();
        arguments.putString(ARG_ASSIGNMENT_ID, assignmentId);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            assignmentId = getArguments().getString(ARG_ASSIGNMENT_ID);
        }

        filePickerLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.OpenDocument(),
                        uri -> {
                            if (uri == null || !isAdded()) {
                                return;
                            }

                            selectedFileUri = uri;

                            try {
                                requireContext()
                                        .getContentResolver()
                                        .takePersistableUriPermission(
                                                uri,
                                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        );
                            } catch (SecurityException ignored) {
                                // The current URI may still be readable.
                            }

                            if (selectedFileText != null) {
                                selectedFileText.setText(
                                        "Selected file: " + getFileName(uri)
                                );
                            }

                            updateSubmitButton();
                        }
                );

        saveAttachmentLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.CreateDocument(
                                "application/octet-stream"
                        ),
                        uri -> {
                            if (uri == null) {
                                pendingAttachmentBytes = null;
                                showStatus("Download cancelled.");
                                return;
                            }

                            saveAttachmentToUri(uri);
                        }
                );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
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

        TextView heading = new TextView(context);
        heading.setText("Assignment Details");
        heading.setTextSize(26);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(
                context.getColor(R.color.primary_text)
        );

        root.addView(heading);

        statusText = new TextView(context);
        statusText.setText("Loading assignment...");
        statusText.setTextSize(14);
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

        detailsContainer = new LinearLayout(context);
        detailsContainer.setOrientation(LinearLayout.VERTICAL);

        root.addView(
                detailsContainer,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        attachmentButton = new Button(context);
        attachmentButton.setText("Download assignment attachment");
        attachmentButton.setVisibility(View.GONE);
        attachmentButton.setOnClickListener(
                v -> downloadAssignmentAttachment()
        );

        root.addView(attachmentButton);

        selectedFileText = new TextView(context);
        selectedFileText.setText("No file selected");
        selectedFileText.setTextSize(14);
        selectedFileText.setTextColor(
                context.getColor(R.color.secondary_text)
        );

        LinearLayout.LayoutParams selectedFileParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        selectedFileParams.topMargin = dp(12);

        root.addView(selectedFileText, selectedFileParams);

        chooseFileButton = new Button(context);
        chooseFileButton.setText("Choose submission file");
        chooseFileButton.setOnClickListener(
                v -> filePickerLauncher.launch(new String[]{"*/*"})
        );

        root.addView(chooseFileButton);

        submitButton = new Button(context);
        submitButton.setText("Submit assignment");
        submitButton.setEnabled(false);
        submitButton.setOnClickListener(v -> uploadSubmission());

        root.addView(submitButton);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showStatus("Your session has expired. Please sign in again.");
            return;
        }

        if (TextUtils.isEmpty(assignmentId)) {
            showStatus("Invalid assignment ID.");
            return;
        }

        try {
            UUID.fromString(assignmentId);
        } catch (IllegalArgumentException e) {
            showStatus("Invalid assignment ID.");
            return;
        }

        loadDetails();
    }

    private void loadDetails() {
        if (executor == null || executor.isShutdown()) {
            return;
        }

        showStatus("Loading assignment details...");

        executor.execute(() -> {
            try {
                String assignmentJson = request(
                        "/api/assignments/" + assignmentId,
                        "GET",
                        null,
                        null,
                        null
                );

                JSONObject loadedAssignment =
                        new JSONObject(assignmentJson);

                JSONObject loadedSubmission = null;
                boolean statusKnown = false;

                try {
                    String submissionJson = request(
                            "/api/assignments/" + assignmentId
                                    + "/submissions/my",
                            "GET",
                            null,
                            null,
                            null
                    );

                    loadedSubmission = new JSONObject(submissionJson);
                    statusKnown = true;

                } catch (Exception e) {
                    String message = e.getMessage();

                    if (message != null
                            && message.contains(
                                    "You have not submitted this assignment"
                            )) {
                        // The backend explicitly confirmed no submission.
                        statusKnown = true;
                    } else {
                        // Do not treat an arbitrary HTTP or network error
                        // as proof that the student has not submitted.
                        statusKnown = false;
                    }
                }

                if (!isAdded()) {
                    return;
                }

                JSONObject finalSubmission = loadedSubmission;
                boolean finalStatusKnown = statusKnown;

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded() || detailsContainer == null) {
                        return;
                    }

                    assignment = loadedAssignment;
                    submission = finalSubmission;
                    submissionStatusKnown = finalStatusKnown;

                    renderDetails();
                });

            } catch (Exception e) {
                showStatus("Unable to load assignment details.");
            }
        });
    }

    private void renderDetails() {
        if (assignment == null || detailsContainer == null) {
            return;
        }

        detailsContainer.removeAllViews();

        addDetail(
                "Title",
                assignment.optString("title", "Assignment")
        );

        addDetail(
                "Subject",
                assignment.optString("subjectName", "Not available")
        );

        addDetail(
                "Subject code",
                assignment.optString("subjectCode", "Not available")
        );

        addDetail(
                "Description",
                assignment.optString(
                        "description",
                        "No description provided"
                )
        );

        addDetail(
                "Deadline",
                assignment.optString("dueDateTime", "Not available")
                        .replace('T', ' ')
        );

        addDetail(
                "Maximum marks",
                assignment.optString("maximumMarks", "Not available")
        );

        String attachmentPath =
                assignment.optString("attachmentPath", "");

        boolean hasAttachment =
                !TextUtils.isEmpty(attachmentPath)
                        && !"null".equalsIgnoreCase(attachmentPath);

        if (attachmentButton != null) {
            attachmentButton.setVisibility(
                    hasAttachment ? View.VISIBLE : View.GONE
            );
        }

        addDetail(
                "Assignment attachment",
                hasAttachment ? "Available" : "No attachment provided"
        );

        if (!submissionStatusKnown) {
            addDetail(
                    "Submission status",
                    "Unable to verify"
            );

            showStatus(
                    "Unable to verify your submission status. "
                            + "Check your connection and reopen this assignment."
            );

        } else if (submission != null) {
            addDetail("Submission status", "Submitted");

            addDetail(
                    "Submitted at",
                    submission.optString("submittedAt", "Not available")
                            .replace('T', ' ')
            );

            addDetail(
                    "Late submission",
                    submission.optBoolean("submittedLate", false)
                            ? "Yes" : "No"
            );

            String comment = submission.optString("comment", "");

            if (!TextUtils.isEmpty(comment)
                    && !"null".equalsIgnoreCase(comment)) {
                addDetail("Comment", comment);
            }

            showStatus("Your submission has been recorded.");

        } else {
            addDetail(
                    "Submission status",
                    "Not submitted"
            );

            showStatus(
                    "Select a file to submit your assignment "
                            + "before the deadline."
            );
        }

        updateSubmitButton();
    }

    private void updateSubmitButton() {
        if (submitButton == null) {
            return;
        }

        if (!submissionStatusKnown) {
            submitButton.setText("Submission status unavailable");
            submitButton.setEnabled(false);
            return;
        }

        if (uploadInProgress) {
            submitButton.setText("Uploading...");
            submitButton.setEnabled(false);
            return;
        }

        if (submission != null) {
            submitButton.setText("Resubmit assignment");
        } else {
            submitButton.setText("Submit assignment");
        }

        submitButton.setEnabled(selectedFileUri != null);
    }

    private void addDetail(String label, String value) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        cardParams.topMargin = dp(10);

        detailsContainer.addView(card, cardParams);

        TextView labelView = new TextView(requireContext());
        labelView.setText(label);
        labelView.setTextSize(12);
        labelView.setTypeface(null, Typeface.BOLD);
        labelView.setTextColor(
                requireContext().getColor(R.color.secondary_text)
        );

        card.addView(labelView);

        TextView valueView = new TextView(requireContext());
        valueView.setText(value);
        valueView.setTextSize(15);
        valueView.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );

        LinearLayout.LayoutParams valueParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        valueParams.topMargin = dp(5);

        card.addView(valueView, valueParams);
    }

    private void uploadSubmission() {
        if (selectedFileUri == null) {
            showStatus("Please select a file first.");
            return;
        }

        if (!submissionStatusKnown) {
            showStatus(
                    "Unable to verify submission status. "
                            + "Please reopen the assignment and try again."
            );
            return;
        }

        if (executor == null || executor.isShutdown()) {
            showStatus("Please reopen this screen and try again.");
            return;
        }

        if (uploadInProgress) {
            return;
        }

        uploadInProgress = true;

        if (chooseFileButton != null) {
            chooseFileButton.setEnabled(false);
        }

        updateSubmitButton();
        showStatus("Uploading your assignment...");

        Uri fileUri = selectedFileUri;

        executor.execute(() -> {
            try {
                String response = uploadFile(
                        "/api/assignments/" + assignmentId
                                + "/submissions",
                        fileUri
                );

                JSONObject responseObject = new JSONObject(response);

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    submission = responseObject;
                    submissionStatusKnown = true;
                    selectedFileUri = null;
                    uploadInProgress = false;

                    if (selectedFileText != null) {
                        selectedFileText.setText("No file selected");
                    }

                    if (chooseFileButton != null) {
                        chooseFileButton.setEnabled(true);
                    }

                    renderDetails();
                    showStatus("Assignment submitted successfully.");
                });

            } catch (Exception e) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    uploadInProgress = false;

                    if (chooseFileButton != null) {
                        chooseFileButton.setEnabled(true);
                    }

                    updateSubmitButton();
                    showUploadError(e.getMessage());
                });
            }
        });
    }

    private void showUploadError(String message) {
        if (TextUtils.isEmpty(message)) {
            showStatus("Submission failed. Please try again.");
            return;
        }

        if (message.contains("deadline has passed")) {
            showStatus(
                    "The submission deadline has passed. "
                            + "The backend does not accept late submissions."
            );
        } else if (message.contains("only submit assignments for your section")) {
            showStatus(
                    "This assignment is not available to your section."
            );
        } else if (message.contains("HTTP 401")) {
            showStatus(
                    "Your session may have expired. Please sign in again."
            );
        } else if (message.contains("HTTP 403")) {
            showStatus(
                    "You are not authorized to submit this assignment."
            );
        } else {
            showStatus(
                    "Submission failed. Check your connection "
                            + "and try again."
            );
        }
    }

    private String uploadFile(String path, Uri uri) throws Exception {
        String boundary =
                "CampusConnect-" + System.currentTimeMillis();

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(BASE_URL + path).openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);
            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            connection.setRequestProperty(
                    "Content-Type",
                    "multipart/form-data; boundary=" + boundary
            );

            String fileName = getFileName(uri);
            String mimeType = requireContext()
                    .getContentResolver()
                    .getType(uri);

            if (TextUtils.isEmpty(mimeType)) {
                mimeType = "application/octet-stream";
            }

            try (OutputStream output = connection.getOutputStream()) {
                String header =
                        "--" + boundary + "\r\n"
                                + "Content-Disposition: form-data; "
                                + "name=\"file\"; filename=\""
                                + fileName.replace("\"", "")
                                + "\"\r\n"
                                + "Content-Type: " + mimeType + "\r\n\r\n";

                output.write(header.getBytes(StandardCharsets.UTF_8));

                try (InputStream input =
                             requireContext()
                                     .getContentResolver()
                                     .openInputStream(uri)) {

                    if (input == null) {
                        throw new IllegalStateException(
                                "Unable to open selected file."
                        );
                    }

                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = input.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                }

                output.write(
                        ("\r\n--" + boundary + "--\r\n")
                                .getBytes(StandardCharsets.UTF_8)
                );

                output.flush();
            }

            int status = connection.getResponseCode();

            InputStream stream =
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            String response = readStream(stream);

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "Upload failed with HTTP " + status
                                + ": " + response
                );
            }

            return response;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void downloadAssignmentAttachment() {
        if (assignment == null || !submissionStatusKnown) {
            showStatus("Please wait until assignment details finish loading.");
            return;
        }

        if (attachmentDownloadInProgress) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            showStatus("Please reopen this screen and try again.");
            return;
        }

        attachmentDownloadInProgress = true;

        if (attachmentButton != null) {
            attachmentButton.setEnabled(false);
        }

        showStatus("Downloading assignment attachment...");

        executor.execute(() -> {
            try {
                DownloadedAttachment downloaded =
                        downloadAttachment(
                                "/api/assignments/" + assignmentId
                                        + "/attachment"
                        );

                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    pendingAttachmentBytes = downloaded.bytes;
                    pendingAttachmentFileName = downloaded.fileName;

                    attachmentDownloadInProgress = false;

                    if (attachmentButton != null) {
                        attachmentButton.setEnabled(true);
                    }

                    saveAttachmentLauncher.launch(
                            pendingAttachmentFileName
                    );
                });

            } catch (Exception e) {
                if (!isAdded()) {
                    return;
                }

                requireActivity().runOnUiThread(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    attachmentDownloadInProgress = false;

                    if (attachmentButton != null) {
                        attachmentButton.setEnabled(true);
                    }

                    showStatus(
                            "Unable to download the attachment. "
                                    + "Please try again."
                    );
                });
            }
        });
    }

    private static class DownloadedAttachment {
        final byte[] bytes;
        final String fileName;

        DownloadedAttachment(byte[] bytes, String fileName) {
            this.bytes = bytes;
            this.fileName = fileName;
        }
    }

    private DownloadedAttachment downloadAttachment(String path)
            throws Exception {

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(BASE_URL + path).openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            int status = connection.getResponseCode();

            if (status < 200 || status >= 300) {
                String error = readStream(connection.getErrorStream());

                throw new IllegalStateException(
                        "Download failed with HTTP " + status
                                + ": " + error
                );
            }

            String fileName = extractFileName(
                    connection.getHeaderField("Content-Disposition")
            );

            try (InputStream input = connection.getInputStream();
                 ByteArrayOutputStream output =
                         new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int count;

                while ((count = input.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                return new DownloadedAttachment(
                        output.toByteArray(),
                        fileName
                );
            }

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String extractFileName(String disposition) {
        if (!TextUtils.isEmpty(disposition)) {
            String marker = "filename=";
            int index = disposition.indexOf(marker);

            if (index >= 0) {
                String fileName =
                        disposition.substring(index + marker.length())
                                .trim();

                if (fileName.startsWith("\"")) {
                    fileName = fileName.substring(1);

                    int end = fileName.indexOf('"');

                    if (end >= 0) {
                        fileName = fileName.substring(0, end);
                    }
                } else {
                    int separator = fileName.indexOf(';');

                    if (separator >= 0) {
                        fileName = fileName.substring(0, separator);
                    }
                }

                fileName = fileName.trim().replace('\\', '/');

                int slash = fileName.lastIndexOf('/');

                if (slash >= 0) {
                    fileName = fileName.substring(slash + 1);
                }

                if (!fileName.isEmpty()
                        && !".".equals(fileName)
                        && !"..".equals(fileName)) {
                    return fileName;
                }
            }
        }

        return "assignment-attachment";
    }

    private void saveAttachmentToUri(Uri uri) {
        byte[] bytes = pendingAttachmentBytes;

        if (bytes == null) {
            showStatus("There is no downloaded file to save.");
            return;
        }

        try (OutputStream output =
                     requireContext()
                             .getContentResolver()
                             .openOutputStream(uri)) {

            if (output == null) {
                throw new IllegalStateException(
                        "Unable to open the selected save location."
                );
            }

            output.write(bytes);
            output.flush();

            pendingAttachmentBytes = null;

            showStatus("Assignment attachment saved successfully.");

        } catch (Exception e) {
            showStatus(
                    "Unable to save the attachment. Please try again."
            );
        }
    }

    private String request(
            String path,
            String method,
            byte[] body,
            String contentType,
            String accept
    ) throws Exception {

        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(BASE_URL + path).openConnection();

            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            connection.setRequestProperty(
                    "Accept",
                    accept == null ? "application/json" : accept
            );

            if (body != null) {
                connection.setDoOutput(true);

                if (contentType != null) {
                    connection.setRequestProperty(
                            "Content-Type",
                            contentType
                    );
                }

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body);
                }
            }

            int status = connection.getResponseCode();

            InputStream stream =
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            String response = readStream(stream);

            if (status < 200 || status >= 300) {
                throw new IllegalStateException(
                        "Request failed with HTTP " + status
                                + ": " + response
                );
            }

            return response;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readStream(InputStream stream) throws Exception {
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
    }

    private String getFileName(Uri uri) {
        String result = "submission_file";

        try (Cursor cursor =
                     requireContext()
                             .getContentResolver()
                             .query(uri, null, null, null, null)) {

            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                );

                if (index >= 0) {
                    String name = cursor.getString(index);

                    if (!TextUtils.isEmpty(name)) {
                        result = name;
                    }
                }
            }

        } catch (Exception ignored) {
            // Retain the fallback name.
        }

        return result;
    }

    private void showStatus(String message) {
        if (!isAdded()) {
            return;
        }

        requireActivity().runOnUiThread(() -> {
            if (!isAdded() || statusText == null) {
                return;
            }

            statusText.setText(message);
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

        pendingAttachmentBytes = null;

        detailsContainer = null;
        statusText = null;
        selectedFileText = null;
        submitButton = null;
        attachmentButton = null;
        chooseFileButton = null;

        super.onDestroyView();
    }
}