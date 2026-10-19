package com.campusconnect.app.features.chatbot;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.campusconnect.app.R;

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

public class ChatbotFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String CHATBOT_ENDPOINT = "/api/chatbot";

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private LinearLayout root;
    private LinearLayout messagesContainer;
    private LinearLayout suggestionsContainer;

    private ScrollView messagesScrollView;
    private EditText messageInput;
    private Button sendButton;
    private ProgressBar progressBar;

    private ExecutorService executor;

    private boolean requestInProgress = false;

    public ChatbotFragment() {
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

        executor = Executors.newSingleThreadExecutor();

        root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(
                context.getColor(R.color.main_background)
        );

        root.setPadding(
                dp(16),
                dp(12),
                dp(16),
                dp(12)
        );

        createHeader(context);
        createMessagesArea(context);
        createSuggestions(context);
        createComposer(context);

        addMessage(
                "Hello! I'm your CampusConnect Assistant. "
                        + "Ask me about your attendance, assignments, "
                        + "timetable, exams, results and campus services.",
                false
        );

        return root;
    }

    private void createHeader(Context context) {
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.VERTICAL);

        TextView eyebrow = new TextView(context);
        eyebrow.setText("CAMPUSCONNECT");
        eyebrow.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        eyebrow.setTextSize(11);
        eyebrow.setLetterSpacing(0.12f);
        eyebrow.setTypeface(null, Typeface.BOLD);

        header.addView(eyebrow);

        TextView title = new TextView(context);
        title.setText("AI Assistant");
        title.setTextColor(
                context.getColor(R.color.primary_text)
        );
        title.setTextSize(26);
        title.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(5);

        header.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText(
                "Your personal campus information assistant"
        );
        subtitle.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        subtitle.setTextSize(13);

        LinearLayout.LayoutParams subtitleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        subtitleParams.topMargin = dp(4);

        header.addView(subtitle, subtitleParams);

        root.addView(
                header,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
    }

    private void createMessagesArea(Context context) {
        messagesScrollView = new ScrollView(context);
        messagesScrollView.setFillViewport(false);
        messagesScrollView.setClipToPadding(false);
        messagesScrollView.setVerticalScrollBarEnabled(true);

        messagesContainer = new LinearLayout(context);
        messagesContainer.setOrientation(LinearLayout.VERTICAL);
        messagesContainer.setPadding(
                dp(2),
                dp(12),
                dp(2),
                dp(12)
        );

        messagesScrollView.addView(
                messagesContainer,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                );

        scrollParams.topMargin = dp(12);

        root.addView(messagesScrollView, scrollParams);
    }

    private void createSuggestions(Context context) {
        suggestionsContainer = new LinearLayout(context);
        suggestionsContainer.setOrientation(
                LinearLayout.VERTICAL
        );

        TextView heading = new TextView(context);
        heading.setText("SUGGESTED QUESTIONS");
        heading.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        heading.setTextSize(10);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setLetterSpacing(0.08f);

        suggestionsContainer.addView(heading);

        String[] suggestions = {
                "What is my attendance?",
                "Show my assignments",
                "When is my next exam?"
        };

        for (String suggestion : suggestions) {
            TextView item = new TextView(context);
            item.setText(suggestion);
            item.setTextColor(
                    context.getColor(R.color.primary_accent)
            );
            item.setTextSize(13);
            item.setPadding(
                    dp(12),
                    dp(10),
                    dp(12),
                    dp(10)
            );
            item.setBackground(
                    createRoundedBackground(
                            context.getColor(R.color.card_surface),
                            dp(12)
                    )
            );
            item.setClickable(true);
            item.setFocusable(true);

            LinearLayout.LayoutParams itemParams =
                    new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );

            itemParams.topMargin = dp(6);

            suggestionsContainer.addView(item, itemParams);

            item.setOnClickListener(v -> {
                if (requestInProgress) {
                    return;
                }

                messageInput.setText(suggestion);
                messageInput.setSelection(
                        messageInput.getText().length()
                );

                sendMessage();
            });
        }

        LinearLayout.LayoutParams suggestionsParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        suggestionsParams.topMargin = dp(8);

        root.addView(
                suggestionsContainer,
                suggestionsParams
        );
    }

    private void createComposer(Context context) {
        LinearLayout composer = new LinearLayout(context);
        composer.setOrientation(LinearLayout.HORIZONTAL);
        composer.setGravity(Gravity.CENTER_VERTICAL);

        messageInput = new EditText(context);
        messageInput.setSingleLine(false);
        messageInput.setMaxLines(3);
        messageInput.setHint("Ask your question...");
        messageInput.setTextSize(14);
        messageInput.setTextColor(
                context.getColor(R.color.primary_text)
        );
        messageInput.setHintTextColor(
                context.getColor(R.color.secondary_text)
        );
        messageInput.setPadding(
                dp(14),
                dp(10),
                dp(14),
                dp(10)
        );
        messageInput.setBackground(
                createRoundedBackground(
                        context.getColor(R.color.card_surface),
                        dp(16)
                )
        );

        LinearLayout.LayoutParams inputParams =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        1f
                );

        inputParams.rightMargin = dp(8);

        composer.addView(messageInput, inputParams);

        sendButton = new Button(context);
        sendButton.setText("Send");
        sendButton.setTextColor(
                context.getColor(R.color.primary_accent)
        );
        sendButton.setAllCaps(false);

        composer.addView(
                sendButton,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        dp(48)
                )
        );

        sendButton.setOnClickListener(v -> sendMessage());

        messageInput.setOnEditorActionListener(
                (textView, actionId, event) -> {
                    sendMessage();
                    return true;
                }
        );

        LinearLayout.LayoutParams composerParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        composerParams.topMargin = dp(10);

        root.addView(composer, composerParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        dp(28),
                        dp(28)
                );

        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(6);

        root.addView(progressBar, progressParams);
    }

    private void sendMessage() {
        if (!isAdded() || requestInProgress) {
            return;
        }

        String message = messageInput.getText() == null
                ? ""
                : messageInput.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            messageInput.setError("Enter a message");
            return;
        }

        SharedPreferences preferences =
                requireContext().getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (TextUtils.isEmpty(token)) {
            Toast.makeText(
                    requireContext(),
                    "Your session has expired. Please sign in again.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        addMessage(message, true);

        messageInput.setText("");

        InputMethodManager inputManager =
                (InputMethodManager) requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(
                    messageInput.getWindowToken(),
                    0
            );
        }

        setRequestInProgress(true);

        executor.execute(() -> {
            HttpURLConnection connection = null;

            try {
                URL url = new URL(
                        BASE_URL + CHATBOT_ENDPOINT
                );

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json; charset=UTF-8"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Authorization",
                        "Bearer " + token
                );

                JSONObject requestBody = new JSONObject();
                requestBody.put("message", message);

                byte[] requestBytes = requestBody
                        .toString()
                        .getBytes(StandardCharsets.UTF_8);

                try (OutputStream outputStream =
                             connection.getOutputStream()) {

                    outputStream.write(requestBytes);
                    outputStream.flush();
                }

                int statusCode = connection.getResponseCode();

                InputStream responseStream;

                if (statusCode >= 200 && statusCode < 300) {
                    responseStream = connection.getInputStream();
                } else {
                    responseStream = connection.getErrorStream();
                }

                String responseBody = readResponse(responseStream);

                if (statusCode < 200 || statusCode >= 300) {
                    throw new IllegalStateException(
                            "The server returned HTTP " + statusCode
                    );
                }

                JSONObject responseJson =
                        new JSONObject(responseBody);

                String response = responseJson.optString(
                        "response",
                        ""
                );

                if (TextUtils.isEmpty(response)) {
                    throw new IllegalStateException(
                            "The assistant returned an empty response."
                    );
                }

                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) {
                            return;
                        }

                        addMessage(response, false);
                        setRequestInProgress(false);
                    });
                }

            } catch (Exception e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!isAdded()) {
                            return;
                        }

                        addMessage(
                                "Sorry, I couldn't get a response. "
                                        + "Please check your connection "
                                        + "and try again.",
                                false
                        );

                        setRequestInProgress(false);
                    });
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readResponse(InputStream stream)
            throws Exception {

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

    private void addMessage(String message, boolean fromStudent) {
        if (!isAdded() || messagesContainer == null) {
            return;
        }

        Context context = requireContext();

        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setGravity(
                fromStudent
                        ? Gravity.END
                        : Gravity.START
        );

        TextView label = new TextView(context);
        label.setText(
                fromStudent
                        ? "YOU"
                        : "CAMPUSCONNECT ASSISTANT"
        );
        label.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        label.setTextSize(9);
        label.setTypeface(null, Typeface.BOLD);

        row.addView(label);

        TextView bubble = new TextView(context);
        bubble.setText(message);
        bubble.setTextColor(
                context.getColor(R.color.primary_text)
        );
        bubble.setTextSize(14);
        bubble.setLineSpacing(dp(3), 1f);
        bubble.setPadding(
                dp(14),
                dp(11),
                dp(14),
                dp(11)
        );

        bubble.setBackground(
                createRoundedBackground(
                        context.getColor(R.color.card_surface),
                        dp(16)
                )
        );

        LinearLayout.LayoutParams bubbleParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        bubbleParams.topMargin = dp(5);

        row.addView(bubble, bubbleParams);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        rowParams.bottomMargin = dp(16);

        messagesContainer.addView(row, rowParams);

        messagesScrollView.post(
                () -> messagesScrollView.fullScroll(View.FOCUS_DOWN)
        );
    }

    private void setRequestInProgress(boolean inProgress) {
        requestInProgress = inProgress;

        if (sendButton != null) {
            sendButton.setEnabled(!inProgress);
        }

        if (messageInput != null) {
            messageInput.setEnabled(!inProgress);
        }

        if (progressBar != null) {
            progressBar.setVisibility(
                    inProgress ? View.VISIBLE : View.GONE
            );
        }
    }

    private GradientDrawable createRoundedBackground(
            int color,
            int radius
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        return drawable;
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

        root = null;
        messagesContainer = null;
        suggestionsContainer = null;
        messagesScrollView = null;
        messageInput = null;
        sendButton = null;
        progressBar = null;

        super.onDestroyView();
    }
}