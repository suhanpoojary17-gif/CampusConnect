package com.campusconnect.app.features.campus;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BusPassApplicationFragment extends Fragment {

    private static final String BASE_URL = "http://127.0.0.1:8080";
    private static final String APPLICATION_ENDPOINT = "/api/bus-passes";

    private static final String PREFS_NAME = "campusconnect_session";
    private static final String TOKEN_KEY = "jwt_token";

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExecutorService executor;

    private EditText routeInput;
    private EditText startDateInput;
    private EditText receiptNumberInput;
    private EditText paymentDateInput;
    private EditText paymentAmountInput;

    private Button submitButton;
    private ProgressBar progressBar;
    private TextView statusText;

    private String selectedStartDate;
    private String selectedPaymentDate;

    public BusPassApplicationFragment() {
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
        root.setPadding(dp(20), dp(24), dp(20), dp(28));

        scrollView.addView(
                root,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );

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
        title.setText("Apply for Bus Pass");
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
                "Enter your route and payment receipt details."
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

        subtitleParams.topMargin = dp(6);
        root.addView(subtitle, subtitleParams);

        LinearLayout formCard = new LinearLayout(context);
        formCard.setOrientation(LinearLayout.VERTICAL);
        formCard.setPadding(dp(16), dp(18), dp(16), dp(18));
        formCard.setBackgroundResource(R.drawable.bg_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.topMargin = dp(24);
        root.addView(formCard, cardParams);

        addFieldLabel(formCard, "Bus route");

        routeInput = createInput(
                "Enter your bus route",
                InputType.TYPE_CLASS_TEXT
        );
        formCard.addView(routeInput);

        addFieldLabel(formCard, "Pass start date");

        startDateInput = createDateInput("Select pass start date");
        formCard.addView(startDateInput);
        startDateInput.setOnClickListener(v -> showDatePicker(true));

        addFieldLabel(formCard, "Payment receipt number");

        receiptNumberInput = createInput(
                "Enter receipt number",
                InputType.TYPE_CLASS_TEXT
        );
        formCard.addView(receiptNumberInput);

        addFieldLabel(formCard, "Payment date");

        paymentDateInput = createDateInput("Select payment date");
        formCard.addView(paymentDateInput);
        paymentDateInput.setOnClickListener(v -> showDatePicker(false));

        addFieldLabel(formCard, "Payment amount (₹)");

        paymentAmountInput = createInput(
                "Enter payment amount",
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        formCard.addView(paymentAmountInput);

        TextView note = new TextView(context);
        note.setText(
                "Your application will be submitted for review. "
                        + "Enter accurate payment receipt details."
        );
        note.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        note.setTextSize(12);
        note.setLineSpacing(dp(3), 1f);

        LinearLayout.LayoutParams noteParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        noteParams.topMargin = dp(16);
        formCard.addView(note, noteParams);

        progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.GONE);

        LinearLayout.LayoutParams progressParams =
                new LinearLayout.LayoutParams(
                        dp(32),
                        dp(32)
                );

        progressParams.gravity = Gravity.CENTER_HORIZONTAL;
        progressParams.topMargin = dp(16);
        formCard.addView(progressBar, progressParams);

        statusText = new TextView(context);
        statusText.setTextColor(
                context.getColor(R.color.secondary_text)
        );
        statusText.setTextSize(13);
        statusText.setVisibility(View.GONE);

        LinearLayout.LayoutParams statusParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        statusParams.topMargin = dp(12);
        formCard.addView(statusText, statusParams);

        submitButton = new Button(context);
        submitButton.setText("Submit Application");
        submitButton.setOnClickListener(v -> submitApplication());

        LinearLayout.LayoutParams submitParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        submitParams.topMargin = dp(20);
        root.addView(submitButton, submitParams);

        Button backButton = new Button(context);
        backButton.setText("Back to Bus Pass");

        LinearLayout.LayoutParams backParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        backParams.topMargin = dp(8);
        root.addView(backButton, backParams);

        backButton.setOnClickListener(v ->
                getParentFragmentManager().popBackStack()
        );

        return scrollView;
    }

    @Override
    public void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        executor = Executors.newSingleThreadExecutor();

        if (savedInstanceState != null) {
            selectedStartDate = savedInstanceState.getString(
                    "selected_start_date"
            );

            selectedPaymentDate = savedInstanceState.getString(
                    "selected_payment_date"
            );
        }
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        if (selectedStartDate != null) {
            startDateInput.setText(selectedStartDate);
        }

        if (selectedPaymentDate != null) {
            paymentDateInput.setText(selectedPaymentDate);
        }
    }

    @Override
    public void onSaveInstanceState(
            @NonNull Bundle outState
    ) {
        outState.putString(
                "selected_start_date",
                selectedStartDate
        );

        outState.putString(
                "selected_payment_date",
                selectedPaymentDate
        );

        super.onSaveInstanceState(outState);
    }

    private void addFieldLabel(
            LinearLayout parent,
            String text
    ) {
        TextView label = new TextView(requireContext());
        label.setText(text);
        label.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );
        label.setTextSize(14);
        label.setTypeface(null, Typeface.BOLD);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        params.topMargin = dp(16);
        parent.addView(label, params);
    }

    private EditText createInput(
            String hint,
            int inputType
    ) {
        EditText input = new EditText(requireContext());

        input.setHint(hint);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setTextColor(
                requireContext().getColor(R.color.primary_text)
        );
        input.setHintTextColor(
                requireContext().getColor(R.color.secondary_text)
        );
        input.setTextSize(14);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));

        return input;
    }

    private EditText createDateInput(String hint) {
        EditText input = createInput(
                hint,
                InputType.TYPE_CLASS_DATETIME
        );

        input.setFocusable(false);
        input.setClickable(true);

        return input;
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();

        if (isStartDate && selectedStartDate != null) {
            calendar = calendarFromIsoDate(selectedStartDate);
        } else if (!isStartDate && selectedPaymentDate != null) {
            calendar = calendarFromIsoDate(selectedPaymentDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String isoDate = String.format(
                            java.util.Locale.US,
                            "%04d-%02d-%02d",
                            year,
                            month + 1,
                            dayOfMonth
                    );

                    if (isStartDate) {
                        selectedStartDate = isoDate;
                        startDateInput.setText(isoDate);
                    } else {
                        selectedPaymentDate = isoDate;
                        paymentDateInput.setText(isoDate);
                    }

                    statusText.setVisibility(View.GONE);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private Calendar calendarFromIsoDate(String date) {
        Calendar calendar = Calendar.getInstance();

        try {
            LocalDate localDate = LocalDate.parse(date);

            calendar.set(
                    localDate.getYear(),
                    localDate.getMonthValue() - 1,
                    localDate.getDayOfMonth()
            );
        } catch (Exception ignored) {
            // Keep today's date if a saved date cannot be parsed.
        }

        return calendar;
    }

    private void submitApplication() {
        String route = routeInput.getText().toString().trim();
        String receiptNumber = receiptNumberInput.getText()
                .toString()
                .trim();

        String amountText = paymentAmountInput.getText()
                .toString()
                .trim();

        if (route.isEmpty()) {
            routeInput.setError("Enter your bus route");
            routeInput.requestFocus();
            return;
        }

        if (selectedStartDate == null) {
            startDateInput.setError("Select a start date");
            Toast.makeText(
                    requireContext(),
                    "Please select a pass start date.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (receiptNumber.isEmpty()) {
            receiptNumberInput.setError(
                    "Enter your payment receipt number"
            );
            receiptNumberInput.requestFocus();
            return;
        }

        if (selectedPaymentDate == null) {
            Toast.makeText(
                    requireContext(),
                    "Please select a payment date.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (amountText.isEmpty()) {
            paymentAmountInput.setError(
                    "Enter the payment amount"
            );
            paymentAmountInput.requestFocus();
            return;
        }

        BigDecimal paymentAmount;

        try {
            paymentAmount = new BigDecimal(amountText);

            if (paymentAmount.signum() <= 0) {
                paymentAmountInput.setError(
                        "Amount must be greater than zero"
                );
                paymentAmountInput.requestFocus();
                return;
            }
        } catch (NumberFormatException exception) {
            paymentAmountInput.setError(
                    "Enter a valid payment amount"
            );
            paymentAmountInput.requestFocus();
            return;
        }

        LocalDate startDate;
        LocalDate paymentDate;

        try {
            startDate = LocalDate.parse(selectedStartDate);
            paymentDate = LocalDate.parse(selectedPaymentDate);
        } catch (Exception exception) {
            showStatus(
                    "Please select valid dates.",
                    false
            );
            return;
        }

        if (startDate.isBefore(LocalDate.now())) {
            showStatus(
                    "The pass start date cannot be in the past.",
                    false
            );
            return;
        }

        if (paymentDate.isAfter(LocalDate.now())) {
            showStatus(
                    "The payment date cannot be in the future.",
                    false
            );
            return;
        }

        SharedPreferences preferences = requireContext()
                .getSharedPreferences(
                        PREFS_NAME,
                        Context.MODE_PRIVATE
                );

        String token = preferences.getString(TOKEN_KEY, null);

        if (token == null || token.trim().isEmpty()) {
            showStatus(
                    "Your session has expired. Please log in again.",
                    false
            );
            return;
        }

        JSONObject request = new JSONObject();

        try {
            request.put("route", route);
            request.put("startDate", selectedStartDate);
            request.put("paymentReceiptNo", receiptNumber);
            request.put("paymentDate", selectedPaymentDate);
            request.put("paymentAmount", paymentAmount);
        } catch (Exception exception) {
            showStatus(
                    "Unable to prepare the application.",
                    false
            );
            return;
        }

        setSubmitting(true);

        executor.execute(() -> {
            try {
                JSONObject response = sendApplication(
                        token,
                        request
                );

                mainHandler.post(() -> {
                    if (!isAdded()) {
                        return;
                    }

                    setSubmitting(false);

                    String status = response.optString(
                            "status",
                            "PENDING"
                    );

                    Toast.makeText(
                            requireContext(),
                            "Application submitted. Status: " + status,
                            Toast.LENGTH_LONG
                    ).show();

                    getParentFragmentManager().popBackStack();
                });

            } catch (Exception exception) {
                String message = exception.getMessage();

                mainHandler.post(() -> {
                    if (isAdded()) {
                        setSubmitting(false);

                        showStatus(
                                message == null
                                        ? "Unable to submit the application."
                                        : message,
                                false
                        );
                    }
                });
            }
        });
    }

    private JSONObject sendApplication(
            String token,
            JSONObject request
    ) throws Exception {

        HttpURLConnection connection = null;

        try {
            URL url = new URL(
                    BASE_URL + APPLICATION_ENDPOINT
            );

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setDoOutput(true);

            connection.setRequestProperty(
                    "Authorization",
                    "Bearer " + token
            );

            connection.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            connection.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            byte[] requestBytes = request.toString()
                    .getBytes(StandardCharsets.UTF_8);

            try (java.io.OutputStream outputStream =
                         connection.getOutputStream()) {
                outputStream.write(requestBytes);
            }

            int responseCode = connection.getResponseCode();

            InputStream responseStream =
                    responseCode >= 200 && responseCode < 300
                            ? connection.getInputStream()
                            : connection.getErrorStream();

            String responseBody = responseStream == null
                    ? ""
                    : readResponse(responseStream);

            if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new Exception(
                        "Your session has expired. Please log in again."
                );
            }

            if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                throw new Exception(
                        "You do not have permission to submit this application."
                );
            }

            if (responseCode < 200 || responseCode >= 300) {
                throw new Exception(
                        extractErrorMessage(
                                responseBody,
                                responseCode
                        )
                );
            }

            if (responseBody.trim().isEmpty()) {
                throw new Exception(
                        "The server returned an empty response."
                );
            }

            return new JSONObject(responseBody);

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private String readResponse(InputStream inputStream)
            throws Exception {

        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        inputStream,
                        StandardCharsets.UTF_8
                )
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    private String extractErrorMessage(
            String responseBody,
            int responseCode
    ) {
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            try {
                JSONObject error = new JSONObject(responseBody);

                String[] possibleKeys = {
                        "message",
                        "error",
                        "detail"
                };

                for (String key : possibleKeys) {
                    String value = error.optString(key, "");

                    if (!value.isEmpty() && !"null".equals(value)) {
                        return value;
                    }
                }
            } catch (Exception ignored) {
                // The response may not be a JSON object.
            }
        }

        return "Application failed (HTTP " + responseCode + ").";
    }

    private void setSubmitting(boolean submitting) {
        if (!isAdded()) {
            return;
        }

        submitButton.setEnabled(!submitting);
        submitButton.setText(
                submitting
                        ? "Submitting..."
                        : "Submit Application"
        );

        progressBar.setVisibility(
                submitting ? View.VISIBLE : View.GONE
        );

        if (submitting) {
            statusText.setVisibility(View.GONE);
        }
    }

    private void showStatus(String message, boolean success) {
        if (!isAdded()) {
            return;
        }

        statusText.setText(message);
        statusText.setVisibility(View.VISIBLE);

        statusText.setTextColor(
                requireContext().getColor(
                        success
                                ? R.color.primary_accent
                                : R.color.secondary_text
                )
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
    public void onDestroy() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }

        mainHandler.removeCallbacksAndMessages(null);

        super.onDestroy();
    }
}