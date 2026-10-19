package com.campusconnect.app.features.feedback;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FeedbackFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private static final String[] CATEGORIES = {
            "TEACHING",
            "FACILITIES",
            "CANTEEN",
            "HOSTEL",
            "PARKING",
            "TRANSPORT",
            "GENERAL"
    };

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExecutorService executor;

    private Spinner categorySpinner;
    private EditText messageInput;
    private CheckBox anonymousCheckBox;
    private Button submitButton;
    private Button refreshButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private LinearLayout feedbackContainer;

    public FeedbackFragment() {
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
        title.setText("Student Feedback");
        title.setTextColor(
                context.getColor(R.color.primary_text)
        );
        title.setTextSize(28);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        titleParams.topMargin = dp(8);

        root.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText(
                "Share your suggestions and help us improve campus life."
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

        // Submission form.
        LinearLayout formCard = new LinearLayout(context);
        formCard.setOrientation(LinearLayout.VERTICAL);
        formCard.setPadding(dp(18), dp(18), dp(18), dp(18));
        formCard.setBackgroundResource(R.drawable.bg_card);

        TextView formTitle = createSectionTitle(
                context,
                "Share your feedback"
        );
        formCard.addView(formTitle);

        addLabel(formCard, "Feedback category");

        categorySpinner = new Spinner(context);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                CATEGORIES
        );
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        categorySpinner.setAdapter(adapter);

        LinearLayout.LayoutParams spinnerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        spinnerParams.topMargin = dp(4);

        formCard.addView(categorySpinner, spinnerParams);

        addLabel(formCard, "Your message");

        messageInput = new EditText(context);
        messageInput.setHint(
                "Tell us what is working well or what could improve..."
        );
        messageInput.setTextSize(14);
        messageInput.setTextColor(
                context.getColor(R.color.primary_text)
        );
        messageInput.setHintTextColor(
                context.getColor(R.color.secondary_text)
        );
        messageInput.setGravity(Gravity.TOP | Gravity.START);
        messageInput.setMinLines(4);
        messageInput.setMaxLines(8);
        messageInput.setPadding(dp(12), dp(12), dp(12), dp(12));
        messageInput.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
                        | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        );

        LinearLayout.LayoutParams messageParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        messageParams.topMargin = dp(4);

        formCard.addView(messageInput, messageParams);

        anonymousCheckBox = new CheckBox(context);
        anonymousCheckBox.setText("Submit anonymously");
        anonymousCheckBox.setTextSize(14);
        anonymousCheckBox.setTextColor(
                context.getColor(R.color.primary_text)
        );
        anonymousCheckBox.setButtonTintList(
                android.content.res.ColorStateList.valueOf(
                        context.getColor(R.color.primary_accent)
                )
        );

        LinearLayout.LayoutParams anonymousParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        anonymousParams.topMargin = dp(8);

        formCard.addView(anonymousCheckBox, anonymousParams);

        TextView privacyNote = new TextView(context);
        privacyNote.setText(
                "Anonymous feedback hides your identity in the admin feedback response."
        );
        privacyNote.setTextSize(12);
        privacyNote.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        privacyNote.setLineSpacing(dp(2), 1.0f);

        LinearLayout.LayoutParams privacyParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        privacyParams.topMargin = dp(2);

        formCard.addView(privacyNote, privacyParams);

        submitButton = new Button(context);
        submitButton.setText("Submit Feedback");
        submitButton.setAllCaps(false);
        submitButton.setOnClickListener(v -> submitFeedback());

        LinearLayout.LayoutParams submitParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        submitParams.topMargin = dp(12);

        formCard.addView(submitButton, submitParams);

        root.addView(formCard);

        // Previously submitted feedback.
        TextView historyTitle = createSectionTitle(
                context,
                "My Feedback"
        );

        LinearLayout.LayoutParams historyTitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        historyTitleParams.topMargin = dp(24);

        root.addView(historyTitle, historyTitleParams);

        TextView historySubtitle = new TextView(context);
        historySubtitle.setText(
                "Review feedback you have submitted previously."
        );
        historySubtitle.setTextSize(13);
        historySubtitle.setTextColor(
                context.getColor(R.color.secondary_text)
        );

        LinearLayout.LayoutParams historySubtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        historySubtitleParams.topMargin = dp(4);

        root.addView(historySubtitle, historySubtitleParams);

        refreshButton = new Button(context);
        refreshButton.setText("Refresh Feedback");
        refreshButton.setAllCaps(false);
        refreshButton.setOnClickListener(v -> loadMyFeedback());

        LinearLayout.LayoutParams refreshParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        refreshParams.topMargin = dp(8);

        root.addView(refreshButton, refreshParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(12);

        root.addView(progressBar, progressParams);

        statusText = new TextView(context);
        statusText.setTextSize(14);
        statusText.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        statusText.setGravity(Gravity.CENTER);
        statusText.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        statusParams.topMargin = dp(12);

        root.addView(statusText, statusParams);

        feedbackContainer = new LinearLayout(context);
        feedbackContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams feedbackParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        feedbackParams.topMargin = dp(4);

        root.addView(feedbackContainer, feedbackParams);

        return scrollView;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        loadMyFeedback();
    }

    private void submitFeedback() {
        if (!isAdded() || messageInput == null || submitButton == null) {
            return;
        }

        String message = messageInput.getText()
                .toString()
                .trim();

        if (message.isEmpty()) {
            messageInput.setError("Please enter your feedback.");
            messageInput.requestFocus();
            return;
        }

        if (message.length() > 5000) {
            messageInput.setError(
                    "Feedback must not exceed 5000 characters."
            );
            return;
        }

        int selectedPosition = categorySpinner.getSelectedItemPosition();

        if (selectedPosition < 0 || selectedPosition >= CATEGORIES.length) {
            showToast("Please select a feedback category.");
            return;
        }

        SharedPreferences preferences = requireContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showToast("Your session has expired. Please sign in again.");
            return;
        }

        JSONObject request = new JSONObject();

        try {
            request.put("category", CATEGORIES[selectedPosition]);
            request.put("message", message);
            request.put("anonymous", anonymousCheckBox.isChecked());
        } catch (Exception e) {
            showToast("Unable to prepare your feedback.");
            return;
        }

        setSubmitting(true);

        executeRequest(
                "POST",
                "/api/feedback",
                token,
                request.toString(),
                result -> {
                    setSubmitting(false);

                    messageInput.setText("");
                    anonymousCheckBox.setChecked(false);
                    categorySpinner.setSelection(
                            CATEGORIES.length - 1
                    );

                    showToast("Feedback submitted successfully.");

                    loadMyFeedback();
                },
                error -> {
                    setSubmitting(false);

                    showToast(
                            "Unable to submit feedback. " + error
                    );
                }
        );
    }

    private void loadMyFeedback() {
        if (!isAdded() || feedbackContainer == null) {
            return;
        }

        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        SharedPreferences preferences = requireContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            showHistoryMessage(
                    "Your session has expired. Please sign in again."
            );
            return;
        }

        if (statusText != null) {
            statusText.setText("Loading your feedback...");
            statusText.setVisibility(View.VISIBLE);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (refreshButton != null) {
            refreshButton.setEnabled(false);
        }

        executor.execute(() -> {
            try {
                JSONArray response = new JSONArray(
                        requestString(
                                "GET",
                                "/api/feedback/my",
                                token,
                                null
                        )
                );

                List<JSONObject> items = new ArrayList<>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject item = response.optJSONObject(i);

                    if (item != null) {
                        items.add(item);
                    }
                }

                mainHandler.post(() -> {
                    if (!isAdded() || feedbackContainer == null) {
                        return;
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (refreshButton != null) {
                        refreshButton.setEnabled(true);
                    }

                    renderFeedback(items);
                });

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }

                    if (refreshButton != null) {
                        refreshButton.setEnabled(true);
                    }

                    showHistoryMessage(
                            "Unable to load your feedback. "
                                    + "Check your connection and try again."
                    );
                });
            }
        });
    }

    private void renderFeedback(List<JSONObject> items) {
        if (!isAdded() || feedbackContainer == null) {
            return;
        }

        feedbackContainer.removeAllViews();

        if (items.isEmpty()) {
            showHistoryMessage(
                    "You haven't submitted any feedback yet."
            );
            return;
        }

        if (statusText != null) {
            statusText.setVisibility(View.GONE);
        }

        for (JSONObject item : items) {
            createFeedbackCard(item);
        }
    }

    private void createFeedbackCard(JSONObject item) {
        Context context = requireContext();

        LinearLayout card = createCard(context);

        String category = item.optString(
                "category",
                "GENERAL"
        );

        String message = item.optString(
                "message",
                ""
        );

        String createdAt = item.optString(
                "createdAt",
                ""
        );

        boolean anonymous = item.optBoolean(
                "anonymous",
                false
        );

        TextView categoryView = createSectionTitle(
                context,
                formatCategory(category)
        );

        card.addView(categoryView);

        if (!createdAt.isEmpty() && !"null".equals(createdAt)) {
            addText(
                    card,
                    "Submitted: " + createdAt.replace('T', ' '),
                    R.color.secondary_text,
                    12
            );
        }

        addText(
                card,
                message.isEmpty() ? "No message available." : message,
                R.color.primary_text,
                14
        );

        addText(
                card,
                anonymous
                        ? "Submitted anonymously"
                        : "Submitted with your identity",
                R.color.secondary_text,
                12
        );
    }

    private String formatCategory(String category) {
        if (TextUtils.isEmpty(category)) {
            return "General";
        }

        String formatted = category
                .toLowerCase(java.util.Locale.ROOT)
                .replace('_', ' ');

        return Character.toUpperCase(formatted.charAt(0))
                + formatted.substring(1);
    }

    private void executeRequest(
            String method,
            String path,
            String token,
            String body,
            RequestSuccess success,
            RequestFailure failure
    ) {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(() -> {
            try {
                String response = requestString(
                        method,
                        path,
                        token,
                        body
                );

                mainHandler.post(() -> {
                    if (isAdded()) {
                        success.onSuccess(response);
                    }
                });

            } catch (Exception e) {
                String message = e.getMessage();

                mainHandler.post(() -> {
                    if (isAdded()) {
                        failure.onFailure(
                                TextUtils.isEmpty(message)
                                        ? "Please try again."
                                        : message
                        );
                    }
                });
            }
        });
    }

    private String requestString(
            String method,
            String path,
            String token,
            String body
    ) throws Exception {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection)
                    new URL(BASE_URL + path).openConnection();

            connection.setRequestMethod(method);
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            if (body != null) {
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                try (OutputStream output = connection.getOutputStream()) {
                    output.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int status = connection.getResponseCode();

            InputStream stream =
                    status >= 200 && status < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            StringBuilder result = new StringBuilder();

            if (stream != null) {
                try (
                        InputStream input = stream;
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(
                                        input,
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
                String details = result.toString();

                if (status == 401 || status == 403) {
                    throw new IllegalStateException(
                            "Access denied. Please verify your session and account permissions."
                    );
                }

                throw new IllegalStateException(
                        "Server returned HTTP "
                                + status
                                + (details.isEmpty() ? "" : ": " + details)
                );
            }

            return result.toString();

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private LinearLayout createCard(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(14);

        if (feedbackContainer != null) {
            feedbackContainer.addView(card, params);
        } else {
            // The form card is added directly to the root layout.
            // Its caller attaches it to the root.
        }

        return card;
    }

    private TextView createSectionTitle(
            Context context,
            String text
    ) {
        TextView view = new TextView(context);
        view.setText(text);
        view.setTextSize(18);
        view.setTypeface(null, Typeface.BOLD);
        view.setTextColor(
                context.getColor(R.color.primary_text)
        );

        return view;
    }

    private void addLabel(
            LinearLayout container,
            String text
    ) {
        TextView label = new TextView(requireContext());
        label.setText(text);
        label.setTextSize(13);
        label.setTypeface(null, Typeface.BOLD);
        label.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(14);

        container.addView(label, params);
    }

    private void addText(
            LinearLayout container,
            String value,
            int colorResource,
            float textSize
    ) {
        TextView text = new TextView(requireContext());
        text.setText(value);
        text.setTextColor(
                requireContext().getColor(colorResource)
        );
        text.setTextSize(textSize);
        text.setLineSpacing(dp(3), 1.0f);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(8);

        container.addView(text, params);
    }

    private void setSubmitting(boolean submitting) {
        if (!isAdded()) {
            return;
        }

        if (submitButton != null) {
            submitButton.setEnabled(!submitting);
            submitButton.setText(
                    submitting ? "Submitting..." : "Submit Feedback"
            );
        }
    }

    private void showHistoryMessage(String message) {
        if (!isAdded() || statusText == null) {
            return;
        }

        statusText.setText(message);
        statusText.setVisibility(View.VISIBLE);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (feedbackContainer != null) {
            feedbackContainer.removeAllViews();
        }

        if (refreshButton != null) {
            refreshButton.setEnabled(true);
        }
    }

    private void showToast(String message) {
        Context context = getContext();

        if (context == null) {
            return;
        }

        mainHandler.post(() -> {
            Context current = getContext();

            if (current != null) {
                Toast.makeText(
                        current,
                        message,
                        Toast.LENGTH_LONG
                ).show();
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

        categorySpinner = null;
        messageInput = null;
        anonymousCheckBox = null;
        submitButton = null;
        refreshButton = null;
        progressBar = null;
        statusText = null;
        feedbackContainer = null;

        super.onDestroyView();
    }

    private interface RequestSuccess {
        void onSuccess(String response);
    }

    private interface RequestFailure {
        void onFailure(String message);
    }
}
